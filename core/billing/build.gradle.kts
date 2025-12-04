plugins {
    alias(libs.plugins.rulebook.android.library)
}

android {
    namespace = "com.rulebook.core.billing"
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.play.billing)
    implementation(libs.androidx.core.ktx)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
}
