// Design system and feature-agnostic UI: theme, UiText + error texts, generic state views and
// the shared-transition plumbing.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.mutissx.napptilusrickandmorty.core.ui"
    compileSdk = 37

    defaultConfig {
        minSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // DataError appears in asUiText's signature.
    api(project(":core:common"))

    // Composables, Modifiers and theme types are this module's public API.
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
