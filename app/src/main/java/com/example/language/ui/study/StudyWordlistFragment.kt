package com.example.language.ui.study

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.R
import com.example.language.adapter.WordListAdapter
import com.example.language.api.ApiResponse
import com.example.language.api.login.UserPreference
import com.example.language.api.study.StudyRepository
import com.example.language.api.study.viewModel.StudyViewModel
import com.example.language.api.study.viewModel.StudyViewModelFactory
import com.example.language.data.WordData
import com.example.language.databinding.FragmentStudyWordlistBinding
import com.example.language.ui.home.MainActivity
import java.util.Locale
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudyWordlistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudyWordlistFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    //TTS
    private lateinit var textToSpeech: TextToSpeech

    private lateinit var binding: FragmentStudyWordlistBinding

    private var wordList : MutableList<WordData> = mutableListOf()


    //API 연결을 위한 수단
    private val studyRepository = StudyRepository()
    private val studyViewModel: StudyViewModel by activityViewModels(){
        StudyViewModelFactory(studyRepository)
    }
    //유저 UID 가져오기
    private lateinit var userPreference : UserPreference
    private lateinit var adapter : WordListAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStudyWordlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //상단 바 제거
        (activity as? MainActivity)?.setUIVisibility(false)

        //TTS 세팅
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
            }
        }

        userPreference = UserPreference(requireContext())

        //단어 가져와
        observeWordList()
        getWordsById()



        adapter = WordListAdapter(wordList,
            onItemClicked = { word, pos ->
                //현재 누른 단어장의 위치 세팅
                studyViewModel.selectWordIndex = pos
                val action = StudyWordlistFragmentDirections.actionStudyWordlistFragmentToStudyWordDetailFragment()
                findNavController().navigate(action)
        },
            onTTSRequest = { word->
                textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        )
        binding.studyWordRecyclerview.adapter = adapter
        binding.studyWordRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        binding.studyWordBackBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }

    //단어 리스트 가져오기
    private fun getWordsById(){
        val wid = studyViewModel.selectWordbookId
        studyViewModel.getWordbook(requireContext(), wid)
    }
    //단어 리스트 observe
    private fun observeWordList(){
        studyViewModel.wordListResult.observe(viewLifecycleOwner) { response ->
            when(response){
                is ApiResponse.Success -> {
                    wordList.clear()
                    studyViewModel.selectWordList.clear()
                    Log.d("log_study", "단어장 리스트 불러오기 성공 : ${response.data}")

                    val words = response.data.data
                    for(word in words){
                        var tmpWord = WordData(word.wordId, word.word, word.meanings
                            , word.distractors, word.example)
                        wordList.add(tmpWord)
                        studyViewModel.selectWordList.add(tmpWord)
                    }
                    adapter.notifyDataSetChanged()

                }
                is ApiResponse.Error -> {
                    Log.d("log_study", "실패 : ${response.message}")
                    Toast.makeText(context, "단어 리스트를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }


        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StudyWordlistFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StudyWordlistFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}