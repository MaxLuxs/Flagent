import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
    java
    `maven-publish`
}

group = "com.flagent"
// version inherited from root (VERSION file)

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

// OpenAPI Generator "native" library uses Java 11+ HttpClient and Jackson
dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.21.0")
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

// OpenAPI-generated code lacks Javadoc on protected fields; suppress doclint to avoid build failure
tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

tasks.test {
    useJUnitPlatform()
}

// Run ./generate.sh (or :java-client:generateClient) to generate code; then build
tasks.register<Exec>("generateClient") {
    description = "Generate Java client from OpenAPI spec"
    group = "code generation"
    commandLine("sh", "${project.projectDir}/generate.sh")
    onlyIf { file("${rootProject.projectDir}/docs/api/openapi.yaml").exists() }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "com.flagent"
            artifactId = "flagent-java-client"
            pom {
                name.set("Flagent Java Client")
                description.set("Java client library for Flagent API")
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
