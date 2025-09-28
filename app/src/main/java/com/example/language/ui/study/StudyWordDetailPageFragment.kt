package com.example.language.ui.study

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.language.R
import com.example.language.data.WordData
import com.example.language.databinding.FragmentStudyWordDetailPageBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class StudyWordDetailPageFragment : Fragment() {
    private lateinit var wordData: WordData

    private lateinit var binding: FragmentStudyWordDetailPageBinding
    //viewPager에서 받아온 단어 보여주기


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            wordData = it.getParcelable<WordData>(ARG_WORD_DATA)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStudyWordDetailPageBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.worddetailEnglishTv.text = wordData.word
        binding.worddetailMeaningTv.text = wordData.meanings.toString()
        binding.worddetailExampleTv.text = wordData.example

    }

    companion object {
        private const val ARG_WORD_DATA = "word_data"

        @JvmStatic
        fun newInstance(word : WordData) =
            StudyWordDetailPageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_WORD_DATA, word)
                }
            }
    }
}