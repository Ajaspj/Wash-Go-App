pluginManagement {
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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Wag-Go-App"
include(":app")

include(":core:model")
project(":core:model").projectDir = file("core/model")

include(":core:designsystem")
project(":core:designsystem").projectDir = file("core/designsystem")

include(":apps:user")
project(":apps:user").projectDir = file("apps/user")

include(":apps:agent")
project(":apps:agent").projectDir = file("apps/agent")

