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
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        //네이버 sdk 저장소 추가
        maven("https://repository.map.naver.com/archive/maven")
        //텍스트 변경 저장소 추가
        maven ("https://jitpack.io")
    }
}

rootProject.name = "sleep"
include(":app")