package com.example.language.domain.usecase.mypage

import com.example.language.data.remote.model.SocketResult
import com.example.language.domain.model.Word
import com.example.language.domain.repository.WordRepository
import javax.inject.Inject

class GetSavedWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    /**
     * @param status "LIKED" 또는 "WRONG"
     */
    suspend operator fun invoke(uid: String, status: String): SocketResult<List<Word>> {
        return wordRepository.getLinkedWords(uid, status)
    }
}