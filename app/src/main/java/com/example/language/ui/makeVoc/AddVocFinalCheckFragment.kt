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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.adapter.WordListEditAdapter
import com.example.language.databinding.DialogEditWordBinding
import com.example.language.api.WordData as ApiWordData
import com.example.language.data.WordData as AppWordData
import com.example.language.databinding.FragmentAddVocFinalCheckBinding

class AddVocFinalCheckFragment : Fragment() {

    private var _binding: FragmentAddVocFinalCheckBinding? = null
    private val binding get() = _binding!!

    private val args: AddVocFinalCheckFragmentArgs by navArgs()

    private lateinit var wordList: MutableList<AppWordData>
    private lateinit var adapter: WordListEditAdapter

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

        val apiWordList: Array<ApiWordData> = args.wordDataList

        wordList = apiWordList.map { apiWord ->
            AppWordData(
                word = apiWord.word,
                meanings = apiWord.meanings.toMutableList(),
                example = apiWord.example
            )
        }.toMutableList()

        adapter = WordListEditAdapter(wordList,
            onItemClicked = { wordData ->
                // 단어 카드 클릭 시 로직 (필요시 구현)
            },
            onEditClicked = { wordData ->
                // '수정' 버튼 클릭 시 다이얼로그 띄우기
                showEditDialog(wordData)
            }
        )

        binding.wordListRecyclerview.adapter = adapter
        binding.wordListRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        binding.saveVocBtn.setOnClickListener {
            // TODO: 현재 'wordList'에 담긴 최종 데이터를 API로 전송 (WordbookRegister)
            // val finalData = wordList
            // callRegisterApi(finalData)
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}