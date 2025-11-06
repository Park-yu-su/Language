package com.example.language.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.language.data.repository.WordbookRepository

class VocViewModelFactory(
    private val repository: WordbookRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // modelClass가 VocViewModel 타입인지 확인
        if (modelClass.isAssignableFrom(VocViewModel::class.java)) {
            // Repository를 주입하여 VocViewModel의 새 인스턴스를 생성
            @Suppress("UNCHECKED_CAST")
            return VocViewModel(repository) as T
        }
        // 다른 ViewModel 타입이라면 오류 발생
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}