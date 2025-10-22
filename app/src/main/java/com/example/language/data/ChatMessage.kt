package com.example.language.data

data class ChatMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean, //true: 사용자(우측), false: 챗봇(좌측)
    val timestamp: String // 날짜/시간
)