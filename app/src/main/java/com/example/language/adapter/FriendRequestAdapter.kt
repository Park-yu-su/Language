package com.example.language.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.language.data.FriendData
import com.example.language.databinding.ItemFriendRequestBinding

class FriendRequestAdapter(
    private var requestList : MutableList<FriendData>,
    private var onAccept: (FriendData) -> Unit,
    private var onReject: (FriendData) -> Unit
    ) : RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder>()
{
    inner class RequestViewHolder(private val binding: ItemFriendRequestBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(data: FriendData){

            /*
            Glide.with(itemView)
                .load(data.profileImage)
                .placeholder(R.drawable.ic_friend_profile)
                .error(R.drawable.ic_friend_profile)
                .circleCrop()
                .into(binding.friendRequestProfile)
            * */
            binding.friendRequestTvName.text = data.name
            binding.friendReqestTvStatus.text = data.introduce

            //수락 시 콜백 후 제거
            binding.friendRequestAcceptBtn.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val acceptedFriend = requestList[position]
                    onAccept(acceptedFriend)  //콜백에서 API 처리하고, 성공 시 removeAt 해줄 것
                }
            }

            //거절 시 콜백 후 제거
            binding.friendRequestRejectBtn.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val rejectedFriend = requestList[position]
                    onReject(rejectedFriend) //콜백에서 API 처리 후 removeAt
                    }
                }
            }
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemFriendRequestBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(requestList[position])
    }

    override fun getItemCount(): Int {
        return requestList.size
    }

}
