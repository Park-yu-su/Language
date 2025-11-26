package com.example.language.ui.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.example.language.R
import com.example.language.databinding.DialogBottomChatmenuBinding
import com.example.language.databinding.DialogBottomStudyinfoBinding
import com.example.language.ui.study.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetChatDialog : BottomSheetDialogFragment(){

    private lateinit var binding: DialogBottomChatmenuBinding
    private var listener: ChatMenuListener? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogBottomChatmenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(parentFragment is ChatMenuListener){
            listener = parentFragment as ChatMenuListener
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var popAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.button_pop)

        //오늘 배운 단어 리뷰
        binding.dialogYoyakBtn.setOnClickListener {
            binding.dialogYoyakBtn.startAnimation(popAnim)
            listener?.onFeatureSelected(ChatFeature.REVIEW_WORDS)
            dismiss()
        }
        //예문 출력
        binding.dialogEyemunBtn.setOnClickListener {
            binding.dialogEyemunBtn.startAnimation(popAnim)
            listener?.onFeatureSelected(ChatFeature.CREATE_EXAMPLE)
            dismiss()
        }
        //레포트 작성
        binding.dialogHaksubbunsukBtn.setOnClickListener {
            binding.dialogHaksubbunsukBtn.startAnimation(popAnim)
            listener?.onFeatureSelected(ChatFeature.CREATE_HAKSUPBUNSUK)
            dismiss()
        }

        

    }


    companion object {
        const val TAG = "BottomSheetChatDialog"
        fun newInstance() = BottomSheetChatDialog()
    }

}