package com.example.language.ui.study

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.language.R
import com.example.language.databinding.FragmentStudyBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudyFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentStudyBinding

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController




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
        binding = FragmentStudyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navHostFragment = childFragmentManager.findFragmentById(R.id.study_fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

        //내 단어장 버튼 클릭
        binding.studyMyVocBtn.setOnClickListener {
            binding.studyMyVocBtn.setBackgroundResource(R.drawable.bg_btn_primary_press)
            binding.studyMyVocTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.studySearchBtn.setBackgroundResource(R.drawable.bg_btn_white_press)
            binding.studySearchTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorFont))
            navController.navigate(R.id.studyVoclistFragment)
        }
        //단어장 검색 버튼 클릭
        binding.studySearchBtn.setOnClickListener {
            binding.studyMyVocBtn.setBackgroundResource(R.drawable.bg_btn_white_press)
            binding.studyMyVocTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorFont))
            binding.studySearchBtn.setBackgroundResource(R.drawable.bg_btn_primary_press)
            binding.studySearchTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            navController.navigate(R.id.studyVocsearchFragment)


        }

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StudyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StudyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}