package com.example.language.ui.friend

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.R
import com.example.language.adapter.FriendAddAdapter
import com.example.language.data.FriendData
import com.example.language.databinding.FragmentFriendAddBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendAddFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendAddFragment : Fragment() {

    private lateinit var binding: FragmentFriendAddBinding

    //어댑터 및 정보 체크
    private lateinit var adapter: FriendAddAdapter
    private var hasResult = mutableListOf<FriendData>()
    private var tmpResult = mutableListOf<FriendData>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendAddBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingRecyclerview()
        
        tmpResult.add(
            FriendData("7130", "핑구", "", "자기소개")
        )
        tmpResult.add(
            FriendData("3174", "뽀로로","", "자기소개")
        )

        //EditText의 입력 처리
        binding.friendAddSearchEdt.addTextChangedListener(object : TextWatcher {

            //입력한 후 처리
            override fun afterTextChanged(s: Editable?) {
                val nowText = s.toString().trim()
                hasResult.clear()
                
                //여기서 View 처리
                if(nowText.isEmpty()){
                    updateView(nowText, false)
                    adapter.notifyDataSetChanged()
                    
                }
                
                //내용이 있을 때 -> 원래는 API 호출로 체크
                else{
                    for(item in tmpResult){
                        if(item.id == nowText){
                            updateView(nowText, true)
                            hasResult.add(item)
                            adapter.notifyDataSetChanged()
                            break
                        }
                        else{
                            updateView(nowText, false)
                            adapter.notifyDataSetChanged()
                        }
                    }
                    
                }
                
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

    }

    //recyclerView 어댑터 정의
    private fun settingRecyclerview(){
        adapter = FriendAddAdapter(hasResult,
            onRequestClick = { friendData ->
                //이거 추가 버튼 눌렀을 때 처리
                //이미 친구이거나 이미 요청을 보냈거나 했을 경우
                
            })

        binding.friendAddSearchResultRecyclerview.adapter = adapter
        binding.friendAddSearchResultRecyclerview.layoutManager =
            LinearLayoutManager(requireContext())
    }

    //nowText(EditTExt)의 여부에 따라 보여주는 화면 체인지
    private fun updateView(nowText: String, hasResult: Boolean){
        //초기 상태
        if(nowText.isEmpty()){
            binding.friendSearchInfoLl.visibility = View.VISIBLE
            binding.friendSearchNoresultLl.visibility = View.GONE
            binding.friendAddSearchResultRecyclerview.visibility = View.GONE
        }
        //검색 했는데, 결과 X
        else if(!hasResult){
            binding.friendSearchInfoLl.visibility = View.GONE
            binding.friendSearchNoresultLl.visibility = View.VISIBLE
            binding.friendAddSearchResultRecyclerview.visibility = View.GONE
        }
        //검색 결과 O
        else{
            binding.friendSearchInfoLl.visibility = View.GONE
            binding.friendSearchNoresultLl.visibility = View.GONE
            binding.friendAddSearchResultRecyclerview.visibility = View.VISIBLE
        }
    }



}