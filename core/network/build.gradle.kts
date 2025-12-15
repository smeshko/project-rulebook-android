plugins {
    alias(libs.plugins.rulebook.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rulebook.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "BASE_URL", "\"https://api.rulebook.app/v1/\"")
    }
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
