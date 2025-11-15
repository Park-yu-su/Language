package com.example.language.ui.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.adapter.VocListAdapter
import com.example.language.data.VocData
import com.example.language.databinding.FragmentMypageMyvocBinding

class MypageMyvocFragment : Fragment() {

    private var _binding: FragmentMypageMyvocBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMypageMyvocBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.manageMyVocBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        // [ ✨ (가정) API 대신 임시 데이터 사용 ✨ ]
        // (실제로는 ViewModel.getSubscribedWordbooks() 등을 호출해야 함)
        val vocList = mutableListOf(
            VocData(1, "고등 필수 단어 100", listOf("고등"), "owner1"),
            VocData(2, "토익 필수 단어", listOf("토익", "커스텀"), "owner1"),
            VocData(3, "내 중등 단어장", listOf("중등", "커스텀"), "owner2")
        )

        // [ ✨ 3. 어댑터 클릭 리스너 (핵심) ✨ ]
        // (어댑터가 클릭된 VocData 객체를 람다로 전달한다고 가정)
        val adapter = VocListAdapter(vocList,
            onItemClicked = { clickedVocData ->

                // 1. 클릭된 단어장의 wid(Int)를 String으로 변환
                val selectedVocId = clickedVocData.wid.toString()

                // 2. Safe Args를 사용해 *vocId만* AddVocInExitFragment로 전달
                val action = MypageMyvocFragmentDirections
                    .actionMypageMyvocFragmentToMypageMyvocDetailFragment(selectedVocId)

                findNavController().navigate(action)
            })

        binding.manageMyVocRecyclerview.adapter = adapter
        binding.manageMyVocRecyclerview.layoutManager = LinearLayoutManager(requireContext())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}