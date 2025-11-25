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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // ZegoCloud repository
        maven { url = uri("https://storage.zego.im/maven2") }
        // JitPack for ZegoCloud
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "HypeMatch"
include(":app")
 