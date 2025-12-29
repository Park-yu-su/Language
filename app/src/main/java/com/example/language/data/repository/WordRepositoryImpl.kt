package com.example.language.data.repository

import com.example.language.data.remote.SocketClient
import com.example.language.data.remote.model.*
import com.example.language.domain.model.Tag
import com.example.language.domain.model.Word
import com.example.language.domain.model.Wordbook
import com.example.language.domain.repository.WordRepository
import javax.inject.Inject

class WordRepositoryImpl @Inject constructor(
    private val socketClient: SocketClient
) : WordRepository {

    override suspend fun getWordbook(wordbookId: Int): SocketResult<Wordbook> {
        val payload = GetWordbookRequestPayload(wordbookId)
        val request = ClientRequest("GetWordbook", payload)

        val result = socketClient.executeRequest<GetWordbookRequestPayload, GetWordbookResponsePayload>(request)

        return when (result) {
            is SocketResult.Success -> {
                val words = result.data.data.map { dto ->
                    Word(dto.wordId, dto.word, dto.meanings, dto.distractors, dto.example)
                }
                // TODO 메타데이터는 일단 임시값. 필요 시 getWordbookInfoWithID 호출 병행
                SocketResult.Success(Wordbook(
                    wordbookId,
                    "단어장 $wordbookId",
                    emptyList(),
                    "",
                    words))
            }
            is SocketResult.Error -> SocketResult.Error(result.code, result.message)
        }
    }

    override suspend fun getSubscribedWordbooks(uid: String): SocketResult<List<Wordbook>> {
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid UID")

        val payload = FriendListRequestPayload(uidInt)
        val request = ClientRequest("GetSubscribedWordbooks", payload)

        val result = socketClient.executeRequest<FriendListRequestPayload, GetSubscribedWordbooksResponsePayload>(request)

        return when (result) {
            is SocketResult.Success -> {
                val list = result.data.data.map { dto ->
                    Wordbook(
                        id = dto.wid,
                        title = dto.title,
                        tags = dto.tags,
                        words = emptyList(), // 목록만 가져올 땐 단어 리스트는 비워둠
                        ownerUid = "" // 구독 목록엔 주인 정보가 없음
                    )
                }
                SocketResult.Success(list)
            }
            is SocketResult.Error -> SocketResult.Error(result.code, result.message)
        }
    }

    override suspend fun registerWordbook(
        title: String, tags: List<String>, ownerUid: String, words: List<Word>
    ): SocketResult<Int> {
        val wordDataList = words.map { WordData(it.text, it.meanings, it.distractors, it.example) }
        val payload = WordbookRegisterRequestPayload(title, tags, ownerUid, wordDataList)
        val request = ClientRequest("Wordbook", payload)

        val result = socketClient.executeRequest<WordbookRegisterRequestPayload, WordbookRegisterResponsePayload>(request)
        return result.map { it.wid }
    }

    override suspend fun deleteWordbook(wordbookId: String, ownerUid: String): SocketResult<String> {
        val payload = WordbookDeleteRequestPayload(wordbookId, ownerUid)
        val request = ClientRequest("WordbookDelete", payload)

        val result = socketClient.executeRequest<WordbookDeleteRequestPayload, WordbookDeleteResponsePayload>(request)
        return result.map { it.wid }
    }

    override suspend fun subscribe(wordbookId: String, uid: String): SocketResult<String> {
        val widInt = wordbookId.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid WID")
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid UID")

        val payload = SubscribeRequestPayload(widInt, uidInt)
        val request = ClientRequest("Subscribe", payload)

        val result = socketClient.executeRequest<SubscribeRequestPayload, SimpleMessagePayload>(request)
        return result.map { it.message }
    }

    override suspend fun cancelSubscription(wordbookId: String, uid: String): SocketResult<String> {
        val widInt = wordbookId.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid WID")
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid UID")

        val payload = SubscribeRequestPayload(widInt, uidInt)
        val request = ClientRequest("Cancel", payload)

        val result = socketClient.executeRequest<SubscribeRequestPayload, SimpleMessagePayload>(request)
        return result.map { it.message }
    }

    // --- 태그 및 검색 ---

    override suspend fun searchTag(query: String): SocketResult<List<Tag>> {
        val payload = SearchTagRequestPayload(query)
        val request = ClientRequest("SearchTag", payload)

        val result = socketClient.executeRequest<SearchTagRequestPayload, SearchTagResponsePayload>(request)

        return when (result) {
            is SocketResult.Success -> {
                val tags = result.data.data.map { dto ->
                    Tag(
                        id = dto.tid,
                        name = dto.name,
                        reference_count = dto.reference_count
                    )
                }
                SocketResult.Success(tags)
            }
            is SocketResult.Error -> SocketResult.Error(result.code, result.message)
        }
    }

    override suspend fun searchWordbookByTag(tagIds: List<Int>): SocketResult<List<Wordbook>> {
        val payload = SearchWordbookRequestPayload(tagIds)
        val request = ClientRequest("SearchWordbook", payload)

        val result = socketClient.executeRequest<SearchWordbookRequestPayload, SearchWordbookResponsePayload>(request)

        return when (result) {
            is SocketResult.Success -> {
                val list = result.data.data.map { dto ->
                    Wordbook(
                        id = dto.wid,
                        title = dto.title,
                        tags = dto.tags,
                        words = emptyList(),
                        ownerUid = "",
                        subscriptionCount = dto.subscription_count
                    )
                }
                SocketResult.Success(list)
            }
            is SocketResult.Error -> SocketResult.Error(result.code, result.message)
        }
    }


    // --- 학습 상태 관리 ---

    override suspend fun linkWordUser(uid: String, wordIds: List<Int>, status: String): SocketResult<String> {
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid UID")
        val payload = LinkUserWordRequestPayload(uidInt, wordIds, status)
        val request = ClientRequest("LinkUserWord", payload)

        val result = socketClient.executeRequest<LinkUserWordRequestPayload, SimpleMessagePayload>(request)
        return result.map { it.message }
    }

    override suspend fun unlinkWordUser(uid: String, wordIds: List<Int>, status: String): SocketResult<String> {
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid UID")
        val payload = LinkUserWordRequestPayload(uidInt, wordIds, status)
        val request = ClientRequest("UnlinkUserWord", payload)

        val result = socketClient.executeRequest<LinkUserWordRequestPayload, SimpleMessagePayload>(request)
        return result.map { it.message }
    }

    override suspend fun getLinkedWords(uid: String, status: String): SocketResult<List<Word>> {
        val uidInt = uid.toIntOrNull() ?: return SocketResult.Error("ERROR", "Invalid UID")
        val payload = GetLinkedWordOfUserRequestPayload(uidInt, status)
        val request = ClientRequest("GetLinkedWordOfUser", payload)

        val result = socketClient.executeRequest<GetLinkedWordOfUserRequestPayload, GetLinkedWordOfUserResponsePayload>(request)

        return when (result) {
            is SocketResult.Success -> {
                val words = result.data.data.map { dto ->
                    Word(dto.wordId, dto.word, dto.meanings, dto.distractors, dto.example)
                }
                SocketResult.Success(words)
            }
            is SocketResult.Error -> SocketResult.Error(result.code, result.message)
        }
    }
}