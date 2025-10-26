package com.example.language.api.friend.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.language.api.ApiResponse
import com.example.language.api.FriendListResponsePayload
import com.example.language.api.PendingRequestsPayload
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

    //요청 대기 리스트 결과
    private val _pendingListResult = MutableLiveData<ApiResponse<FriendListResponsePayload>>()
    val pendingListResult = _pendingListResult



    //친구 리스트 출력
    fun getFriendList(context: Context, uid: Int){
        viewModelScope.launch {
            val response = repository.getFriendList(context, uid)
            _friendListResult.value = response
        }
    }

    //친구 요청 리스트 출력
    fun getPendingList(context: Context, uid: Int, type:String){
        viewModelScope.launch {
            val response = repository.getPendingRequests(context, uid, type)
            _pendingListResult.value = response
        }
    }

    //친구 요청 수락
    fun acceptFriend(context: Context, myUid: Int, friendUid: Int){
        viewModelScope.launch {
            repository.acceptFriend(context, myUid, friendUid)
        }
    }

    //친구 요청 거절
    fun rejectFriend(context: Context, myUid: Int, friendUid: Int) {
        viewModelScope.launch {
            repository.rejectFriend(context, myUid, friendUid)
        }
    }

    //친구 삭제
    fun deleteFriend(context: Context, myUid: Int, friendUid: Int) {
        viewModelScope.launch {
            val response = repository.deleteFriend(context, myUid, friendUid)
            Log.d("log_friend", "")
        }
    }

}