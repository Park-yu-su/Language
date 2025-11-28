package com.example.language.ui.mypage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.example.language.R
import com.example.language.databinding.DialogBottomUserImageBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetUserImageDialog : BottomSheetDialogFragment() {

    private var _binding: DialogBottomUserImageBinding? = null
    private val binding get() = _binding!!

    private var listener: OnImageSelectedListener? = null
    private var selectedResId: Int? = null // 선택된 이미지 리소스 ID 저장

    interface OnImageSelectedListener {
        fun onProfileImageUpdated(resId: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (parentFragment is OnImageSelectedListener) {
            listener = parentFragment as OnImageSelectedListener
        } else {
            throw RuntimeException("$parentFragment must implement OnImageSelectedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBottomUserImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageViews = listOf(
            binding.userImg1,
            binding.userImg2,
            binding.userImg3,
            binding.userImg4
        )

        val resourceIds = listOf(
            R.drawable.img_default_user1,
            R.drawable.img_default_user2,
            R.drawable.img_default_user3,
            R.drawable.img_default_user4
        )

        // 이미지 클릭 리스너 설정
        imageViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                // 선택한 이미지 ID 저장
                selectedResId = resourceIds[index]
                // UI 갱신 (선택 효과)
                updateSelectionUI(imageViews, imageView)
            }
        }

        // 취소 버튼
        binding.dialogCancelCdv.setOnClickListener {
            dismiss()
        }

        // 변경 버튼
        binding.dialogOkCdv.setOnClickListener {
            if (selectedResId != null) {
                // 선택된 이미지를 부모에게 전달
                listener?.onProfileImageUpdated(selectedResId!!)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "변경할 프로필 이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 선택된 이미지는 강조하고, 나머지는 흐리게 처리하는 함수
    private fun updateSelectionUI(allViews: List<ImageView>, selectedView: ImageView) {
        allViews.forEach { view ->
            if (view == selectedView) {
                // 선택됨: 완전 불투명 + 약간 확대
                view.alpha = 1.0f
                view.scaleX = 1.1f
                view.scaleY = 1.1f
                // 필요하다면 테두리 추가: view.setBackgroundResource(R.drawable.bg_selection_border)
            } else {
                // 선택 안됨: 반투명 + 원래 크기
                view.alpha = 0.4f
                view.scaleX = 1.0f
                view.scaleY = 1.0f
                view.background = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "BottomSheetUserImageDialog"
        fun newInstance() = BottomSheetUserImageDialog()
    }
}