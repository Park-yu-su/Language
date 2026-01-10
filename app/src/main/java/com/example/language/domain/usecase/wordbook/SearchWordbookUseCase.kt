package com.example.language.domain.usecase.wordbook

import com.example.language.data.remote.model.SocketResult
import com.example.language.domain.model.Wordbook
import com.example.language.domain.repository.WordRepository
import javax.inject.Inject

class SearchWordbookUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(tagIds: List<Int>): SocketResult<List<Wordbook>> {
        if (tagIds.isEmpty()) return SocketResult.Success(emptyList())
        return wordRepository.searchWordbookByTag(tagIds)
    }
}