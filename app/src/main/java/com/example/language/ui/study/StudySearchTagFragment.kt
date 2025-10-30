package com.example.language.ui.study

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
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.R
import com.example.language.adapter.VocSearchIdAdapter
import com.example.language.api.ApiResponse
import com.example.language.api.TagData
import com.example.language.api.login.UserPreference
import com.example.language.api.study.StudyRepository
import com.example.language.api.study.viewModel.StudyViewModel
import com.example.language.api.study.viewModel.StudyViewModelFactory
import com.example.language.data.VocData
import com.example.language.databinding.DialogCustomSelectBinding
import com.example.language.databinding.FragmentStudySearchTagBinding
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudySearchTagFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudySearchTagFragment : Fragment() {

    private lateinit var binding: FragmentStudySearchTagBinding

    private lateinit var adapter: VocSearchIdAdapter

    //태그 검색 결과
    private var tagResult = mutableListOf<TagData>()


    private var hasResult = mutableListOf<VocData>()
    //임시 data
    private var inputString : String = ""


    //API 연결을 위한 수단
    private val studyRepository = StudyRepository()
    private val studyViewModel: StudyViewModel by activityViewModels(){
        StudyViewModelFactory(studyRepository)
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
        binding = FragmentStudySearchTagBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference(requireContext())
        updateView(1) //초기 화면

        settingRecyclerView()

        observeSearchTagId()
        observeWordbookByTag()

        //버튼을 누를 때 editText 세팅
        binding.searchtagSearchImv.setOnClickListener {
            handleTextToTag()

        }

        //editText에서 enter 버튼
        binding.searchtagSearchEdt.setOnKeyListener { _, keyCode, event ->
            // Enter 키 (또는 Done 키)를 누르고, 키 누름 이벤트(ACTION_DOWN)일 때만 처리
            if (keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.action == android.view.KeyEvent.ACTION_DOWN) {

                // handleTextToTag() 함수 호출
                handleTextToTag()

                // 키보드를 숨기는 것이 일반적입니다. (선택 사항)
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchtagSearchEdt.windowToken, 0)

                // 이벤트를 소비하여 추가적인 기본 동작을 막습니다. (예: 새 줄 생성)
                return@setOnKeyListener true
            }
            // 다른 키 이벤트는 처리하지 않고 false 반환
            return@setOnKeyListener false
        }
        
    



    }
    
    //editText 값을 파싱
    private fun handleTextToTag(){
        //editText 받기
        inputString = binding.searchtagSearchEdt.text.toString().trim()
        //비면 내용 없기 = 초기 화면(1)
        if(inputString.isBlank()){
            updateView(1)
            return
        }

        studyViewModel.searchTag(requireContext(), inputString)

    }

    //tag id 존재하는지 observe
    private fun observeSearchTagId() {
        studyViewModel.searchTagResult.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResponse.Success -> {
                    tagResult.clear()

                    var tmptmp = mutableListOf<String>()
                    val tags = response.data.data
                    for (tag in tags) {
                        tagResult.add(tag)
                        tmptmp.add(tag.name)
                    }

                    Log.d("log_study", "태그 검색 성공 : ${tmptmp}")

                    //비어 있으면 결과 X
                    if(tagResult.isEmpty()){
                        updateView(2)
                    }
                    //태그가 있으면 단어장 O
                    else{
                        updateView(3)
                        getWordbookByTag()
                    }

                }

                is ApiResponse.Error -> {
                    Log.d("log_study", "태그 검색 실패 : ${response.message}")
                    Toast.makeText(context, "태그 검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //태그들을 줘서 단어장 정보를 받는 함수
    private fun getWordbookByTag(){
        var tidList = mutableListOf<Int>()
        for(tag in tagResult){
            tidList.add(tag.tid)
        }
        studyViewModel.searchWordbookByTag(requireContext(), tidList)
    }
    //tag로 얻은 단어장들 observe
    private fun observeWordbookByTag(){
        studyViewModel.searchTagWordbookResult.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ApiResponse.Success -> {
                    hasResult.clear()
                    Log.d("log_study", "태그로 단어장 불러오기 성공 : ${response.data}")

                    var wordbooks = response.data.data
                    for(wordbook in wordbooks){
                        hasResult.add(VocData(wordbook.wid, wordbook.title, wordbook.tags, ""))
                    }
                    adapter.notifyDataSetChanged()

                }
                is ApiResponse.Error -> {
                    Log.d("log_study", "태그로 단어장 불러오기 실패 : ${response.message}")
                    Toast.makeText(context, "단어장 로드 중 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
                }
            }
    }
    

    //view에 따라 변경 (type : 1=초기 상태 / 2=검색 결과 X / 3=검색 결과 O
    private fun updateView(type : Int){
        //초기 상태
        if(type == 1){
            binding.searchtagSearchInfoLl.visibility = View.VISIBLE
            binding.searchtagSearchNoresultLl.visibility = View.GONE
            binding.searchtagSearchResultRecyclerview.visibility = View.GONE
        }
        //검색 했는데, 결과 X
        else if(type == 2){
            binding.searchtagSearchInfoLl.visibility = View.GONE
            binding.searchtagSearchNoresultLl.visibility = View.VISIBLE
            binding.searchtagSearchResultRecyclerview.visibility = View.GONE
        }
        //검색 결과 O
        else if(type == 3){
            binding.searchtagSearchInfoLl.visibility = View.GONE
            binding.searchtagSearchNoresultLl.visibility = View.GONE
            binding.searchtagSearchResultRecyclerview.visibility = View.VISIBLE
        }
        else{
            binding.searchtagSearchInfoLl.visibility = View.VISIBLE
            binding.searchtagSearchNoresultLl.visibility = View.GONE
            binding.searchtagSearchResultRecyclerview.visibility = View.GONE
        }
    }


    //recyclerview 설정
    private fun settingRecyclerView(){
        adapter = VocSearchIdAdapter(hasResult,
            onItemClicked = { vocData ->
                shoAddVocDialog(vocData)
            })
        binding.searchtagSearchResultRecyclerview.adapter = adapter
        binding.searchtagSearchResultRecyclerview.layoutManager =
            LinearLayoutManager(requireContext())
    }

    //커스텀 다이얼로그 띄우기
    private fun shoAddVocDialog(voc : VocData){
        //1. 바인딩 생성
        val dialogBinding = DialogCustomSelectBinding.inflate(layoutInflater)

        //2. 내용 채우기
        val message = "${voc.title}를\n 내 단어장에 추가하시겠습니까?"
        dialogBinding.dialogMessageTv.text = message

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
            var stringUid = userPreference.getUid() ?: "0"
            var uid = stringUid.toInt()

            Toast.makeText(requireContext(), "단어장 추가 완료", Toast.LENGTH_SHORT).show()
            studyViewModel.subscribeWordbook(requireContext(), voc.wid, uid)
            dialog.dismiss()
        }

        dialog.show()

    }



    override fun onDestroyView() {
        super.onDestroyView()
    }
}


/*
        binding.searchtagSearchEdt.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                val nowText = p0.toString().trim()
                //기존 꺼 바꿔
                hasResult.clear()
                //현재 비어 있으면
                if(nowText.isEmpty()){
                    adapter.notifyDataSetChanged()
                }
                //내용이 있으면 -> API
                else{
                    for(item in tmpResult){
                        hasResult.add(item)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        })
        */