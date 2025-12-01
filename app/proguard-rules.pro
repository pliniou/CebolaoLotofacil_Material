# --- Android Components ---
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View

# --- Hilt & Dagger ---
-keep class com.cebolao.lotofacil.CebolaoApplication
-keep class dagger.hilt.internal.aggregatedroot.codegen.** { *; }
-keep class androidx.hilt.** { *; }
-keepclassmembers class * { @javax.inject.Inject <init>(...); }

# --- Kotlinx Serialization ---
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
    @kotlinx.serialization.Serializable <fields>;
}
-keepnames class * implements kotlinx.serialization.KSerializer

# --- Retrofit & Networking ---
-keepattributes Signature, Exceptions, InnerClasses
-keep class com.cebolao.lotofacil.data.network.** { *; }
-keep interface com.cebolao.lotofacil.data.network.** { *; }

# --- Data Models (Preserve fields for JSON/Serialization) ---
-keep class com.cebolao.lotofacil.data.LotofacilGame { *; }
-keep class com.cebolao.lotofacil.data.HistoricalDraw { *; }
-keep class com.cebolao.lotofacil.data.StatisticsReport { *; }
-keep class com.cebolao.lotofacil.data.model.** { *; }

# --- R8 Optimizations ---
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}