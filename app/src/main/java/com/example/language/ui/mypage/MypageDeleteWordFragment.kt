package com.example.language.ui.mypage

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.R
import com.example.language.adapter.WordListSelectAdapter
import com.example.language.databinding.DialogCustomSelectBinding
import com.example.language.databinding.FragmentMypageDeleteWordBinding
import com.example.language.viewModel.AppWordData
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.activityViewModels
import com.example.language.api.ApiResponse
import com.example.language.api.WordData
import com.example.language.api.WordbookUpdateRequestPayload
import com.example.language.api.mypage.MypageRepository
import com.example.language.api.mypage.viewModel.MypageViewModel
import com.example.language.api.mypage.viewModel.MypageViewModelFactory
import kotlin.getValue

class MypageDeleteWordFragment : Fragment() {

    private var _binding: FragmentMypageDeleteWordBinding? = null
    private val binding get() = _binding!!

    private var selectedWordSet: MutableSet<String> = mutableSetOf()
    private lateinit var adapter: WordListSelectAdapter

    private var fragmentWordList: MutableList<AppWordData> = mutableListOf()

    private val myPageRepository = MypageRepository()
    private val myPageViewModel: MypageViewModel by activityViewModels {
        MypageViewModelFactory(myPageRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMypageDeleteWordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.deleteWordBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        // RecyclerView 초기 설정
        setupRecyclerView()

        // 삭제 버튼 초기 상태 설정
        updateMainDeleteButtonState()

        // 관찰자(Observer) 초기화됨
        initObservers()

        // 삭제 버튼 클릭 리스너 설정
        binding.deleteBtn.setOnClickListener {
            if (selectedWordSet.isNotEmpty()) {
                showDeleteConfirmDialog()
            } else {
                Toast.makeText(requireContext(), "삭제할 단어를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = WordListSelectAdapter(
            fragmentWordList,
            selectedWordSet, // 2. Set을 어댑터에 전달

            onItemClicked = { clickedData ->
                // (아이템 전체 클릭 시 동작, 예: 상세 보기)
                // Toast.makeText(requireContext(), "${clickedData.word} 상세 보기", Toast.SHORT_SHORT).show()
            },

            // 3. '선택 버튼' 클릭 시 동작
            onSelectClicked = { clickedData ->
                val wordId = clickedData.word // (고유 ID)

                // 4. Set 상태 변경
                if (selectedWordSet.contains(wordId)) {
                    selectedWordSet.remove(wordId)
                } else {
                    selectedWordSet.add(wordId)
                }

                // 5. 어댑터 UI 갱신 (해당 아이템만)
                val position = fragmentWordList.indexOf(clickedData)

                if (position != -1) { // (안전장치)
                    adapter.notifyItemChanged(position)
                }

                // 6. 메인 "삭제하기" 버튼 상태 갱신
                updateMainDeleteButtonState()
            }
        )

        binding.mypageDeleteWordListRecyclerview.adapter = adapter
        binding.mypageDeleteWordListRecyclerview.layoutManager =
            LinearLayoutManager(requireContext())
    }

    // 7. "삭제하기" 버튼 활성화/비활성화 함수
    private fun updateMainDeleteButtonState() {
        if (selectedWordSet.isNotEmpty()) {
            binding.deleteBtn.isEnabled = true
            // ⭐️ 활성화된 이미지 리소스로 변경
            binding.deleteBtn.setImageResource(R.drawable.ic_delete_btn_selected)
        } else {
            binding.deleteBtn.isEnabled = false
            binding.deleteBtn.setImageResource(R.drawable.ic_delete_btn_unselected)
        }
    }

    private fun initObservers() {
        // (1) 단어 목록 로드 결과 관찰
        myPageViewModel.wordListResult.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResponse.Success -> {
                    fragmentWordList.clear()

                    response.data.data.forEach { wordItem ->

                        // AppWordData 생성자에 맞게 데이터 매핑
                        fragmentWordList.add(
                            AppWordData(
                                wordId = wordItem.wordId,
                                word = wordItem.word,
                                meanings = wordItem.meanings.toMutableList(),
                                distractors = wordItem.distractors,
                                example = wordItem.example
                            )
                        )
                    }
                    adapter.notifyDataSetChanged()
                }

                is ApiResponse.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "불러오기 실패: ${response.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // (2) 단어장 수정(삭제 포함) 결과 관찰
        myPageViewModel.wordbookUpdateResult.observe(viewLifecycleOwner) { response ->

            if (response == null) return@observe

            when (response) {
                is ApiResponse.Success -> {
                    Toast.makeText(requireContext(), "단어가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    myPageViewModel.initUpdateResult()
                    findNavController().popBackStack()
                }

                is ApiResponse.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "삭제 실패: ${response.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * 단어 삭제 확인 다이얼로그를 띄웁니다.
     */
    private fun showDeleteConfirmDialog() {
        // 1. 바인딩 생성
        val dialogBinding = DialogCustomSelectBinding.inflate(layoutInflater)

        // 2. 내용 채우기
        val message = "해당 단어를 삭제하시겠습니까?"
        dialogBinding.dialogMessageTv.text = message
        dialogBinding.dialogOkTv.text = "삭제"
        dialogBinding.dialogCancelTv.text = "취소"

        // 3. 다이얼로그 생성
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // 다이얼로그 투명
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        // 4. "취소" 버튼 리스너
        dialogBinding.dialogCancelCdv.setOnClickListener {
            dialog.dismiss()
        }

        // 5. "삭제" 버튼 리스너
        dialogBinding.dialogOkCdv.setOnClickListener {

            // 1. ViewModel에 저장된 기존 단어장 정보 가져오기
            val currentInfo = myPageViewModel.selectWordbookInfo

            // 2. 남길 단어들만 필터링하고 API 모델(WordData)로 변환
            val remainingWordDataList = fragmentWordList
                .filter { !selectedWordSet.contains(it.word) }
                .map { appWord ->
                    WordData(
                        word = appWord.word,
                        meanings = appWord.meanings,
                        distractors = appWord.distractors,
                        example = appWord.example
                    )
                }

            // 3. Payload 생성 (기존 정보 유지 + 변경된 단어 리스트)
            val payload = WordbookUpdateRequestPayload(
                wid = myPageViewModel.selectWordbookId.toString(),
                title = currentInfo.title,
                tags = currentInfo.tags,
                owner_uid = currentInfo.owner_uid,
                data = remainingWordDataList      // 수정된 단어 리스트
            )

            // 4. API 호출
            myPageViewModel.updateWordbook(requireContext(), payload)

            dialog.dismiss() // 로직 실행 후 다이얼로그 닫기
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}