package com.example.language.api.study.viewModel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.language.api.ApiResponse
import com.example.language.api.GetSubscribedWordbooksResponsePayload
import com.example.language.api.GetWordbookResponsePayload
import com.example.language.api.SearchTagResponsePayload
import com.example.language.api.SearchWordbookResponsePayload
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

    //태그 검색 시 유효한지 (ID 주는지) live
    private val _searchTagResult = MutableLiveData<ApiResponse<SearchTagResponsePayload>>()
    val searchTagResult = _searchTagResult
    //태그 검색 결과 단어장 리스트
    private val _searchTagWordbookResult = MutableLiveData<ApiResponse<SearchWordbookResponsePayload>>()
    val searchTagWordbookResult = _searchTagWordbookResult



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

    //3. 태그가 존재하는지 탐색
    fun searchTag(context: Context, query: String) {
        viewModelScope.launch {
            val response = repository.searchTag(context, query)
            _searchTagResult.value = response
        }
    }

    //4. 태그 검색
    fun searchWordbookByTag(context: Context, tids: List<Int>){
        viewModelScope.launch{
            val response = repository.searchWordbookbyTag(context, tids)
            _searchTagWordbookResult.value = response
        }
    }

    //5. 단어장 추가하기
    fun subscribeWordbook(context: Context, wid: Int, subscriber: Int){
        viewModelScope.launch {
            val response = repository.subscribeWordbook(context, wid, subscriber)
        }
    }


}