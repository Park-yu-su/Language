package com.example.language.api.mypage

import android.content.Context
import com.example.language.api.ApiClient
import com.example.language.api.ApiResponse
import com.example.language.api.AuthResponsePayload
import com.example.language.api.GetSubscribedWordbooksResponsePayload
import com.example.language.api.GetWordbookResponsePayload

class MypageRepository {

    //1. 내 단어장 가져오기
    suspend fun getSubscribedWordbooks(context: Context, uid: Int)
            : ApiResponse<GetSubscribedWordbooksResponsePayload> {
        return ApiClient.getSubscribedWordbooks(context, uid)
    }

    //2. 단어장 아이디를 줬을 때 해당 단어장의 단어들 가져오기
    suspend fun getWordbook(context: Context, wid: Int)
            : ApiResponse<GetWordbookResponsePayload> {
        return ApiClient.getWordbook(context, wid)
    }



    //3. auth 이용해서 유저 정보 업데이트
    suspend fun loginUser(context: Context, email: String, nickname: String)
            : ApiResponse<AuthResponsePayload> {
        // ApiClient의 함수를 호출하고 결과를 반환합니다.
        // ApiClient는 Dispatchers.IO에서 실행되므로, 여기서도 suspend 함수가 됩니다.
        return ApiClient.authenticate(context, email, nickname)
    }


}