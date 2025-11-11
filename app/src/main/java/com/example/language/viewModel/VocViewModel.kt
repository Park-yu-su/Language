package com.example.language.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.language.api.ApiResponse
import com.example.language.api.DictionaryResponsePayload
import com.example.language.api.SimpleMessagePayload
import com.example.language.api.TagUpdateRequestPayload
import com.example.language.api.WordData
import com.example.language.api.WordbookDeleteResponsePayload
import com.example.language.api.WordbookRegisterRequestPayload
import com.example.language.api.WordbookRegisterResponsePayload
import com.example.language.api.WordbookUpdateRequestPayload
import com.example.language.api.WordbookUpdateResponsePayload
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

    private val _currentVocId = MutableLiveData<String?>()
    val currentVocId: LiveData<String?> = _currentVocId

    // --- API State ---
    private val _registerStatus = MutableLiveData<ApiResponse<WordbookRegisterResponsePayload>?>()
    val registerStatus: LiveData<ApiResponse<WordbookRegisterResponsePayload>?> = _registerStatus

    private val _updateStatus = MutableLiveData<ApiResponse<WordbookUpdateResponsePayload>?>()
    val updateStatus: LiveData<ApiResponse<WordbookUpdateResponsePayload>?> = _updateStatus

    private val _deleteStatus = MutableLiveData<ApiResponse<WordbookDeleteResponsePayload>?>()
    val deleteStatus: LiveData<ApiResponse<WordbookDeleteResponsePayload>?> = _deleteStatus

    private val _tagUpdateStatus = MutableLiveData<ApiResponse<SimpleMessagePayload>?>()
    val tagUpdateStatus: LiveData<ApiResponse<SimpleMessagePayload>?> = _tagUpdateStatus

    private val _analysisStatus = MutableLiveData<ApiResponse<DictionaryResponsePayload>?>()
    val analysisStatus: LiveData<ApiResponse<DictionaryResponsePayload>?> = _analysisStatus

    // API 호출 로딩 상태
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

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

    // --- 4. 단어장 데이터 로드/설정 함수 ---

    /**
     * AddVocInExitFragment에서 호출.
     * vocId를 사용해 Repository에서 단어장의 *모든 정보* (이름, 태그, 단어 목록)를
     * 불러와 ViewModel 상태를 초기화합니다.
     *
     * (가정: repository.getWordbookDetails(vocId)가
     * WordbookDetails(title, tags, words) 같은 객체를 반환)
     */
    fun loadVocabookDetails(context: Context, vocId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // (이 부분은 Repository 구현에 따라 달라집니다)
            // 1. Repository에서 단어장 상세 정보 가져오기
            // val response = repository.getWordbookDetails(context, vocId)

            // (임시 하드코딩 예시)
            // -------------------------------------------------------------------
            val tempWords = listOf(
                AppWordData("apple", mutableListOf("사과"), "ex1"),
                AppWordData("banana", mutableListOf("바나나"), "ex2")
            )
            val response = object { // (실제로는 API 응답 객체여야 함)
                val title = "임시 단어장 제목"
                val tags = listOf("태그1", "태그2")
                val words = tempWords
            }
            // -------------------------------------------------------------------

            // 2. ViewModel 상태 업데이트
            if (response != null) { // (실제로는 response is ApiResponse.Success)
                _currentVocId.value = vocId
                title.value = response.title
                tags.value = response.tags.joinToString(", ")
                _wordList.value = response.words
            } else {
                // (에러 처리)
            }
            _isLoading.value = false
        }
    }

    /**
     * AddVocFinalCheckFragment에서 NavArgs로 받은 데이터를 동기화할 때 사용.
     * (ViewModel 중심 흐름에서는 이 함수의 필요성이 낮아질 수 있습니다.)
     */
    fun setInitialData(initialTitle: String, initialWords: List<AppWordData>) {
        if (title.value == null) {
            title.value = initialTitle
        }
        if (_wordList.value == null) {
            _wordList.value = initialWords
        }
    }

    /**
     * AddVocInExitFragment의 수정 다이얼로그에서 호출.
     * 로컬에서 수정된 단어 목록(MutableList)을 받아
     * ViewModel의 LiveData를 갱신(동기화)합니다.
     */
    fun updateWordList(newList: List<AppWordData>) {
        _wordList.value = newList
    }

    // --- 5. 단어 추가 함수 (SelectWayAddVocFragment용) ---

    /**
     * [사진 분석] API를 호출하고, 성공 시 단어 목록을 _wordList에 추가합니다.
     */

    /**
     * [사진 분석] API를 호출하고, 성공 시 단어 목록을 _wordList에 추가합니다.
     */
    fun uploadDictionaryImages(
        context: Context,
        fileNames: List<String>,
        fileSizes: List<Long>,
        combinedFileBytes: ByteArray
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = repository.uploadDictionaryImages(context, fileNames, fileSizes, combinedFileBytes)
            _analysisStatus.value = response

            if (response is ApiResponse.Success) {
                val newApiWords = response.data.data
                val newAppWords = newApiWords.map { mapWordDataToAppWordData(it) }

                val currentList = _wordList.value ?: emptyList()
                _wordList.value = currentList + newAppWords
            }
            _isLoading.value = false
        }
    }

    /**
     * [수동 추가] 다이얼로그에서 입력받은 단어를 _wordList에 추가합니다.
     */
    fun addManualWord(word: AppWordData) {
        val currentList = _wordList.value ?: emptyList()
        _wordList.value = currentList + word
    }

    // --- 6. 단어장 CUD API 호출 함수 ---

    /**
     * [생성] 새 단어장을 서버에 등록합니다.
     * (참고: 현재 흐름은 '수정' 위주이지만, '새 단어장 만들기' 흐름에서 사용)
     */
    fun registerNewWordbook(context: Context, title: String, tags: List<String>, data: List<ApiWordData>) {
        viewModelScope.launch {
            _isLoading.value = true
            val ownerUid = _ownerUid.value
            if (ownerUid.isNullOrEmpty()) {
                _registerStatus.value = ApiResponse.Error("VIEWMODEL_ERROR", "UID가 없습니다.")
                _isLoading.value = false
                return@launch
            }

            val payload = WordbookRegisterRequestPayload(
                title = title,
                tags = tags,
                owner_uid = ownerUid,
                data = data
            )
            _registerStatus.value = repository.registerWordbook(context.applicationContext, payload)
            _isLoading.value = false
        }
    }

    /**
     * [수정] AddVocFinalCheckFragment에서 '저장' 버튼 클릭 시 호출됩니다.
     * ViewModel이 현재 상태로 (ID, 제목, 태그, 전체 단어 목록) '수정' API를 호출합니다.
     */
    fun updateCurrentWordbook(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. ViewModel의 현재 상태에서 모든 데이터 가져오기
            val wid = _currentVocId.value
            val title = title.value
            val ownerUid = _ownerUid.value
            val tagsList = tags.value.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val words = _wordList.value ?: emptyList()

            // 2. 유효성 검사
            if (wid.isNullOrEmpty() || title.isNullOrEmpty() || ownerUid.isNullOrEmpty()) {
                _updateStatus.value = ApiResponse.Error("VIEWMODEL_ERROR", "단어장 ID, 제목, 또는 사용자 UID가 없습니다.")
                _isLoading.value = false
                return@launch
            }

            // 3. 데이터 변환 (AppWordData -> ApiWordData)
            val finalApiData = words.map { appWord ->
                ApiWordData(
                    word = appWord.word,
                    meanings = appWord.meanings.toList(), // MutableList -> List
                    example = appWord.example
                )
            }

            // 4. Payload 생성
            val payload = WordbookUpdateRequestPayload(
                wid = wid,
                title = title,
                tags = tagsList,
                owner_uid = ownerUid,
                data = finalApiData
            )

            // 5. API 호출
            _updateStatus.value = repository.updateWordbook(context.applicationContext, payload)
            _isLoading.value = false
        }
    }

    /**
     * [삭제] 현재 단어장을 삭제합니다.
     */
    fun deleteCurrentWordbook(context: Context) {
        viewModelScope.launch {
            val wid = _currentVocId.value
            val ownerUid = _ownerUid.value

            if (wid.isNullOrEmpty() || ownerUid.isNullOrEmpty()) {
                _deleteStatus.value = ApiResponse.Error("VIEWMODEL_ERROR", "ID 또는 UID가 없습니다.")
                return@launch
            }

            _isLoading.value = true
            _deleteStatus.value = repository.deleteWordbook(context.applicationContext, wid, ownerUid)
            _isLoading.value = false
        }
    }

    /**
     * AddNewVocFragment에서 "새 단어장 만들기"를 시작할 때 호출됩니다.
     * ViewModel의 상태를 '새 단어장'에 맞게 초기화합니다.
     */
    fun setupForNewVocabook(newTitle: String, newTags: List<String>) {
        _currentVocId.value = null // [중요] 새 단어장이므로 ID는 null
        title.value = newTitle
        tags.value = newTags.joinToString(", ")
        _wordList.value = emptyList() // [중요] 단어 목록은 비어있음
    }

    // --- 7. 유틸리티 및 리셋 함수 ---

    /**
     * API 응답(WordData)을 UI 모델(AppWordData)로 변환합니다. (List -> MutableList)
     */
    private fun mapWordDataToAppWordData(apiWord: WordData): AppWordData {
        return AppWordData(
            word = apiWord.word,
            meanings = apiWord.meanings.toMutableList(), // .toMutableList()로 변환
            example = apiWord.example
        )
    }

    /**
     * UI(Fragment)에서 API 응답 이벤트를 소비한 후, LiveData를 null로 리셋합니다.
     * (화면 회전 시 이벤트가 중복 실행되는 것을 방지)
     */
    fun resetRegisterStatus() { _registerStatus.value = null }
    fun resetUpdateStatus() { _updateStatus.value = null }
    fun resetDeleteStatus() { _deleteStatus.value = null }
    fun resetTagUpdateStatus() { _tagUpdateStatus.value = null }
    fun resetAnalysisStatus() { _analysisStatus.value = null }

}