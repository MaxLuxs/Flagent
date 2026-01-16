import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.flagent"
version = "1.1.19"

repositories {
    mavenCentral()
}

dependencies {
    // Base SDK - use local project if available, otherwise Maven
    if (findProject(":kotlin-client") != null) {
        implementation(project(":kotlin-client"))
    } else {
        implementation("com.flagent:flagent-kotlin-client:1.1.19")
    }
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // Testing
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.ktor.client.core)
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