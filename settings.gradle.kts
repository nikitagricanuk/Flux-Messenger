pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "flux"

include(":apps:backend")
include(":shared:api-contract")
include(":shared:core")
include(":shared:client-sdk")
include(":apps:android")
include(":apps:desktop")
