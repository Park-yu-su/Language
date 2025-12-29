package com.example.language.domain.repository

import com.example.language.data.remote.model.SocketResult
import com.example.language.domain.model.Friend

interface FriendRepository {
    // 친구 목록 조회
    suspend fun getFriendList(uid: String): SocketResult<List<Friend>>

    // 친구 요청 보내기
    suspend fun addFriend(requesterId: String, requesteeId: String): SocketResult<String>

    // 친구 요청 수락
    suspend fun acceptFriend(requesterId: String, requesteeId: String): SocketResult<String>

    // 친구 요청 거절
    suspend fun rejectFriend(requesterId: String, requesteeId: String): SocketResult<String>

    // 친구 삭제
    suspend fun deleteFriend(requesterId: String, requesteeId: String): SocketResult<String>

    // 대기 중인 요청 조회 (type: "sent" | "received")
    suspend fun getPendingRequests(uid: String, type: String): SocketResult<List<Friend>>
}