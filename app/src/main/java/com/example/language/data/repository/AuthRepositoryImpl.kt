package com.example.language.data.repository

import com.example.language.data.remote.SocketClient
import com.example.language.data.remote.model.*
import com.example.language.domain.model.User
import com.example.language.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val socketClient: SocketClient
) : AuthRepository {

    override suspend fun authenticate(
        email: String, nickname: String, image: String, oneline: String
    ): SocketResult<User> {
        val payload = AuthRequestPayload(email, nickname, image, oneline)
        val request = ClientRequest("Authentication", payload)

        val result = socketClient.executeRequest<AuthRequestPayload, AuthResponsePayload>(request)

        return when (result) {
            is SocketResult.Success -> {
                val d = result.data
                SocketResult.Success(User(d.uid, d.nickname, d.email, d.image, d.oneline))
            }
            is SocketResult.Error -> SocketResult.Error(result.code, result.message)
        }
    }

    override suspend fun searchUser(uid: String): SocketResult<User> {
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("INVALID_UID", "UID must be integer")

        val payload = FriendListRequestPayload(uidInt)
        val request = ClientRequest("SearchUserByUid", payload)

        val result = socketClient.executeRequest<FriendListRequestPayload, SearchUserResponsePayload>(request)

        return when (result) {
            is SocketResult.Success -> {
                val d = result.data
                // 검색 결과엔 이메일/소개글이 없을 수 있음 -> 빈 문자열 처리
                // TODO 따로 이메일/소개글을 불러오는 API와 연계 필요
                SocketResult.Success(User(d.uid.toString(), d.nickname, "", d.image, ""))
            }
            is SocketResult.Error -> SocketResult.Error(result.code, result.message)
        }
    }
}