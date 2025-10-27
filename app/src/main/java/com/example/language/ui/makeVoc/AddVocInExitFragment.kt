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
import com.example.language.data.WordData
import com.example.language.databinding.DialogEditWordBinding
import com.example.language.databinding.FragmentAddVocInExitBinding
import androidx.core.graphics.drawable.toDrawable

class AddVocInExitFragment : Fragment() {

    private var _binding: FragmentAddVocInExitBinding? = null
    private val binding get() = _binding!!

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

        //일단 임시 데이터
        val wordList: MutableList<WordData> = mutableListOf(
            WordData("word", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example"),
            WordData("word2", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example2"),
            WordData("word3", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example3"),
            WordData("word4", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example4"),
            WordData("word5", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example5")
        )

        val adapter = WordListEditAdapter(wordList,
            onItemClicked = { wordData ->
                // 필요하다면 여기에 단어 카드 전체 클릭 시의 로직을 유지하거나 추가합니다.
            },
            onEditClicked = { wordData ->
                val dialogBinding =
                    DialogEditWordBinding.inflate(LayoutInflater.from(requireContext()))
                val dialogView = dialogBinding.root

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
                            // TODO: wordList 내부의 원본 단어 데이터를 updatedWordData로 교체하고
                            //       어댑터에 notifyDataSetChanged() 또는 notifyItemChanged()를 호출하여 화면 업데이트
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

                // 둥근 모서리를 표현하기 위해
                alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

                alertDialog.show()
            }
        )

        binding.wordListRecyclerview.adapter = adapter
        binding.wordListRecyclerview.layoutManager = LinearLayoutManager(requireContext())

    }
}