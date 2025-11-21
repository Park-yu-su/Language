package com.example.language.ui.makeVoc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.map
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

    private val uid by lazy {
        userPreference.getUid().toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMakeVocBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //상단 바 지우기
        (activity as MainActivity).setUIVisibility(false)

        viewModel.getSubscribedWordbooks(requireContext().applicationContext)

        val adapter = VocListAdapter(mutableListOf()) { clickedVocData ->
            // 클릭 이벤트 처리
            val selectedVocId = clickedVocData.wid.toString()
            val selectedVocTitle = clickedVocData.title
            val action = MakeVocFragmentDirections
                .actionMakeVocFragmentToAddVocInExitFragment(selectedVocId, selectedVocTitle)
            findNavController().navigate(action)
        }

        binding.makeVocRecyclerview.adapter = adapter
        binding.makeVocRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        viewModel.transformedVocList.observe(viewLifecycleOwner) { newVocList ->
            adapter.updateData(newVocList)
        }

        // [ ✨ 4. '새 단어장' 버튼 리스너 ✨ ]
        binding.addNewVocBtn.setOnClickListener {
            // '새 단어장' 흐름(AddNewVocFragment)으로 이동
            findNavController().navigate(R.id.action_makeVocFragment_to_addNewVocFragment)
        }

        binding.makeVocBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        // 앱이 포그라운드로 올 때, 혹은 백스택에서 돌아올 때 항상 실행됨
        viewModel.getSubscribedWordbooks(requireContext().applicationContext)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}