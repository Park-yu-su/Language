package com.example.language.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- 공통 데이터 모델 ---
@Serializable
data class WordData(
    val word: String,
    val meanings: List<String>,
    val distractors: List<String>,
    val example: String
)

@Serializable
data class WordDataWithWordID(
    @SerialName("word_id") val wordId: Int,
    val word: String,
    val meanings: List<String>,
    val distractors: List<String>,
    val example: String
)

@Serializable
data class TagData(
    val tid: Int,
    val name: String,
    val reference_count: Int
)

@Serializable
data class WordbookMeta(
    val wid: Int,
    val title: String,
    val tags: List<String>
)

@Serializable
data class WordbookSearchResultData(
    val wid: Int,
    val title: String,
    val tags: List<String>,
    val subscription_count: Int
)

@Serializable
data class SubscribedWordbooksData(
    val wid: Int,
    val title: String,
    val tags: List<String>,
)

// --- Payloads ---

// 단어장 사진 분석 (Dictionary)
@Serializable
data class DictionaryRequestPayload(
    val cnt: String,
    val file_name: List<String>,
    val file_size: List<Long>
)

@Serializable
data class DictionaryTextRequestPayload(
    val data: List<WordData>
)

@Serializable
data class DictionaryResponsePayload(
    val data: List<WordData>
)

// 단어장 생성 (Wordbook)
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

// 단어장 수정 (WordbookUpdate)
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

// 단어장 삭제 (WordbookDelete)
@Serializable
data class WordbookDeleteRequestPayload(
    val wid: String,
    val owner_uid: String,
)

@Serializable
data class WordbookDeleteResponsePayload(
    val wid: String
)

// 단어장 조회 (GetWordbook)
@Serializable
data class GetWordbookRequestPayload(
    val wid: Int
)

@Serializable
data class GetWordbookResponsePayload(
    val data: List<WordDataWithWordID>
)

// 태그 관리 (TagUpdate)
@Serializable
data class TagUpdateRequestPayload(
    val wid: String,
    val tags: List<String>
)

// 태그 검색 (SearchTag)
@Serializable
data class SearchTagRequestPayload(
    val query: String
)
@Serializable
data class SearchTagResponsePayload(
    val data: List<TagData>
)

// 단어장 검색 (SearchWordbook)
@Serializable
data class SearchWordbookRequestPayload(
    val tids: List<Int>
)

@Serializable
data class SearchWordbookResponsePayload(
    val data: List<WordbookSearchResultData>
)

// 단어장 구독 (Subscribe)
@Serializable
data class SubscribeRequestPayload(
    val wid: Int,
    val subscriber: Int
)

// 구독중인 단어장 리스트 (GetSubscribedWordbooks)
@Serializable
data class GetSubscribedWordbooksResponsePayload(
    val data: List<SubscribedWordbooksData>
)

// 유저별 단어 상태 저장 (LinkUserWord)
@Serializable
data class LinkUserWordRequestPayload(
    val uid: Int,
    val word_ids: List<Int>,
    val status: String // liked | wrong | review
)

// 유저의 상태별 단어 불러오기 (GetUserWordStatus)
@Serializable
data class GetLinkedWordOfUserRequestPayload(
    val uid: Int,
    val status: String
)
@Serializable
data class GetLinkedWordOfUserResponsePayload(
    val data: List<WordDataWithWordID>
)

// 단어장 ID로 단어장 metadata 불러오기 (getWordbookInfoWithID)
@Serializable
data class GetWordbookInfoWithIDResponsePayload(
    val data: WordbookMeta
)