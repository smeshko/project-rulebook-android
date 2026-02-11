plugins {
    alias(libs.plugins.rulebook.android.feature)
}

android {
    namespace = "com.rulebook.feature.library"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
