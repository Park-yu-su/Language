package com.example.language.api.study

import android.content.Context
import com.example.language.api.ApiClient
import com.example.language.api.ApiResponse
import com.example.language.api.GetSubscribedWordbooksResponsePayload
import com.example.language.api.GetWordbookInfoWithIDResponsePayload
import com.example.language.api.GetWordbookResponsePayload
import com.example.language.api.SearchTagResponsePayload
import com.example.language.api.SearchWordbookResponsePayload
import com.example.language.api.SimpleMessagePayload

class StudyRepository {

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

    //3. 단어장 태그 검색하기
    suspend fun searchWordbookbyTag(context: Context, tids: List<Int>)
    : ApiResponse<SearchWordbookResponsePayload>{
        return ApiClient.searchWordbook(context, tids)
    }
    suspend fun searchWordbookbyTagOR(context: Context, tids: List<Int>)
    : ApiResponse<SearchWordbookResponsePayload>{
        return ApiClient.searchWordbookOr(context, tids)
    }

    //4. String 태그 -> 태그 id로 변환
    suspend fun searchTag(context: Context, query: String)
    : ApiResponse<SearchTagResponsePayload>{
        return ApiClient.searchTag(context, query)
    }


    //5. 검색한 단어장을 구독(추가)
    suspend fun subscribeWordbook(context: Context, wid: Int, subscriber: Int)
    : ApiResponse<SimpleMessagePayload>{
        return ApiClient.subscribe(context, wid, subscriber)
    }

    //6. 단어장 ID 검색하기
    suspend fun searchWordbookbyId(context: Context, wid: Int)
    : ApiResponse<GetWordbookInfoWithIDResponsePayload>{
        return ApiClient.getWordbookInfoWithID(context, wid)
    }


    //7. 홈화면용 랜덤 단어 리스트 가져오기
    suspend fun getRandomWord(context: Context, uid: Int)
    : ApiResponse<GetWordbookResponsePayload>{
        return ApiClient.getRandomSubscribedWord(context, uid)

    }




}