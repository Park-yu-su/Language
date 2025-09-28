package com.example.language.ui.friend

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.language.R
import com.example.language.databinding.FragmentFriendBinding
import com.example.language.viewModel.FriendViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentFriendBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    //ViewModel 생성
    private val viewModel: FriendViewModel by activityViewModels()


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
        binding = FragmentFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //navGraph 연결
        navHostFragment = childFragmentManager.findFragmentById(R.id.friend_fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

        //친구목록 버튼 누를 때
        binding.friendListBtn.setOnClickListener {
            //색깔 바꾸기
            binding.friendRequestBtn.setBackgroundResource(R.drawable.bg_btn_white_press)
            binding.friendRequestTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorFont))
            binding.friendListBtn.setBackgroundResource(R.drawable.bg_btn_primary_press)
            binding.friendListTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            //친구 목록 표시
            navController.navigate(R.id.FriendListFragment)

        }
        //친구요청목록 버튼 누를 때
        binding.friendRequestBtn.setOnClickListener {
            //색깔 바꾸기
            binding.friendListBtn.setBackgroundResource(R.drawable.bg_btn_white_press)
            binding.friendListTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorFont))
            binding.friendRequestBtn.setBackgroundResource(R.drawable.bg_btn_primary_press)
            binding.friendRequestTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            //친구 요청 목록 표시
            navController.navigate(R.id.FriendRequestFragment)
        }

        //친구 삭제 버튼
        binding.friendDeleteFriendBtn.setOnClickListener {
            binding.friendDeleteFriendBtn.visibility = View.INVISIBLE
            binding.friendDeleteFinishFriendBtn.visibility = View.VISIBLE
            viewModel.isDelete.value = true
            Log.d("log_friend", "friendFragment에서 친구삭제모드를 true로 변경")
        }
        //친구 삭제완료 버튼
        binding.friendDeleteFinishFriendBtn.setOnClickListener {
            binding.friendDeleteFinishFriendBtn.visibility = View.INVISIBLE
            binding.friendDeleteFriendBtn.visibility = View.VISIBLE
            viewModel.isDelete.value = false
            Log.d("log_friend", "friendFragment에서 친구삭제모드를 false로 변경")
        }
        //친구 추가 버튼
        binding.friendAddFriendBtn.setOnClickListener {
            //나를 호스팅하는 navController get
            navController.navigate(R.id.FriendListFragment)
            findNavController().navigate(R.id.action_friendFragment_to_friendAddFragment)
        }


    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FriendFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}