package com.example.language.ui.study

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.language.R
import com.example.language.adapter.SearchPagerAdapter
import com.example.language.databinding.FragmentStudyVocsearchBinding
import com.example.language.ui.home.MainActivity
import com.google.android.material.tabs.TabLayoutMediator

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudyVocsearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudyVocsearchFragment : Fragment() {

    private lateinit var binding: FragmentStudyVocsearchBinding

    //태그 종류
    private val tabTitles = listOf("ID 검색", "태그 검색")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudyVocsearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setUIVisibility(false)

        //ViewPager 연결
        val pagerAdapter = SearchPagerAdapter(this)
        binding.vocsearchViewpager.adapter = pagerAdapter

        //Tab과 연결
        TabLayoutMediator(binding.vocsearchTablayout, binding.vocsearchViewpager)
        { tab, position ->
            tab.text = tabTitles[position]
        }.attach()



    }

}