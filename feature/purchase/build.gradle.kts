plugins {
    alias(libs.plugins.rulebook.android.feature)
}

android {
    namespace = "com.rulebook.feature.purchase"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:billing"))
}
