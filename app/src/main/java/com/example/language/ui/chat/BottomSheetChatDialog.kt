package com.example.language.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.language.databinding.DialogBottomChatmenuBinding
import com.example.language.databinding.DialogBottomStudyinfoBinding
import com.example.language.ui.study.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetChatDialog : BottomSheetDialogFragment(){

    private lateinit var binding: DialogBottomChatmenuBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogBottomChatmenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    companion object {
        const val TAG = "BottomSheetChatDialog"
        fun newInstance() = BottomSheetChatDialog()
    }

}