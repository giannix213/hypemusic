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
        // JitPack for ZegoCloud UIKit (MUST be before ZegoCloud maven)
        maven { url = uri("https://jitpack.io") }
        // ZegoCloud official repository
        maven { 
            url = uri("https://storage.zego.im/maven2")
            isAllowInsecureProtocol = false
        }
        // Repositorio alternativo de ZegoCloud
        maven {
            url = uri("https://www.jitpack.io")
        }
    }
}

rootProject.name = "HypeMatch"
include(":app")
 