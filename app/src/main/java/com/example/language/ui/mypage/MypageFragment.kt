package com.example.language.ui.mypage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.language.R
import com.example.language.api.ApiResponse
import com.example.language.api.SubscribedWordbooksData
import com.example.language.api.friend.FriendRepository
import com.example.language.api.friend.viewModel.FriendViewModel
import com.example.language.api.friend.viewModel.FriendViewModelFactory
import com.example.language.api.login.UserPreference
import com.example.language.api.mypage.MypageRepository
import com.example.language.api.mypage.viewModel.MypageViewModel
import com.example.language.api.mypage.viewModel.MypageViewModelFactory
import com.example.language.databinding.FragmentMypageBinding
import com.example.language.ui.home.MainActivity
import kotlin.getValue

class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    private lateinit var userPreference : UserPreference

    private val myPageRepository = MypageRepository()
    private val myPageViewModel: MypageViewModel by activityViewModels() {
        MypageViewModelFactory(myPageRepository)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        (activity as MainActivity).setUIVisibilityOnlyTopbar()

//        binding.searchFriendBtn.setOnClickListener {
//            findNavController().navigate(R.id.action_mypageFragment_to_friendAddFragment)
//        }

        userPreference = UserPreference(requireContext())
        
        binding.userProfileNicknameTv.text = userPreference.getName()

        binding.manageMyVocBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mypageFragment_to_mypageMyvocFragment)

        }
        binding.mypageVoc1Iv.setOnClickListener {
            findNavController().navigate(R.id.action_mypageFragment_to_mypageMyvocFragment)
        }
        binding.mypageVoc2Iv.setOnClickListener {
            findNavController().navigate(R.id.action_mypageFragment_to_mypageMyvocFragment)
        }
        
        binding.manageMyInfoBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mypageFragment_to_mypageMyprofileFragment)
        }
        binding.mypageSettingBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mypageFragment_to_mypageSettingFragment)
        }

        observeMywordbook()
        //단어장 가져오기
        getMywordbook()
        
    }
    
    //단어 리스트 가져오기
    private fun getMywordbook(){
        var stringUid = userPreference.getUid() ?: "0"
        var uid = stringUid.toInt()
        myPageViewModel.getSubscribedWordbooks(requireContext(), uid)
    }
    
    //API 결과 관찰
    private fun observeMywordbook() {
        myPageViewModel.wordbookListResult.observe(viewLifecycleOwner) { response ->
            when(response){
                is ApiResponse.Success -> {
                    Log.d("log_mypage", "단어장 가져오기 성공")
                    val wordbooks = response.data.data

                    myPageViewModel.mywordbookList.clear()
                    for(i in 0 until wordbooks.size) {
                        myPageViewModel.mywordbookList.add(wordbooks[i])
                    }

                    if(wordbooks.size > 1){
                        showVocUI(2)
                        putVocData1(wordbooks[0])
                        putVocData2(wordbooks[1])
                    }
                    else if(wordbooks.size == 1){
                        showVocUI(1)
                        putVocData1(wordbooks[0])
                    }

                    else{showVocUI(0)}
                }
                is ApiResponse.Error -> {
                    Log.d("log_mypage", "단어장 가져오기 실패")
                }
            }
        }
    }
    

    //0 = 0개 또는 오류 / 1 = 1개 / 2 = 2개
    private fun showVocUI(mode : Int){
        if(mode == 0){
            binding.myapgeVoc1Cl.visibility = View.GONE
            binding.myapgeVoc2Cl.visibility = View.GONE
            binding.myapgeView.visibility = View.GONE
            binding.myapgeVocnoTv.visibility = View.VISIBLE
        }
        else if(mode == 1){
            binding.myapgeVoc1Cl.visibility = View.VISIBLE
            binding.myapgeVoc2Cl.visibility = View.GONE
            binding.myapgeView.visibility = View.GONE
            binding.myapgeVocnoTv.visibility = View.GONE
        }
        else{
            binding.myapgeVoc1Cl.visibility = View.VISIBLE
            binding.myapgeVoc2Cl.visibility = View.VISIBLE
            binding.myapgeView.visibility = View.VISIBLE
            binding.myapgeVocnoTv.visibility = View.GONE
        }
    }

    private fun putVocData1(data : SubscribedWordbooksData){
        binding.vocName1Tv.text = data.title
        binding.vocTag1Tv.text = data.tags[0]
    }
    private fun putVocData2(data : SubscribedWordbooksData){
        binding.vocName2Tv.text = data.title
        binding.vocTag2Tv.text = data.tags[0]
    }
    
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}