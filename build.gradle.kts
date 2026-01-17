plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
}

allprojects {
    group = "com.flagent"
    version = "1.0.0"
    
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

// Configure Java toolchain repositories for auto-download
java {
    toolchains {
        download {
            enabled = true
            downloadRepositories {
                maven {
                    url = uri("https://api.adoptium.net/v3/binary/version/jdk-21.0.10+12/mac/aarch64/jdk/hotspot/normal/eclipse")
                }
            }
        }
    }
}
