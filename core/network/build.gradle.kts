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
        // Production URL as default
        buildConfigField("String", "BASE_URL", "\"https://api.rulebook.app/v1/\"")
    }

    buildTypes {
        debug {
            // Staging/development URL for debug builds
            buildConfigField("String", "BASE_URL", "\"https://api-staging.rulebook.app/v1/\"")
        }
        release {
            // Production URL for release builds
            buildConfigField("String", "BASE_URL", "\"https://api.rulebook.app/v1/\"")
        }
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
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
