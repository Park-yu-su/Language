package com.example.language.ui.test

import android.os.Bundle
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
import com.example.language.adapter.VocListAdapter
import com.example.language.api.ApiResponse
import com.example.language.api.login.UserPreference
import com.example.language.api.study.StudyRepository
import com.example.language.api.study.viewModel.StudyViewModel
import com.example.language.api.study.viewModel.StudyViewModelFactory
import com.example.language.api.test.TestRepository
import com.example.language.api.test.viewModel.TestViewModel
import com.example.language.api.test.viewModel.TestViewModelFactory
import com.example.language.data.VocData
import com.example.language.data.WordData
import com.example.language.databinding.FragmentTestSelectVocBinding
import com.example.language.ui.study.StudyVoclistFragmentDirections
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TestSelectVocFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TestSelectVocFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentTestSelectVocBinding

    private var vocList : MutableList<VocData> = mutableListOf()
    private lateinit var adapter : VocListAdapter

    //API 연결을 위한 수단
    private val testRepository = TestRepository()
    private val testViewModel: TestViewModel by activityViewModels(){
        TestViewModelFactory(testRepository)
    }

    //유저 UID 가져오기
    private lateinit var userPreference : UserPreference

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
        binding = FragmentTestSelectVocBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference(requireContext())


        //단어장을 터치할 경우, 단어 리스트를 가져오기
        adapter = VocListAdapter(vocList,
            onItemClicked = { data ->
                testViewModel.selectWordbookId = data.wid
                //다음 화면으로 이동
                val action = TestSelectVocFragmentDirections.actionTestSelectVocFragmentToTestSelectModeFragment()
                findNavController().navigate(action)
            })

        binding.selectVocRecyclerview.adapter = adapter
        binding.selectVocRecyclerview.layoutManager = LinearLayoutManager(requireContext())


        observeMyWordbook()
        getMyWordbook()

    }


    //내 단어장 가져오기
    private fun getMyWordbook(){
        var stringUid = userPreference.getUid() ?: "0"
        var uid = stringUid.toInt()
        testViewModel.getSubscribedWordbooks(requireContext(), uid)
    }
    //내 단어장 리스트 observe
    private fun observeMyWordbook(){
        testViewModel.wordbookListResult.observe(viewLifecycleOwner) {response ->
            when(response){
                is ApiResponse.Success -> {
                    vocList.clear()
                    Log.d("log_test", "단어장 리스트 불러오기 성공 : ${response.data}")

                    val words = response.data.data
                    for(word in words){
                        vocList.add(VocData(word.wid, word.title, word.tags, ""))
                    }
                    adapter.notifyDataSetChanged()


                }
                is ApiResponse.Error -> {
                    Log.d("log_test", "실패 : ${response.message}")
                    Toast.makeText(context, "단어장 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
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
         * @return A new instance of fragment TestSelectVocFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TestSelectVocFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}