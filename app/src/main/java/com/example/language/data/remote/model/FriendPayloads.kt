package com.example.language.data.remote.model

import kotlinx.serialization.Serializable

// 친구 목록 조회 (Friend)
@Serializable
data class FriendListRequestPayload(
    val uid: Int
)

@Serializable
data class FriendListResponsePayload(
    val uids: List<String>,
    val nicknames: List<String>,
    val images: List<String>,
    val onelines: List<String>
)

// 친구 요청/삭제/수락/거절 (Request/Reject/Accept/Delete)
@Serializable
data class FriendRequestPayload(
    val requester: Int,
    val requestie: Int
)

// 대기중인 친구 요청 조회 (PendingRequests)
@Serializable
data class PendingRequestsPayload(
    val uid: Int,
    val type: String // "sent" or "received"
)