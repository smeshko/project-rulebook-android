plugins {
    alias(libs.plugins.rulebook.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rulebook.core.billing"

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))

    implementation(libs.play.billing)
    implementation(libs.androidx.core.ktx)
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.security.crypto)
    implementation(libs.kotlinx.serialization.json)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
}
