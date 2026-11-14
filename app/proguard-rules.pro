# Keep Room entities & DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# Keep Moshi & JSON models
-keepclassmembers class com.example.data.** { *; }
-keepclassmembers class com.example.overlay.** { *; }

# Keep WorkManager workers & BroadcastReceivers
-keep class com.example.overlay.ServiceWatchdogWorker { *; }
-keep class com.example.overlay.UnlockBroadcastReceiver { *; }
-keep class com.example.overlay.UnlockOverlayService { *; }
-keep class com.example.widget.** { *; }

# Keep Kotlin Coroutines & Compose runtime stability
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
