package com.example.language.ui.makeVoc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.language.R
import com.example.language.databinding.FragmentAddNewVocBinding

class AddNewVocFragment : Fragment() {

    private var _binding: FragmentAddNewVocBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddNewVocBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vocNameEt = binding.vocNameEt
        val addNewVocWithNameBtn = binding.addNewVocWithNameBtn
        addNewVocWithNameBtn.setOnClickListener {
            val action = AddNewVocFragmentDirections.
            actionAddNewVocFragmentToSelectWayAddVocFragment(vocNameEt.text.toString())
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}