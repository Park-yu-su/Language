package com.example.language.domain.model

// 단어장
data class Wordbook(
    val id: Int,
    val title: String,
    val tags: List<String>,
    val ownerUid: String,
    val words: List<Word> = emptyList(), // 단어장 안의 단어들 (상세 조회 시 사용)
    val subscriptionCount: Int = 0
)

// 단어 하나
data class Word(
    val id: Int,
    val text: String,
    val meanings: List<String>,
    val distractors: List<String>,
    val example: String
)

data class Tag(
    val id: Int,
    val name: String,
    val reference_count: Int
)