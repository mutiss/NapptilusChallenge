// Generic local storage helpers: safe DB calls, error mapping and a Room builder. Databases,
// entities and DAOs belong to the features that own them.
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.mutissx.napptilusrickandmorty.core.persistence"
    compileSdk = 37

    defaultConfig {
        minSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Result/DataError appear in safeDbCall's signature.
    api(project(":core:common"))
    // RoomDatabase is part of the public API (buildRoomDatabase).
    api(libs.androidx.room.ktx)

    testImplementation(libs.junit)
}
