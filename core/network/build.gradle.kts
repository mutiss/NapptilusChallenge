// Generic networking: HTTP clients, caching/retry interceptors, error mapping and connectivity.
// No endpoint knowledge: features contribute their base URL, API interfaces and cache policies.
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.mutissx.napptilusrickandmorty.core.network"
    compileSdk = 37

    defaultConfig {
        minSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        // BuildConfig.DEBUG drives the HTTP logging level.
        buildConfig = true
    }
}

dependencies {
    // Result/DataError appear in safeApiCall's signature.
    api(project(":core:common"))

    // OkHttp and Retrofit types are part of the public API (clients, interceptors, Retrofit.Builder).
    api(libs.okhttp)
    api(libs.retrofit)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
