package com.example.language.api.friend.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.language.api.friend.FriendRepository


class FriendViewModelFactory(private val repository: FriendRepository)
    : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // modelClass가 LoginViewModel 타입인지 확인
        if (modelClass.isAssignableFrom(FriendViewModel::class.java)) {
            // LoginRepository를 주입하여 LoginViewModel의 새 인스턴스를 생성
            @Suppress("UNCHECKED_CAST")
            return FriendViewModel(repository) as T
        }
        // 다른 ViewModel 타입이라면 오류 발생
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}