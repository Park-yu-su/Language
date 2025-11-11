package com.example.language.ui.chat

//챗봇 메뉴에서 선택할 수 있는 기능을 나타내는 Enum class
enum class ChatFeature {
    REVIEW_WORDS, //오늘 배운 단어 정리하기
    CREATE_EXAMPLE,  //예문 생성하기
    CREATE_ODAB //오답 노트 생성하기
}

//ChatFragment가 구현할 인터페이스
interface ChatMenuListener {
    fun onFeatureSelected(feature: ChatFeature)
}