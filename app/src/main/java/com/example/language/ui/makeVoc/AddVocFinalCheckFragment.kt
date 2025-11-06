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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.adapter.WordListEditAdapter
import com.example.language.api.ApiResponse
import com.example.language.databinding.DialogEditWordBinding
import com.example.language.api.WordData as ApiWordData
import com.example.language.data.WordData as AppWordData
import com.example.language.databinding.FragmentAddVocFinalCheckBinding
import com.example.language.api.login.UserPreference
import com.example.language.data.repository.WordbookRepository
import com.example.language.viewModel.VocViewModel
import com.example.language.viewModel.VocViewModelFactory

class AddVocFinalCheckFragment : Fragment() {

    private var _binding: FragmentAddVocFinalCheckBinding? = null
    private val binding get() = _binding!!

    // --- 1. ViewModel 및 Factory 주입 ---
    private val userPreference by lazy {
        UserPreference(requireContext().applicationContext)
    }
    private val repository by lazy {
        WordbookRepository(userPreference)
    }
    private val viewModelFactory by lazy {
        VocViewModelFactory(repository)
    }
    private val viewModel: VocViewModel by viewModels { viewModelFactory }

    // --- 2. Safe Args ---
    private val args: AddVocFinalCheckFragmentArgs by navArgs()

    // --- 3. Adapter ---
    private lateinit var adapter: WordListEditAdapter

    private lateinit var wordList: MutableList<AppWordData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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

        val vocName: String = args.vocName

        val apiWordList: Array<ApiWordData> = args.wordDataList

        wordList = apiWordList.map { apiWord ->
            AppWordData(
                word = apiWord.word,
                meanings = apiWord.meanings.toMutableList(),
                example = apiWord.example
            )
        }.toMutableList()

        // 2. ViewModel 초기화
        viewModel.setInitialData(args.vocName, wordList)

        // 3. 어댑터 설정
        setupRecyclerView()

        // 4. ViewModel <-> UI 바인딩 설정
        setupDataBinding()

        // 5. 버튼 리스너 설정
        setupClickListeners()

        // 6. API 결과 관찰 설정
        observeApiResults()
    }

    /**
     * RecyclerView와 어댑터를 초기화합니다.
     */
    private fun setupRecyclerView() {
        adapter = WordListEditAdapter(wordList,
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

        // 기존 데이터로 다이얼로그 필드 채우기
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

                    // 원본 리스트에서 현재 아이템의 인덱스를 찾습니다.
                    val index = wordList.indexOf(wordData)
                    if (index != -1) {
                        // 리스트의 데이터를 업데이트합니다.
                        wordList[index] = updatedWordData
                        // 어댑터에 해당 아이템이 변경되었음을 알립니다.
                        adapter.notifyItemChanged(index)
                    }

                } else {
                    Toast.makeText(
                        requireContext(),
                        "단어와 최소한 하나의 뜻은 필수입니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        // 둥근 모서리 적용
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }

    /**
     * ViewModel의 LiveData와 UI EditText 간의 양방향 바인딩을 설정합니다.
     */
    private fun setupDataBinding() {
        // ViewModel -> UI (초기값 설정)

        // ViewModel의 wordList (데이터) -> UI (어댑터)
        // (updateWord 함수가 호출될 때 어댑터를 갱신하기 위함)
        viewModel.wordList.observe(viewLifecycleOwner) { updatedList ->
            wordList.clear()
            wordList.addAll(updatedList)
            adapter.notifyDataSetChanged() // ⭐️ ListAdapter.submitList가 더 효율적입니다.
        }
    }

    /**
     * '저장' 버튼의 클릭 리스너를 설정합니다.
     */
    private fun setupClickListeners() {
        binding.saveVocBtn.setOnClickListener {
            val title = viewModel.title.value?.trim()
            val ownerUid = viewModel.ownerUid.value?.trim()
            val tagsString = viewModel.tags.value

            val tagsList: List<String> = tagsString.orEmpty()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            // 1. [핵심] Safe Args에서 WID를 가져옵니다.
            val currentWid = "1"

            // 유효성 검사
            if (currentWid.isNullOrEmpty()) {
                showToast("수정할 단어장 ID(WID)가 없습니다.")
                return@setOnClickListener
            }
            if (title.isNullOrEmpty()) {
                showToast("단어장 제목을 입력해주세요.")
                return@setOnClickListener
            }
            if (ownerUid.isNullOrEmpty()) {
                showToast("사용자 정보(UID)가 없습니다.")
                return@setOnClickListener
            }
            if (wordList.isEmpty()) {
                showToast("단어가 하나 이상 있어야 합니다.")
                return@setOnClickListener
            }

            // [핵심] 데이터 변환 (AppWordData -> ApiWordData)
            val finalApiData = wordList.map { appWord ->
                ApiWordData(
                    word = appWord.word,
                    meanings = appWord.meanings.toList(),
                    example = appWord.example
                )
            }

            viewModel.updateWordbook(
                context = requireContext(),
                wid = currentWid,
                title = title,
                tags = tagsList,
                owner_uid = ownerUid,
                data = finalApiData
            )
        }
    }

    /**
     * ViewModel의 API 호출 결과 LiveData를 관찰합니다.
     */
    private fun observeApiResults() {
        /* 생성(Register) 결과 관찰
        viewModel.registerStatus.observe(viewLifecycleOwner) { response ->
            // ⭐️ null 체크 추가 (초기 상태이거나, 다른 LiveData가 방금 사용된 경우)
            response ?: return@observe

            when (response) {
                is ApiResponse.Success -> {
                    showToast("'${response.data.title}' 생성 성공!")
                    findNavController().popBackStack()
                }
                is ApiResponse.Error -> {
                    showToast("생성 실패: ${response.message}")
                }
            }
            //  중요: 이벤트를 한 번만 소비하도록 LiveData를 null로 리셋
            viewModel.registerStatus.value = null
        } */

        // 수정(Update) 결과 관찰
        viewModel.updateStatus.observe(viewLifecycleOwner) { response ->
            response ?: return@observe

            when (response) {
                is ApiResponse.Success -> {
                    showToast("'${response.data.title}' 수정 성공!")
                    findNavController().popBackStack()
                }

                is ApiResponse.Error -> {
                    showToast("수정 실패: ${response.message}")
                }
            }
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