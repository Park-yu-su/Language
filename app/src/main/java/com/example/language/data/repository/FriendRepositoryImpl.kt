package com.example.language.data.repository

import com.example.language.data.remote.SocketClient
import com.example.language.data.remote.model.*
import com.example.language.domain.model.Friend
import com.example.language.domain.repository.FriendRepository
import javax.inject.Inject

class FriendRepositoryImpl @Inject constructor(
    private val socketClient: SocketClient
) : FriendRepository {

    override suspend fun getFriendList(uid: String): SocketResult<List<Friend>> {
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("INVALID_UID", "UID error")
        val payload = FriendListRequestPayload(uidInt)
        val request = ClientRequest("Friend", payload)

        val result = socketClient.executeRequest<FriendListRequestPayload, FriendListResponsePayload>(request)

        return when (result) {
            is SocketResult.Success -> {
                val d = result.data
                val friends = d.uids.mapIndexed { index, id ->
                    Friend(
                        uid = id,
                        nickname = d.nicknames.getOrElse(index) { "" },
                        profileImage = d.images.getOrElse(index) { "" },
                        introduce = d.onelines.getOrElse(index) { "" }
                    )
                }
                SocketResult.Success(friends)
            }
            is SocketResult.Error -> SocketResult.Error(result.code, result.message)
        }
    }

    override suspend fun addFriend(requesterId: String, requesteeId: String): SocketResult<String> {
        val r1 = requesterId.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid ID")
        val r2 = requesteeId.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid ID")

        val payload = FriendRequestPayload(r1, r2)
        val request = ClientRequest("Request", payload)

        val result = socketClient.executeRequest<FriendRequestPayload, SimpleMessagePayload>(request)
        return result.map { it.message } // 성공 시 메시지 반환
    }

    override suspend fun acceptFriend(requesterId: String, requesteeId: String): SocketResult<String> {
        val r1 = requesterId.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid ID")
        val r2 = requesteeId.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid ID")

        val payload = FriendRequestPayload(r1, r2)
        val request = ClientRequest("Accept", payload)

        val result = socketClient.executeRequest<FriendRequestPayload, SimpleMessagePayload>(request)
        return result.map { it.message }
    }

    override suspend fun rejectFriend(requesterId: String, requesteeId: String): SocketResult<String> {
        val r1 = requesterId.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid ID")
        val r2 = requesteeId.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid ID")

        val payload = FriendRequestPayload(r1, r2)
        val request = ClientRequest("Reject", payload)

        val result = socketClient.executeRequest<FriendRequestPayload, SimpleMessagePayload>(request)
        return result.map { it.message }
    }

    override suspend fun deleteFriend(requesterId: String, requesteeId: String): SocketResult<String> {
        val r1 = requesterId.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid ID")
        val r2 = requesteeId.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid ID")

        val payload = FriendRequestPayload(r1, r2)
        val request = ClientRequest("DeleteFriend", payload)

        val result = socketClient.executeRequest<FriendRequestPayload, SimpleMessagePayload>(request)
        return result.map { it.message }
    }

    override suspend fun getPendingRequests(uid: String, type: String): SocketResult<List<Friend>> {
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("INVALID_UID", "UID error")
        val payload = PendingRequestsPayload(uidInt, type)
        val request = ClientRequest("PendingRequests", payload)

        val result = socketClient.executeRequest<PendingRequestsPayload, FriendListResponsePayload>(request)

        return when (result) {
            is SocketResult.Success -> {
                val d = result.data
                val friends = d.uids.mapIndexed { index, id ->
                    Friend(
                        uid = id,
                        nickname = d.nicknames.getOrElse(index) { "" },
                        profileImage = d.images.getOrElse(index) { "" },
                        introduce = d.onelines.getOrElse(index) { "" },
                        isRequestSent = false // 대기 중인 목록이므로 false로 둠 (필요시 true)
                    )
                }
                SocketResult.Success(friends)
            }
            is SocketResult.Error -> SocketResult.Error(result.code, result.message)
        }
    }
}