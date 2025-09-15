package com.example.language.ui.makeVoc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.language.databinding.FragmentAddVocManuallyBinding

class AddVocManuallyFragment : Fragment() {

    private var _binding: FragmentAddVocManuallyBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddVocManuallyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addVocBtn.setOnClickListener {
            // 단어 추가 로직 구현
        }

        binding.addVocFinishBtn.setOnClickListener {
            // 추가 완료 로직 구현
        }
    }

}