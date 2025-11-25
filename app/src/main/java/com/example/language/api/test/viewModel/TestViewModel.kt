package com.example.language.api.test.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.language.api.ApiResponse
import com.example.language.api.GetSubscribedWordbooksResponsePayload
import com.example.language.api.GetWordbookResponsePayload
import com.example.language.api.SttResponsePayload
import com.example.language.api.test.TestRepository
import com.example.language.data.WordData
import kotlinx.coroutines.launch

class TestViewModel(private val repository: TestRepository) : ViewModel() {

    //음원 결과
    private val _voiceResult = MutableLiveData<ApiResponse<SttResponsePayload>>()
    val voiceResult: LiveData<ApiResponse<SttResponsePayload>> = _voiceResult

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

    //3. 보이스 파일 -> 전송
    fun sendVoiceForSTT(context: Context, fileBytes: ByteArray,
                        fileName: String, answer: String) {

        viewModelScope.launch {
            val response = repository.sendVoiceForSTT(context, fileBytes, fileName, answer)
            _voiceResult.value = response
        }
    }

    // status : liked | wrong | review
    // liked : 좋아요 한 단어
    // wrong : 틀린 단어
    // review : 리뷰할 단어
    //4. 각 태그에 따라 유저의 좋아요 한 단어, 틀린 단어, 리뷰할 단어 등록
    fun linkWordUser(context: Context, uid: Int, word_ids: List<Int>, status: String) {
        viewModelScope.launch {
            val response = repository.linkWordUser(context, uid, word_ids, status)
        }
    }
    fun unlinkWordUser(context: Context, uid: Int, word_ids: List<Int>, status: String) {
        viewModelScope.launch {
            val response = repository.unlinkWordUser(context, uid, word_ids, status)
        }
    }

    fun submitQuiz(context: Context, uid: Int,
                   wordId: Int, wordText: String, question: String,
                   userAnswer: String, correctAnswer: String){
        viewModelScope.launch {
            repository.quizSubmit(context, uid, wordId, wordText, question, userAnswer, correctAnswer)
        }
    }

}
