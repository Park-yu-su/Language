package com.example.language.ui.study

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.R
import com.example.language.adapter.WordListAdapter
import com.example.language.data.WordData
import com.example.language.databinding.FragmentStudyWordlistBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudyWordlistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudyWordlistFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentStudyWordlistBinding

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
        binding = FragmentStudyWordlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //일단 임시 데이터
        val wordList: MutableList<WordData> = mutableListOf(
            WordData("word", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example"),
            WordData("word2", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example2"),
            WordData("word3", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example3"),
            WordData("word4", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example4"),
            WordData("word5", mutableListOf("meanings", "meanings", "meanings", "meanings"), "example5")
        )

        val adapter = WordListAdapter(wordList, onItemClicked = {
            val action = StudyWordlistFragmentDirections.actionStudyWordlistFragmentToStudyWordDetailFragment()
            findNavController().navigate(action)
        })
        binding.studyWordRecyclerview.adapter = adapter
        binding.studyWordRecyclerview.layoutManager = LinearLayoutManager(requireContext())

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StudyWordlistFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StudyWordlistFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}