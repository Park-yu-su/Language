package com.example.language.data.remote.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// 서버에 보낼 요청 껍데기
@Serializable
data class ClientRequest<T>(
    val type: String, // 요청 타입
    val payload: T    // 실제 데이터
)

// 서버에서 오는 날것의 응답 (JSON 파싱 전)
@Serializable
data class GenericServerResponse(
    val status: String, // "ACCEPT", "REJECT", "ERROR"
    val code: String? = null,
    val payload: JsonElement // 나중에 내용물에 따라 다시 파싱
)

// 앱 내부에서 쓸 응답 결과 (Sealed Class)
sealed class SocketResult<out T> {
    data class Success<out T>(val data: T) : SocketResult<T>()
    data class Error(val code: String?, val message: String) : SocketResult<Nothing>()
}

// 에러 메시지용 페이로드
@Serializable
data class SimpleMessagePayload(
    val message: String
)

/**
 * SocketResult 데이터를 변환해주는 공용 확장 함수
 * 예: SocketResult<Payload> -> SocketResult<DomainModel>
 */
fun <T, R> SocketResult<T>.map(transform: (T) -> R): SocketResult<R> {
    return when (this) {
        is SocketResult.Success -> SocketResult.Success(transform(data))
        is SocketResult.Error -> SocketResult.Error(code, message)
    }
}