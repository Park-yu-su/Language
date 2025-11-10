package com.example.language.viewModel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.language.api.ApiClient
import com.example.language.api.ApiResponse
import com.example.language.api.SimpleMessagePayload
import com.example.language.api.TagUpdateRequestPayload
import com.example.language.api.WordData
import com.example.language.api.WordbookDeleteResponsePayload
import com.example.language.api.WordbookRegisterRequestPayload
import com.example.language.api.WordbookRegisterResponsePayload
import com.example.language.api.WordbookUpdateRequestPayload
import com.example.language.api.WordbookUpdateResponsePayload
import com.example.language.api.login.UserPreference
import com.example.language.api.WordData as ApiWordData
import com.example.language.data.WordData as AppWordData
import com.example.language.data.repository.WordbookRepository
import kotlinx.coroutines.launch

class VocViewModel(
    private val repository: WordbookRepository
) : ViewModel() {

    // --- UI State (LiveData) ---
    val title = MutableLiveData<String>()
    val tags = MutableLiveData<String>()

    // 최종 단어 목록
    private val _wordList = MutableLiveData<List<AppWordData>>()
    val wordList: LiveData<List<AppWordData>> = _wordList

    // --- API State ---
    private val _registerStatus = MutableLiveData<ApiResponse<WordbookRegisterResponsePayload>>()
    val registerStatus: LiveData<ApiResponse<WordbookRegisterResponsePayload>> = _registerStatus

    private val _updateStatus = MutableLiveData<ApiResponse<WordbookUpdateResponsePayload>>()
    val updateStatus: LiveData<ApiResponse<WordbookUpdateResponsePayload>> = _updateStatus

    private val _deleteStatus = MutableLiveData<ApiResponse<WordbookDeleteResponsePayload>>()
    val deleteStatus: LiveData<ApiResponse<WordbookDeleteResponsePayload>> = _deleteStatus

    private val _tagUpdateStatus = MutableLiveData<ApiResponse<SimpleMessagePayload>>()
    val tagUpdateStatus: LiveData<ApiResponse<SimpleMessagePayload>> = _tagUpdateStatus

    // --- User Data ---
    private val _ownerUid = MutableLiveData<String?>()
    val ownerUid: LiveData<String?> = _ownerUid

    init {
        // 3. ViewModel이 생성되자마자 UID 로드
        loadUid()
    }

    private fun loadUid() {
        // Repository에서 UID를 가져와 LiveData에 설정
        val uid = repository.getUid()
        if (uid != null) {
            _ownerUid.value = uid
        } else {
            // TODO: UID가 없는 경우 (로그아웃 상태) 처리
        }
    }

    // --- Initializers ---
    fun setInitialData(initialTitle: String, initialWords: List<AppWordData>) {
        if (title.value == null) {
            title.value = initialTitle
        }
        if (_wordList.value == null) {
            _wordList.value = initialWords
        }
    }

    // --- Word List Manipulation ---
    fun updateWord(originalWord: AppWordData, updatedWord: AppWordData) {
        val currentList = _wordList.value?.toMutableList() ?: return
        val index = currentList.indexOf(originalWord)
        if (index != -1) {
            currentList[index] = updatedWord
            _wordList.value = currentList
        }
    }

    // --- API Call ---
    /**
     * 단어장 등록 API 호출 (Fragment에서 Context를 전달받음)
     */
    fun registerWordbook(
        context: Context,
        title: String,
        tags: List<String>,
        owner_uid: String,
        data: List<WordData>
    ) {
        val payload = WordbookRegisterRequestPayload(title, tags, owner_uid, data)

        viewModelScope.launch {
            val response = repository.registerWordbook(context, payload)

            _registerStatus.value = response
        }
    }

    /**
     * 4.5.2 단어장 수정
     */
    fun updateWordbook(
        context: Context,
        wid: String,
        title: String,
        tags: List<String>,
        owner_uid: String,
        data: List<ApiWordData>
    ) {
        val payload = WordbookUpdateRequestPayload(wid, title, tags, owner_uid, data)

        viewModelScope.launch {
            val response = repository.updateWordbook(context.applicationContext, payload)
            _updateStatus.value = response
        }
    }

    /**
     * 4.5.3 단어장 삭제
     */
    fun deleteWordbook(context: Context, wid: String, ownerUid: String) {
        viewModelScope.launch {
            // 이 API는 Repository에서 payload를 생성하지 않고, wid와 uid를 직접 받습니다.
            val response = repository.deleteWordbook(context.applicationContext, wid, ownerUid)
            _deleteStatus.value = response
        }
    }

    /**
     * 4.6. 태그 관리
     */
    fun updateTag(context: Context, wid: String, tags: List<String>) {
        val payload = TagUpdateRequestPayload(wid, tags)

        viewModelScope.launch {
            val response = repository.updateTag(context.applicationContext, payload)
            _tagUpdateStatus.value = response
        }
    }

}