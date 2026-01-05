package com.example.language.domain.usecase.mypage

import com.example.language.data.remote.model.SocketResult
import com.example.language.domain.model.User
import com.example.language.domain.repository.AuthRepository
import javax.inject.Inject

class GetMyProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * 내 UID를 이용해서 최신 유저 정보를 가져옵니다.
     */
    suspend operator fun invoke(uid: String): SocketResult<User> {
        return authRepository.searchUser(uid)
    }
}