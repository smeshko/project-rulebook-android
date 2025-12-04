plugins {
    alias(libs.plugins.rulebook.android.feature)
}

android {
    namespace = "com.rulebook.feature.camera"
}

dependencies {
    implementation(project(":core:data"))

    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
}
