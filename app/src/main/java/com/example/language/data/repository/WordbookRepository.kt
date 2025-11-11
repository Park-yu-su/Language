package com.example.language.data.repository

import android.content.Context
import com.example.language.api.ApiClient
import com.example.language.api.ApiResponse
import com.example.language.api.DictionaryResponsePayload
import com.example.language.api.GetLinkedWordOfUserResponsePayload
import com.example.language.api.GetSubscribedWordbooksResponsePayload
import com.example.language.api.GetWordbookResponsePayload
import com.example.language.api.SearchTagResponsePayload
import com.example.language.api.SearchWordbookResponsePayload
import com.example.language.api.SimpleMessagePayload
import com.example.language.api.WordbookDeleteResponsePayload
import com.example.language.api.WordbookRegisterRequestPayload
import com.example.language.api.WordbookRegisterResponsePayload
import com.example.language.api.WordbookUpdateRequestPayload
import com.example.language.api.WordbookUpdateResponsePayload
import com.example.language.api.login.UserPreference

class WordbookRepository(
    private val userPreference: UserPreference
) {

    // --- 1. 사용자 정보 ---

    /**
     * SharedPreferences에서 저장된 UID (String)를 가져옵니다.
     */
    fun getUid(): String? {
        return userPreference.getUid()
    }

    /**
     * UID (String)를 Int로 변환합니다. (ApiClient 호출용)
     * 실패 시 null을 반환합니다.
     */
    private fun getUidAsInt(): Int? {
        return getUid()?.toIntOrNull()
    }

    // --- 2. 단어장 CUD 및 사진 업로드 ---

    /**
     * 새 단어장 등록
     */
    suspend fun registerWordbook(context: Context, payload: WordbookRegisterRequestPayload): ApiResponse<WordbookRegisterResponsePayload> {
        return ApiClient.registerWordbook(context, payload)
    }

    /**
     * 기존 단어장 수정
     */
    suspend fun updateWordbook(context: Context, payload: WordbookUpdateRequestPayload): ApiResponse<WordbookUpdateResponsePayload> {
        return ApiClient.updateWordbook(context, payload)
    }

    /**
     * 단어장 삭제
     * ViewModel의 wid(String)와 uid(String)를 Int로 변환합니다.
     */
    suspend fun deleteWordbook(context: Context, wid: String, ownerUid: String): ApiResponse<WordbookDeleteResponsePayload> {
        return ApiClient.deleteWordbook(context, wid, ownerUid)
    }

    /**
     * 사진 분석 업로드
     */
    suspend fun uploadDictionaryImages(
        context: Context,
        fileNames: List<String>,
        fileSizes: List<Long>,
        combinedFileBytes: ByteArray
    ): ApiResponse<DictionaryResponsePayload> {
        return ApiClient.uploadImagesForDictionary(context, fileNames, fileSizes, combinedFileBytes)
    }

    // --- 3. (신규) 단어장 Get 및 구독 관리 ---

    /**
     * 단어장 ID(wid)로 단어장의 단어 목록을 가져옵니다.
     * ViewModel의 wid(String)를 Int로 변환합니다.
     */
    suspend fun getWordbook(context: Context, wid: String): ApiResponse<GetWordbookResponsePayload> {
        val widInt = wid.toIntOrNull() ?: return ApiResponse.Error("REPO_ERROR", "Invalid WID format")
        return ApiClient.getWordbook(context, widInt)
    }

    /**
     * 구독된(내) 단어장 목록을 가져옵니다.
     * 내부적으로 UID(String)를 Int로 변환합니다.
     */
    suspend fun getSubscribedWordbooks(context: Context): ApiResponse<GetSubscribedWordbooksResponsePayload> {
        val uidInt = getUidAsInt() ?: return ApiResponse.Error("REPO_ERROR", "Invalid UID")
        return ApiClient.getSubscribedWordbooks(context, uidInt)
    }

    /**
     * 단어장을 구독(내 단어장에 추가)합니다.
     */
    suspend fun subscribe(context: Context, wid: String): ApiResponse<SimpleMessagePayload> {
        val widInt = wid.toIntOrNull() ?: return ApiResponse.Error("REPO_ERROR", "Invalid WID")
        val uidInt = getUidAsInt() ?: return ApiResponse.Error("REPO_ERROR", "Invalid UID")
        return ApiClient.subscribe(context, widInt, uidInt)
    }

    /**
     * 단어장 구독을 취소(내 단어장에서 삭제)합니다.
     */
    suspend fun cancelSubscription(context: Context, wid: String): ApiResponse<SimpleMessagePayload> {
        val widInt = wid.toIntOrNull() ?: return ApiResponse.Error("REPO_ERROR", "Invalid WID")
        val uidInt = getUidAsInt() ?: return ApiResponse.Error("REPO_ERROR", "Invalid UID")
        return ApiClient.cancelSubscription(context, widInt, uidInt)
    }

    // --- 4. (신규) 태그 관련 ---

    /**
     * 태그 이름(query)으로 태그 ID를 검색합니다.
     */
    suspend fun searchTag(context: Context, query: String): ApiResponse<SearchTagResponsePayload> {
        return ApiClient.searchTag(context, query)
    }

    /**
     * 태그 ID 목록으로 단어장을 검색합니다.
     */
    suspend fun searchWordbook(context: Context, tids: List<Int>): ApiResponse<SearchWordbookResponsePayload> {
        return ApiClient.searchWordbook(context, tids)
    }

    // --- 5. (신규) 단어-사용자 연결 (좋아요, 틀림, 리뷰) ---

    /**
     * 특정 단어들(word_ids)을 특정 상태(status)로 연결(링크)합니다.
     */
    suspend fun linkWordUser(context: Context, word_ids: List<Int>, status: String): ApiResponse<SimpleMessagePayload> {
        val uidInt = getUidAsInt() ?: return ApiResponse.Error("REPO_ERROR", "Invalid UID")
        return ApiClient.linkWordUser(context, uidInt, word_ids, status)
    }

    /**
     * 특정 단어들(word_ids)의 상태 연결(링크)을 해제합니다.
     */
    suspend fun unlinkWordUser(context: Context, word_ids: List<Int>, status: String): ApiResponse<SimpleMessagePayload> {
        val uidInt = getUidAsInt() ?: return ApiResponse.Error("REPO_ERROR", "Invalid UID")
        return ApiClient.unlinkWordUser(context, uidInt, word_ids, status)
    }

    /**
     * 특정 상태(status)로 연결된 단어 목록을 가져옵니다. (좋아요한 단어 목록 등)
     */
    suspend fun getLinkedWordOfUser(context: Context, status: String): ApiResponse<GetLinkedWordOfUserResponsePayload> {
        val uidInt = getUidAsInt() ?: return ApiResponse.Error("REPO_ERROR", "Invalid UID")
        return ApiClient.getLinkedWordOfUser(context, uidInt, status)
    }
}