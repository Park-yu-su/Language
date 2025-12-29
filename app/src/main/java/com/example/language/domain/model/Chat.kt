package com.example.language.domain.model

// 채팅 메시지
data class ChatMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean, // true: 나, false: 상대방(AI)
    val timestamp: String // 날짜/시간
)

// AI 세션 정보
data class AiSession(
    val sessionId: String,
    val title: String = ""
)