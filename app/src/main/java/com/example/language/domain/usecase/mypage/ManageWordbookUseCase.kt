package com.example.language.domain.usecase.mypage

import com.example.language.data.remote.model.SocketResult
import com.example.language.domain.model.Word
import com.example.language.domain.repository.WordRepository
import javax.inject.Inject

class ManageWordbookUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    // 단어장 생성
    suspend fun create(
        title: String,
        tags: List<String>,
        ownerUid: String,
        words: List<Word>
    ): SocketResult<Int> {
        if (title.isBlank()) {
            return SocketResult.Error("VALIDATION", "제목을 입력해주세요.")
        }
        return wordRepository.registerWordbook(title, tags, ownerUid, words)
    }

    // 단어장 삭제
    suspend fun delete(wordbookId: String, ownerUid: String): SocketResult<String> {
        return wordRepository.deleteWordbook(wordbookId, ownerUid)
    }
}