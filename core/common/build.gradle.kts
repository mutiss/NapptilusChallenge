import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Pure Kotlin: shared result/error types and contracts, no Android and no feature knowledge.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    // Flow is part of the public API (ConnectivityObserver.isOnline).
    api(libs.kotlinx.coroutines.core)
}
