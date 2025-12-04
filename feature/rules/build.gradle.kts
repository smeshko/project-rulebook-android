plugins {
    alias(libs.plugins.rulebook.android.feature)
}

android {
    namespace = "com.rulebook.feature.rules"
}

dependencies {
    implementation(project(":core:data"))
}
