import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    jacoco
}


dependencies {
    // Shared models
    implementation(project(":shared"))
    
    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.server.auth)
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.ktor.server.swagger)
    
    // Database - Exposed
    implementation(libs.bundles.exposed)
    
    // Database Drivers
    implementation(libs.bundles.database.drivers)
    implementation(libs.hikaricp)
    
    // Kotlinx
    implementation(libs.bundles.kotlinx)
    
    // Logging
    implementation(libs.bundles.logging)
    
    // Monitoring
    implementation(libs.bundles.monitoring)
    
    // Messaging
    implementation(libs.bundles.messaging)
    
    // JWT
    implementation(libs.java.jwt)
    
    // Utilities
    implementation(libs.commons.lang3)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.module.kotlin)
    
    // Testing
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.bundles.testcontainers)
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

application {
    mainClass.set("flagent.application.ApplicationKt")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "flagent.application.ApplicationKt"
        )
    }
    // Don't create fat jar by default - use installDist or distZip instead
    // Fat jar can be created with separate task if needed
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    ignoreFailures = true
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/application/ApplicationKt.class",
                    "**/config/AppConfig*.class"
                )
            }
        })
    )
}
