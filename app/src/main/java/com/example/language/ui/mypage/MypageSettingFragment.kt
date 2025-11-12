package com.example.language.ui.mypage

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.example.language.databinding.FragmentMypageSettingBinding

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

        val volumeSeekBar = binding.volumeSeekBar
        val volumeTxt = binding.volumeTxt

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
            override fun onStopTrackingTouch(seekBar: SeekBar?) { /* 필요시 구현 */ }
        })

        // 초기 로드 시 텍스트 위치 설정
        // post를 사용하여 레이아웃이 완전히 그려진 후 너비(width)를 가져오도록 함
        volumeSeekBar.post {
            updateTextPosition(volumeSeekBar)
        }

        // 시스템에서 Vibrator 서비스 가져오기
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 (API 31) 이상
            val vibratorManager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            // Android 12 미만 (Deprecated)
            @Suppress("DEPRECATION")
            requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val vibeSwitch = binding.vibeSwitch
        // 초기 상태 설정
        vibeSwitch.isChecked = false

        // 스위치 리스너 설정
        vibeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // --- ON: 기기 진동 실행 ---
                // 진동기가 있는지 확인
                if (vibrator?.hasVibrator() == true) {
                    // 예시: 500ms(0.5초) 동안 한 번 진동
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // Android 8.0 (API 26) 이상
                        val effect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                        vibrator?.vibrate(effect)
                    } else {
                        // Android 8.0 미만 (Deprecated)
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(500)
                    }
                }
            } else {
                // --- OFF: 현재 진행 중인 진동 취소 ---
                vibrator?.cancel()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vibrator?.cancel()
        _binding = null
    }
}