package com.example.language.ui.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.adapter.VocListAdapter
import com.example.language.api.ApiResponse
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

        // 어댑터 설정
        setupRecyclerView()

        // [관찰] 서버 응답이 오면 UI를 갱신하도록 관찰자 등록
        observeWordbookList()

        // [요청] 화면이 만들어질 때마다(돌아올 때 포함) 서버에 최신 목록 요청
        fetchWordbooks()
    }

    private fun setupRecyclerView() {
        adapter = VocListAdapter(vocList,
            onItemClicked = { clickedVocData ->
                val selectedVocId = clickedVocData.wid.toString()
                myPageViewModel.selectWordbookId = selectedVocId.toInt()
                myPageViewModel.selectWordbookInfo = clickedVocData

                val action = MypageMyvocFragmentDirections
                    .actionMypageMyvocFragmentToMypageMyvocDetailFragment(selectedVocId)

                findNavController().navigate(action)
            })

        binding.manageMyVocRecyclerview.adapter = adapter
        binding.manageMyVocRecyclerview.layoutManager = LinearLayoutManager(requireContext())
    }

    // 서버에 목록 요청하는 함수
    private fun fetchWordbooks() {
        val myUid = userPreference.getUid()?.toIntOrNull() ?: 0

        myPageViewModel.getSubscribedWordbooks(requireContext(), myUid)
    }

    // LiveData 관찰 및 UI 업데이트 함수
    private fun observeWordbookList() {
        myPageViewModel.wordbookListResult.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResponse.Success -> {
                    vocList.clear()

                    // ViewModel의 캐시 데이터도 최신화 (선택 사항이지만 권장)
                    // myPageViewModel.mywordbookList.clear()
                    // myPageViewModel.mywordbookList.addAll(response.data.data)

                    val ownerUid = userPreference.getUid().toString()

                    response.data.data.forEach { item ->
                        vocList.add(
                            VocData(
                                wid = item.wid,
                                title = item.title,
                                tags = item.tags,
                                owner_uid = ownerUid
                            )
                        )
                    }
                    adapter.notifyDataSetChanged()
                }
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), "목록 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}