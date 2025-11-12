package com.example.language.ui.makeVoc

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.adapter.WordListEditAdapter
import com.example.language.viewModel.AppWordData
import com.example.language.databinding.DialogEditWordBinding
import com.example.language.databinding.FragmentAddVocInExitBinding
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.language.R
import com.example.language.api.login.UserPreference
import com.example.language.data.repository.WordbookRepository
import com.example.language.viewModel.VocViewModel
import com.example.language.viewModel.VocViewModelFactory
import kotlin.getValue


class AddVocInExitFragment : Fragment() {

    private var _binding: FragmentAddVocInExitBinding? = null
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
    // (NavGraph에서 이 프래그먼트가 "vocId"라는 String 인자를 받는다)
    private val args: AddVocInExitFragmentArgs by navArgs()

    private lateinit var adapter: WordListEditAdapter
    // 프래그먼트 내에서 어댑터가 직접 수정할 로컬 리스트
    private var fragmentWordList: MutableList<AppWordData> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentAddVocInExitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [ ✨ 3. RecyclerView 초기 설정 ✨ ]
        // (fragmentWordList는 현재 비어있음)
        setupRecyclerView()

        // [ ✨ 4. ViewModel 관찰 시작 ✨ ]
        // (데이터 로드, UI 업데이트)
        observeViewModel()

        // [ ✨ 5. ViewModel 로드 ✨ ]
        // Safe Args로 받은 vocId를 사용해 ViewModel에 데이터 로드 명령
        val vocId = args.vocId
        viewModel.loadVocabookDetails(requireContext().applicationContext, vocId)

        // [ ✨ 6. '단어 추가' 버튼 리스너 ✨ ]
        binding.addVocBtn.setOnClickListener {
            // Safe Args 없이 다음 화면으로 이동
            findNavController().navigate(R.id.action_addVocInExitFragment_to_selectWayAddVocFragment)
        }

        binding.addVocInExitBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * RecyclerView와 어댑터를 초기화합니다.
     */
    private fun setupRecyclerView() {
        // [!] 어댑터가 AppWordData를 사용하도록 수정됨 (이전 단계)
        adapter = WordListEditAdapter(fragmentWordList, // 비어있는 로컬 리스트로 어댑터 생성
            onItemClicked = { wordData ->
                // (클릭 로직)
            },
            onEditClicked = { wordData ->
                // 수정 다이얼로그 표시
                showEditDialog(wordData)
            }
        )
        binding.wordListRecyclerview.adapter = adapter
        binding.wordListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
    }

    /**
     * ViewModel의 LiveData를 관찰하여 UI를 업데이트합니다.
     */
    private fun observeViewModel() {
        // 로딩 상태 관찰
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // (ProgressBar가 있다면)
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // 단어장 제목 관찰 (UI에 제목 TextView가 있다면)
        viewModel.title.observe(viewLifecycleOwner) { title ->
            // binding.vocTitleTextView.text = title
        }

        // [ ✨ 8. 핵심: 단어 목록 관찰 ✨ ]
        viewModel.wordList.observe(viewLifecycleOwner) { wordsFromViewModel ->
            // ViewModel의 리스트가 변경되면(API 로드 완료)
            // 프래그먼트의 로컬 리스트를 갱신하고 어댑터에게 알립니다.
            fragmentWordList.clear()
            fragmentWordList.addAll(wordsFromViewModel)
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * 단어 수정 다이얼로그를 표시합니다.
     */
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
                    val updatedWordData = wordData.copy(
                        word = newWord,
                        example = newExample,
                        meanings = newMeanings
                    )

                    // [ ✨ 9. 로컬 리스트 및 ViewModel 갱신 ✨ ]
                    val index = fragmentWordList.indexOf(wordData)
                    if (index != -1) {
                        // 1. 로컬 리스트(fragmentWordList) 갱신
                        fragmentWordList[index] = updatedWordData
                        // 2. 어댑터 UI 갱신
                        adapter.notifyItemChanged(index)
                        // 3. 갱신된 리스트를 ViewModel에 전달 (동기화)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}