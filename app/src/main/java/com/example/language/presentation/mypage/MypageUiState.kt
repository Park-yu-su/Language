package com.example.language.presentation.mypage

import com.example.language.domain.model.User
import com.example.language.domain.model.Word
import com.example.language.domain.model.Wordbook

sealed interface MypageUiState {
    data object Loading : MypageUiState

    data class Success(
        val profile: User,                // 내 정보
        val wordbooks: List<Wordbook>,    // 내 단어장
        val likedWords: List<Word>,       // 보관함(좋아요) 단어
        val wrongWords: List<Word>        // 오답노트 단어
    ) : MypageUiState

    data class Error(val message: String) : MypageUiState
}