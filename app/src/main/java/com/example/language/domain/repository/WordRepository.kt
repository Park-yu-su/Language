package com.example.language.domain.repository

import com.example.language.data.remote.model.SocketResult
import com.example.language.domain.model.Tag
import com.example.language.domain.model.Word
import com.example.language.domain.model.Wordbook

interface WordRepository {
    // --- 단어장 조회 및 관리 ---

    // 단어장 상세 조회 (단어 포함)
    suspend fun getWordbook(wordbookId: Int): SocketResult<Wordbook>

    // 구독 중인(내) 단어장 목록 조회
    suspend fun getSubscribedWordbooks(uid: String): SocketResult<List<Wordbook>>

    // 새 단어장 만들기 (반환값: 생성된 Wordbook ID)
    suspend fun registerWordbook(
        title: String,
        tags: List<String>,
        ownerUid: String,
        words: List<Word>
    ): SocketResult<Int>

    // 단어장 삭제
    suspend fun deleteWordbook(wordbookId: String, ownerUid: String): SocketResult<String>

    // 단어장 구독 (내 보관함에 추가)
    suspend fun subscribe(wordbookId: String, uid: String): SocketResult<String>

    // 단어장 구독 취소
    suspend fun cancelSubscription(wordbookId: String, uid: String): SocketResult<String>


    // --- 태그 및 검색 ---

    // 태그 이름으로 검색 (태그 ID 반환 등)
    suspend fun searchTag(query: String): SocketResult<List<Tag>>

    // 태그 ID로 단어장 검색
    suspend fun searchWordbookByTag(tagIds: List<Int>): SocketResult<List<Wordbook>>


    // --- 학습 상태 (좋아요, 오답 등) ---

    // 단어 상태 연결 (좋아요/오답 등)
    suspend fun linkWordUser(uid: String, wordIds: List<Int>, status: String): SocketResult<String>

    // 단어 상태 해제
    suspend fun unlinkWordUser(uid: String, wordIds: List<Int>, status: String): SocketResult<String>

    // 특정 상태의 단어 목록 가져오기 (예: 오답노트)
    suspend fun getLinkedWords(uid: String, status: String): SocketResult<List<Word>>
}