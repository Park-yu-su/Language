package com.example.language.ui.chat

import android.Manifest
import android.R.attr.repeatCount
import android.R.attr.repeatMode
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.R
import com.example.language.adapter.ChatAdapter
import com.example.language.api.ApiResponse
import com.example.language.api.chat.ChatRepository
import com.example.language.api.chat.viewModel.ChatViewModel
import com.example.language.api.chat.viewModel.ChatViewModelFactory
import com.example.language.api.friend.FriendRepository
import com.example.language.api.friend.viewModel.FriendViewModel
import com.example.language.api.friend.viewModel.FriendViewModelFactory
import com.example.language.api.login.UserPreference
import com.example.language.data.ChatMessage
import com.example.language.databinding.FragmentChatBinding
import com.example.language.ui.home.MainActivity
import com.example.language.ui.study.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ChatFragment : Fragment(), ChatMenuListener {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatAdapter
    //private val messageList = mutableListOf<ChatMessage>()

    //viewModel
    private val chatRepository = ChatRepository()
    private val chatViewModel: ChatViewModel by activityViewModels() {
        ChatViewModelFactory(chatRepository)
    }

    private lateinit var userPreference : UserPreference


    //녹음 관련
    private var audioRecord: AudioRecord? = null
    private var isRecord = false
    private lateinit var myVoice: String
    private var recordJob: Job? = null
    //녹음 권한 요청 result
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                //권한 허용
                startVoiceRecording()
            } else {
                //권한이 거부
                Toast.makeText(requireContext(), "녹음 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    //애니메이션
    private lateinit var animation: Animation

    //온보딩 메시지
    private val onboardingMessages = listOf(
        "반가워요! 영어 공부가 막막할 때 제가 도와드릴게요 :D",
        "반가워요! 녹음 기능을 통해 간단한 토크를 진행해봐요!",
        "틀려도 괜찮아요! 마이크를 켜고 자신 있게 말해보세요.",
        "오늘 배운 단어, 저랑 같이 복습해 볼까요?",
        "안녕하세요!👋 당신만의 AI 영어 튜터입니다."
    )


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

        (activity as MainActivity).setUIVisibility(false)
        userPreference = UserPreference(requireContext())


        setRecyclerView() //챗봇 recylcer
        setInputListeners() //버튼 클릭 리스너
        setMicMode() //초기는 editText = 없음

        observeSessionID()
        getChatSessionID()
        observeAIResponse()
        observeSTTResult()
        observeBusinessTalk()


        //강제 패딩 제거
        (activity as MainActivity).binding.mainFragmentContainer.setPadding(0, 0, 0, 0)
        animation = AnimationUtils.loadAnimation(requireContext(), R.anim.chat_scale_change)
        //녹음이 저장될 내 파일 경로 초기화
        myVoice = requireContext().getExternalFilesDir(null)?.absolutePath + "/voice.wav"
        

    }


    /**API 관련**/
    //챗봇 대화를 위한 Session ID 생성
    private fun getChatSessionID(){
        //이전 session ID가 없으면
        if(chatViewModel.sessionId == ""){
            var uid = userPreference.getUid() ?: "0"
            var chatName = "${getTodayDate()} 영어 채팅방"
            chatViewModel.startSession(requireContext(), uid.toInt(), chatName)
        }
    }
    //sessionId observe
    private fun observeSessionID(){
        chatViewModel.startSessionResult.observe(viewLifecycleOwner){ response ->
            when(response){
                is ApiResponse.Success -> {
                    Log.d("log_chat", "채팅방 SessionId 받아오기 성공")
                    var sessionId = response.data.sessionId
                    chatViewModel.sessionId = sessionId
                    
                    //만약 내용이 없으면 내용 생성
                    if(chatViewModel.messageList.size == 0){

                        val randomMessage = onboardingMessages.random()
                        addBotResponse(randomMessage)
                    }
                    
                }
                is ApiResponse.Error -> {
                    Log.d("log_chat", "채팅방 SessionId 받아오기 실패")
                    Toast.makeText(requireContext(), "채팅방 생성 실패", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
    //observe 대화 결과
    private fun observeAIResponse(){
        chatViewModel.chatInputResult.observe(viewLifecycleOwner){ response ->
            //로딩 왔으니 제거
            chatAdapter.hideLoading()

            response?.let {
                when(response){
                    is ApiResponse.Success -> {
                        Log.d("log_chat", "응답 받아오기 성공")
                        var message = response.data.response
                        addBotResponse(message)
                    }
                    is ApiResponse.Error -> {
                        Log.d("log_chat", "응답 받아오기 실패")
                        Toast.makeText(requireContext(), "오류 발생", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }

                chatViewModel.clearLiveData()

            }

        }
    }
    private fun observeBusinessTalk(){
        chatViewModel.businessTalkResult.observe(viewLifecycleOwner) { response ->

            chatAdapter.hideLoading()

            response?.let {
                when(response){
                    is ApiResponse.Success -> {
                        var message = response.data.response.response
                        var feedback = response.data.response.feedback
                        addBotResponse(message, feedback)
                    }
                    is ApiResponse.Error -> {
                        Log.d("log_chat", "응답 받아오기 실패")
                        Toast.makeText(requireContext(), "오류 발생", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }

                chatViewModel.clearLiveData()
            }


        }
    }


    
    //녹음 결과 observe
    private fun observeSTTResult(){
        chatViewModel.sttResult.observe(viewLifecycleOwner){ response ->
            response?.let{
                when(response){
                    is ApiResponse.Success -> {
                        Log.d("log_chat", "STT 성공")
                        var text = response.data.message

                        /**이제 여기서 채팅방에 내용 출력 후 send**/
                        val uid = userPreference.getUid() ?: "0"
                        val message = ChatMessage(System.currentTimeMillis(), text,
                            true, System.currentTimeMillis().toString())
                        chatAdapter.addMessage(message)

                        //API 전송 전 로딩 표시
                        chatAdapter.showLoading()
                        binding.chatRecyclerview.scrollToPosition(chatAdapter.itemCount - 1)

                        chatViewModel.businessTalk(requireContext(), uid.toInt(), chatViewModel.sessionId, text)


                    }
                    is ApiResponse.Error -> {
                        Toast.makeText(requireContext(), "녹음 중 문제 발생", Toast.LENGTH_SHORT).show()
                        Log.d("log_chat", "STT 실패")
                    }
                    else -> {}
                }
                chatViewModel.clearSTT()
            }
            
        }
    }



    /**로직 관련**/
    //recylcerview 셋업
    private fun setRecyclerView(){
        chatAdapter = ChatAdapter(chatViewModel.messageList, requireContext())
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
        binding.chatRecyclerview.scrollToPosition(chatViewModel.messageList.size - 1)

        //입력창 초기화
        binding.chatTextEdt.text.clear()

        //API 전송 전 로딩 표시
        chatAdapter.showLoading()
        binding.chatRecyclerview.scrollToPosition(chatAdapter.itemCount - 1)

        //전송 
        val uid = userPreference.getUid() ?: "0"
        chatViewModel.chatInput(requireContext(), uid.toInt(), chatViewModel.sessionId, text)

    }

    //챗봇 응답 함수(임시)
    private fun addBotResponse(text: String) {
        val botMessage = ChatMessage(System.currentTimeMillis(), text, false, System.currentTimeMillis().toString())
        chatAdapter.addMessage(botMessage)
        binding.chatRecyclerview.scrollToPosition(chatViewModel.messageList.size - 1)
    }

    private fun addBotResponse(message: String, feedback: String) {

        var text = ""
        if(feedback == "" || feedback.isEmpty()){
            text = message
        }
        else{
            text = "$message\n\n --- \n **피드백 사항** \n\n$feedback"
        }

        val botMessage = ChatMessage(System.currentTimeMillis(), text, false, System.currentTimeMillis().toString())
        chatAdapter.addMessage(botMessage)
        binding.chatRecyclerview.scrollToPosition(chatViewModel.messageList.size - 1)
    }

    //bottomSheetDialog 띄우기
    private fun showBottomSheetDialog(){
        val bottomSheet = BottomSheetChatDialog.newInstance()
        bottomSheet.show(childFragmentManager, BottomSheetChatDialog.TAG)
    }

    //콜백함수를 여기서 오버라이딩해서 구현 (bottomSheetDialog + ChatCallBackClass의 함수)
    override fun onFeatureSelected(feature: ChatFeature) {
        //리뷰
        if(feature == ChatFeature.REVIEW_WORDS){
            //현재 메시지를 넣어서 IN
            val message = ChatMessage(System.currentTimeMillis(), "오늘 배운 단어 리뷰",
                true, System.currentTimeMillis().toString())
            chatAdapter.addMessage(message)

            //RecyclerView를 가장 아래로 스크롤
            binding.chatRecyclerview.scrollToPosition(chatViewModel.messageList.size - 1)

            //API 전송 전 로딩 표시
            chatAdapter.showLoading()
            binding.chatRecyclerview.scrollToPosition(chatAdapter.itemCount - 1)

            //전송
            val uid = userPreference.getUid() ?: "0"
            chatViewModel.getTodayReview(requireContext(), uid.toInt(), chatViewModel.sessionId)
        }
        //예문
        else if(feature == ChatFeature.CREATE_EXAMPLE){
            //현재 메시지를 넣어서 IN
            val message = ChatMessage(System.currentTimeMillis(), "예문 생성",
                true, System.currentTimeMillis().toString())
            chatAdapter.addMessage(message)

            //RecyclerView를 가장 아래로 스크롤
            binding.chatRecyclerview.scrollToPosition(chatViewModel.messageList.size - 1)

            //API 전송 전 로딩 표시
            chatAdapter.showLoading()
            binding.chatRecyclerview.scrollToPosition(chatAdapter.itemCount - 1)

            //전송
            val uid = userPreference.getUid() ?: "0"
            chatViewModel.getExample(requireContext(), uid.toInt(), chatViewModel.sessionId)

        }

        //레포트 출력
        else if(feature == ChatFeature.CREATE_HAKSUPBUNSUK){
            val message = ChatMessage(System.currentTimeMillis(), "학습 레포트 출력"
            , true, System.currentTimeMillis().toString())
            chatAdapter.addMessage(message)

            binding.chatRecyclerview.scrollToPosition(chatViewModel.messageList.size - 1)

            //API 전송 전 로딩 표시
            chatAdapter.showLoading()
            binding.chatRecyclerview.scrollToPosition(chatAdapter.itemCount - 1)

            //전송
            val uid = userPreference.getUid() ?: "0"
            chatViewModel.getReport(requireContext(), uid.toInt(), chatViewModel.sessionId)

        }

    }

    //이미지 아이콘 변환
    private fun setSendMode() {
        binding.chatSendBtn.setImageResource(R.drawable.ic_send)
    }

    private fun setMicMode() {
        binding.chatSendBtn.setImageResource(R.drawable.ic_mic_chat)
    }

    //녹음 시작 함수(후에 가져오기)
    private fun startVoiceRecording(){
        //이미 권한 OK!
        if(ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){

            //녹음 로직 수행
            Log.d("log_chat", "녹음 시작")

            //0. 애니 시작
            startAuraAnimation()
            binding.chatSendBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.charPressColor))
            binding.chatSendBtn.isEnabled = false


            //1. 녹음 환경 세팅(기존 HZ그대로 유지)
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            )


            //2. 녹음
            audioRecord!!.startRecording()
            isRecord = true

            //3. 녹음을 위한 코루틴 및 비동기 실행
            recordJob = lifecycleScope.launch(Dispatchers.IO){
                try{
                    //버퍼를 통해 바이트 단위로 저장한다.
                    BufferedOutputStream(FileOutputStream(myVoice)).use { outputStream ->
                        val buffer = ByteArray(audioRecord?.bufferSizeInFrames ?: 0)
                        while (isActive) {
                            val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                            if (read > 0) {
                                outputStream.write(buffer, 0, read)
                            }
                        }
                    }
                } catch (e: Exception){
                    e.printStackTrace()
                } finally {
                    // 녹음 중지 및 자원 해제
                    audioRecord?.stop()
                    audioRecord?.release()
                    audioRecord = null
                    Log.d("log_chat", "녹음 코루틴 종료 및 자원 해제")
                }
            }

            //4. Delay로 멈추기
            lifecycleScope.launch(Dispatchers.Main){
                delay(7000)
                stopRecord()
            }


        }
        else{
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }


    //녹음 종료 및 로직 처리
    private fun stopRecord(){
        if (audioRecord != null) {
            Log.d("log_chat", "녹음 종료")
            //1. 코루틴에게 녹음 중지 요청(job) -> isActive = False
            recordJob?.cancel()

            isRecord = false

            //1.5. 애니메이션 stop
            stopAuraAnimation()
            binding.chatSendBtn.isEnabled = true
            binding.chatSendBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.chatColor))

            //2. 파일로 저장
            val recordFile = File(myVoice)
            if(recordFile.exists()){
                //wav 파일 path 생성
                val myVoiceConvert = requireContext().getExternalFilesDir(null)?.absolutePath + "/voice_converted.wav"
                convertPcmToWav(myVoice, myVoiceConvert)
                val recordedFileConvert = File(myVoiceConvert)

                //이 이후는 서버한테 보내는 로직
                sendWavForSTT(recordedFileConvert)
                //playWavFile(recordedFileConvert, requireContext())
            }

        }
    }

    //음원(wav)을 송수신하는 코드
    private fun sendWavForSTT(wavFile: File){
        val wavBytes = wavFile.readBytes()
        val fileName = wavFile.name

        chatViewModel.doSTT(requireContext(), wavBytes, fileName)
    }

    /******************************************************************************/

    //오늘 날짜 출력
    fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Date()

        return dateFormat.format(currentDate)
    }


    //애니메이션 시작
    private fun startAuraAnimation() {
        Log.d("log_chat", "애니메이션 start")
        binding.chatSendAnimateView.visibility = View.VISIBLE
        binding.chatSendAnimateView.startAnimation(animation)
        // 배경색을 동적으로 변경하여 웅웅거리는 효과 추가 (선택 사항)
        (binding.chatSendAnimateView.background as? GradientDrawable)?.setColor(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        )

    }

    //애니메이션 종료
    private fun stopAuraAnimation() {
        Log.d("log_chat", "애미네이션 종료")
        binding.chatSendAnimateView.visibility = View.INVISIBLE
        binding.chatSendAnimateView.clearAnimation()
    }



    //PCM -> WAW 파일로 바꾸는 코드
    @Throws(IOException::class)
    private fun convertPcmToWav(pcmPath: String, wavPath: String) {
        val pcmFile = File(pcmPath)
        val wavFile = File(wavPath)

        FileInputStream(pcmFile).use { pcmInputStream ->
            FileOutputStream(wavFile).use { wavOutputStream ->
                val buffer = ByteArray(audioRecord?.bufferSizeInFrames ?: 0)
                var bytesRead: Int

                val totalAudioLen = pcmInputStream.channel.size()
                val totalDataLen = totalAudioLen + 36
                val byteRate = 16000L * 2 * 1
                writeWavHeader(wavOutputStream, totalAudioLen, totalDataLen, 16000, 1, byteRate)

                while (pcmInputStream.read(buffer).also { bytesRead = it } != -1) {
                    wavOutputStream.write(buffer, 0, bytesRead)
                }
            }
        }
    }

    //파일 앞에 WAV 해더를 삽입
    @Throws(IOException::class)
    private fun writeWavHeader(
        out: FileOutputStream, totalAudioLen: Long, totalDataLen: Long,
        sampleRate: Int, channels: Int, byteRate: Long
    ) {
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (2 * 1).toByte()
        header[33] = 0
        header[34] = 16
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()
        out.write(header, 0, 44)
    }

    //녹음한 파일을 재생하는 코드
    private fun playWavFile(file: File, context: Context) {
        // MediaPlayer 객체 생성
        Log.d("log_chat", "녹음본 재생")
        val mediaPlayer = MediaPlayer()

        try {
            // 파일 경로 설정
            mediaPlayer.setDataSource(file.absolutePath)

            // 재생 준비 (비동기 방식)
            mediaPlayer.prepareAsync()

            // 재생 준비가 완료되면 실행될 리스너
            mediaPlayer.setOnPreparedListener { mp ->
                mp.start()
            }

            // 재생이 완료되면 실행될 리스너
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release() // 재생이 끝나면 MediaPlayer 리소스 해제
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // 오류 발생 시에도 리소스 해제
            mediaPlayer.release()
            Toast.makeText(context, "오디오 재생 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }


}