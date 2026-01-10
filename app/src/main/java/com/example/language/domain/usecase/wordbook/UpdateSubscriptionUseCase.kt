package com.example.language.domain.usecase.wordbook

import com.example.language.data.remote.model.SocketResult
import com.example.language.domain.repository.WordRepository
import javax.inject.Inject

class UpdateSubscriptionUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    /**
     * @param isSubscribe true면 구독, false면 구독 취소
     */
    suspend operator fun invoke(wordbookId: String, uid: String, isSubscribe: Boolean): SocketResult<String> {
        return if (isSubscribe) {
            wordRepository.subscribe(wordbookId, uid)
        } else {
            wordRepository.cancelSubscription(wordbookId, uid)
        }
    }
}