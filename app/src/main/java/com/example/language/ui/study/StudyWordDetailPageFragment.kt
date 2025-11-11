package com.example.language.ui.study

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.example.language.R
import com.example.language.data.WordData
import com.example.language.databinding.FragmentStudyWordDetailPageBinding
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class StudyWordDetailPageFragment : Fragment() {
    private lateinit var wordData: WordData

    private lateinit var binding: FragmentStudyWordDetailPageBinding
    private lateinit var textToSpeech: TextToSpeech
    //viewPager에서 받아온 단어 보여주기

    private var currentPosition: Int = 0 //현재 위치
    private var totalWordsCount: Int = 0 //총 개수


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            wordData = it.getParcelable<WordData>(ARG_WORD_DATA)!!
            currentPosition = it.getInt(ARG_POSITION)
            totalWordsCount = it.getInt(ARG_TOTAL_COUNT)
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

        //TTS 세팅
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
            }
        }

        //해석 개수에 따라 세팅
        var tmpMean = ""
        if(wordData.meanings.get(0) != "") {
            tmpMean += wordData.meanings.get(0).toString()
        }
        if(wordData.meanings.get(1) != "") {
            tmpMean += " / "
            tmpMean += wordData.meanings.get(1).toString()
        }
        if(wordData.meanings.get(2) != "") {
            tmpMean += " / "
            tmpMean += wordData.meanings.get(2).toString()

        }
        if(wordData.meanings.get(3) != "") {
            tmpMean += " / "
            tmpMean += wordData.meanings.get(3).toString()
        }


        binding.worddetailEnglishTv.text = wordData.word
        binding.worddetailJinhangTv.text = "${currentPosition}/${totalWordsCount}"
        binding.worddetailMeaningTv.text = tmpMean
        binding.worddetailExampleTv.text = wordData.example

        binding.worddetailListenBtn.setOnClickListener {
            val nowWord = binding.worddetailEnglishTv.text.toString()
            binding.worddetailListenBtn.startAnimation(
                AnimationUtils.loadAnimation(requireContext(), R.anim.button_pop))

            textToSpeech.speak(nowWord, TextToSpeech.QUEUE_FLUSH, null, null)

        }

    }

    companion object {
        private const val ARG_WORD_DATA = "word_data"
        private const val ARG_POSITION = "word_position"
        private const val ARG_TOTAL_COUNT = "total_count"

        @JvmStatic
        fun newInstance(word : WordData, position: Int, totalCount: Int) =
            StudyWordDetailPageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_WORD_DATA, word)
                    putInt(ARG_POSITION, position)
                    putInt(ARG_TOTAL_COUNT, totalCount)
                }
            }
    }
}