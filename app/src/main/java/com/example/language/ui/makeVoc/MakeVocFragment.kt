package com.example.language.ui.makeVoc

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
import com.example.language.data.VocData
import com.example.language.data.repository.WordbookRepository
import com.example.language.databinding.FragmentMakeVocBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        val tempVocIdList = listOf("voc_id_1", "voc_id_2", "voc_id_3")

        val vocList = mutableListOf(
            VocData("고등 필수 단어 100", mutableListOf("고등"), "owner1"),
            VocData("토익 필수 단어", mutableListOf("토익", "커스텀"), "owner1"),
            VocData("내 중등 단어장", mutableListOf("중등", "커스텀"), "owner2")
        )

        // 어댑터가 클릭된 아이템의 'vocId' (또는 position)를 반환해야 합니다.
        // 여기서는 position을 vocId 대신 사용한다고 가정합니다.
        val adapter = VocListAdapter(vocList,
            onItemClicked = { position -> // (람다 인자 변경)

                // 1. 클릭된 아이템의 vocId 가져오기
                val selectedVocId = tempVocIdList[0] // (실제로는 position이나 객체에서 ID를 가져와야 함)

                // 2. [ ✨ 변경 ✨ ] Safe Args를 사용해 *vocId만* 전달
                val action = MakeVocFragmentDirections
                    .actionMakeVocFragmentToAddVocInExitFragment(selectedVocId) // (NavGraph가 vocId를 받도록 수정 필요)

                findNavController().navigate(action)
            })

        binding.makeVocRecyclerview.adapter = adapter
        binding.makeVocRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        binding.addNewVocBtn.setOnClickListener {
            val action = MakeVocFragmentDirections.actionMakeVocFragmentToAddNewVocFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}