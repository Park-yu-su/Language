package com.example.language.data.remote.model

import kotlinx.serialization.Serializable

// 인증 (Authentication)
@Serializable
data class AuthRequestPayload(
    val email: String,
    val nickname: String,
    val image: String,
    val oneline: String
)

@Serializable
data class AuthResponsePayload(
    val uid: String,
    val nickname: String,
    val email: String,
    val image: String,
    val oneline: String
)

// 유저 검색 (SearchUserByUid)
@Serializable
data class SearchUserResponsePayload(
    val uid: Int,
    val nickname: String,
    val image: String
)