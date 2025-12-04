plugins {
    alias(libs.plugins.rulebook.android.feature)
}

android {
    namespace = "com.rulebook.feature.settings"
}

dependencies {
    implementation(project(":core:data"))
}
