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

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}
