package com.example.language.api.chat.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.language.api.AIResponseDataPayload
import com.example.language.api.ApiResponse
import com.example.language.api.SessionStartResponsePayload
import com.example.language.api.chat.ChatRepository
import com.example.language.data.ChatMessage
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository): ViewModel() {

    //챗봇 설정 시 sessionID 세팅
    private val _startSessionResult = MutableLiveData<ApiResponse<SessionStartResponsePayload>>()
    val startSessionResult = _startSessionResult

    //대화 결과 얻는 응답 저장
    private val _chatInputResult = MutableLiveData<ApiResponse<AIResponseDataPayload>>()
    val chatInputResult = _chatInputResult

    //채팅방 ID를 기억
    var sessionId : String = ""

    //채팅 내용을 기억
    val messageList = mutableListOf<ChatMessage>()


    //1. 챗봇 실행
    fun startSession(context: Context, uid: Int, name: String){
        viewModelScope.launch {
            val response = repository.startSession(context, uid, name)
            _startSessionResult.value = response
        }
    }

    //2. 챗봇한테 대화 물어보기
    fun chatInput(context: Context, uid: Int, sessionId: String, message: String) {
        viewModelScope.launch {
            val response = repository.chatInput(context, uid, sessionId, message)
            _chatInputResult.value = response
        }
    }

    //3. 오늘 배운 단어 리뷰
    fun getTodayReview(context: Context, uid: Int, sessionId: String) {
        viewModelScope.launch {
            val response = repository.todayReview(context, uid, sessionId)
            _chatInputResult.value = response
        }
    }

    //4. 예문 생성
    fun getExample(context: Context, uid: Int, sessionId: String) {
        viewModelScope.launch {
            val response = repository.generateExample(context, uid, sessionId)
            _chatInputResult.value = response
        }
    }

    //5. 레포트 작성
    fun getReport(context: Context, uid: Int, sessionId: String) {
        viewModelScope.launch {
            val response = repository.getReport(context, uid, sessionId)
            _chatInputResult.value = response
        }
    }
}