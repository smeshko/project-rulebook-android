plugins {
    alias(libs.plugins.rulebook.android.library)
}

android {
    namespace = "com.rulebook.core.analytics"
}

dependencies {
    // TODO: Add TelemetryDeck in Story 1.6
    // implementation(libs.telemetrydeck)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
}
