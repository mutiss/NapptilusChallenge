plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
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

        buildConfigField(
            "String",
            "BASE_URL",
            "\"https://rickandmortyapi.com/api/\""
        )
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
        buildConfig = true
    }
    testFixtures {
        enable = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Compose BOM (material3 brings ui and ui-graphics; ui-tooling-preview is declared
    // because only the debug-only ui-tooling would otherwise provide it)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // AndroidX core & lifecycle. core-ktx is transitive too, but declaring it pins 1.19.x
    // (otherwise 1.18). viewmodel-compose brings lifecycle-runtime/viewmodel-ktx.
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Networking
    implementation(libs.bundles.networking)

    // Room (room-ktx brings room-runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Koin (koin-androidx-compose brings koin-android and koin-core)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.androidx.compose)

    // Image loading
    implementation(libs.coil.compose)

    // Paging
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Unit tests (koin-test-junit4 brings koin-test; the Koin BOM is inherited from implementation)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.koin.test.junit4)

    // Android tests
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.turbine)

    // Test fixtures (fakes shared between the test and androidTest source sets).
    // The Compose Compiler plugin applies to every source set in the module, so it needs
    // Compose Runtime on this classpath too, even though these fixtures use no Compose APIs.
    testFixturesImplementation(platform(libs.androidx.compose.bom))
    testFixturesImplementation(libs.androidx.compose.ui)
    testFixturesImplementation(libs.androidx.paging.runtime)

    // Debug
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
