package com.example.language.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.language.data.WordData
import com.example.language.ui.study.StudyWordDetailPageFragment

class WordPagerAdapter(fragment: Fragment,
                       private var wordList: MutableList<WordData>)
    : FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment {
        val word = wordList[position]
        return StudyWordDetailPageFragment.newInstance(word)
    }

    override fun getItemCount(): Int {
        return wordList.size
    }

}