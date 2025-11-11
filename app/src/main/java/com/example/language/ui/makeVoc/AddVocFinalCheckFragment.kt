package com.example.language.ui.makeVoc

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.adapter.WordListEditAdapter
import com.example.language.api.ApiResponse
import com.example.language.api.login.UserPreference
import com.example.language.data.repository.WordbookRepository
import com.example.language.databinding.DialogEditWordBinding
import com.example.language.viewModel.AppWordData
import com.example.language.databinding.FragmentAddVocFinalCheckBinding
import com.example.language.viewModel.VocViewModel
import com.example.language.viewModel.VocViewModelFactory
import kotlin.getValue

class AddVocFinalCheckFragment : Fragment() {

    private var _binding: FragmentAddVocFinalCheckBinding? = null
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

    // --- Adapter ---
    private lateinit var adapter: WordListEditAdapter
    private var fragmentWordList: MutableList<AppWordData> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddVocFinalCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentWordList = viewModel.wordList.value?.toMutableList() ?: mutableListOf()

        // 1. 어댑터 설정
        setupRecyclerView()

        // 2. ViewModel <-> UI 바인딩 설정
        setupDataBinding()

        // 3. 버튼 리스너 설정
        setupClickListeners()

        // 4. API 결과 관찰 설정
        observeApiResults()
    }

    /**
     * RecyclerView와 어댑터를 초기화합니다.
     */
    private fun setupRecyclerView() {
        adapter = WordListEditAdapter(fragmentWordList,
            onItemClicked = { /* (필요시 구현) */ },
            onEditClicked = { wordData ->
                // '수정' 버튼 클릭 시 다이얼로그 띄우기
                showEditDialog(wordData)
            }
        )
        binding.wordListRecyclerview.adapter = adapter
        binding.wordListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showEditDialog(wordData: AppWordData) {
        val dialogBinding =
            DialogEditWordBinding.inflate(LayoutInflater.from(requireContext()))
        val dialogView = dialogBinding.root

        // (기존 데이터 채우기...)
        dialogBinding.inputWord.setText(wordData.word)
        dialogBinding.inputExample.setText(wordData.example)
        dialogBinding.inputMeaning1.setText(wordData.meanings.getOrNull(0))
        dialogBinding.inputMeaning2.setText(wordData.meanings.getOrNull(1))
        dialogBinding.inputMeaning3.setText(wordData.meanings.getOrNull(2))
        dialogBinding.inputMeaning4.setText(wordData.meanings.getOrNull(3))

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("단어 수정하기: ${wordData.word}")
            .setView(dialogView)
            .setPositiveButton("저장") { dialog, _ ->
                // (새로운 단어/뜻 파싱...)
                val newWord = dialogBinding.inputWord.text.toString().trim()
                val newExample = dialogBinding.inputExample.text.toString().trim()
                val newMeanings = mutableListOf<String>()
                listOf(
                    dialogBinding.inputMeaning1,
                    dialogBinding.inputMeaning2,
                    dialogBinding.inputMeaning3,
                    dialogBinding.inputMeaning4
                ).forEach { editText ->
                    val meaning = editText.text.toString().trim()
                    if (meaning.isNotEmpty()) {
                        newMeanings.add(meaning)
                    }
                }

                if (newWord.isNotEmpty() && newMeanings.isNotEmpty()) {
                    // [!] UI 모델(AppWordData) 업데이트
                    val updatedWordData = wordData.copy(
                        word = newWord,
                        example = newExample,
                        meanings = newMeanings
                    )

                    val index = fragmentWordList.indexOf(wordData)
                    if (index != -1) {
                        // 1. 로컬 리스트(fragmentWordList) 갱신
                        fragmentWordList[index] = updatedWordData
                        // 2. 어댑터 UI 갱신
                        adapter.notifyItemChanged(index)

                        // [ ✨ 6. ViewModel 동기화 ✨ ]
                        // 수정된 로컬 리스트를 ViewModel에 전달
                        viewModel.updateWordList(fragmentWordList)
                    }

                } else {
                    Toast.makeText(requireContext(), "단어와 최소한 하나의 뜻은 필수입니다.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }

    /**
     * ViewModel의 LiveData와 UI EditText 간의 양방향 바인딩을 설정합니다.
     */
    private fun setupDataBinding() {
        // ViewModel의 wordList (SelectWay에서 추가된 경우) -> UI (어댑터)
        viewModel.wordList.observe(viewLifecycleOwner) { updatedList ->
            if (fragmentWordList != updatedList) { // 불필요한 갱신 방지
                fragmentWordList.clear()
                fragmentWordList.addAll(updatedList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * '저장' 버튼의 클릭 리스너를 설정합니다.
     */
    private fun setupClickListeners() {
        binding.saveVocBtn.setOnClickListener {
            // [ ✨ 핵심 ✨ ]
            // ViewModel의 currentVocId가 null인지 (새 단어장)
            // 아닌지 (기존 단어장) 확인하여 분기

            val currentWid = viewModel.currentVocId.value

            if (currentWid == null) {
                // (1) 새 단어장 '생성'
                showToast("새 단어장 저장 중...")
                viewModel.registerNewWordbook(requireContext().applicationContext)
            } else {
                // (2) 기존 단어장 '수정'
                showToast("단어장 수정 중...")
                viewModel.updateCurrentWordbook(requireContext().applicationContext)
            }
        }
    }

    /**
     * ViewModel의 API 호출 결과 LiveData를 관찰합니다.
     */
    private fun observeApiResults() {
        // '생성' 결과 관찰
        viewModel.registerStatus.observe(viewLifecycleOwner) { response ->
            response ?: return@observe // 이벤트가 없으면 리턴

            when (response) {
                is ApiResponse.Success -> {
                    showToast("'${response.data.title}' 생성 성공!")
                    findNavController().popBackStack() // (수정 필요시 수정)
                }
                is ApiResponse.Error -> {
                    showToast("생성 실패: ${response.message}")
                }
            }
            // [!] 이벤트 소비 후 리셋
            viewModel.resetRegisterStatus()
        }

        // '수정' 결과 관찰
        viewModel.updateStatus.observe(viewLifecycleOwner) { response ->
            response ?: return@observe // 이벤트가 없으면 리턴

            when (response) {
                is ApiResponse.Success -> {
                    showToast("'${response.data.title}' 수정 성공!")
                    findNavController().popBackStack()
                }
                is ApiResponse.Error -> {
                    showToast("수정 실패: ${response.message}")
                }
            }
            // [!] 이벤트 소비 후 리셋
            viewModel.resetUpdateStatus()
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