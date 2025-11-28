package com.example.language.api.chat

import android.content.Context
import com.example.language.api.AIResponseDataPayload
import com.example.language.api.ApiClient
import com.example.language.api.ApiResponse
import com.example.language.api.BusinessTalkResponsePayload
import com.example.language.api.SessionStartResponsePayload
import com.example.language.api.SimpleMessagePayload

class ChatRepository {

    //1. 챗봇 대화 시작 설정
    suspend fun startSession(context: Context, uid: Int, name: String)
    : ApiResponse<SessionStartResponsePayload>{
        return ApiClient.startSession(context, uid, name)

    }

    //2. 챗봇에게 메시지 보내기
    suspend fun chatInput(context: Context, uid: Int, sessionId: String, message: String)
    : ApiResponse<AIResponseDataPayload>{
        return ApiClient.chatInput(context, uid, sessionId, message)
    }

    //3. 오늘 배운 단어 리뷰
    suspend fun todayReview(context: Context, uid: Int, sessionId: String)
    : ApiResponse<AIResponseDataPayload>{
        return ApiClient.todayReview(context, uid, sessionId)
    }

    //4. 예문 생성
    suspend fun generateExample(context: Context, uid: Int, sessionId: String)
    : ApiResponse<AIResponseDataPayload>{
        return ApiClient.generateExample(context, uid, sessionId)
    }

    //5. 레포트 작성
    suspend fun getReport(context: Context, uid: Int, sessionId: String)
    : ApiResponse<AIResponseDataPayload>{
        return ApiClient.analyzeLearning(context, uid, sessionId)
    }

    //6. 음성 -> String 바꾸기
    suspend fun doSTT(context: Context, fileBytes: ByteArray, fileName: String)
    : ApiResponse<SimpleMessagePayload>{
        return ApiClient.SendBackSTT(context, fileBytes, fileName)
    }

    //7. 바꾼 String -> 비즈니스 talk
    suspend fun businessTalk(context: Context, uid: Int, sessionId: String, text: String)
    : ApiResponse<BusinessTalkResponsePayload>{
        return ApiClient.businessTalk(context, uid, sessionId, text)
    }

}