package com.example.language.ui.test

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.language.R
import com.example.language.data.WordData
import com.example.language.databinding.FragmentTestSpeakingBinding
import com.example.language.ui.home.MainActivity
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
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class TestSpeakingFragment : Fragment() {


    private lateinit var binding: FragmentTestSpeakingBinding

    //녹음 관련 변수
    private var audioRecord: AudioRecord? = null
    private var isRecord = false
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var myVoice: String
    private var recordJob: Job? = null

    //현재 단어 관련 변수
    //더미 데이터
    private var tmpData = mutableListOf(
        WordData("APPLE", mutableListOf("사과"), "An apple a day keeps the doctor away."),
        WordData("EFFICIENT", mutableListOf("효율적인"), "We need an efficient solution."),
        WordData("PROGRAMMING", mutableListOf("프로그래밍"), "I love programming."),
        WordData("LANGUAGE", mutableListOf("언어"), "English is a global language."),
        WordData("DEVELOPMENT", mutableListOf("개발"), "Software development is complex.")
    )
    private var nowWordIndex = 0
    private var totalWord = tmpData.size
    private var isLike = false


    //기타 변수
    private lateinit var papAnim : Animation

    //권한 요청 result
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                //권한 허용
                startRecord()
            } else {
                //권한이 거부
                Toast.makeText(requireContext(), "녹음 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTestSpeakingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //상단 하단 제거
        (activity as? MainActivity)?.setUIVisibility(false)

        //애니메이션 로드
        papAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.button_pop)

        //TTS랑 녹음 기능 세팅
        settingTTSwithVoice()

        //UI 세팅
        updateUI()
        binding.speakProgressbar.max = totalWord

        //녹음 버튼
        binding.spaekRecordBtn.setOnClickListener {
            binding.spaekRecordBtn.startAnimation(papAnim)
            startRecord()
        }

        //듣기 버튼(TTS)
        binding.speakListenBtn.setOnClickListener {
            binding.speakListenBtn.startAnimation(papAnim)
            startTTS()
        }

        //다음 단어 버튼
        binding.speakNextBtn.setOnClickListener {
            binding.speakNextBtn.startAnimation(papAnim)
            nowWordIndex++
            updateUI()
        }

        //뒤로 가기 버튼
        binding.speakBackBtn.setOnClickListener {
            binding.speakBackBtn.startAnimation(papAnim)
            navigateToHome()
        }

        //좋아 버튼
        binding.speakLikeBtn.setOnClickListener {
            binding.speakLikeBtn.startAnimation(papAnim)
            handleLike()
        }

    }

    //홈 화면 이동
    fun navigateToHome(){
        requireActivity().onBackPressed()
    }

    //좋아요 관리
    fun handleLike(){
        val likeBtn = binding.speakLikeBtn


        //안좋아
        if(!isLike){
            isLike = true
            likeBtn.setImageResource(R.drawable.ic_like_heart)

        }
        else{
            isLike = false
            likeBtn.setImageResource(R.drawable.ic_like_heart2)
        }

    }

    //단어 UI 관련 Handler
    private fun updateUI(){
        //아직 남으면 업뎃
        if(nowWordIndex < totalWord){
            var nowWord = tmpData.get(nowWordIndex)

            //UI 적용
            binding.speakWordTv.text = nowWord.word
            binding.speakProgressTv.text = "${nowWordIndex + 1}/${totalWord}"
            binding.speakProgressbar.progress = nowWordIndex + 1

        }
        //끝나면
        else{
            Toast.makeText(context, "테스트 완료!", Toast.LENGTH_LONG).show()
            navigateToHome()
        }
    }

    //TTS 세팅을 위한 코드 (영단어 발음)
    private fun settingTTSwithVoice() {
        //TTS 객체 초기화
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
            }
        }
        //녹음이 저장될 내 파일 경로 초기화
        myVoice = requireContext().getExternalFilesDir(null)?.absolutePath + "/voice.wav"
    }

    //TTS Start
    private fun startTTS(){
        val text = binding.speakWordTv.text.toString()
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    //녹음 시작
    private fun startRecord(){
        //이미 권한 OK!
        if(ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){

            //녹음 로직 수행
            Log.d("log_speak", "녹음 시작")
            //1. 녹음 환경 세팅(기존 HZ그대로 유지)
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            )

            binding.spaekRecordBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.Main1_1))
            binding.spaekRecordBtn.setImageResource(R.drawable.ic_mic_recording)
            binding.spaekRecordBtn.isEnabled = false
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
                    Log.d("log_speak", "녹음 코루틴 종료 및 자원 해제")
                }
            }

            //4. Delay로 멈추기
            lifecycleScope.launch(Dispatchers.Main){
                delay(3000)
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
            Log.d("log_speak", "녹음 종료")
            //1. 코루틴에게 녹음 중지 요청(job) -> isActive = False
            recordJob?.cancel()

            binding.spaekRecordBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            binding.spaekRecordBtn.setImageResource(R.drawable.ic_mic)
            binding.spaekRecordBtn.isEnabled = true
            isRecord = false


            //2. 파일로 저장
            val recordFile = File(myVoice)
            if(recordFile.exists()){
                //wav 파일 path 생성
                val myVoiceConvert = requireContext().getExternalFilesDir(null)?.absolutePath + "/voice_converted.wav"
                convertPcmToWav(myVoice, myVoiceConvert)
                val recordedFileConvert = File(myVoiceConvert)

                //이 이후는 서버한테 보내는 로직
                playWavFile(recordedFileConvert, requireContext())
            }

        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.setUIVisibility(true)
    }


}