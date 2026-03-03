plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.flagent"
// version from root VERSION

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.kotlin.stdlib)
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "flagent-annotations"
            pom {
                name.set("Flagent Annotations")
                description.set("Annotations for Flagent build-time verification (@FlagKey)")
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
