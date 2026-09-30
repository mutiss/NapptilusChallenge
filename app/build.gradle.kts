plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.mutissx.napptilusrickandmorty"
    // core-ktx 1.19 / lifecycle 2.11 require compiling against API 37.
    compileSdk = 37

    defaultConfig {
        applicationId = "com.mutissx.napptilusrickandmorty"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Keep rules live in src/main/keepRules/rules.keep (merged automatically by AGP).
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

// The app module only wires things together: Application (Koin + Coil), the Activity, and the
// navigation host with its bottom bar. Features and core modules hold everything else.
dependencies {
    implementation(project(":feature:characters"))
    implementation(project(":core:network"))
    implementation(project(":core:ui"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.androidx.compose)
    implementation(libs.coil.compose)

    // Unit tests: the Koin graph check spans every module, so it lives here.
    testImplementation(libs.junit)
    testImplementation(libs.koin.test.junit4)
    testImplementation(libs.okhttp.logging.interceptor)
    testImplementation(libs.androidx.lifecycle.viewmodel.compose)
}
