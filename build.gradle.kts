plugins {
    base
    id("com.android.application") version "9.0.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    group = "ru.flux"
    version = "0.0.1-SNAPSHOT"
}
