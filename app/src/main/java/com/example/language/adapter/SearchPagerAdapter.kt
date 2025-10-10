package com.example.language.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.language.ui.study.StudySearchIdFragment
import com.example.language.ui.study.StudySearchTagFragment

class SearchPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> StudySearchIdFragment()
            1 -> StudySearchTagFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}