package com.example.language.ui.study

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.R
import com.example.language.adapter.VocListAdapter
import com.example.language.api.ApiResponse
import com.example.language.api.login.UserPreference
import com.example.language.api.study.StudyRepository
import com.example.language.api.study.viewModel.StudyViewModel
import com.example.language.api.study.viewModel.StudyViewModelFactory
import com.example.language.data.VocData
import com.example.language.databinding.FragmentStudyVoclistBinding
import com.example.language.ui.home.MainActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudyVoclistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudyVoclistFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentStudyVoclistBinding

    private lateinit var adapter: VocListAdapter
    private var vocList : MutableList<VocData> = mutableListOf()

    
    //API 연결을 위한 수단
    private val studyRepository = StudyRepository()
    private val studyViewModel: StudyViewModel by activityViewModels(){
        StudyViewModelFactory(studyRepository)
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
        binding = FragmentStudyVoclistBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setUIVisibility(true)

        (activity as MainActivity).setTopBar("공부하기", false, true)
        (activity as MainActivity).showToprightIcon(true, 1)

        userPreference = UserPreference(requireContext())


        //검색 버튼 관찰 -> 리스트냐 검색이냐
        studyViewModel.searchEventStart.observe(viewLifecycleOwner) { start ->
            if (start) {
                //이동
                var action = StudyVoclistFragmentDirections.actionStudyVoclistFragmentToVocSearchFragment()
                findNavController().navigate(action)

                // 이벤트 소비 후 false로 설정하여 재실행 방지
                studyViewModel.searchEventStart.value = false
            }

        }

        adapter = VocListAdapter(vocList,
            onItemClicked = { data ->
                studyViewModel.selectWordbookId = data.wid
                val action = StudyVoclistFragmentDirections.actionStudyVoclistFragmentToStudyWordlistFragment()
                findNavController().navigate(action)
            })

        binding.studyRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.studyRecyclerview.adapter = adapter

        //세팅 시작
        observeMyWordbook()
        getMyWordbook()



    }

    //내 단어장 가져오기
    private fun getMyWordbook(){
        var stringUid = userPreference.getUid() ?: "0"
        var uid = stringUid.toInt()
        studyViewModel.getSubscribedWordbooks(requireContext(), uid)
    }
    //내 단어장 리스트 observe
    private fun observeMyWordbook(){
        studyViewModel.wordbookListResult.observe(viewLifecycleOwner) {response ->
            when(response){
                is ApiResponse.Success -> {
                    vocList.clear()
                    Log.d("log_study", "단어장 리스트 불러오기 성공 : ${response.data}")

                    val words = response.data.data
                    for(word in words){
                        vocList.add(VocData(word.wid, word.title, word.tags, ""))
                    }
                    adapter.notifyDataSetChanged()


                }
                is ApiResponse.Error -> {
                    Log.d("log_study", "실패 : ${response.message}")
                    Toast.makeText(context, "단어장 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }





    override fun onDestroyView() {
        super.onDestroyView()
        //(activity as MainActivity).showToprightIcon(false, 1)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StudyVoclistFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StudyVoclistFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}