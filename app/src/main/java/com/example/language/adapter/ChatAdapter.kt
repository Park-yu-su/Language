package com.example.language.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.language.data.ChatMessage
import com.example.language.databinding.ItemChatLoadingBinding
import com.example.language.databinding.ItemChatMessageBotBinding
import com.example.language.databinding.ItemChatMessageUserBinding
import io.noties.markwon.Markwon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val messages: MutableList<ChatMessage>, private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //타입 (1) = 유저 / (2) = 봇 / (3) = 로딩
    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_BOT = 2
    private val VIEW_TYPE_LOADING = 3
    private val timeFormat = SimpleDateFormat("a h:mm", Locale.getDefault())

    //마크다운 객체 생성
    private val markdown = Markwon.create(context)

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
    //여기에 Markdown 처리를 하자.
    inner class BotViewHolder(private val binding: ItemChatMessageBotBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage, timeString: String) {

            markdown.setMarkdown(binding.chatMessageTv, message.text)
            //binding.chatMessageTv.text = message.text
            binding.timestamp.text = timeString
            binding.timestamp.visibility = View.INVISIBLE
        }
    }

    //뷰홀더 : 로딩
    inner class LoadingViewHolder(private val binding: ItemChatLoadingBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }


    //(추가)메시지 타입에 따라 뷰 타입 결정
    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.isLoading -> VIEW_TYPE_LOADING
            message.isUser -> VIEW_TYPE_USER
            else -> VIEW_TYPE_BOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType){
            //유저
            VIEW_TYPE_USER -> {
                val binding = ItemChatMessageUserBinding.inflate(inflater, parent, false)
                UserViewHolder(binding)
            }
            //봇
            VIEW_TYPE_BOT -> {
                val binding = ItemChatMessageBotBinding.inflate(inflater, parent, false)
                BotViewHolder(binding)
            }
            //로딩
            VIEW_TYPE_LOADING -> {
                val binding = ItemChatLoadingBinding.inflate(inflater, parent, false)
                LoadingViewHolder(binding)
            }
            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
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

    //로딩 추가/제거 함수
    fun showLoading() {
        val loadingMessage = ChatMessage(0, "", false, System.currentTimeMillis().toString(), true)
        messages.add(loadingMessage)
        notifyItemInserted(messages.size - 1)
    }

    fun hideLoading() {
        if (messages.isNotEmpty() && messages.last().isLoading) {
            val position = messages.size - 1
            messages.removeAt(position)
            notifyItemRemoved(position)
        }
    }



}