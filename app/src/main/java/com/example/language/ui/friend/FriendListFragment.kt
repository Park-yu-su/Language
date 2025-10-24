package com.example.language.ui.friend

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.R
import com.example.language.adapter.FriendListAdapter
import com.example.language.api.ApiResponse
import com.example.language.api.friend.FriendRepository
import com.example.language.api.friend.viewModel.FriendViewModel
import com.example.language.api.friend.viewModel.FriendViewModelFactory
import com.example.language.api.login.UserPreference
import com.example.language.data.FriendData
import com.example.language.databinding.FragmentFriendListBinding
import com.example.language.ui.home.MainActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendListFragment : Fragment() {

    private lateinit var binding: FragmentFriendListBinding
    private var friendList: MutableList<FriendData> = mutableListOf() //친구 목록 데이터
    private lateinit var adatper : FriendListAdapter

    //FriendFragment가 소유한 ViewModel 인스턴스를 사용
    private val friendRepository = FriendRepository()
    private val friendViewModel: FriendViewModel by activityViewModels() {
        FriendViewModelFactory(friendRepository)
    }

    //유저 UID 가져오기
    private lateinit var userPreference : UserPreference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFriendListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference(requireContext())

        //상단 바
        (activity as? MainActivity)?.setUIVisibility(true)

        (activity as MainActivity).setTopBar("친구", false, true)
        (activity as MainActivity).showToprightIcon(true, 2)

        //친구 기능 관찰
        friendViewModel.friendEventStart.observe(viewLifecycleOwner){start ->
            if(start){
                findNavController().navigate(R.id.action_FriendListFragment_to_friendHandleFragment)
                friendViewModel.friendEventStart.value = false
            }
        }

        //친구 리스트 API 호출
        observeFriendList()
        getFriendListByAPI()


        //임시 데이터 셋
        /*
        friendList.add(FriendData("1","친구1", "자기소개"))
        friendList.add(FriendData("2","친구2", "자기소개"))
        friendList.add(FriendData("3","친구3", "자기소개"))

         */

        settingRecyclerView()



    }

    override fun onDestroyView() {
        super.onDestroyView()
        //(activity as MainActivity).showToprightIcon(false, 2)
    }


    //친구 목록을 가져오기
    private fun getFriendListByAPI(){
        var stringUid = userPreference.getUid() ?: "0"
        var uid = stringUid.toInt()
        if(uid != 0) {
            friendViewModel.getFriendList(requireContext(), 1)
        }
    }

    //친구 목록 결과 obserce
    private fun observeFriendList() {
        friendViewModel.friendListResult.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResponse.Success -> {
                    friendList.clear()
                    Log.d("log_friend", "성공 : ${response.data}")

                    val uids = response.data.uids
                    val nicknames = response.data.nicknames
                    val images = response.data.images

                    for(i in 0 until uids.size){
                        friendList.add(FriendData(uids[i], nicknames[i], images[i], ""))
                    }
                    adatper.notifyDataSetChanged()

                }

                is ApiResponse.Error -> {
                    Log.d("log_friend", "실패 : ${response.message}")
                    Toast.makeText(context, "친구 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    //recyclerView 세팅하기
    private fun settingRecyclerView(){
        adatper = FriendListAdapter(friendList,
            onAlarmClicked = {

        })
        binding.friendRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.friendRecyclerview.adapter = adatper
    }


}