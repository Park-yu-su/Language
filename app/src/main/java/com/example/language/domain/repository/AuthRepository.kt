package com.example.language.domain.repository

import com.example.language.data.remote.model.SocketResult
import com.example.language.domain.model.User

interface AuthRepository {
    // 카카오 로그인 및 회원가입
    suspend fun authenticate(
        email: String,
        nickname: String,
        image: String,
        oneline: String
    ): SocketResult<User>

    // 유저 ID로 유저 검색
    suspend fun searchUser(uid: String): SocketResult<User>
}