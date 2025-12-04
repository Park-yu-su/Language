package com.example.language.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.language.R
import com.example.language.databinding.ItemWordCardBinding
import com.example.language.viewModel.AppWordData

class WordListEditAdapter(
    private var wordList: MutableList<AppWordData>,
    private var onItemClicked: (AppWordData) -> Unit,
    private var onEditClicked: (AppWordData) -> Unit
) : RecyclerView.Adapter<WordListEditAdapter.WordListEditViewHolder>() {

    inner class WordListEditViewHolder(private val binding: ItemWordCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: AppWordData) {
            binding.englishTv.text = data.word
            binding.exampleTv.text = data.example
            binding.meangins1Tv.text = data.meanings.get(0)
            if (data.meanings.size > 1) {binding.meangins2Tv.text = data.meanings.get(1)}
            if (data.meanings.size > 2) {binding.meangins3Tv.text = data.meanings.get(2)}
            if (data.meanings.size > 3) {binding.meangins4Tv.text = data.meanings.get(3)}

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

    fun updateData(newItems: List<AppWordData>) {
        this.wordList.clear()
        this.wordList.addAll(newItems)
        notifyDataSetChanged()      // 새로 고침!
    }
}