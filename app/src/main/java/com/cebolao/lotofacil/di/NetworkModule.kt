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
    private const val CACHE_SIZE = 10L * 1024 * 1024 // 10 MB
    private const val TIMEOUT_SEC = 30L
    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS = 500L
    private const val HTTP_TOO_MANY_REQUESTS = 429
    private const val RATE_LIMIT_INTERCEPTOR = "RateLimitInterceptor"

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
        return Cache(File(context.cacheDir, CACHE_DIR), CACHE_SIZE)
    }

    @Provides
    @Singleton
    @Named(RATE_LIMIT_INTERCEPTOR)
    fun provideRateLimitingInterceptor(): Interceptor = Interceptor { chain ->
        var response: Response?
        var exception: IOException? = null
        
        // Lógica de retry simplificada
        for (attempt in 0 until MAX_RETRIES) {
            try {
                response = chain.proceed(chain.request())
                if (response.code != HTTP_TOO_MANY_REQUESTS) return@Interceptor response
                
                // Se for 429, fecha e espera
                response.close()
            } catch (e: IOException) {
                exception = e
            }
            
            // Backoff exponencial simples
            val delay = RETRY_DELAY_MS * (attempt + 1)
            try { Thread.sleep(delay) } catch (_: InterruptedException) { }
        }

        throw exception ?: IOException("Failed after $MAX_RETRIES attempts due to Rate Limiting")
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
            .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}