package com.example.language.ui.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.language.databinding.DialogPictureSelectBinding
import androidx.core.graphics.drawable.toDrawable

class PictureSelectDialogFragment : DialogFragment() {

    private var _binding: DialogPictureSelectBinding? = null
    private val binding get() = _binding!!

    interface OnPictureSelectListener {
        fun onGallerySelected()
        fun onCameraSelected()
    }

    private var listener: OnPictureSelectListener? = null

    fun setListener(listener: OnPictureSelectListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = DialogPictureSelectBinding.inflate(inflater, container, false)

        // 다이얼로그 기본 타이틀바와 배경 제거
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.getFromAlbumBtn.setOnClickListener {
            listener?.onGallerySelected()
            dismiss()
        }

        binding.getFromCameraBtn.setOnClickListener {
            listener?.onCameraSelected()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        val window = dialog?.window ?: return

        // 1. 기기의 화면 너비 픽셀 값을 가져옵니다.
        val displayMetrics = requireContext().resources.displayMetrics

        // 2. 화면 너비의 80%로 너비를 계산합니다. (0.8f = 80%)
        //    (0.9f = 90% 등 원하는 비율로 조절 가능)
        val width = (displayMetrics.widthPixels * 0.8f).toInt()

        // 3. 다이얼로그의 너비는 계산된 값(width)으로, 높이는 내용물에 맞게(WRAP_CONTENT) 설정
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}