package com.cebolao.lotofacil.di

import android.content.Context
import com.cebolao.lotofacil.BuildConfig
import com.cebolao.lotofacil.data.network.ApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://servicebus2.caixa.gov.br/portaldeloterias/api/"
    private const val CACHE_DIR = "http_cache"
    private const val CACHE_SIZE_BYTES = 10L * 1024 * 1024 // 10 MB
    private const val TIMEOUT_SECONDS = 30L
    private const val RATE_LIMIT_INTERCEPTOR = "RateLimitInterceptor"
    private const val MEDIA_TYPE_JSON = "application/json"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache {
        return Cache(File(context.cacheDir, CACHE_DIR), CACHE_SIZE_BYTES)
    }

    /**
     * Interceptor simples para lidar com Rate Limiting (HTTP 429) usando Backoff Exponencial.
     */
    @Provides
    @Singleton
    @Named(RATE_LIMIT_INTERCEPTOR)
    fun provideRateLimitingInterceptor(): Interceptor = object : Interceptor {
        private val MAX_RETRIES = 3
        private val BASE_DELAY_MS = 500L
        private val HTTP_TOO_MANY_REQUESTS = 429

        override fun intercept(chain: Interceptor.Chain): Response {
            var response: Response?
            var exception: IOException? = null
            
            for (attempt in 0 until MAX_RETRIES) {
                try {
                    response = chain.proceed(chain.request())
                    if (response.code != HTTP_TOO_MANY_REQUESTS) return response
                    
                    response.close() // Fecha para liberar recursos antes de tentar de novo
                } catch (e: IOException) {
                    exception = e
                }
                
                // Backoff: 500ms, 1000ms, 1500ms...
                val delay = BASE_DELAY_MS * (attempt + 1)
                try { Thread.sleep(delay) } catch (_: InterruptedException) { }
            }

            throw exception ?: IOException("Failed after $MAX_RETRIES attempts due to Rate Limiting (429)")
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        @Named(RATE_LIMIT_INTERCEPTOR) rateLimitInterceptor: Interceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(logging)
            .addInterceptor(rateLimitInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(MEDIA_TYPE_JSON.toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}