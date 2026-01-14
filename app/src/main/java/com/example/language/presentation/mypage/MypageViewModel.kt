package com.example.language.presentation.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.language.data.remote.model.SocketResult
import com.example.language.domain.usecase.mypage.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MypageViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getSubscribedWordbooksUseCase: GetSubscribedWordbooksUseCase,
    private val getSavedWordsUseCase: GetSavedWordsUseCase,
    private val manageWordbookUseCase: ManageWordbookUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MypageUiState>(MypageUiState.Loading)
    val uiState: StateFlow<MypageUiState> = _uiState.asStateFlow()

    fun loadData(uid: String) {
        viewModelScope.launch {
            _uiState.value = MypageUiState.Loading

            val profileDeferred = async { getMyProfileUseCase(uid) }
            val wordbooksDeferred = async { getSubscribedWordbooksUseCase(uid) }
            val likedWordsDeferred = async { getSavedWordsUseCase(uid, "LIKED") }
            val wrongWordsDeferred = async { getSavedWordsUseCase(uid, "WRONG") }

            // 결과 받기
            val profileResult = profileDeferred.await()
            val wordbooksResult = wordbooksDeferred.await()
            val likedResult = likedWordsDeferred.await()
            val wrongResult = wrongWordsDeferred.await()

            if (profileResult is SocketResult.Error) {
                _uiState.value = MypageUiState.Error(profileResult.message)
                return@launch
            }

            // 성공 상태 업데이트
            _uiState.value = MypageUiState.Success(
                profile = (profileResult as SocketResult.Success).data,
                wordbooks = (wordbooksResult as? SocketResult.Success)?.data ?: emptyList(),
                likedWords = (likedResult as? SocketResult.Success)?.data ?: emptyList(),
                wrongWords = (wrongResult as? SocketResult.Success)?.data ?: emptyList()
            )
        }
    }

    /**
     * 단어장 삭제
     */
    fun deleteWordbook(wordbookId: String, uid: String) {
        viewModelScope.launch {
            val result = manageWordbookUseCase.delete(wordbookId, uid)

            when (result) {
                is SocketResult.Success -> {
                    // 삭제 성공 시 목록 새로고침
                    loadData(uid)
                }
                is SocketResult.Error -> {
                    Timber.d("Delete failed: ${result.message}")
                }
            }
        }
    }
}