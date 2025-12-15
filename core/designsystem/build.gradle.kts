plugins {
    alias(libs.plugins.rulebook.android.library.compose)
}

android {
    namespace = "com.rulebook.core.designsystem"
}

dependencies {
    api(libs.compose.foundation)
    api(libs.compose.material.icons.extended)

    testImplementation(libs.junit)
}
