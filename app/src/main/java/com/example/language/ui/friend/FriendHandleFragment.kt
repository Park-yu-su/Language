package com.example.language.ui.friend

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.language.R
import com.example.language.adapter.FriendAddAdapter
import com.example.language.adapter.FriendRequestAdapter
import com.example.language.data.FriendData
import com.example.language.databinding.FragmentFriendHandleBinding
import com.example.language.ui.home.MainActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendHandleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendHandleFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentFriendHandleBinding
    
    //recyclerview 관련
    //1. 요청 목록
    private var requestList: MutableList<FriendData> = mutableListOf() //친구 목록 데이터
    private lateinit var requestAdatper : FriendRequestAdapter

    //2. 삭제 목록

    //3. 검색 관련(추가)
    private lateinit var addAdapter: FriendAddAdapter
    private var searchResult = mutableListOf<FriendData>()
    private var tmpResult = mutableListOf<FriendData>()


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
        binding = FragmentFriendHandleBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //상단 바 제거
        (activity as? MainActivity)?.setUIVisibility(false)

        //더미 데이터
        requestList.add(
            FriendData("22", "사람입니다", "자기소개")
        )
        requestList.add(
            FriendData("33", "너도?", "자기소개")
        )
        tmpResult.add(
            FriendData("7130", "핑구", "자기소개")
        )
        tmpResult.add(
            FriendData("3174", "뽀로로", "자기소개")
        )

        //EditText listenr -> request/delete or add를 보여준다.
        binding.friendHandleSearchEdt.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                val nowText = p0.toString().trim()
                //기존 꺼 바꿔
                searchResult.clear()

                //현재 비어 있으면 -> 요청이랑 친구 삭제 목록을 보여준다.
                if(nowText.isEmpty()){
                    updateView(nowText)
                }

                //내용이 있으면 -> 검색 목록을 보여준다. -> API
                else{
                    for(item in tmpResult){
                        searchResult.add(item)
                        addAdapter.notifyDataSetChanged()
                    }
                    updateView(nowText)
                }


            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })


        //친구 요청
        settingRequestRecyclerView()
        //친구 검색 및 추가
        settingAddRecyclerView()

    }

    //EditText의 내용 여부에 따라 아래에 보여주는 화면을 다르게 표시
    private fun updateView(nowText: String){
        //초기 상태 (검색 X)
        if(nowText.isEmpty()){
            binding.friendHandleDoubleRecyclerView.visibility = View.VISIBLE
            binding.friendHandleSearchRecyclerView.visibility = View.GONE
        }

        //검색을 한 경우
        else{
            binding.friendHandleDoubleRecyclerView.visibility = View.GONE
            binding.friendHandleSearchRecyclerView.visibility = View.VISIBLE
        }
    }




    //친구요청목록 recyclerview 관리
    private fun settingRequestRecyclerView(){
        requestAdatper = FriendRequestAdapter(
            requestList,

            //일단은 외부 API 없이 바로 지우는 걸로
            onAccept = { friend ->
                val index = requestList.indexOfFirst { it.id == friend.id }
                if(index != -1){
                    requestList.removeAt(index)
                    requestAdatper.notifyItemRemoved(index)
                    Toast.makeText(requireContext(), "수락", Toast.LENGTH_SHORT).show()
                }
            },
            onReject = {friend ->
                val index = requestList.indexOfFirst { it.id == friend.id }
                if(index != -1){
                    requestList.removeAt(index)
                    requestAdatper.notifyItemRemoved(index)
                    Toast.makeText(requireContext(), "거절", Toast.LENGTH_SHORT).show()
                }

            })

        binding.friendRequestRecyclerview.adapter = requestAdatper
        binding.friendRequestRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        //nested 관련
        binding.friendRequestRecyclerview.isNestedScrollingEnabled = false
    }

    //친구삭제할까목록 recyclerView

    //친구검색및추가 recyclerview
    private fun settingAddRecyclerView(){
        addAdapter = FriendAddAdapter(searchResult, onRequestClick = {
            /**여기서는 검색한 친구 추가 시 로직을 처리**/


        })
        binding.friendHandleSearchRecyclerView.adapter = addAdapter
        binding.friendHandleSearchRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendHandleFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FriendHandleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}