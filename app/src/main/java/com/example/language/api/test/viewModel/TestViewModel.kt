package com.example.language.api.test.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.language.api.ApiResponse
import com.example.language.api.SttResponsePayload
import com.example.language.api.test.TestRepository
import kotlinx.coroutines.launch

class TestViewModel(private val repository: TestRepository) : ViewModel() {

    private val _voiceResult = MutableLiveData<ApiResponse<SttResponsePayload>>()
    val voiceResult: LiveData<ApiResponse<SttResponsePayload>> = _voiceResult


    fun sendVoiceForSTT(context: Context, fileBytes: ByteArray,
                        fileName: String, answer: String) {

        viewModelScope.launch {
            val response = repository.sendVoiceForSTT(context, fileBytes, fileName, answer)
            _voiceResult.value = response

        }

    }

}
