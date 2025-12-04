package com.example.language.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.language.R
import com.example.language.data.FriendData
import com.example.language.databinding.ItemFriendDeleteBinding

class FriendDeleteAdapter(
    private var friendList: MutableList<FriendData>,
    private val onDeleteClicked: (uid: String, name:String, position: Int) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class FriendDeleteViewHolder(private val binding: ItemFriendDeleteBinding) :
        RecyclerView.ViewHolder(binding.root){
            fun bind(friend: FriendData){
                //이미지
                /*
                Glide.with(itemView)
                    .load(friend.profileImage)
                    .placeholder(R.drawable.ic_friend_profile)
                    .error(R.drawable.ic_friend_profile)
                    .circleCrop()
                    .into(binding.friendProfile)

                 */

                val imageResId = when(friend.userImage) {
                    "0" -> R.drawable.img_default_user1
                    "1" -> R.drawable.img_default_user2
                    "2" -> R.drawable.img_default_user3
                    "3" -> R.drawable.img_default_user4
                    else -> R.drawable.img_default_user1
                }
                binding.friendDeleteProfile.setImageResource(imageResId)


                binding.friendDeleteTvName.text = friend.name
                binding.friendDeleteTvStatus.text = friend.introduce

                binding.friendDeleteActionBtn.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        val friend = friendList[adapterPosition]
                        /**API 호출**/

                        //로직에서 제거
                        onDeleteClicked(friend.id, friend.name, adapterPosition)
                    }
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemFriendDeleteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendDeleteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FriendDeleteViewHolder).bind(friendList[position])
    }

    override fun getItemCount(): Int {
        return friendList.size

    }


}