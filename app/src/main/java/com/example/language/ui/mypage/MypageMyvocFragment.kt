package com.example.language.ui.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.adapter.VocListAdapter
import com.example.language.api.login.UserPreference
import com.example.language.api.mypage.MypageRepository
import com.example.language.api.mypage.viewModel.MypageViewModel
import com.example.language.api.mypage.viewModel.MypageViewModelFactory
import com.example.language.data.VocData
import com.example.language.databinding.FragmentMypageMyvocBinding
import kotlin.getValue

class MypageMyvocFragment : Fragment() {

    private var _binding: FragmentMypageMyvocBinding? = null
    private val binding get() = _binding!!

    private lateinit var userPreference : UserPreference

    private var vocList = mutableListOf<VocData>()

    private lateinit var adapter : VocListAdapter

    private val myPageRepository = MypageRepository()
    private val myPageViewModel: MypageViewModel by activityViewModels() {
        MypageViewModelFactory(myPageRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMypageMyvocBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.manageMyVocBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        userPreference = UserPreference(requireContext())

        // [ ✨ 3. 어댑터 클릭 리스너 (핵심) ✨ ]
        // (어댑터가 클릭된 VocData 객체를 람다로 전달한다고 가정)
        adapter = VocListAdapter(vocList,
            onItemClicked = { clickedVocData ->

                // 1. 클릭된 단어장의 wid(Int)를 String으로 변환
                val selectedVocId = clickedVocData.wid.toString()
                myPageViewModel.selectWordbookId = selectedVocId.toInt()
                myPageViewModel.selectWordbookInfo = clickedVocData

                // 2. Safe Args를 사용해 *vocId만* AddVocInExitFragment로 전달
                val action = MypageMyvocFragmentDirections
                    .actionMypageMyvocFragmentToMypageMyvocDetailFragment(selectedVocId)

                findNavController().navigate(action)
            })

        binding.manageMyVocRecyclerview.adapter = adapter
        binding.manageMyVocRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        getVocList()

    }


    private fun getVocList(){
        vocList.clear()
        myPageViewModel.mywordbookList.forEach {
            vocList.add(VocData(it.wid, it.title, it.tags, ""))
        }
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}