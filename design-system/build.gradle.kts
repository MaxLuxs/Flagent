plugins {
    kotlin("jvm")
    `maven-publish`
}

val generateDesignTokens = tasks.register<Exec>("generateDesignTokens") {
    group = "design-system"
    description = "Generate CSS, TS, Kotlin, Swift from design-system/tokens/tokens.json"
    workingDir = project.projectDir
    commandLine("node", "build-tokens.js")
    outputs.dir(project.file("build/kotlin"))
}

val copyCssToFrontend = tasks.register<Copy>("copyCssToFrontend") {
    group = "design-system"
    description = "Copy generated flagent-tokens.css to frontend resources"
    dependsOn(generateDesignTokens)
    from(project.file("build/css/flagent-tokens.css"))
    into(project.rootProject.project(":frontend").layout.projectDirectory.dir("src/jsMain/resources"))
}

val copyTsToJsDebugUi = tasks.register<Copy>("copyTsToJsDebugUi") {
    group = "design-system"
    description = "Copy generated FlagentTokens.ts to JS debug UI"
    dependsOn(generateDesignTokens)
    from(project.file("build/ts/FlagentTokens.ts"))
    into(project.rootProject.file("sdk/javascript-debug-ui/src"))
}

val copySwiftToSwiftDebugUi = tasks.register<Copy>("copySwiftToSwiftDebugUi") {
    group = "design-system"
    description = "Copy generated FlagentTokens.swift to Swift debug UI"
    dependsOn(generateDesignTokens)
    from(project.file("build/swift/FlagentTokens.swift"))
    into(project.rootProject.file("sdk/swift-debug-ui/Sources/FlagentDebugUI"))
}

tasks.named("compileKotlin") {
    dependsOn(copyCssToFrontend, copyTsToJsDebugUi, copySwiftToSwiftDebugUi)
}

kotlin {
    jvmToolchain(21)
}

sourceSets {
    main {
        kotlin.setSrcDirs(listOf("build/kotlin"))
    }
}

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui.graphics)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("Flagent Design Tokens")
                description.set("Design tokens (Kotlin) for Flagent — generated from design-system/tokens/tokens.json")
                url.set("https://github.com/MaxLuxs/Flagent")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
            }
        }
    }
}
