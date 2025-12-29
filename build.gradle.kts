plugins {
    // Android Application 플러그인
    alias(libs.plugins.android.application) apply false

    // Kotlin 플러그인
    alias(libs.plugins.jetbrains.kotlin.android) apply false

    // Compose Compiler
    alias(libs.plugins.compose.compiler) apply false

    // Hilt (의존성 주입)
    alias(libs.plugins.hilt.android) apply false

    // KSP (Annotation Processing 대체)
    alias(libs.plugins.ksp) apply false

    // Kotlin Serialization (JSON 파싱)
    alias(libs.plugins.kotlin.serialization) apply false

    // Google Services (Firebase용)
    // alias(libs.plugins.google.services) apply false
}

// *****************************
// Legacy
// *****************************
//
////추가 firebase & 구글 서비스 플러그인
//buildscript {
//    repositories {
//        google()
//        mavenCentral()
//    }
//    dependencies {
//        // Google Services 플러그인 클래스패스
//        //classpath("com.google.gms:google-services:4.4.3")
//        // fragment간 안전한 데이터 전달
//        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.9.3")
//    }
//}
//
//
//// Top-level build file where you can add configuration options common to all sub-projects/modules.
//plugins {
//    alias(libs.plugins.android.application) apply false
//    alias(libs.plugins.kotlin.android) apply false
//
//    // Firebase Gradle 플러그인 등록 (apply false)
//    //id("com.google.gms.google-services") version "4.4.3" apply false
//}