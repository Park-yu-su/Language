package com.example.language.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.language.data.ChatMessage
import com.example.language.databinding.ItemChatMessageBotBinding
import com.example.language.databinding.ItemChatMessageUserBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //타입 (1) = 유저 / (2) = 봇
    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_BOT = 2
    private val timeFormat = SimpleDateFormat("a h:mm", Locale.getDefault())

    //뷰홀더 : 유저 말풍선
    inner class UserViewHolder(private val binding: ItemChatMessageUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage, timeString: String) {
            binding.chatMessageTv.text = message.text
            binding.timestamp.text = timeString
            binding.timestamp.visibility = View.INVISIBLE
        }
    }

    //뷰홀더 : 봇 말풍선
    inner class BotViewHolder(private val binding: ItemChatMessageBotBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage, timeString: String) {
            binding.chatMessageTv.text = message.text
            binding.timestamp.text = timeString
            binding.timestamp.visibility = View.INVISIBLE
        }
    }


    //(추가)메시지 타입에 따라 뷰 타입 결정
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        //유저
        return if (viewType == VIEW_TYPE_USER) {
            val binding = ItemChatMessageUserBinding.inflate(inflater, parent, false)
            UserViewHolder(binding)
        }
        //봇
        else {
            val binding = ItemChatMessageBotBinding.inflate(inflater, parent, false)
            BotViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val timeString = timeFormat.format(Date(message.timestamp.toLong()))

        when (holder) {
            is UserViewHolder -> holder.bind(message, timeString)
            is BotViewHolder -> holder.bind(message, timeString)
        }
    }

    override fun getItemCount(): Int = messages.size

    //만약 새 메시지를 추가할 경우 recyclerview에 notify
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }


}