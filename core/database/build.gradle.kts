plugins {
    alias(libs.plugins.rulebook.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.rulebook.core.database"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.kotlinx.serialization.json)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // Unit Testing
    testImplementation(libs.junit)

    // Instrumented Testing (Room + Coroutines)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
