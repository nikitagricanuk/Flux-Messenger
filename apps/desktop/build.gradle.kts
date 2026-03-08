plugins {
    application
    `java`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("ru.flux.desktop.DesktopApplication")
}

dependencies {
    implementation(project(":shared:api-contract"))
    implementation(project(":shared:core"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.4")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
