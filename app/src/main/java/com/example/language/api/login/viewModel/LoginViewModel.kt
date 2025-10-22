package com.example.language.api.login.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.language.api.ApiResponse
import com.example.language.api.AuthResponsePayload
import com.example.language.api.login.LoginRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {

    // UI에 보여줄 로그인 결과 (LiveData)
    private val _loginResult = MutableLiveData<ApiResponse<AuthResponsePayload>>()
    val loginResult: LiveData<ApiResponse<AuthResponsePayload>> = _loginResult

    fun requestLogin(context: Context, email: String, nickname: String) {
        viewModelScope.launch {
            val response = repository.loginUser(context, email, nickname)
            _loginResult.value = response
        }
    }
}