package com.example.language.ui.makeVoc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.language.R
import com.example.language.api.login.UserPreference
import com.example.language.data.repository.WordbookRepository
import com.example.language.databinding.FragmentAddNewVocBinding
import com.example.language.viewModel.VocViewModel
import com.example.language.viewModel.VocViewModelFactory
import com.google.android.material.chip.Chip

class AddNewVocFragment : Fragment() {

    private var _binding: FragmentAddNewVocBinding? = null
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

    // 프래그먼트에서 임시로 관리할 태그 리스트
    private val tagList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddNewVocBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [ ✨ 화면 진입 시 ViewModel 상태 초기화 ✨ ]
        // '새 단어장 만들기' 화면에 들어오는 즉시,
        // ViewModel의 ID, 제목, 태그, *단어 목록*을 모두 비웁니다.
        // 이렇게 하면 이전 작업(단어장 수정)의 단어 목록이 누적되지 않습니다.
        tagList.clear() // 프래그먼트의 로컬 태그 리스트도 비움
        viewModel.setupForNewVocabook("", emptyList())
        // [ ✨ 태그 입력 및 '다음' 버튼 리스너 설정 ✨ ]
        setupTagInputListener()
        setupNextButtonListener()

        binding.addNewVocBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * 태그 입력 EditText에 '엔터' (Done) 리스너를 설정합니다.
     */
    private fun setupTagInputListener() {
        binding.inputTagEt.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val tagText = textView.text.toString().trim()

                if (tagText.isNotEmpty() && tagList.size < 4) {
                    tagList.add(tagText)
                    addChipToGroup(tagText)
                    textView.text = ""
                } else if (tagList.size >= 4) {
                    showToast("태그는 4개까지 추가할 수 있습니다.")
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }

    /**
     * ChipGroup에 동적으로 Chip을 생성하여 추가합니다.
     */
    private fun addChipToGroup(tagText: String) {
        val chip = Chip(requireContext()).apply {
            setChipBackgroundColorResource(R.color.Main1_1)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            text = "#$tagText"
            isCloseIconVisible = true
            // 닫기 버튼 리스너 (UI와 로컬 리스트에서 제거)
            setOnCloseIconClickListener {
                binding.tagChipGroup.removeView(it)
                tagList.remove(tagText)
            }
        }
        binding.tagChipGroup.addView(chip)
    }

    /**
     * '다음' 버튼 리스너를 설정합니다.
     */
    private fun setupNextButtonListener() {
        binding.addNewVocNextBtn.setOnClickListener {

            // [ ✨ 3. Safe Args 로직 변경 ✨ ]
            val vocName = binding.insertVocNameEt.text.toString().trim()

            if (vocName.isEmpty()) {
                showToast("단어장 이름을 입력해주세요.")
                return@setOnClickListener
            }

            // 1. ViewModel에 '새 단어장' 정보 설정
            viewModel.setupForNewVocabook(vocName, tagList)

            // 2. Safe Args 없이 SelectWayAddVocFragment로 이동
            findNavController().navigate(R.id.action_addNewVocFragment_to_selectWayAddVocFragment)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}