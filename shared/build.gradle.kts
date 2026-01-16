plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}


kotlin {
    jvm()
    
    js(IR) {
        browser()
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Kotlinx Serialization
                implementation(libs.kotlinx.serialization.json)
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
}
