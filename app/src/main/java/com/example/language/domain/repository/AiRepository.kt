package com.example.language.domain.repository

import com.example.language.data.remote.model.SocketResult
import com.example.language.domain.model.AiSession

interface AiRepository {
    // --- 채팅 세션 ---

    // 새 대화 세션 시작
    suspend fun startSession(uid: String, name: String): SocketResult<AiSession>

    // 채팅 메시지 전송 및 응답 받기
    suspend fun chatInput(uid: String, sessionId: String, message: String): SocketResult<String>

    // 비즈니스 회화 피드백 요청
    suspend fun businessTalk(uid: String, sessionId: String, text: String): SocketResult<String>


    // --- 학습 보조 ---

    // 오늘 배운 단어 리뷰 (AI)
    suspend fun todayReview(uid: String, sessionId: String): SocketResult<String>

    // 예문 생성 요청
    suspend fun generateExample(uid: String, sessionId: String): SocketResult<String>

    // 학습 분석 레포트
    suspend fun analyzeLearning(uid: String, sessionId: String): SocketResult<String>


    // --- 퀴즈 및 STT ---

    // 퀴즈 답안 제출
    suspend fun quizSubmit(
        uid: String,
        wordId: Int,
        wordText: String,
        question: String,
        userAnswer: String,
        correctAnswer: String
    ): SocketResult<String>

    // 음성 인식 (STT) 요청
    suspend fun sendVoiceForSTT(
        fileBytes: ByteArray,
        fileName: String,
        answer: String
    ): SocketResult<String>
}