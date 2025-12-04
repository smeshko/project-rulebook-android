plugins {
    alias(libs.plugins.rulebook.android.feature)
}

android {
    namespace = "com.rulebook.feature.onboarding"
}

dependencies {
    implementation(project(":core:data"))
}
