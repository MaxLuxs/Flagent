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
}

repositories {
    mavenCentral()
}

val springBootVersion = "3.2.0"

dependencies {
    implementation(project(":java-client"))
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.21")
    implementation("org.springframework.boot:spring-boot-starter:${springBootVersion}")
    implementation("org.springframework.boot:spring-boot-autoconfigure:${springBootVersion}")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator:${springBootVersion}")
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "com.flagent"
            artifactId = "flagent-spring-boot-starter"
            pom {
                name.set("Flagent Spring Boot Starter")
                description.set("Auto-configuration for Flagent Java client in Spring Boot")
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
