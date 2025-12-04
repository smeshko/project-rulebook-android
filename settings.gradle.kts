pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Rulebook"

// App module
include(":app")

// Core modules
include(":core:analytics")
include(":core:billing")
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:model")
include(":core:network")

// Feature modules
include(":feature:camera")
include(":feature:library")
include(":feature:onboarding")
include(":feature:purchase")
include(":feature:rules")
include(":feature:settings")
