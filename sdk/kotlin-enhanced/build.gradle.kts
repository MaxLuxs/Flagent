import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

java {
    toolchain {
        languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(21))
    }
}

group = "com.flagent"
// version inherited from root (VERSION file)

repositories {
    mavenCentral()
}

dependencies {
    // Base SDK - use local project if available, otherwise Maven
    if (findProject(":kotlin-client") != null) {
        implementation(project(":kotlin-client"))
    } else {
        implementation("com.flagent:flagent-kotlin-client:0.1.0")
    }
    
    // Shared evaluator (single source of truth for rollout/constraint logic)
    if (findProject(":shared") != null) {
        implementation(project(":shared"))
    }
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    
    // Ktor client (RealtimeClient SSE, OfflineFlagentManager optional httpClient)
    implementation(libs.ktor.client.core)
    
    // Logging (RealtimeClient)
    implementation(libs.kotlin.logging.jvm)
    
    // Testing
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.ktor.client.core)
    testImplementation(libs.ktor.client.cio)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

tasks.test {
    useJUnitPlatform()
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Flagent Kotlin Enhanced Client")
                description.set("Enhanced Kotlin client library for Flagent API with caching and management")
                url.set("https://github.com/MaxLuxs/Flagent")
                
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                
                developers {
                    developer {
                        id.set("flagent")
                        name.set("Flagent Team")
                    }
                }
            }
        }
    }
}