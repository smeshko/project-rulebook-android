plugins {
    alias(libs.plugins.rulebook.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.rulebook.core.database"
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
}
