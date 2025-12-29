import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp) // kapt 대체 (Room, Hilt 속도 향상)
    alias(libs.plugins.kotlin.serialization)
    // alias(libs.plugins.google.services) Firebase 사용시 주석 해제
    // fragment에 직렬화를 위해
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.language"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.language"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //API 추가
        val properties = Properties()
        val localProperties = project.rootProject.file("local.properties")
        if (localProperties.exists()) {
            properties.load(localProperties.inputStream())
        }

        manifestPlaceholders["KAKAO_API_KEY"] = properties.getProperty("KAKAO_API_KEY")
        buildConfigField("String", "KAKAO_API_KEY", "\"${properties.getProperty("KAKAO_API_KEY")}\"")
        buildConfigField("String", "BASE_URL", "\"${properties.getProperty("API_BASE_URL")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true // TODO Compose로 완전히 넘어가므로 false로 조정 필요
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.splashscreen)

    // Compose (UI)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // DI (Hilt)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Network & JSON
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)
    implementation(libs.kotlinx.serialization.json)

    // Image Loading (Glide 대신 Coil 사용 예정)
    implementation(libs.coil.compose)

    // Database (Room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Firebase
//    implementation(platform(libs.firebase.bom))
//    implementation(libs.firebase.analytics)
//    implementation(libs.firebase.firestore)
//    implementation(libs.firebase.messaging)

    // Kakao
    implementation(libs.kakao.user)
    implementation(libs.kakao.auth)

    // Lottie (Animation)
    implementation(libs.lottie.compose)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // *********************************
    // Legacy
    // ********************************

//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    implementation(libs.androidx.activity)
//    implementation(libs.androidx.constraintlayout)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//
//    // 1) HTTP 클라이언트 & JSON 파싱
//    implementation("com.squareup.retrofit2:retrofit:3.0.0")
//    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
//
//
//    // 3) Material Components (DatePicker 포함)
//    implementation("com.google.android.material:material:1.9.0")
//
//    // 4) bottomnavigation
//    implementation("androidx.navigation:navigation-fragment-ktx:2.7.3")
//    implementation("androidx.navigation:navigation-ui-ktx:2.7.3")
//    implementation("com.google.android.material:material:1.11.0")
//
//    // 5) 기타 기능 및 위젯들 implement
//
//    // 5) kakao login
//    implementation("com.kakao.sdk:v2-user:2.13.0")
//    implementation("com.kakao.sdk:v2-auth:2.13.0")
//
//    //6) splash screen
//    implementation("androidx.core:core-splashscreen:1.0.0")
//
//    //7) grid layout
//    implementation("androidx.gridlayout:gridlayout:1.0.0")
//
//    //달력
//    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
//    // ThreeTenABP: Android용 Backport
//    implementation("com.jakewharton.threetenabp:threetenabp:1.3.1")
//
//    //원형 그래프(PieChart)
//    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
//
//    // Firebase Analytics
//    implementation("com.google.firebase:firebase-analytics-ktx:22.5.0")
//
//    // Cloud Firestore
//    implementation("com.google.firebase:firebase-firestore-ktx:25.1.4")
//
//    // Firebase Cloud Messaging
//    implementation("com.google.firebase:firebase-messaging-ktx:24.1.2")
//
//    //Http-SSE 통신
//    implementation("com.squareup.okhttp3:okhttp:4.12.0")
//    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
//
//    //프로필 이미지 관련 Glide 라이브러리
//    implementation("com.github.bumptech.glide:glide:4.16.0")
//    kapt("com.github.bumptech.glide:compiler:4.16.0")
//
//    //프로필 이미지를 둥글게 만들어 주는 라이브러리
//    implementation("de.hdodenhof:circleimageview:3.1.0")
//
//    //RoomDB
//    val roomVersion = "2.6.1"
//    implementation("androidx.room:room-runtime:$roomVersion")
//    implementation("androidx.room:room-ktx:$roomVersion")
//    kapt("androidx.room:room-compiler:$roomVersion")
//
//
//    //flexLayout (유연한 recycler)
//    implementation("com.google.android.flexbox:flexbox:3.0.0")
//
//
//    /**서버 관련**/
//    //Kotlinx Serialization 런타임 라이브러리 추가 (필수)
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
//
//    //Kotlin Coroutines (비동기 처리)
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
//
//    //애니메이션을 위한 lottie
//    implementation("com.airbnb.android:lottie:6.0.0")
//
//    //Markdown 텍스트
//    implementation("io.noties.markwon:core:4.6.2")

}