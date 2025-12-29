package com.example.language.data.repository

import com.example.language.data.remote.SocketClient
import com.example.language.data.remote.model.*
import com.example.language.domain.model.AiSession
import com.example.language.domain.repository.AiRepository
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor(
    private val socketClient: SocketClient
) : AiRepository {

    override suspend fun startSession(uid: String, name: String): SocketResult<AiSession> {
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("ERROR", "UID error")
        val payload = SessionStartRequestPayload(uidInt, name)
        val request = ClientRequest("SessionStart", payload)

        val result = socketClient.executeRequest<SessionStartRequestPayload, SessionStartResponsePayload>(request)
        return result.map { AiSession(it.sessionId, name) }
    }

    override suspend fun chatInput(uid: String, sessionId: String, message: String): SocketResult<String> {
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("ERROR", "UID error")
        val payload = ChatInputRequestPayload(uidInt, sessionId, message)
        val request = ClientRequest("ChatInput", payload)

        val result = socketClient.executeRequest<ChatInputRequestPayload, AIResponseDataPayload>(request)
        return result.map { it.response }
    }

    override suspend fun businessTalk(uid: String, sessionId: String, text: String): SocketResult<String> {
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("ERROR", "UID error")
        val payload = BusinessTalkReqeustPayload(uidInt, sessionId, text)
        val request = ClientRequest("BusinessTalk", payload)

        val result = socketClient.executeRequest<BusinessTalkReqeustPayload, BusinessTalkResponsePayload>(request)

        // TODO 피드백과 응답을 합쳐서 리턴하거나, 도메인 모델(AiFeedback)로 변경해서 리턴
        return result.map { "${it.response.feedback}\n\n${it.response.response}" }
    }

    // --- 학습 보조 ---

    override suspend fun todayReview(uid: String, sessionId: String): SocketResult<String> {
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("ERROR", "UID error")
        val payload = AnalyzeLearningRequestPayload(uidInt, sessionId)
        val request = ClientRequest("TodayReview", payload)

        val result = socketClient.executeRequest<AnalyzeLearningRequestPayload, AIResponseDataPayload>(request)
        return result.map { it.response }
    }

    override suspend fun generateExample(uid: String, sessionId: String): SocketResult<String> {
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("ERROR", "UID error")
        val payload = AnalyzeLearningRequestPayload(uidInt, sessionId)
        val request = ClientRequest("GenerateExample", payload)

        val result = socketClient.executeRequest<AnalyzeLearningRequestPayload, AIResponseDataPayload>(request)
        return result.map { it.response }
    }

    override suspend fun analyzeLearning(uid: String, sessionId: String): SocketResult<String> {
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("ERROR", "UID error")
        val payload = AnalyzeLearningRequestPayload(uidInt, sessionId)
        val request = ClientRequest("AnalyzeLearning", payload)

        val result = socketClient.executeRequest<AnalyzeLearningRequestPayload, AIResponseDataPayload>(request)
        return result.map { it.response }
    }


    // --- 퀴즈 및 STT ---

    override suspend fun quizSubmit(
        uid: String, wordId: Int, wordText: String, question: String,
        userAnswer: String, correctAnswer: String
    ): SocketResult<String> {
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("ERROR", "UID error")
        val payload = QuizSubmitRequestPayload(uidInt, wordId, wordText, question, userAnswer, correctAnswer)
        val request = ClientRequest("QuizSubmit", payload)

        val result = socketClient.executeRequest<QuizSubmitRequestPayload, SimpleMessagePayload>(request)
        return result.map { it.message }
    }

    override suspend fun sendVoiceForSTT(
        fileBytes: ByteArray, fileName: String, answer: String
    ): SocketResult<String> {
        val payload = SttRequestPayload(fileName, fileBytes.size.toLong(), answer)
        val request = ClientRequest("STT", payload)

        // 파일 바이트 배열을 함께 전송
        val result = socketClient.executeRequest<SttRequestPayload, SttResponsePayload>(request, fileBytes)
        return result.map { it.result }
    }
}