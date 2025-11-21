package com.example.language.api.login

import android.content.Context
import android.content.SharedPreferences
import com.example.language.api.AuthResponsePayload

class UserPreference(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


    //유저 정보 API Response로 받은 거 저장
    fun saveUserInfo(uid: String, nickname: String, email: String) {
        prefs.edit().apply {
            putString(KEY_UID, uid)
            putString(KEY_NICKNAME, nickname)
            putString(KEY_EMAIL, email)
            apply() // 비동기로 저장
        }
    }

    //UID 불러오기
    fun getUid(): String? {
        //UID가 없으면 null 반환
        return prefs.getString(KEY_UID, null)
    }

    //이름 불러오기
    fun getName(): String {
        return prefs.getString(KEY_NICKNAME, null) ?: ""
    }

    fun updateName(name: String){
        prefs.edit().apply {
            putString(KEY_NICKNAME, name)
            apply()
        }
    }

    //이메일 가져오기
    fun getEmail(): String{
        return prefs.getString(KEY_EMAIL, null) ?: ""
    }

    //그레이트리셋
    fun clearAll() {
        prefs.edit().clear().apply()
    }
    

    //컴페니언으로 관리
    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_UID = "uid"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_EMAIL = "email"
        // private const val KEY_IMAGE = "image"
    }

}