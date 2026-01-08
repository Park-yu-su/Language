package com.example.language.domain.usecase.wordbook

import com.example.language.data.remote.model.SocketResult
import com.example.language.domain.model.Tag
import com.example.language.domain.repository.WordRepository
import javax.inject.Inject

class SearchTagUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(query: String): SocketResult<List<Tag>> {
        if (query.isBlank()) return SocketResult.Success(emptyList())
        return wordRepository.searchTag(query)
    }
}