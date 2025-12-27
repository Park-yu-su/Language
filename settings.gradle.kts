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

        // JitPack (서드파티 라이브러리용)
        maven { url = uri("https://jitpack.io") }

        // Kakao SDK (카카오 로그인용)
        maven { url = uri("https://devrepo.kakao.com/nexus/content/groups/public/") }
    }
}

rootProject.name = "Language"
include(":app")

// *******************
// Legacy
// *******************
//
//pluginManagement {
//    repositories {
//        google {
//            content {
//                includeGroupByRegex("com\\.android.*")
//                includeGroupByRegex("com\\.google.*")
//                includeGroupByRegex("androidx.*")
//            }
//        }
//        mavenCentral()
//        gradlePluginPortal()
//        //Material CalenderView와 PieChart를 위한 import
//        maven { url = uri("https://jitpack.io") }
//    }
//}
//dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositories {
//        google()
//        mavenCentral()
//        //Material CalenderView와 PieChart를 위한 import
//        maven { url = uri("https://jitpack.io") }
//
//        //카카오 소셜 로그인
//        maven("https://devrepo.kakao.com/nexus/content/groups/public/")
//    }
//}
//
//rootProject.name = "Language"
//include(":app")
 