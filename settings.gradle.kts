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
        maven { url = uri("https://jitpack.io") }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "HaruFocus"
include(":app")

// core
include(
    ":core:designsystem",
    ":core:ui",
    ":core:data",
    ":core:domain",
    ":core:util",
    ":core:common",
    ":core:datastore",
    ":core:datastore:api",
    ":core:datastore:impl",
    ":core:database",
    ":core:testing",
    ":core:ui-test-hilt"
)

// feature
include(
    ":feature:timer",
    ":feature:stats",
    ":feature:setting",
    ":feature:widget",
)

// performance
include(":baselineprofile")
