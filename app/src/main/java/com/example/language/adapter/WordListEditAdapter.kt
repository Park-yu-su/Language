package com.example.language.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.language.R
import com.example.language.data.WordData
import com.example.language.databinding.ItemWordCardBinding

class WordListEditAdapter(
    private var wordList: MutableList<WordData>,
    private var onItemClicked: (WordData) -> Unit,
    private var onEditClicked: (WordData) -> Unit
) : RecyclerView.Adapter<WordListEditAdapter.WordListEditViewHolder>() {

    inner class WordListEditViewHolder(private val binding: ItemWordCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: WordData) {
            binding.englishTv.text = data.word
            binding.exampleTv.text = data.example
            binding.meangins1Tv.text = data.meanings.get(0).toString()

            binding.root.setOnClickListener {
                onItemClicked(data)
            }

            binding.editImv.setOnClickListener {
                binding.editImv.startAnimation(
                    AnimationUtils.loadAnimation(itemView.context, R.anim.button_pop)
                )

                onEditClicked(data)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int,
    ): WordListEditViewHolder {
        val binding = ItemWordCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WordListEditViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: WordListEditViewHolder, position: Int,
    ) {
        holder.bind(wordList[position])
    }

    override fun getItemCount(): Int {
        return wordList.size
    }
}