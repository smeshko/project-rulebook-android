plugins {
    alias(libs.plugins.rulebook.android.feature)
}

android {
    namespace = "com.rulebook.feature.rules"
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.koin.test)
}
