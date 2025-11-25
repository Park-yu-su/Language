package com.example.language.api.test

import android.content.Context
import com.example.language.api.ApiClient
import com.example.language.api.ApiResponse
import com.example.language.api.GetSubscribedWordbooksResponsePayload
import com.example.language.api.GetWordbookResponsePayload
import com.example.language.api.SimpleMessagePayload
import com.example.language.api.SttResponsePayload

class TestRepository {

    //1. STT 처리용 음원 보내기
    suspend fun sendVoiceForSTT(context: Context, fileBytes: ByteArray, fileName: String, answer: String)
    : ApiResponse<SttResponsePayload> {
        return ApiClient.sendVoiceForSTT(context, fileBytes, fileName, answer)
    }

    //2. 내 단어장 가져오기
    suspend fun getSubscribedWordbooks(context: Context, uid: Int)
            : ApiResponse<GetSubscribedWordbooksResponsePayload> {
        return ApiClient.getSubscribedWordbooks(context, uid)
    }

    //3. 단어장 태그를 줬을 때 해당 단어장의 단어들 가져오기
    suspend fun getWordbook(context: Context, wid: Int)
            : ApiResponse<GetWordbookResponsePayload> {
        return ApiClient.getWordbook(context, wid)
    }

    // status : liked | wrong | review
    // liked : 좋아요 한 단어
    // wrong : 틀린 단어
    // review : 리뷰할 단어

    //4. 각 태그에 따라 유저의 좋아요 한 단어, 틀린 단어, 리뷰할 단어 등록
    suspend fun linkWordUser(context: Context, uid: Int, word_ids: List<Int>, status: String)
            : ApiResponse<SimpleMessagePayload>{
        return ApiClient.linkWordUser(context, uid, word_ids, status)

    }

    //5. 좋아요 취소 등에 사용
    suspend fun unlinkWordUser(context: Context, uid: Int, word_ids: List<Int>, status: String)
            : ApiResponse<SimpleMessagePayload>{
        return ApiClient.unlinkWordUser(context, uid, word_ids, status)

    }

    //6. 푼 단어 제추
    suspend fun quizSubmit(context: Context, uid: Int,
                           wordId: Int, wordText: String, question: String,
                           userAnswer: String, correctAnswer: String)
    : ApiResponse<SimpleMessagePayload> {
        return ApiClient.quizSubmit(context, uid, wordId, wordText, question, userAnswer, correctAnswer)
    }


}