plugins {
    alias(libs.plugins.rulebook.android.feature)
}

android {
    namespace = "com.rulebook.feature.library"
}

dependencies {
    implementation(project(":core:data"))

    implementation(libs.coil.compose)
}
