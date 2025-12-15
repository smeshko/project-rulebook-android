plugins {
    alias(libs.plugins.rulebook.android.library)
}

android {
    namespace = "com.rulebook.core.analytics"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // TelemetryDeck App ID - use placeholder for development
        // Production app ID should be configured via local.properties or CI secrets
        buildConfigField("String", "TELEMETRY_APP_ID", "\"${findProperty("TELEMETRY_APP_ID") ?: ""}\"")
    }
}

dependencies {
    // Analytics
    implementation(libs.telemetrydeck)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // Testing
    testImplementation(libs.junit)
}
