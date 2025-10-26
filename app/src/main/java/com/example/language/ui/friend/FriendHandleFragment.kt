package com.example.language.ui.friend

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.language.R
import com.example.language.adapter.FriendAddAdapter
import com.example.language.adapter.FriendDeleteAdapter
import com.example.language.adapter.FriendRequestAdapter
import com.example.language.api.ApiResponse
import com.example.language.api.friend.FriendRepository
import com.example.language.api.friend.viewModel.FriendViewModel
import com.example.language.api.friend.viewModel.FriendViewModelFactory
import com.example.language.api.login.UserPreference
import com.example.language.data.FriendData
import com.example.language.databinding.DialogCustomSelectBinding
import com.example.language.databinding.FragmentFriendHandleBinding
import com.example.language.ui.home.MainActivity
import kotlin.getValue

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
    private var friendList: MutableList<FriendData> = mutableListOf() //친구 목록 데이터
    private lateinit var deleteAdapter : FriendDeleteAdapter

    //3. 검색 관련(추가)
    private lateinit var addAdapter: FriendAddAdapter
    private var searchResult = mutableListOf<FriendData>()
    private var tmpResult = mutableListOf<FriendData>()

    //API
    private val friendRepository = FriendRepository()
    private val friendViewModel: FriendViewModel by activityViewModels() {
        FriendViewModelFactory(friendRepository)
    }

    //유저 UID 가져오기
    private lateinit var userPreference : UserPreference


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
        userPreference = UserPreference(requireContext())

        //더미 데이터(요청 리스트)
        /*
        requestList.add(
            FriendData("22", "사람입니다", "","자기소개")
        )
        requestList.add(
            FriendData("33", "너도?", "","자기소개")
        )
        */

        tmpResult.add(
            FriendData("7130", "핑구", "","자기소개")
        )
        tmpResult.add(
            FriendData("3174", "뽀로로", "","자기소개")
        )
        //임시 데이터 셋(친구 리스트-삭제 때)
        /*
        friendList.add(FriendData("1","친구1", "","자기소개"))
        friendList.add(FriendData("2","친구2", "","자기소개"))
        friendList.add(FriendData("3","친구3", "","자기소개"))
         */


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

        binding.friendHandleBackBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        //친구 요청
        settingRequestRecyclerView()
        //친구 검색 및 추가
        settingAddRecyclerView()
        //친구 삭제
        settingDeleteRecycleView()
        //API로 친구 리스트 불러오기
        getFriendList()
        //친구 요청 리스트 API 호출
        observePendingList()
        getPendingList()

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

    //친구 목록을 가져오기(이미 FriendList에서 받아온 정보)
    private fun getFriendList(){
        var response = friendViewModel.friendListResult.value

        when (response) {
            is ApiResponse.Success -> {
                //성공 응답일 경우에만 data에 안전하게 접근
                val data = response.data
                friendList.clear()

                val uids = data.uids
                val nicknames = data.nicknames
                val images = data.images

                for(i in 0 until uids.size){
                    // FriendData 생성자 순서에 맞춰 데이터를 추가합니다.
                    friendList.add(FriendData(uids[i], nicknames[i], images[i], "현재는 이미지 URL"))
                }

                deleteAdapter.notifyDataSetChanged()
            }

            is ApiResponse.Error -> {
                // 통신 오류 발생 시 처리
                Log.e("log_friend", "친구 핸들링 친구 목록 로드 실패: ${response.message}")
                Toast.makeText(context, "친구 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }

            null -> {
                // LiveData에 아직 값이 설정되지 않았거나 null인 경우
                Log.d("log_friend", "친구 목록 데이터가 ViewModel에 아직 없습니다.")
                Toast.makeText(context, "친구 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //친구 요청 목록 가져오기
    private fun getPendingList(){
        var stringUid = userPreference.getUid() ?: "0"
        var uid = stringUid.toInt()
        Log.d("log_friend", "친구요청리스트 호출 시 내 UID : ${uid}")
        if(uid != 0) {
            friendViewModel.getPendingList(requireContext(), uid, "received")
        }
    }

    //친구 요청 목록 observe
    private fun observePendingList(){
        friendViewModel.pendingListResult.observe(viewLifecycleOwner){ response ->
            when (response) {
                is ApiResponse.Success -> {
                    requestList.clear()
                    Log.d("log_friend", "친구 요청 리스트 불러오기 성공 : ${response.data}")

                    val uids = response.data.uids
                    val nicknames = response.data.nicknames
                    val images = response.data.images

                    for(i in 0 until uids.size){
                        requestList.add(FriendData(uids[i], nicknames[i], images[i], ""))
                    }
                    requestAdatper.notifyDataSetChanged()

                }
                is ApiResponse.Error -> {
                    Log.d("log_friend", "실패 : ${response.message}")
                    Toast.makeText(context, "친구 요청 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
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

                    Log.d("log_friend", "추가할 친구 UID: ${friend.id}")
                    var StringUid = userPreference.getUid() ?: "0"
                    var uid = StringUid.toInt()

                    /**API로 친구 요청 수락**/
                    friendViewModel.acceptFriend(requireContext(), uid, friend.id.toInt())


                }
            },
            onReject = {friend ->
                val index = requestList.indexOfFirst { it.id == friend.id }
                if(index != -1){
                    requestList.removeAt(index)
                    requestAdatper.notifyItemRemoved(index)
                    Toast.makeText(requireContext(), "거절", Toast.LENGTH_SHORT).show()

                    Log.d("log_friend", "거절할 친구 UID: ${friend.id}")
                    var StringUid = userPreference.getUid() ?: "0"
                    var uid = StringUid.toInt()

                    /**API로 친구 요청 거절**/
                    friendViewModel.rejectFriend(requireContext(), uid, friend.id.toInt())

                }

            })

        binding.friendRequestRecyclerview.adapter = requestAdatper
        binding.friendRequestRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        //nested 관련
        binding.friendRequestRecyclerview.isNestedScrollingEnabled = false

        /**미리 API로 requestList 얻었다 가정**/
        if(requestList.size == 0){
            binding.friendRequestEmptyTv.visibility = View.VISIBLE
        }
        else{
            binding.friendRequestEmptyTv.visibility = View.GONE
        }

    }

    //친구삭제할까목록 recyclerView
    private fun settingDeleteRecycleView(){
        deleteAdapter = FriendDeleteAdapter(friendList, onDeleteClicked = { uid, name, adapterposition ->
            /**여기서는 삭제 로직을 수행**/
            showAddVocDialog(uid,name, adapterposition)

        })

        binding.friendDeleteRecyclerview.adapter = deleteAdapter
        binding.friendDeleteRecyclerview.layoutManager =
            LinearLayoutManager(requireContext())

    }

    //친구검색및추가 recyclerview
    private fun settingAddRecyclerView(){
        addAdapter = FriendAddAdapter(searchResult, onRequestClick = {
            /**여기서는 검색한 친구 추가 시 로직을 처리**/


        })
        binding.friendHandleSearchRecyclerView.adapter = addAdapter
        binding.friendHandleSearchRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

    }

    //커스텀 다이얼로그 띄우기
    private fun showAddVocDialog(uid: String, nickname: String, adapterPosition: Int){
        //1. 바인딩 생성
        val dialogBinding = DialogCustomSelectBinding.inflate(layoutInflater)

        //2. 내용 채우기
        val message = "${nickname}를\n 친구 목록에서 삭제하시겠습니까?"
        dialogBinding.dialogMessageTv.text = message
        dialogBinding.dialogOkTv.text = "예"
        dialogBinding.dialogCancelTv.text = "아니오"

        //3. 다이얼로그 생성
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        //다이얼로그 투명
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        //4. 버튼 리스너
        dialogBinding.dialogCancelCdv.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.dialogOkCdv.setOnClickListener {
            //여기서 실질적인 추가 로직 (API)
            Toast.makeText(requireContext(), "찬구 삭제 완료", Toast.LENGTH_SHORT).show()
            //UI 제거
            friendList.removeAt(adapterPosition)
            deleteAdapter.notifyItemRemoved(adapterPosition)
            dialog.dismiss()
            //실제 API 호출
            var StringUid = userPreference.getUid() ?: "0"
            var myUid = StringUid.toInt()
            var friendUid = uid.toInt()
            Log.d("log_friend", "내 UID: ${myUid} / 삭제 UID: ${friendUid}")
            friendViewModel.deleteFriend(requireContext(), myUid, friendUid)

        }

        dialog.show()

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