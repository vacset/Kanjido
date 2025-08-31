plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "me.seta.vacset.kanjido"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.seta.vacset.kanjido"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // Compose BOM (main)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.foundation)
    testImplementation(libs.junit.junit)
    // Compose BOM also for androidTest and debug (so test/tooling artifacts get versions)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(platform(libs.androidx.compose.bom))

    // Compose UI set (bundle)
    implementation(libs.bundles.compose)

    // Preview tooling (debug only is common, but implementation also works)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // AndroidX platform libs
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Features
    implementation(libs.zxing)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.compose.material.icons)

    // Android tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // IMPORTANT: Use the alias you defined above (compose prefix), not the template one.
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Compose test manifest (debug)
    debugImplementation(libs.androidx.ui.test.manifest)
}
