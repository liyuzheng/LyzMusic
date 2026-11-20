enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LYZMusic"
include(":app")
include(":core")
include(":core:core_tools")
include(":core:core_mvvm")
include(":core:core_common")
include(":core:core_router")
include(":feature-impl")
include(":data")
include(":feature-api")
include(":core:core_widget")
include(":core:core_widget:core_paging")
include(":core:core_network")
include(":core:core_widget:core_glassview")
include(":domain")
include(":domain:database")
include(":domain:paging")
include(":domain:common-ui")
include(":feature-impl:home")
include(":feature-impl:discover")
include(":feature-impl:music-lib")
include(":feature-impl:profile")
include(":data:profile")
include(":feature-api:discover")
include(":domain:network")
