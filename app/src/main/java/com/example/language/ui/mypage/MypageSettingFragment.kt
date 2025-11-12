package com.example.language.ui.mypage

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.navigation.fragment.findNavController
import com.example.language.databinding.FragmentMypageSettingBinding
import com.example.language.ui.mypage.PrefsConstants.PREFS_NAME
import com.example.language.ui.mypage.PrefsConstants.PREF_TTS_VOLUME
import com.example.language.ui.mypage.PrefsConstants.PREF_VIBE_STATUS

class MypageSettingFragment : Fragment() {

    private var _binding: FragmentMypageSettingBinding? = null
    private val binding get() = _binding!!

    // 진동기(Vibrator) 인스턴스 변수
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMypageSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SharedPreferences 인스턴스 가져오기
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        binding.settingBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        val volumeSeekBar = binding.volumeSeekBar
        val volumeTxt = binding.volumeTxt

        val savedVolume = prefs.getInt(PREF_TTS_VOLUME, 0)
        volumeSeekBar.progress = savedVolume

        // 텍스트 위치를 업데이트하는 함수
        val updateTextPosition = { seekBar: SeekBar ->
            // 현재 값으로 텍스트 업데이트
            volumeTxt.text = seekBar.progress.toString()

            // SeekBar 손잡이(thumb)의 위치 계산
            val thumbBounds = seekBar.thumb.bounds

            // TextView가 thumb의 중앙에 오도록 translationX 계산
            val thumbCenterX = thumbBounds.left + thumbBounds.width() / 2
            val textHalfWidth = volumeTxt.width / 2

            volumeTxt.translationX = (thumbCenterX - textHalfWidth).toFloat()
        }

        // SeekBar 리스너 설정
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // seekBar가 null이 아닐 때만 위치 업데이트
                seekBar?.let {
                    updateTextPosition(it)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { /* 필요시 구현 */ }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    prefs.edit().putInt(PREF_TTS_VOLUME, it.progress).apply()
                }
            }
        })

        // 초기 로드 시 텍스트 위치 설정
        // post를 사용하여 레이아웃이 완전히 그려진 후 너비(width)를 가져오도록 함
        volumeSeekBar.post {
            updateTextPosition(volumeSeekBar)
        }

        // 저장된 Vibe 상태 불러오기 (기본값: false/OFF)
        val savedVibeStatus = prefs.getBoolean(PREF_VIBE_STATUS, false)
        binding.vibeSwitch.isChecked = savedVibeStatus

        // Switch 리스너 설정
        binding.vibeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // 상태 변경 시 SharedPreferences에 저장
            prefs.edit().putBoolean(PREF_VIBE_STATUS, isChecked).apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vibrator?.cancel()
        _binding = null
    }
}