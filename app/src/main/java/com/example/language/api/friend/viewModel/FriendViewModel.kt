package com.example.language.api.friend.viewModel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.language.api.ApiResponse
import com.example.language.api.FriendListResponsePayload
import com.example.language.api.SttResponsePayload
import com.example.language.api.friend.FriendRepository
import com.example.language.data.FriendData
import kotlinx.coroutines.launch

class FriendViewModel(private val repository: FriendRepository): ViewModel() {
    //친구 관리 창으로 이동하는 메서드
    val friendEventStart = MutableLiveData<Boolean>()

    //친구 리스트 결과
    private val _friendListResult = MutableLiveData<ApiResponse<FriendListResponsePayload>>()
    val friendListResult = _friendListResult



    //친구 리스트 출력
    fun getFriendList(context: Context, uid: Int){
        viewModelScope.launch {
            val response = repository.getFriendList(context, uid)
            _friendListResult.value = response
        }
    }

    //친구 요청 수락
    fun acceptFriend(context: Context, myUid: Int, friendUid: Int){
        viewModelScope.launch {
            repository.acceptFriend(context, myUid, friendUid)
        }
    }


}