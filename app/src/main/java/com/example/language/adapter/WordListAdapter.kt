package com.example.language.adapter

import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.language.R
import com.example.language.data.WordData
import com.example.language.databinding.ItemStudyWordCardBinding
import java.util.Locale

class WordListAdapter(
    private var wordList: MutableList<WordData>,
    private var onItemClicked: (WordData, Int) -> Unit,
    private var onTTSRequest: (String) -> Unit
) : RecyclerView.Adapter<WordListAdapter.WordListViewHolder>() {

    inner class WordListViewHolder(private val binding: ItemStudyWordCardBinding) :
            RecyclerView.ViewHolder(binding.root) {
                fun bind(data: WordData){
                    binding.studyEnglishTv.text = data.word
                    binding.studyExampleTv.text = data.example

                    if(data.meanings.get(0) != ""){
                        binding.studyMeangins1Tv.text = data.meanings.get(0).toString()

                    }
                    if(data.meanings.get(1) != ""){
                        binding.studyMeangins2Tv.text = data.meanings.get(1).toString()
                    }
                    if(data.meanings.get(2) != ""){
                        binding.studyMeangins3Tv.text = data.meanings.get(2).toString()
                    }
                    if(data.meanings.get(3) != ""){
                        binding.studyMeangins4Tv.text = data.meanings.get(3).toString()
                    }


                    binding.root.setOnClickListener {
                        onItemClicked(data, adapterPosition)
                    }

                    binding.studyListenImv.setOnClickListener {
                        binding.studyListenImv.startAnimation(
                            AnimationUtils.loadAnimation(itemView.context, R.anim.button_pop))
                        onTTSRequest(data.word)

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

