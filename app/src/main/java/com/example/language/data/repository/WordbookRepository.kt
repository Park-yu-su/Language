package com.example.language.data.repository

import android.content.Context
import com.example.language.api.ApiClient
import com.example.language.api.ApiResponse
import com.example.language.api.DictionaryResponsePayload
import com.example.language.api.SimpleMessagePayload
import com.example.language.api.TagUpdateRequestPayload
import com.example.language.api.WordbookDeleteResponsePayload
import com.example.language.api.WordbookRegisterRequestPayload
import com.example.language.api.WordbookRegisterResponsePayload
import com.example.language.api.WordbookUpdateRequestPayload
import com.example.language.api.WordbookUpdateResponsePayload
import com.example.language.api.login.UserPreference

class WordbookRepository(
    private val userPreference: UserPreference
) {

    // UID를 가져오는 함수
    fun getUid(): String? {
        return userPreference.getUid()
    }

    /**
     * 4.5.1 단어장 생성 (WordbookRegister)
     * ApiClient의 함수를 호출합니다.
     */
    suspend fun registerWordbook(
        context: Context,
        payload: WordbookRegisterRequestPayload
    ): ApiResponse<WordbookRegisterResponsePayload> {

        return ApiClient.registerWordbook(context, payload)
    }

    /** 4.5.2 단어장 수정 **/
    suspend fun updateWordbook(
        context: Context,
        payload: WordbookUpdateRequestPayload
    ): ApiResponse<WordbookUpdateResponsePayload> {

        return ApiClient.updateWordbook(context, payload)
    }

    /** 4.5.3 단어장 삭제 **/
    suspend fun deleteWordbook(
        context: Context,
        wid: String,
        ownerUid: String
    ): ApiResponse<WordbookDeleteResponsePayload> {

        return ApiClient.deleteWordbook(context, wid, ownerUid)
    }

    /** 4.6. 태그 관리 **/
    suspend fun updateTag(
        context: Context,
        payload: TagUpdateRequestPayload
    ): ApiResponse<SimpleMessagePayload> {

        return ApiClient.updateTag(context, payload)
    }

    /** 4.3. 단어장 사진 분석 (Dictionary) **/
    suspend fun uploadDictionaryImages(
        context: Context,
        fileNames: List<String>,
        fileSizes: List<Long>,
        combinedFileBytes: ByteArray
    ): ApiResponse<DictionaryResponsePayload> {

        return ApiClient.uploadImagesForDictionary(context, fileNames, fileSizes, combinedFileBytes)
    }
}