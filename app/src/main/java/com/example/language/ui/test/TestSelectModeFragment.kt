package com.example.language.ui.test

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.language.R
import com.example.language.api.ApiResponse
import com.example.language.api.test.TestRepository
import com.example.language.api.test.viewModel.TestViewModel
import com.example.language.api.test.viewModel.TestViewModelFactory
import com.example.language.data.WordData
import com.example.language.databinding.FragmentTestSelectModeBinding
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TestSelectModeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TestSelectModeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentTestSelectModeBinding

    //API 연결을 위한 수단
    private val testRepository = TestRepository()
    private val testViewModel: TestViewModel by activityViewModels(){
        TestViewModelFactory(testRepository)
    }


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
        binding = FragmentTestSelectModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navOptions = NavOptions.Builder()
            // popUpTo: nav_graph_test의 시작점까지 스택을 정리
            .setPopUpTo(R.id.testSelectVocFragment, true)
            // inclusive = true: testSelectVocFragment 자체도 스택에서 제거
            .setLaunchSingleTop(true)
            .build()

        observeWordList()
        getWordsById()

        //버튼 클릭 리스너들
        binding.selectModeVoctestBtn.setOnClickListener {
            val action = TestSelectModeFragmentDirections.actionTestSelectModeFragmentToTestSpeakingFragment()
            findNavController().navigate(action, navOptions)
        }

        binding.selectModeMeaningtestBtn.setOnClickListener {
            val action = TestSelectModeFragmentDirections.actionTestSelectModeFragmentToTestMeaningFragment()
            findNavController().navigate(action, navOptions)
        }

    }


    //단어 리스트 가져오기
    private fun getWordsById(){
        val wid = testViewModel.selectWordbookId
        testViewModel.getWordbook(requireContext(), wid)
    }
    //단어 리스트 observe
    private fun observeWordList(){
        testViewModel.wordListResult.observe(viewLifecycleOwner) { response ->
            when(response){
                is ApiResponse.Success -> {
                    testViewModel.selectWordList.clear()
                    Log.d("log_test", "단어장 리스트 불러오기 성공 : ${response.data}")

                    val words = response.data.data
                    for(word in words){
                        var tmpWord = WordData(word.wordId, word.word, word.meanings
                            , word.distractors, word.example)
                        testViewModel.selectWordList.add(tmpWord)
                    }

                }
                is ApiResponse.Error -> {
                    Log.d("log_test", "실패 : ${response.message}")
                    Toast.makeText(context, "단어 리스트를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }


        }

    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TestSelectModeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TestSelectModeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}