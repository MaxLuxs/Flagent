import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor.plugin)
    alias(libs.plugins.dependency.check)
    application
    jacoco
}

dependencyCheck {
    failBuildOnCVSS = 11f  // Don't fail on CVSS, only report
    failOnError = false    // Don't fail when NVD is unreachable (403, 429, etc.)
    formats = listOf("HTML", "JSON")
    // Without NVD API key, NVD returns 429; skip update and use cache only
    val nvdKey = System.getenv("NVD_API_KEY")
    autoUpdate = !nvdKey.isNullOrBlank()
    if (!nvdKey.isNullOrBlank()) {
        nvd.apiKey = nvdKey
    }
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

    // Database Migrations
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

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
    implementation(libs.jbcrypt)
    runtimeOnly(libs.jjwt.jackson)

    // Billing
    implementation(libs.stripe.java)

    // Firebase / Google Auth (for Remote Config REST API)
    implementation(libs.google.auth.library.oauth2.http)

    // MCP (Model Context Protocol) - optional, for AI assistants integration
    implementation(libs.kotlin.sdk.server)

    // Utilities
    implementation(libs.commons.lang3)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.module.kotlin)

    // Testing
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.ktor.client.mock)
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

// Run backend with dev mode: X-API-Key optional, uses first active tenant; admin login enabled for /login
tasks.register<JavaExec>("runDev") {
    group = "application"
    description = "Run backend with FLAGENT_DEV_MODE+FLAGENT_DEV_SKIP_TENANT_AUTH (X-API-Key optional) and admin auth for login"
    mainClass.set(application.mainClass.get())
    classpath = sourceSets.main.get().runtimeClasspath
    workingDir = rootProject.projectDir
    environment("FLAGENT_DEV_MODE", "true")
    environment("FLAGENT_DEV_SKIP_TENANT_AUTH", "true")
    environment("FLAGENT_ADMIN_AUTH_ENABLED", "true")
    environment("FLAGENT_ADMIN_EMAIL", "admin@local")
    environment("FLAGENT_ADMIN_PASSWORD", "admin")
    environment("FLAGENT_JWT_AUTH_SECRET", "dev-secret-at-least-32-characters-long")
    environment("FLAGENT_ADMIN_API_KEY", "dev-admin-key")
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
        val tagsToExclude = mutableListOf("performance", "compatibility")
        if (!project.hasProperty("includeE2E")) {
            tagsToExclude.add("e2e")  // E2E/load tests use full app and may need Kafka; run with -PincludeE2E (sets FLAGENT_RECORDER_ENABLED=false)
        }
        if (project.hasProperty("includeCompatibilityTests")) {
            excludeTags("performance")
        } else {
            excludeTags(*tagsToExclude.toTypedArray())
        }
    }
    // Exclude integration tests by default (they need Postgres). CI runs them with -PincludeIntegrationTests.
    if (!project.hasProperty("includeIntegrationTests")) {
        filter {
            excludeTestsMatching("*IntegrationTest")
        }
    }
    finalizedBy(tasks.jacocoTestReport)
    maxParallelForks = 1
    // DB env: use Postgres when CI runs integration tests (-PincludeIntegrationTests); else SQLite in-memory
    if (!project.hasProperty("includeIntegrationTests")) {
        environment("FLAGENT_DB_DBDRIVER", "sqlite3")
        environment("FLAGENT_DB_DBCONNECTIONSTR", ":memory:")
    } else {
        // Local integration tests: use SQLite in-memory when Postgres not configured
        if (System.getenv("FLAGENT_DB_DBDRIVER").isNullOrBlank()) {
            environment("FLAGENT_DB_DBDRIVER", "sqlite3")
            environment("FLAGENT_DB_DBCONNECTIONSTR", ":memory:")
        }
        // Fast cache refresh for integration tests (eval needs fresh data after flag setup)
        environment("FLAGENT_EVALCACHE_REFRESHINTERVAL", "100ms")
    }
    // Admin auth for AuthRoutesTest (success, wrong email, blank)
    environment("FLAGENT_ADMIN_AUTH_ENABLED", "true")
    environment("FLAGENT_ADMIN_EMAIL", "admin@test.com")
    environment("FLAGENT_ADMIN_PASSWORD", "secret123")
    environment("FLAGENT_JWT_AUTH_SECRET", "test-secret-at-least-32-characters-long")
    // E2ETest creates tenant via POST /admin/tenants; when enterprise is present AdminAuth requires X-Admin-Key
    environment("FLAGENT_ADMIN_API_KEY", "test-admin-key")
    // When running E2E tests (-PincludeE2E), use noop recorder so Kafka is not required
    if (project.hasProperty("includeE2E")) {
        environment("FLAGENT_RECORDER_ENABLED", "false")
    }
    // IntegrationWebhookRoutesTest - GitHub webhook auto-create
    environment("FLAGENT_GITHUB_AUTO_CREATE_FLAG", "true")
    reports {
        junitXml.required.set(false)
        html.required.set(false)
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
