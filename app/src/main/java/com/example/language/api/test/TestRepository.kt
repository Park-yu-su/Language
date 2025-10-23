package com.example.language.api.test

import android.content.Context
import com.example.language.api.ApiClient
import com.example.language.api.ApiResponse
import com.example.language.api.SttResponsePayload

class TestRepository {

    //1. STT 처리용 음원 보내기
    suspend fun sendVoiceForSTT(context: Context, fileBytes: ByteArray, fileName: String, answer: String)
    : ApiResponse<SttResponsePayload> {
        return ApiClient.sendVoiceForSTT(context, fileBytes, fileName, answer)
    }

}