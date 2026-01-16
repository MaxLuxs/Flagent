rootProject.name = "flagent"

include(":shared")
include(":backend")
include(":frontend")
include(":ktor-flagent")

// SDK projects
include(":kotlin-client")
include(":kotlin-enhanced")
include(":kotlin-debug-ui")

project(":shared").projectDir = file("shared")
project(":backend").projectDir = file("backend")
project(":frontend").projectDir = file("frontend")
project(":ktor-flagent").projectDir = file("ktor-flagent")

project(":kotlin-client").projectDir = file("sdk/kotlin")
project(":kotlin-enhanced").projectDir = file("sdk/kotlin-enhanced")
project(":kotlin-debug-ui").projectDir = file("sdk/kotlin-debug-ui")
