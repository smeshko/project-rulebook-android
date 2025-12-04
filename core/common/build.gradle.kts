plugins {
    alias(libs.plugins.rulebook.android.library)
}

android {
    namespace = "com.rulebook.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
