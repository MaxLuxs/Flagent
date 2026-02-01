import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.ktor.plugin)
    alias(libs.plugins.dependency.check)
    application
    jacoco
}

dependencyCheck {
    failBuildOnCVSS = 11f  // Don't fail on CVSS, only report
    failOnError = false    // Don't fail when NVD is unreachable (403, etc.)
    formats = listOf("HTML", "JSON")
    val dataDir = project.findProperty("dependencyCheck.dataDirectory")?.toString()
        ?: "${System.getenv("RUNNER_TEMP") ?: layout.buildDirectory.get().asFile}/dependency-check-data"
    data.directory.set(dataDir)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // Shared models
    implementation(project(":shared"))

    // Enterprise module (optional, when internal/flagent-enterprise submodule is present).
    // runtimeOnly to avoid circular dependency: flagent-enterprise depends on backend for EnterpriseConfigurator interface.
    if (project.findProject(":flagent-enterprise") != null) {
        runtimeOnly(project(":flagent-enterprise"))
    }

    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.server.auth)
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.routing.openapi)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.sse)

    // Ktor Client (for SSO/OAuth)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)

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
    // Override transitive lz4-java (kafka-clients pulls org.lz4:1.8.0); root build substitutes with at.yawk.lz4 fork
    implementation(libs.lz4.java)

    // JWT (JJWT)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // Billing
    implementation(libs.stripe.java)

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
    useJUnitPlatform {
        excludeTags("performance")
    }
    // Exclude integration tests by default (they need Postgres). CI runs them with -PincludeIntegrationTests.
    if (!project.hasProperty("includeIntegrationTests")) {
        filter {
            excludeTestsMatching("*IntegrationTest")
        }
    }
    finalizedBy(tasks.jacocoTestReport)
    ignoreFailures = true
    maxParallelForks = 1
    environment("FLAGENT_DB_DBDRIVER", "sqlite3")
    environment("FLAGENT_DB_DBCONNECTIONSTR", ":memory:")
    reports {
        junitXml.required.set(false)
    }
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

// Configure shadowJar to use zip64 for large archives
tasks.named<ShadowJar>("shadowJar") {
    isZip64 = true
}

// Configure OpenAPI compiler extension for runtime metadata generation
ktor {
    openApi {
        enabled = true
        codeInferenceEnabled = true
        onlyCommented = false
    }
}
