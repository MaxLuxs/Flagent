// Root build file for Android Sample (samples/android)
// Plugins applied with (apply false) so app module can apply them

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
