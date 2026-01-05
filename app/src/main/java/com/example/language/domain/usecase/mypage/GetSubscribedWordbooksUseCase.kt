package com.example.language.domain.usecase.mypage

import com.example.language.data.remote.model.SocketResult
import com.example.language.domain.model.Wordbook
import com.example.language.domain.repository.WordRepository
import javax.inject.Inject

class GetSubscribedWordbooksUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(uid: String): SocketResult<List<Wordbook>> {
        return wordRepository.getSubscribedWordbooks(uid)
    }
}