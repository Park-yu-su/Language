package com.example.language.domain.usecase.wordbook

import com.example.language.data.remote.model.SocketResult
import com.example.language.domain.model.Wordbook
import com.example.language.domain.repository.WordRepository
import javax.inject.Inject

class GetWordbookDetailUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(wordbookId: Int): SocketResult<Wordbook> {
        return wordRepository.getWordbook(wordbookId)
    }
}