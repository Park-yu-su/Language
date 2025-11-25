package com.example.language.api.login

import android.content.Context
import com.example.language.api.ApiClient
import com.example.language.api.ApiResponse
import com.example.language.api.AuthResponsePayload

class LoginRepository {

    // 1. API 클라이언트 호출 함수
    suspend fun loginUser(context: Context, email: String, nickname: String, image: String, oneline: String)
    : ApiResponse<AuthResponsePayload> {
        // ApiClient의 함수를 호출하고 결과를 반환합니다.
        // ApiClient는 Dispatchers.IO에서 실행되므로, 여기서도 suspend 함수가 됩니다.
        return ApiClient.authenticate(context, email, nickname, image, oneline)
    }

}