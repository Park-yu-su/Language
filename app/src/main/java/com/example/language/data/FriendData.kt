package com.example.language.data

data class FriendData (
    var id: String, //ID
    var name: String, //이름
    var userImage: String, //이미지
    var introduce: String, //자기소개
    var isRequestSent: Boolean = false //추가 눌렀는지 체크
)