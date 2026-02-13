plugins {
    alias(libs.plugins.rulebook.android.feature)
}

android {
    namespace = "com.rulebook.feature.settings"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
