plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "flux"

include(":apps:backend")
include(":shared:api-contract")
include(":shared:core")
include(":shared:client-sdk")

include(":apps:desktop")
