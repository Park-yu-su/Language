package com.example.language.ui.makeVoc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.adapter.VocListAdapter
import com.example.language.data.VocData
import com.example.language.databinding.FragmentMakeVocBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MakeVocFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MakeVocFragment : Fragment() {

    private var _binding: FragmentMakeVocBinding? = null
    private val binding get() = _binding!!

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        _binding = FragmentMakeVocBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //임시 데이터
        var vocList = mutableListOf(
            VocData("고등 필수 단어 100", mutableListOf("고등"), "owner1"),
            VocData("토익 필수 단어", mutableListOf("토익", "커스텀"), "owner1"),
            VocData("내 중등 단어장", mutableListOf("중등", "커스텀"), "owner2"),
            VocData("IT 개발 용어", mutableListOf("업무", "커스텀", "어려움"), "owner3")
        )

        val adapter = VocListAdapter(vocList,
            onItemClicked = {
                val action = MakeVocFragmentDirections.actionMakeVocFragmentToAddVocInExitFragment()
                findNavController().navigate(action)
            })

        binding.makeVocRecyclerview.adapter = adapter
        binding.makeVocRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        binding.addNewVocBtn.setOnClickListener {
            val action = MakeVocFragmentDirections.actionMakeVocFragmentToAddNewVocFragment()
            findNavController().navigate(action)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MakeVocFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MakeVocFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}