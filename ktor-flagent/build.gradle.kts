import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

group = "io.ktor"

dependencies {
    // Shared models
    api(project(":shared"))
    
    // Ktor Server
    api(libs.ktor.server.core)
    api(libs.ktor.server.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)
    
    // Ktor Client
    api(libs.ktor.client.core)
    api(libs.ktor.client.cio)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.client.logging)
    
    // Kotlinx Serialization
    api(libs.kotlinx.serialization.json)
    
    // Coroutines
    api(libs.kotlinx.coroutines.core)
    
    // Logging
    implementation(libs.bundles.logging)
    
    // Metrics (optional - only if metrics are enabled)
    api(libs.ktor.server.metrics.micrometer)
    api(libs.micrometer.registry.prometheus)
    api(libs.datadog.client)
    
    // Testing
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.mockk)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
