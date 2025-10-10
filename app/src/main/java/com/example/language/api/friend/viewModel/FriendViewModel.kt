package com.example.language.api.friend.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.language.data.FriendData

class FriendViewModel: ViewModel() {
    val friendEventStart = MutableLiveData<Boolean>()

    //현재 친구 목록
    val friendList = MutableLiveData<MutableList<FriendData>>()
    //삭제 or NO
    val isDelete = MutableLiveData<Boolean>()

}