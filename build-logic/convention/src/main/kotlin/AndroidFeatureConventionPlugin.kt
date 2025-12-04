import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.api.artifacts.VersionCatalogsExtension

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "rulebook.android.library.compose")

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:model"))
                add("implementation", project(":core:common"))

                // Lifecycle
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())

                // Koin
                val koinBom = libs.findLibrary("koin-bom").get()
                add("implementation", platform(koinBom))
                add("implementation", libs.findLibrary("koin-core").get())
                add("implementation", libs.findLibrary("koin-android").get())
                add("implementation", libs.findLibrary("koin-androidx-compose").get())

                // Navigation
                add("implementation", libs.findLibrary("compose-navigation").get())
            }
        }
    }
}
