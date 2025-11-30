package com.example.language.api.mypage.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.language.api.ApiResponse
import com.example.language.api.AuthResponsePayload
import com.example.language.api.GetSubscribedWordbooksResponsePayload
import com.example.language.api.GetWordbookResponsePayload
import com.example.language.api.SubscribedWordbooksData
import com.example.language.api.WordbookDeleteResponsePayload
import com.example.language.api.WordbookRegisterRequestPayload
import com.example.language.api.WordbookRegisterResponsePayload
import com.example.language.api.WordbookUpdateRequestPayload
import com.example.language.api.WordbookUpdateResponsePayload
import com.example.language.api.mypage.MypageRepository
import com.example.language.data.VocData
import kotlinx.coroutines.launch

class MypageViewModel(private val repository: MypageRepository): ViewModel() {

    // UI에 보여줄 로그인 결과 (LiveData)
    private val _loginResult = MutableLiveData<ApiResponse<AuthResponsePayload>>()
    val loginResult: LiveData<ApiResponse<AuthResponsePayload>> = _loginResult


    //내 단어장 가져오는 live
    private val _wordbookListResult = MutableLiveData<ApiResponse<GetSubscribedWordbooksResponsePayload>>()
    val wordbookListResult = _wordbookListResult

    //단어 리스트 가져오는 lvie
    private val _wordListResult = MutableLiveData<ApiResponse<GetWordbookResponsePayload>>()
    val wordListResult = _wordListResult

    //내가 가지고 있는 단어장 리스트
    var mywordbookList = mutableListOf<SubscribedWordbooksData>()

    lateinit var selectWordbookInfo : VocData
    var selectWordbookId : Int = 0

    // 4. 단어장 생성 결과
    private val _wordbookRegisterResult = MutableLiveData<ApiResponse<WordbookRegisterResponsePayload>>()
    val wordbookRegisterResult: LiveData<ApiResponse<WordbookRegisterResponsePayload>> = _wordbookRegisterResult

    // 5. 단어장 수정 결과 (단어 삭제 시에도 사용)
    private val _wordbookUpdateResult = MutableLiveData<ApiResponse<WordbookUpdateResponsePayload>?>()
    val wordbookUpdateResult: LiveData<ApiResponse<WordbookUpdateResponsePayload>?> = _wordbookUpdateResult

    // 6. 단어장 삭제 결과
    private val _wordbookDeleteResult = MutableLiveData<ApiResponse<WordbookDeleteResponsePayload>?>()
    val wordbookDeleteResult: LiveData<ApiResponse<WordbookDeleteResponsePayload>?> = _wordbookDeleteResult

    // 삭제 결과 초기화 함수
    fun initDeleteResult() {
        _wordbookDeleteResult.value = null
    }

    // 단어장 수정(단어 삭제) 결과 초기화 함수
    fun initUpdateResult() {
        _wordbookUpdateResult.value = null
    }

    //1. 내 단어장 가져오기
    fun getSubscribedWordbooks(context: Context, uid: Int) {
        viewModelScope.launch {
            val response = repository.getSubscribedWordbooks(context, uid)
            _wordbookListResult.value = response
        }
    }

    //2. 단어장 ID로 단어들 가져오기
    fun getWordbook(context: Context, wid: Int) {
        viewModelScope.launch {
            val response = repository.getWordbook(context, wid)
            _wordListResult.value = response
        }
    }

    //3. 유저 정보 업데이트
    fun updateUserInfo(context: Context, email: String, nickname: String, image: String, oneline: String) {
        viewModelScope.launch {
            val response = repository.loginUser(context, email, nickname, image, oneline)
            _loginResult.value = response
        }
    }

    // 4. 단어장 생성
    fun registerWordbook(context: Context, payload: WordbookRegisterRequestPayload) {
        viewModelScope.launch {
            val response = repository.registerWordbook(context, payload)
            _wordbookRegisterResult.value = response
        }
    }

    // 5. 단어장 수정 (단어 하나를 삭제하고 싶을 때, 리스트를 수정해서 이 함수를 호출합니다)
    fun updateWordbook(context: Context, payload: WordbookUpdateRequestPayload) {
        viewModelScope.launch {
            val response = repository.updateWordbook(context, payload)
            _wordbookUpdateResult.value = response
        }
    }

    // 6. 단어장 자체 삭제
    fun deleteWordbook(context: Context, wid: String, ownerUid: String) {
        viewModelScope.launch {
            val response = repository.deleteWordbook(context, wid, ownerUid)
            _wordbookDeleteResult.value = response
        }
    }
}