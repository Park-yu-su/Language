package com.example.language.api.study.viewModel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.language.api.ApiResponse
import com.example.language.api.GetSubscribedWordbooksResponsePayload
import com.example.language.api.GetWordbookResponsePayload
import com.example.language.api.study.StudyRepository
import com.example.language.data.WordData
import kotlinx.coroutines.launch

class StudyViewModel(private val repository: StudyRepository) : ViewModel() {
    val searchEventStart = MutableLiveData<Boolean>()

    //내 단어장 가져오는 live
    private val _wordbookListResult = MutableLiveData<ApiResponse<GetSubscribedWordbooksResponsePayload>>()
    val wordbookListResult = _wordbookListResult

    //단어 리스트 가져오는 lvie
    private val _wordListResult = MutableLiveData<ApiResponse<GetWordbookResponsePayload>>()
    val wordListResult = _wordListResult


    //단어장 리스트에서 내가 고른 단어장(편하게 하자)
    var selectWordbookId : Int = 0
    //내가 고른 단어장에서의 단어들
    var selectWordList : MutableList<WordData> = mutableListOf()
    //내가 단어들 중 몇 번째 단어를 터치했는지 정보
    var selectWordIndex : Int = 0



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

}