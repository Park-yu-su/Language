package com.example.language.ui.study

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.language.R
import com.example.language.adapter.WordPagerAdapter
import com.example.language.data.WordData
import com.example.language.databinding.FragmentStudyWordDetailBinding
import com.example.language.ui.home.MainActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudyWordDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudyWordDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentStudyWordDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStudyWordDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //상단 바 제거
        (activity as? MainActivity)?.setUIVisibility(false)

        //일단 임시 데이터
        val wordList: MutableList<WordData> = mutableListOf(
            WordData("word", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example"),
            WordData("word2", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example2"),
            WordData("word3", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example3"),
            WordData("word4", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example4"),
            WordData("word5", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example5")
        )
        val startIndex = 2 // 이건 차후 ViewModel로

        //viewpager 연결
        binding.wordViewPager.adapter = WordPagerAdapter(this, wordList)
        binding.wordViewPager.setCurrentItem(startIndex, false)

        //뒤로가기
        binding.wordDetailBackBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StudyWordDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StudyWordDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}