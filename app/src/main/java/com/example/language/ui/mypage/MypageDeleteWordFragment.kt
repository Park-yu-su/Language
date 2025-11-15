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

class MypageDeleteWordFragment : Fragment() {

    private var _binding: FragmentMypageDeleteWordBinding? = null
    private val binding get() = _binding!!

    private var selectedWordSet: MutableSet<String> = mutableSetOf()
    private lateinit var adapter: WordListSelectAdapter
    private var fragmentWordList: MutableList<AppWordData> = mutableListOf()

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

        // ⭐️ 2. [추가] RecyclerView 초기 설정
        setupRecyclerView()

        // ⭐️ 3. [추가] 삭제 버튼 초기 상태 설정
        updateMainDeleteButtonState()

        // ⭐️ 4. [추가] 삭제 버튼 클릭 리스너 설정
        binding.deleteBtn.setOnClickListener {
            if (selectedWordSet.isNotEmpty()) {
                showDeleteConfirmDialog()
            } else {
                Toast.makeText(requireContext(), "삭제할 단어를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
        // ⭐️ 5. [추가] TODO: ViewModel 등에서 데이터 로드
        // 예: viewModel.wordList.observe(viewLifecycleOwner) { words ->
        //    fragmentWordList.clear()
        //    fragmentWordList.addAll(words)
        //    adapter.notifyDataSetChanged()
        // }
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

    /**
     * ⭐️ [추가] 단어 삭제 확인 다이얼로그를 띄웁니다.
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

        // 5. ⭐️ "삭제" 버튼 리스너
        dialogBinding.dialogOkCdv.setOnClickListener {

            // 1. 실제 데이터 리스트에서 선택된 항목들 제거
            // (it.word를 고유 ID로 사용)
            fragmentWordList.removeAll { selectedWordSet.contains(it.word) }

            // 2. 선택 목록(Set) 비우기
            selectedWordSet.clear()

            // 3. 어댑터에 전체 데이터 변경 알림
            adapter.notifyDataSetChanged()

            // 4. "삭제하기" 버튼 다시 비활성화
            updateMainDeleteButtonState()

            // 5. (ViewModel 사용 시)
            // viewModel.deleteWords(selectedWordSet)

            dialog.dismiss() // ⭐️ 로직 실행 후 다이얼로그 닫기
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}