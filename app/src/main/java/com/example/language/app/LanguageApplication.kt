package com.example.language.app

import android.app.Application
import com.example.language.BuildConfig
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class LanguageApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, BuildConfig.KAKAO_API_KEY)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree()) // 디버그 모드일 때만 로그 출력 활성화
        }
    }
}