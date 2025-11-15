package com.example.language.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.language.R
import com.example.language.databinding.ItemWordCardWithSelectBinding
import com.example.language.viewModel.AppWordData

class WordListSelectAdapter(
    private var wordList: MutableList<AppWordData>,

    // ⭐️ 1. [추가] Fragment에서 관리하는 '선택된 단어 ID 목록' Set
    private var selectedWordSet: Set<String>,

    private var onItemClicked: (AppWordData) -> Unit,

    // ⭐️ 2. [이름 변경] 'edit' 대신 'select' (선택 토글)로 목적 변경
    private var onSelectClicked: (AppWordData) -> Unit
) : RecyclerView.Adapter<WordListSelectAdapter.WordListSelectViewHolder>() {

    inner class WordListSelectViewHolder(private val binding: ItemWordCardWithSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: AppWordData) {
            // --- 데이터 바인딩 ---
            binding.englishTv.text = data.word
            binding.exampleTv.text = data.example

            // ⭐️ 3. [수정] get(0)은 위험하므로 getOrNull로 안전하게 변경
            binding.meangins1Tv.text = data.meanings.getOrNull(0) ?: ""
            binding.meangins2Tv.text = data.meanings.getOrNull(1) ?: ""
            binding.meangins3Tv.text = data.meanings.getOrNull(2) ?: ""
            binding.meangins4Tv.text = data.meanings.getOrNull(3) ?: ""

            binding.root.setOnClickListener {
                onItemClicked(data)
            }

            // ⭐️ 4. [핵심] '선택 버튼' UI 업데이트
            // (data.word를 고유 ID로 사용한다고 가정)
            if (selectedWordSet.contains(data.word)) {
                // 선택된 상태: 파란색
                binding.selectImv.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.Main1_1)
                )
            } else {
                // 기본 상태: 회색 (예시)
                binding.selectImv.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.Gray3)
                )
            }

            // ⭐️ 5. [핵심] '선택 버튼' 클릭 리스너
            binding.selectImv.setOnClickListener {
                // 팝 애니메이션
                it.startAnimation(
                    AnimationUtils.loadAnimation(itemView.context, R.anim.button_pop)
                )

                // Fragment에 "이 아이템 선택/해제 할래"라고 알림
                onSelectClicked(data)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int,
    ): WordListSelectViewHolder {
        val binding = ItemWordCardWithSelectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WordListSelectViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: WordListSelectViewHolder, position: Int,
    ) {
        holder.bind(wordList[position])
    }

    override fun getItemCount(): Int {
        return wordList.size
    }
}