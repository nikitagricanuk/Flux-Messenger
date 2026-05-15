plugins {
    alias(libs.plugins.android.application)
    id("androidx.navigation.safeargs")
}

android {
    namespace = "ru.flux.android"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "ru.flux.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "BACKEND_BASE_URL", "\"http://10.0.2.2:8080/\"")
        buildConfigField("String", "OAUTH_CALLBACK_SCHEME", "\"flux\"")
        buildConfigField("String", "OAUTH_CALLBACK_HOST", "\"auth\"")
        buildConfigField("String", "OAUTH_CALLBACK_PATH", "\"/callback\"")
        buildConfigField("String", "OAUTH_START_PATH_TEMPLATE", "\"api/auth/oauth/%s/start\"")
        buildConfigField("String", "OAUTH_CODE_EXCHANGE_PATH", "\"api/auth/oauth/exchange\"")
        buildConfigField("String", "PASSKEY_OPTIONS_PATH", "\"api/auth/passkey/authenticate/start\"")
        buildConfigField("String", "PASSKEY_VERIFY_PATH", "\"api/auth/passkey/authenticate/finish\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

tasks.register("adbReverse") {
    description = "Forward host ports 80 and 8080 to the emulator via adb reverse"
    doLast {
        ProcessBuilder("adb", "reverse", "tcp:80", "tcp:80").inheritIO().start().waitFor()
        ProcessBuilder("adb", "reverse", "tcp:8080", "tcp:8080").inheritIO().start().waitFor()
    }
}

tasks.whenTaskAdded {
    if (name == "installDebug") {
        finalizedBy("adbReverse")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.blurview) {
        // blurview bundles an older coordinatorlayout which conflicts with the one
        // pulled by appcompat/material. Exclude it — the project already has it.
        exclude(group = "androidx.coordinatorlayout", module = "coordinatorlayout")
    }
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.fragment)

    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    implementation(libs.security.crypto)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.browser)
    implementation(libs.glide)
    implementation(libs.legacy.support.v4)



    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
