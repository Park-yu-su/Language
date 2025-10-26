package com.example.language.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * API 응답을 타입-세이프하게 표현하기 위한 클래스. 성공 또는 에러를 나타냅니다.
 * @param T 성공했을 때의 데이터 타입
 */
sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val code: String, val message: String) : ApiResponse<Nothing>()
}

/**
 * 서버로부터 오는 모든 JSON 응답을 일단 파싱하기 위한 1차적인 데이터 클래스.
 * payload를 유연하게 처리하기 위해 JsonElement 타입을 사용합니다.
 */
@Serializable
data class GenericServerResponse(
    val status: String,
    val code: String,
    val payload: JsonElement // payload를 JsonElement로 받아 어떤 형태의 JSON 객체든 수용
)

/**
 * 서버로 보내는 요청의 최상위 구조
 * @param T payload의 타입
 */
@Serializable
data class ClientRequest<T>(
    val intention: String,
    val payload: T
)

//--- 각 Intention 별 Payload 데이터 클래스 ---

// 4.1. 인증 (Authentication)
@Serializable
data class AuthRequestPayload(
    val email: String,
    val nickname: String,
    val image: String
)

@Serializable
data class AuthResponsePayload(
    val uid: String,
    val nickname: String,
    val email: String,
    val image: String
)

// 4.2.1. 친구 목록 조회 (Friend)
@Serializable
data class FriendListRequestPayload(
    val uid: Int
)

@Serializable
data class FriendListResponsePayload(
    val uids: List<String>,
    val nicknames: List<String>,
    val images: List<String>
)


// 4.2.2. 친구 요청/삭제 (Request/Reject)
@Serializable
data class FriendRequestPayload(
    val requester: Int,
    val requestie: Int
)

// 4.2.3. 대기중인 친구 요청 조회 (PendingRequests)
@Serializable
data class PendingRequestsPayload(
    val uid: Int,
    val type: String // 서버에서 "sent" 또는 "received"를 구분하기 위함
)

// 4.3. 단어장 사진 분석 (Dictionary)
@Serializable
data class DictionaryRequestPayload(
    val cnt: String,
    val file_name: List<String>,
    val file_size: List<Long>
)

@Serializable
data class DictionaryResponsePayload(
    val data: List<WordData>
)

// 4.4. 발음 평가 (STT)
@Serializable
data class SttRequestPayload(
    val file_name: String,
    val file_size: Long,
    val answer: String
)

@Serializable
data class SttResponsePayload(
    val result: String
)

// 4.5.1 단어장 생성 (Wordbook)
@Serializable
data class WordbookRegisterRequestPayload(
    val title: String,
    val tags: List<String>,
    val owner_uid: String,
    val data: List<WordData>
)

@Serializable
data class WordbookRegisterResponsePayload(
    val wid: Int,
    val title: String
)

// 4.5.2 단어장 수정 (WordbookUpdate)
@Serializable
data class WordbookUpdateRequestPayload(
    val wid: String,
    val title: String,
    val tags: List<String>,
    val owner_uid: String,
    val data: List<WordData>
)

@Serializable
data class WordbookUpdateResponsePayload(
    val wid: String,
    val title: String
)


// 4.5.3 단어장 삭제 (WordbookDelete)
@Serializable
data class WordbookDeleteRequestPayload(
    val wid: String,
    val owner_uid: String,
)

@Serializable
data class WordbookDeleteResponsePayload(
    val wid: String
)

// 4.6. 태그 관리 (TagUpdate)
@Serializable
data class TagUpdateRequestPayload(
    val wid: String,
    val tags: List<String>
)

// 공통 사용 모델
@Serializable
data class WordData(
    val word: String,
    val meanings: List<String>,
    val example: String
)

@Serializable
data class SimpleMessagePayload(
    val message: String
)
