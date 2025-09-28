package com.example.language.ui.friend

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.R
import com.example.language.adapter.FriendAddAdapter
import com.example.language.adapter.FriendListAdapter
import com.example.language.adapter.FriendRequestAdapter
import com.example.language.data.FriendData
import com.example.language.databinding.FragmentFriendRequestBinding
import com.example.language.viewModel.FriendViewModel
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendRequestFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendRequestFragment : Fragment() {

    private lateinit var binding: FragmentFriendRequestBinding

    private var requestList: MutableList<FriendData> = mutableListOf() //친구 목록 데이터
    private lateinit var adatper : FriendRequestAdapter

    //FriendFragment가 소유한 ViewModel 인스턴스를 사용
    private val viewModel: FriendViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFriendRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestList.add(
            FriendData("22", "사람입니다", "자기소개")
        )
        requestList.add(
            FriendData("33", "너도?", "자기소개")
        )

        //recyclerView 세팅
        settingRecyclerView()

    }

    private fun settingRecyclerView() {
        adatper = FriendRequestAdapter(
            requestList,
            
            //일단은 외부 API 없이 바로 지우는 걸로
            onAccept = { friend -> 
                val index = requestList.indexOfFirst { it.id == friend.id }
                if(index != -1){
                    requestList.removeAt(index)
                    adatper.notifyItemRemoved(index)
                    Toast.makeText(requireContext(), "수락", Toast.LENGTH_SHORT).show()
                }
            },
            onReject = {friend ->
                val index = requestList.indexOfFirst { it.id == friend.id }
                if(index != -1){
                    requestList.removeAt(index)
                    adatper.notifyItemRemoved(index)
                    Toast.makeText(requireContext(), "거절", Toast.LENGTH_SHORT).show()
                }
                
            })

        binding.friendRequestRecyclerview.adapter = adatper
        binding.friendRequestRecyclerview.layoutManager = LinearLayoutManager(requireContext())
    }

}