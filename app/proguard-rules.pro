# Add project specific ProGuard rules here.

# Keep AccessibilityService
-keep class com.chhotu.onetap.DragService { *; }

# Keep OverlayService  
-keep class com.chhotu.onetap.OverlayService { *; }

# Keep SettingsManager
-keep class com.chhotu.onetap.SettingsManager { *; }

# SharedPreferences
-keep class * extends android.content.SharedPreferences {
    *;
}
