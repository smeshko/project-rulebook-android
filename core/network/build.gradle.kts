plugins {
    alias(libs.plugins.rulebook.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rulebook.core.network"
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
}
