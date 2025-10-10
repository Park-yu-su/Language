package com.example.language.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.language.data.VocData
import com.example.language.data.WordData
import com.example.language.databinding.ItemStudyVocCardBinding

class VocListAdapter(
    private var vocList: MutableList<VocData>,
    private var onItemClicked: (VocData) -> Unit
): RecyclerView.Adapter<VocListAdapter.VocalListViewHolder>()  {

    inner class VocalListViewHolder(private val binding: ItemStudyVocCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: VocData){

            //단어 바인딩
            binding.stduyVocNameTv.text = data.title


            //해당 단어 누를 때 함수 콜백
            binding.root.setOnClickListener {
                onItemClicked(data)
            }

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocalListViewHolder {
        val binding = ItemStudyVocCardBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return VocalListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VocalListViewHolder, position: Int) {
        holder.bind(vocList[position])
    }

    override fun getItemCount(): Int {
        return vocList.size
    }




}