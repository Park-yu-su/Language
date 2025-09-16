package com.example.language.ui.friend

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.R
import com.example.language.adapter.FriendListAdapter
import com.example.language.data.FriendData
import com.example.language.databinding.FragmentFriendListBinding
import com.example.language.viewModel.FriendViewModel

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
    private val viewModel: FriendViewModel by activityViewModels()

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

        //임시 데이터 셋
        friendList.add(FriendData("친구1", "자기소개"))
        friendList.add(FriendData("친구2", "자기소개"))
        friendList.add(FriendData("친구3", "자기소개"))

        settingRecyclerView()

        //LiveData 관찰
        viewModel.friendList.observe(viewLifecycleOwner) { newList ->
            friendList = newList
            adatper.notifyDataSetChanged()
        }
        viewModel.isDelete.observe(viewLifecycleOwner){ isDelete ->
            adatper.isDelete = isDelete
            Log.d("log_friend", "FriendListFragment 친구삭제 모드: $isDelete")
            adatper.notifyDataSetChanged()
        }

    }


    private fun settingRecyclerView(){
        adatper = FriendListAdapter(friendList, isDelete = viewModel.isDelete.value ?: false,
            onAlarmClicked = {

        })
        binding.friendRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.friendRecyclerview.adapter = adatper
    }


}