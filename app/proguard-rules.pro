# Voucher Keeper - ProGuard Rules for Release Build
# =================================================

# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ===== Kotlin & Coroutines =====
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.coroutines.CoroutinesInternalError

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ===== Room Database =====
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Room entities and DAOs
-keep class com.hananel.voucherkeeper.data.local.entity.** { *; }
-keep class com.hananel.voucherkeeper.data.local.dao.** { *; }

# ===== Hilt / Dagger =====
-dontwarn com.google.errorprone.annotations.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Hilt generated classes
-keep class **_HiltModules { *; }
-keep class **_HiltComponents { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

# ===== Jetpack Compose =====
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# Keep Composable functions
-keep @androidx.compose.runtime.Composable class * { *; }
-keep @androidx.compose.runtime.Composable interface * { *; }

# ===== Broadcast Receivers (SMS) =====
-keep public class * extends android.content.BroadcastReceiver
-keep class com.hananel.voucherkeeper.receiver.** { *; }

# ===== DataStore =====
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
  <fields>;
}

# ===== Serialization =====
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ===== General Android =====
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.ContentProvider

# Keep custom Application class
-keep class com.hananel.voucherkeeper.VoucherKeeperApplication { *; }

# ===== Remove Logging in Release =====
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep only warnings and errors
-assumenosideeffects class android.util.Log {
    public static *** w(...);
    public static *** e(...);
}

# ===== Optimization Settings =====
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose