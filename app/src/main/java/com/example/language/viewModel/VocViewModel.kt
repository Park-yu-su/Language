package com.example.language.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.language.api.ApiResponse
import com.example.language.api.DictionaryResponsePayload
import com.example.language.api.SimpleMessagePayload
import com.example.language.api.SubscribedWordbooksData
import com.example.language.api.WordData as ApiRegisterWordData
import com.example.language.api.WordbookDeleteResponsePayload
import com.example.language.api.WordbookRegisterRequestPayload
import com.example.language.api.WordbookRegisterResponsePayload
import com.example.language.api.WordbookUpdateRequestPayload
import com.example.language.api.WordbookUpdateResponsePayload
import com.example.language.api.WordDataWithWordID
import com.example.language.data.VocData
import com.example.language.data.repository.WordbookRepository
import com.example.language.data.repository.toVocDataList
import kotlinx.coroutines.launch

// UI용 데이터 클래스
data class AppWordData(
    var wordId : Int,
    var word : String,
    val meanings: MutableList<String>,
    val distractors: List<String>,
    val example: String
)

class VocViewModel(
    private val repository: WordbookRepository
) : ViewModel() {

    // --- User Data ---
    private val _ownerUid = MutableLiveData<String?>()
    val ownerUid: LiveData<String?> = _ownerUid

    // --- UI State (LiveData) ---
    private val _title = MutableLiveData<String>()
    val title = _title

    private val _tags = MutableLiveData<List<String>>()
    val tags: LiveData<List<String>> = _tags

    // 최종 단어 목록
    private val _wordList = MutableLiveData<List<AppWordData>>()
    val wordList: LiveData<List<AppWordData>> = _wordList

    private val _currentVocId = MutableLiveData<String?>()
    val currentVocId: LiveData<String?> = _currentVocId

    // 단어장 목록
    private val _vocaBookList = MutableLiveData<List<SubscribedWordbooksData>>()
    val vocaBookList: LiveData<List<SubscribedWordbooksData>> = _vocaBookList

    private val _transformedVocList = MutableLiveData<List<VocData>>()
    val transformedVocList: LiveData<List<VocData>> = _transformedVocList

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

    private val _linkWordStatus = MutableLiveData<ApiResponse<SimpleMessagePayload>?>()
    val linkWordStatus: LiveData<ApiResponse<SimpleMessagePayload>?> = _linkWordStatus

    // API 호출 로딩 상태
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

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

    fun setVocTitle(title: String) {
        _title.value = title
    }

    fun setVocId(vocId: String) {
        _currentVocId.value = vocId
    }

    fun setTags(newTags: List<String>) {
        _tags.value = newTags
    }

    // --- 4. 단어장 데이터 로드/설정 함수 ---

    fun getSubscribedWordbooks(context: Context) {
        viewModelScope.launch {
            val response = repository.getSubscribedWordbooks(context)
            if (response is ApiResponse.Success) {
                val dataList = response.data.data

                _vocaBookList.value = dataList
                _transformedVocList.value = dataList.toVocDataList(_ownerUid.value.toString())
            } else {
                _vocaBookList.value = emptyList()
                _transformedVocList.value = emptyList()
            }
        }
    }

    /**
     * [MakeVocFragment에서 호출]
     * 단어장의 기본 정보(ID, 이름, 태그)를 ViewModel에 설정합니다.
     * 이어서 loadVocabookDetails를 호출하여 단어 목록을 불러옵니다.
     */
//    fun loadVocabookForEditing(context: Context, vocData: VocData) {
//        _currentVocId.value = vocData.wid.toString()
//        _title.value = vocData.title
//        tags.value = vocData.tags.joinToString(", ")
//        _wordList.value = emptyList() // (일단 비우고, 로드 시작)
//
//        // 단어 목록(words) 로드
//        loadVocabookDetails(context, vocData.wid.toString())
//    }

    /**
     * [새 단어장 흐름 시작]
     * AddNewVocFragment에서 호출.
     */
    fun setupForNewVocabook(newTitle: String, newTags: List<String>) {
        _currentVocId.value = null // 새 단어장이므로 ID 없음
        _title.value = newTitle
        _tags.value = newTags
        _wordList.value = emptyList() // 단어 목록 비어있음
    }

    /**
     * [✨ 7. (수정) 단어 목록만 불러오기]
     * Repository에서 실제 단어장 단어 목록을 불러옵니다.
     */
    fun loadVocabookDetails(context: Context, vocId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // Repository는 String -> Int 변환을 담당
            val response = repository.getWordbook(context.applicationContext, vocId)

            if (response is ApiResponse.Success) {
                // API 응답: List<WordDataWithWordID>
                val apiWordList = response.data.data
                // [!] API 모델 -> UI 모델로 변환
                _wordList.value = apiWordList.map { mapApiWordToAppWord(it) }
            } else if (response is ApiResponse.Error) {
                // (로드 실패 처리)
                _wordList.value = emptyList() // 실패 시 빈 리스트
            }
            _isLoading.value = false
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

    // 단어 목록을 비우는 초기화 함수입니다.
    fun clearWordList() {
        _wordList.value = emptyList()
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

            try {
                val response = repository.uploadDictionaryImages(
                    context,
                    fileNames,
                    fileSizes,
                    combinedFileBytes
                )

                if (response is ApiResponse.Success) {
                    // (response.data.data가 List<WordData> - ID 없는 모델 - 라고 가정)
                    val newApiWords = response.data.data
                    val newAppWords = newApiWords.map { apiWord ->
                        // [!] '등록'용 모델(WordData) -> UI 모델(AppWordData)로 변환
                        AppWordData(
                            wordId = 0, // 새 단어
                            word = apiWord.word,
                            meanings = apiWord.meanings.toMutableList(),
                            distractors = apiWord.distractors,
                            example = apiWord.example
                        )
                    }
                    val currentList = _wordList.value ?: emptyList()
                    _wordList.value = newAppWords + currentList
                }

                _analysisStatus.value = response
                // (실패 시 response가 Error 상태이므로 analysisStatus가 관찰함)
            } catch (e: Exception) {
                // [2. 예외 처리] (네트워크 오류 등)
                _analysisStatus.value = ApiResponse.Error("VM_ERROR", e.message ?: "Upload failed")
            } finally {
                // [3. (중요) 항상 로딩 종료] (화면 터치 방지 해제)
                _isLoading.value = false
            }
        }
    }

    /**
     * [수동 추가] 다이얼로그에서 입력받은 단어를 _wordList에 추가합니다.
     */
    fun addManualWord(word: AppWordData) {
        val currentList = _wordList.value ?: emptyList()
        _wordList.value = listOf(word) + currentList
    }

    // --- 6. 단어장 CUD API 호출 함수 ---

    /**
     * [생성] 새 단어장을 서버에 등록합니다.
     * (참고: 현재 흐름은 '수정' 위주이지만, '새 단어장 만들기' 흐름에서 사용)
     */
    fun registerNewWordbook(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val title = title.value
            val ownerUid = _ownerUid.value
            val tagsList = tags.value.orEmpty().filter { it.isNotBlank() }
            val words = _wordList.value ?: emptyList() // List<AppWordData>

            if (title.isNullOrEmpty() || ownerUid.isNullOrEmpty()) {
                _registerStatus.value = ApiResponse.Error("VIEWMODEL_ERROR", "제목 또는 UID가 없습니다.")
                _isLoading.value = false
                return@launch
            }

            // [!] UI 모델 -> '등록'용 API 모델 (ApiRegisterWordData)로 변환
            val finalApiData = words.map { appWord ->
                ApiRegisterWordData( // (ID가 없는 WordData 모델)
                    word = appWord.word,
                    meanings = appWord.meanings.toList(), // MutableList -> List
                    distractors = appWord.distractors,
                    example = appWord.example
                )
            }

            val payload = WordbookRegisterRequestPayload(
                title = title,
                tags = tagsList,
                owner_uid = ownerUid,
                data = finalApiData
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
            val tagsList = tags.value.orEmpty().filter { it.isNotBlank() }
            val words = _wordList.value ?: emptyList()

            // 2. 유효성 검사
            if (wid.isNullOrEmpty() || title.isNullOrEmpty() || ownerUid.isNullOrEmpty()) {
                _updateStatus.value = ApiResponse.Error("VIEWMODEL_ERROR", "단어장 ID, 제목, 또는 사용자 UID가 없습니다.")
                _isLoading.value = false
                return@launch
            }

            // [ ✨ 3. 데이터 변환 (오류 수정) ✨ ]
            // 'update' 요청도 'register'와 동일하게 ID가 없는 모델을 사용합니다.
            // (AppWordData -> ApiRegisterWordData)
            val finalApiData = words.map { appWord ->
                ApiRegisterWordData( // [!] WordDataWithWordID -> ApiRegisterWordData (WordData 별칭)로 변경
                    // wordId = appWord.wordId, // <-- 이 줄 삭제
                    word = appWord.word,
                    meanings = appWord.meanings.toList(),
                    distractors = appWord.distractors,
                    example = appWord.example
                )
            }

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
            // [!] Repository가 String ID를 받도록 수정됨 (이전 대화)
            _deleteStatus.value = repository.deleteWordbook(context.applicationContext, wid, ownerUid)
            _isLoading.value = false
        }
    }

    // --- [ ✨ 단어-유저 연결 함수 ✨ ] ---

    /**
     * 단어에 '좋아요', '틀림', '리뷰' 상태를 연결합니다.
     * (status: "liked" | "wrong" | "review")
     */
    fun linkWordToUser(context: Context, wordId: Int, status: String) {
        viewModelScope.launch {
            if (wordId <= 0) { // 0(새 단어)이거나 음수면 API 호출 방지
                _linkWordStatus.value = ApiResponse.Error("VIEWMODEL_ERROR", "저장되지 않은 단어입니다.")
                return@launch
            }
            _isLoading.value = true
            _linkWordStatus.value = repository.linkWordUser(context.applicationContext, listOf(wordId), status)
            _isLoading.value = false
        }
    }

    /**
     * 단어의 '좋아요', '틀림', '리뷰' 상태 연결을 해제합니다.
     */
    fun unlinkWordFromUser(context: Context, wordId: Int, status: String) {
        viewModelScope.launch {
            if (wordId <= 0) return@launch // 0(새 단어)이면 무시

            _isLoading.value = true
            _linkWordStatus.value = repository.unlinkWordUser(context.applicationContext, listOf(wordId), status)
            _isLoading.value = false
        }
    }

    // --- 7. 유틸리티 및 리셋 함수 ---

    // [ ✨ 4. (필수) 변환 헬퍼 함수 ✨ ]
    /**
     * API *응답* (ApiWordData, ID 있음) -> UI 모델 (AppWordData)
     */
    private fun mapApiWordToAppWord(apiWord: WordDataWithWordID): AppWordData {
        return AppWordData(
            wordId = apiWord.wordId,
            word = apiWord.word,
            meanings = apiWord.meanings.toMutableList(), // List -> MutableList
            distractors = apiWord.distractors,
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
    fun resetLinkWordStatus() { _linkWordStatus.value = null }
}