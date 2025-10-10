package com.example.language.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.language.data.FriendData
import com.example.language.databinding.ItemFriendBinding

class FriendListAdapter(private var friendList: MutableList<FriendData>,
                        private val onAlarmClicked: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    inner class FriendListViewHolder(private val binding: ItemFriendBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: FriendData) {
            //이미지
            /*
            Glide.with(itemView)
                .load(friend.profileImage)
                .placeholder(R.drawable.ic_friend_profile)
                .error(R.drawable.ic_friend_profile)
                .circleCrop()
                .into(binding.friendProfile)

             */

            binding.friendTvName.text = friend.name
            binding.friendTvStatus.text = friend.introduce

            //각 버튼에 콜백 함수 전달
            binding.friendActionBtn.setOnClickListener {
                onAlarmClicked()
            }


        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FriendListViewHolder).bind(friendList[position])
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendListViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return friendList.size
    }


}