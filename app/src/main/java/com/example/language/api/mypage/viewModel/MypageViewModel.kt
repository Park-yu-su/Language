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
import com.example.language.api.mypage.MypageRepository
import com.example.language.data.VocData
import com.example.language.data.WordData
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


}