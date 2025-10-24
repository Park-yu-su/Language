package com.example.language.api.friend

import android.content.Context
import com.example.language.api.ApiClient
import com.example.language.api.ApiResponse
import com.example.language.api.FriendListResponsePayload
import com.example.language.api.SimpleMessagePayload

class FriendRepository {

    //1. 친구 리스트 가져오기
    suspend fun getFriendList(context: Context, uid: Int)
    : ApiResponse<FriendListResponsePayload> {
        return ApiClient.getFriendList(context, uid)
    }

    //2. 친구 추가하기
    suspend fun addFriend(context: Context, requesterId: Int, requestieId: Int)
    : ApiResponse<SimpleMessagePayload> {
        return ApiClient.addFriend(context, requesterId, requestieId)
    }

    //3. 친구 요청 수락
    suspend fun acceptFriend(context: Context, requesterId: Int, requestieId: Int)
    : ApiResponse<SimpleMessagePayload> {
        return ApiClient.acceptFriend(context, requesterId, requestieId)
    }



}