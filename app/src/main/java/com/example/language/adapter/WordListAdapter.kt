package com.example.language.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.language.data.WordData
import com.example.language.databinding.ItemStudyWordCardBinding

class WordListAdapter(
    private var wordList: MutableList<WordData>,
    private var onItemClicked: (WordData) -> Unit
) : RecyclerView.Adapter<WordListAdapter.WordListViewHolder>() {

    inner class WordListViewHolder(private val binding: ItemStudyWordCardBinding) :
            RecyclerView.ViewHolder(binding.root) {
                fun bind(data: WordData){
                    binding.studyEnglishTv.text = data.word
                    binding.studyExampleTv.text = data.example
                    binding.studyMeanginsTv.text = data.meanings.get(0).toString()

                    binding.root.setOnClickListener {
                        onItemClicked(data)
                    }

                }
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int
    ): WordListViewHolder {
        val binding = ItemStudyWordCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return WordListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordListViewHolder, position: Int
    ) {
        holder.bind(wordList[position])
    }

    override fun getItemCount(): Int {
        return wordList.size
    }

}

