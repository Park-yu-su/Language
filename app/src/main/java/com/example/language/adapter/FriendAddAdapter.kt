package com.example.language.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.language.R
import com.example.language.data.FriendData
import com.example.language.databinding.ItemFriendAddBinding

//친구 검색 시 나오는 recyclerAdapter
class FriendAddAdapter(
    private var searchList: MutableList<FriendData>,
    private var onRequestClick: (FriendData) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //앞으로 onBindViewHolder에서 하는 대신 bind에서 처리
    inner class AddViewHolder(private val binding: ItemFriendAddBinding) :
            RecyclerView.ViewHolder(binding.root) {
                //데이터 바인딩
                fun bind(item: FriendData) {
                    //이미지
                    /*
                    Glide.with(itemView)
                        .load(friend.profileImage)
                        .placeholder(R.drawable.ic_friend_profile)
                        .error(R.drawable.ic_friend_profile)
                        .circleCrop()
                        .into(binding.friendProfile)
        
                     */
                    
                    binding.friendAddTvName.text = item.name
                    binding.friendAddTvStatus.text = item.introduce
                
                    //일단 버튼 누를 시 뿅
                    binding.friendAddAddBtn.setOnClickListener { 
                        //일단 콜백함수로 빼서 처리
                        onRequestClick(item)

                        //일단 FriendData 부분에 isRequest 부분을 추가해서 누르면 더 이상 요청 안되게 막기
                        binding.friendAddAddBtn.isEnabled = false
                        binding.friendAddAddBtn.setImageResource(R.color.colorBellIcon)

                    }
                
                }
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemFriendAddBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AddViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AddViewHolder).bind(searchList[position])
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

}