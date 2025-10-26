package com.example.language.api.friend

import android.content.Context
import com.example.language.api.ApiClient
import com.example.language.api.ApiResponse
import com.example.language.api.FriendListResponsePayload
import com.example.language.api.SearchUserResponsePayload
import com.example.language.api.SimpleMessagePayload

class FriendRepository {

    //1. 친구 리스트 가져오기 (O)
    suspend fun getFriendList(context: Context, uid: Int)
    : ApiResponse<FriendListResponsePayload> {
        return ApiClient.getFriendList(context, uid)
    }

    //2. 친구 추가
    suspend fun addFriend(context: Context, requesterId: Int, requestieId: Int)
    : ApiResponse<SimpleMessagePayload> {
        return ApiClient.addFriend(context, requesterId, requestieId)
    }

    //3. 친구 요청 수락
    suspend fun acceptFriend(context: Context, requesterId: Int, requestieId: Int)
    : ApiResponse<SimpleMessagePayload> {
        return ApiClient.acceptFriend(context, requesterId, requestieId)
    }

    //4. 친구 요청 거절
    suspend fun rejectFriend(context: Context, requesterId: Int, requestieId: Int)
    : ApiResponse<SimpleMessagePayload> {
        return ApiClient.rejectFriend(context, requesterId, requestieId)
    }

    //5. 요청 리스트 반환
    suspend fun getPendingRequests(context: Context, uid: Int, type: String)
    : ApiResponse<FriendListResponsePayload> {
        return ApiClient.getPendingRequests(context, uid, type)
    }

    //6. 친구 삭제
    suspend fun deleteFriend(context: Context, requesterID: Int, requestieID: Int)
    : ApiResponse<SimpleMessagePayload> {
        return ApiClient.deleteFriend(context, requesterID, requestieID)
    }

    //7. 유저 검색
    suspend fun searchUserByUid(context: Context, uid: Int)
    : ApiResponse<SearchUserResponsePayload> {
        return ApiClient.searchUserByUid(context, uid)
    }


}