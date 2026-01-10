package com.example.language.domain.usecase.wordbook

import com.example.language.data.remote.model.SocketResult
import com.example.language.domain.repository.WordRepository
import javax.inject.Inject

class UpdateWordStatusUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    /**
     * @param isLink true면 연결(상태 추가), false면 해제(상태 삭제)
     * @param status "LIKED", "WRONG", "REVIEW"
     */
    suspend operator fun invoke(
        uid: String,
        wordIds: List<Int>,
        status: String,
        isLink: Boolean
    ): SocketResult<String> {
        return if (isLink) {
            wordRepository.linkWordUser(uid, wordIds, status)
        } else {
            wordRepository.unlinkWordUser(uid, wordIds, status)
        }
    }
}