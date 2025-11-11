package com.example.language.api.chat.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.language.api.chat.ChatRepository


class ChatViewModelFactory(private val repository: ChatRepository)
    : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // modelClass가 LoginViewModel 타입인지 확인
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            // LoginRepository를 주입하여 LoginViewModel의 새 인스턴스를 생성
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository) as T
        }
        // 다른 ViewModel 타입이라면 오류 발생
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}