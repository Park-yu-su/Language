package com.example.language.ui.makeVoc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.R
import com.example.language.adapter.VocListAdapter
import com.example.language.api.login.UserPreference
import com.example.language.data.VocData
import com.example.language.data.repository.WordbookRepository
import com.example.language.databinding.FragmentMakeVocBinding
import com.example.language.ui.home.MainActivity
import com.example.language.viewModel.VocViewModel
import com.example.language.viewModel.VocViewModelFactory

class MakeVocFragment : Fragment() {

    private var _binding: FragmentMakeVocBinding? = null
    private val binding get() = _binding!!

    // --- [ ✨ 1. Factory, Repository, Preference 선언 ✨ ] ---
    // (ViewModel 선언보다 *먼저* 선언해야 합니다)
    private val userPreference by lazy {
        UserPreference(requireContext().applicationContext)
    }
    private val repository by lazy {
        WordbookRepository(userPreference)
    }
    private val viewModelFactory by lazy {
        VocViewModelFactory(repository)
    }

    // --- [ ✨ 2. ViewModel 선언 (수정) ✨ ] ---
    // 팩토리를 람다로 전달합니다.
    private val viewModel: VocViewModel by activityViewModels<VocViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMakeVocBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [ ✨ 2. (가정) API 대신 임시 데이터 사용 ✨ ]
        // (실제로는 ViewModel.getSubscribedWordbooks() 등을 호출해야 함)

        //상단 바 지우기
        (activity as MainActivity).setUIVisibility(false)
        val vocList = mutableListOf(
            VocData(1, "고등 필수 단어 100", listOf("고등"), "owner1"),
            VocData(2, "토익 필수 단어", listOf("토익", "커스텀"), "owner1"),
            VocData(3, "내 중등 단어장", listOf("중등", "커스텀"), "owner2")
        )

        // [ ✨ 3. 어댑터 클릭 리스너 (핵심) ✨ ]
        // (어댑터가 클릭된 VocData 객체를 람다로 전달한다고 가정)
        val adapter = VocListAdapter(vocList,
            onItemClicked = { clickedVocData ->

                // 1. 클릭된 단어장의 wid(Int)를 String으로 변환
                val selectedVocId = clickedVocData.wid.toString()

                // 2. Safe Args를 사용해 *vocId만* AddVocInExitFragment로 전달
                val action = MakeVocFragmentDirections
                    .actionMakeVocFragmentToAddVocInExitFragment(selectedVocId)

                findNavController().navigate(action)
            })

        binding.makeVocRecyclerview.adapter = adapter
        binding.makeVocRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        // [ ✨ 4. '새 단어장' 버튼 리스너 ✨ ]
        binding.addNewVocBtn.setOnClickListener {
            // '새 단어장' 흐름(AddNewVocFragment)으로 이동
            findNavController().navigate(R.id.action_makeVocFragment_to_addNewVocFragment)
        }

        binding.makeVocBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}