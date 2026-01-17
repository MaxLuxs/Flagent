plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    jacoco
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
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}

// JaCoCo configuration for multiplatform JVM target
val jvmTestTask = tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    ignoreFailures = true  // Allow coverage report generation even if tests fail
}

tasks.register<org.gradle.testing.jacoco.tasks.JacocoReport>("jacocoTestReport") {
    dependsOn(jvmTestTask)
    group = "verification"
    
    reports {
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml"))
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/test/html"))
        csv.required.set(false)
    }
    
    // For multiplatform projects, JaCoCo only works with JVM target
    val jacocoExecFile = layout.buildDirectory.file("jacoco/jvmTest.exec").get().asFile
    executionData.setFrom(jacocoExecFile.takeIf { it.exists() }?.let { files(it) } ?: files())
    
    val jvmMainClasses = layout.buildDirectory.dir("classes/kotlin/jvm/main")
    classDirectories.setFrom(fileTree(jvmMainClasses))
    
    val commonMainSrc = file("$projectDir/src/commonMain/kotlin")
    val jvmMainSrc = file("$projectDir/src/jvmMain/kotlin")
    sourceDirectories.setFrom(commonMainSrc, jvmMainSrc)
}

jvmTestTask.configure {
    finalizedBy(tasks.named("jacocoTestReport"))
}
