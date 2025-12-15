plugins {
    alias(libs.plugins.rulebook.android.library)
}

android {
    namespace = "com.rulebook.core.datastore"
}

dependencies {
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.core.ktx)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
