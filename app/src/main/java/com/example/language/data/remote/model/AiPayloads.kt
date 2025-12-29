package com.example.language.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- 공통 데이터 ---
@Serializable
data class AIResponseDataPayload(
    @SerialName("session_id")
    val sessionId: String,
    val response: String
)

@Serializable
data class AIFeedbackData(
    val feedback: String,
    val response: String
)

// --- Payloads ---

// 발음 평가 (STT) - Request
@Serializable
data class SttRequestPayload(
    val file_name: String,
    val file_size: Long,
    val answer: String
)

// STT - Response & SendBack
@Serializable
data class SttResponsePayload(
    val result: String
)

@Serializable
data class SendBackRequestPayload(
    val file_name: String,
    val file_size: Long
)

// 신규 세션 생성
@Serializable
data class SessionStartRequestPayload(
    val uid: Int,
    val name: String
)
@Serializable
data class SessionStartResponsePayload(
    @SerialName("session_id")
    val sessionId: String,
)

// 챗봇 인풋
@Serializable
data class ChatInputRequestPayload(
    val uid: Int,
    @SerialName("session_id")
    val sessionId: String,
    val message: String
)

// 퀴즈 제출
@Serializable
data class QuizSubmitRequestPayload(
    val uid: Int,
    @SerialName("word_id")
    val wordId: Int,
    @SerialName("word_text")
    val wordText: String,
    val question: String,
    @SerialName("user_answer")
    val userAnswer: String,
    @SerialName("correct_answer")
    val correctAnswer: String
)

// 학습 분석
@Serializable
data class AnalyzeLearningRequestPayload(
    val uid: Int,
    @SerialName("session_id")
    val sessionID: String
)

// 비즈니스 토킹
@Serializable
data class BusinessTalkReqeustPayload(
    val uid: Int,
    @SerialName("session_id")
    val sessionID: String,
    val text: String
)

@Serializable
data class BusinessTalkResponsePayload(
    @SerialName("session_id")
    val sessionID: String,
    val response: AIFeedbackData
)