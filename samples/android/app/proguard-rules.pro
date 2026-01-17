# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Flagent SDK classes
-keep class com.flagent.** { *; }

# Keep Ktor client classes
-keep class io.ktor.** { *; }

# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-keepattributes Signature
-keepattributes Exceptions
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
