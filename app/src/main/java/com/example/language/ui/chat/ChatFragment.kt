package com.example.language.ui.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.R
import com.example.language.adapter.ChatAdapter
import com.example.language.data.ChatMessage
import com.example.language.databinding.FragmentChatBinding
import com.example.language.ui.study.BottomSheetDialog

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerView()
        setInputListeners()
        setMicMode() //초기는 editText = 없음

    }


    //recylcerview 셋업
    private fun setRecyclerView(){
        chatAdapter = ChatAdapter(messageList)
        binding.chatRecyclerview.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true // 메시지가 아래쪽에서 위로 쌓이도록 설정
            }
            adapter = chatAdapter
        }

    }

    //editText 및 버튼들 감지
    private fun setInputListeners(){
        //EditText 내용 변화 감지 -> 버튼 유무
        binding.chatTextEdt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    setMicMode()
                } else {
                    setSendMode()
                }
            }
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
        })

        //보내기/녹음 버튼 클릭 리스너
        binding.chatSendBtn.setOnClickListener {
            val text = binding.chatTextEdt.text.toString()
            //버튼 누를 때 내용이 있으면 텍스트 보내고, 아니면 녹음 시작
            if (text.isNotBlank()) {
                sendUserMessage(text) //텍스트 보내기
            } else {
                startVoiceRecording() //녹음 시작
            }
        }

        //메뉴 버튼 클릭 리스너
        binding.chatMenuBtn.setOnClickListener {
            showBottomSheetDialog()
        }
    }


    //텍스트 전송 함수
    private fun sendUserMessage(text: String){
        //현재 메시지를 넣어서 IN
        val message = ChatMessage(System.currentTimeMillis(), text, true, System.currentTimeMillis().toString())
        chatAdapter.addMessage(message)

        //RecyclerView를 가장 아래로 스크롤
        binding.chatRecyclerview.scrollToPosition(messageList.size - 1)

        // 입력창 초기화
        binding.chatTextEdt.text.clear()

        addBotResponse("응답입니다.")

    }

    //챗봇 응답 함수(임시)
    private fun addBotResponse(text: String) {
        val botMessage = ChatMessage(System.currentTimeMillis(), text, false, System.currentTimeMillis().toString())
        chatAdapter.addMessage(botMessage)
        binding.chatRecyclerview.scrollToPosition(messageList.size - 1)
    }



    //녹음 시작 함수(후에 가져오기)
    private fun startVoiceRecording(){

    }

    //bottomSheetDialog 띄우기
    private fun showBottomSheetDialog(){
        val bottomSheet = BottomSheetChatDialog.newInstance()
        bottomSheet.show(childFragmentManager, BottomSheetChatDialog.TAG)
    }

    //이미지 아이콘 변환
    private fun setSendMode() {
        binding.chatSendBtn.setImageResource(R.drawable.ic_search)
    }

    private fun setMicMode() {
        binding.chatSendBtn.setImageResource(R.drawable.ic_mic)
    }

}