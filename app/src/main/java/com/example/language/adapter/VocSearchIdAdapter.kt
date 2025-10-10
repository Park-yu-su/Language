package com.example.language.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.language.R
import com.example.language.data.VocData
import com.example.language.databinding.ItemStudyVocCardBinding

class VocSearchIdAdapter(
    private var vocList: MutableList<VocData>,
    private var onItemClicked: (VocData) -> Unit
): RecyclerView.Adapter<VocSearchIdAdapter.VocSearchIdViewHolder>() {

    inner class VocSearchIdViewHolder(private val binding: ItemStudyVocCardBinding):
        RecyclerView.ViewHolder(binding.root){
        fun bind(data: VocData){
            binding.stduyVocNameTv.text = data.title
            binding.studyVocAddBtn.visibility = View.VISIBLE

            binding.studyVocAddBtn.setOnClickListener {
                binding.studyVocAddBtn.startAnimation(
                    AnimationUtils.loadAnimation(itemView.context, R.anim.button_pop))
                onItemClicked(data)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocSearchIdViewHolder {
        val binding = ItemStudyVocCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return VocSearchIdViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VocSearchIdViewHolder, position: Int) {
        holder.bind(vocList.get(position))
    }

    override fun getItemCount(): Int {
        return vocList.size
    }

}