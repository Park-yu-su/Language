package com.example.language.domain.model

// 로그인한 '내' 정보
data class User(
    val uid: String,
    val nickname: String,
    val email: String,
    val profileImage: String,
    val introduce: String
)

// 친구 목록에 뜨는 '친구' 정보
data class Friend(
    val uid: String,
    val nickname: String,
    val profileImage: String,
    val introduce: String,
    val isRequestSent: Boolean = false // 친구 요청 보낸 상태인지
)