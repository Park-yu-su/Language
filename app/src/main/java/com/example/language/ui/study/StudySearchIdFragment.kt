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
import com.example.language.api.login.UserPreference
import com.example.language.api.study.StudyRepository
import com.example.language.api.study.viewModel.StudyViewModel
import com.example.language.api.study.viewModel.StudyViewModelFactory
import com.example.language.data.FriendData
import com.example.language.data.VocData
import com.example.language.data.WordData
import com.example.language.databinding.DialogCustomSelectBinding
import com.example.language.databinding.FragmentStudySearchIdBinding
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudySearchIdFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudySearchIdFragment : Fragment() {

    private lateinit var binding: FragmentStudySearchIdBinding

    private lateinit var adapter: VocSearchIdAdapter


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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStudySearchIdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        userPreference = UserPreference(requireContext())
        updateView(1) //초기

        settingRecyclerView()
        observeSearchId()


        //버튼을 누를 때 editText 세팅
        binding.searchidSearchImv.setOnClickListener {
            handleTextToId()

        }

        //editText에서 enter 버튼
        binding.searchidSearchEdt.setOnKeyListener { _, keyCode, event ->
            // Enter 키 (또는 Done 키)를 누르고, 키 누름 이벤트(ACTION_DOWN)일 때만 처리
            if (keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.action == android.view.KeyEvent.ACTION_DOWN) {

                // handleTextToTag() 함수 호출
                handleTextToId()

                // 키보드를 숨기는 것이 일반적입니다. (선택 사항)
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchidSearchEdt.windowToken, 0)

                // 이벤트를 소비하여 추가적인 기본 동작을 막습니다. (예: 새 줄 생성)
                return@setOnKeyListener true
            }
            // 다른 키 이벤트는 처리하지 않고 false 반환
            return@setOnKeyListener false
        }


        binding.searchidSearchInfoTv.setOnClickListener {
            val bottomSheet = BottomSheetDialog.newInstance()
            // requireFragmentManager() 또는 childFragmentManager 사용
            bottomSheet.show(childFragmentManager, BottomSheetDialog.TAG)

        }

    }

    private fun settingRecyclerView(){
        adapter = VocSearchIdAdapter(hasResult,
            onItemClicked = { vocData ->
                shoAddVocDialog(vocData)
            })
        binding.searchidSearchResultRecyclerview.adapter = adapter
        binding.searchidSearchResultRecyclerview.layoutManager =
            LinearLayoutManager(requireContext())
    }

    //editText 값을 파싱
    private fun handleTextToId(){
        //editText 받기
        inputString = binding.searchidSearchEdt.text.toString().trim()
        //비면 내용 없기 = 초기 화면(1)
        if(inputString.isBlank()){
            updateView(1)
            return
        }

        var checkwid = inputString.toIntOrNull()
        if(checkwid != null){
            studyViewModel.searchWordbookById(requireContext(), checkwid)
        }
        else{
            Toast.makeText(requireContext(), "단어장 ID를 정확히 입력해주세요.", Toast.LENGTH_SHORT).show()
        }

    }

    //ID 검색 결과 observe
    private fun observeSearchId(){
        studyViewModel.searchIdwordbookResult.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ApiResponse.Success -> {
                    updateView(3) //검색 찾음
                    val result = response.data.data //검색한 단어장의 정보가 담김
                    hasResult.clear()
                    Log.d("log_study", "ID 검색 성공 : ${result}")

                    hasResult.add(VocData(result.wid, result.title, result.tags, ""))
                    adapter.notifyDataSetChanged()

                }
                is ApiResponse.Error -> {
                    Log.d("log_study", "ID 검색 실패 : ${response.message}")
                    updateView(2) //검색 결과 없음
                }
            }
        }
    }


    //nowText(EditTExt)의 여부에 따라 보여주는 화면 체인지
    //view에 따라 변경 (type : 1=초기 상태 / 2=검색 결과 X / 3=검색 결과 O
    private fun updateView(type: Int){
        //초기 상태
        if(type == 1){
            binding.searchidSearchInfoLl.visibility = View.VISIBLE
            binding.searchidSearchNoresultLl.visibility = View.GONE
            binding.searchidSearchResultRecyclerview.visibility = View.GONE
        }
        //검색 했는데, 결과 X
        else if(type == 2){
            binding.searchidSearchInfoLl.visibility = View.GONE
            binding.searchidSearchNoresultLl.visibility = View.VISIBLE
            binding.searchidSearchResultRecyclerview.visibility = View.GONE
        }
        //검색 결과 O
        else if(type == 3){
            binding.searchidSearchInfoLl.visibility = View.GONE
            binding.searchidSearchNoresultLl.visibility = View.GONE
            binding.searchidSearchResultRecyclerview.visibility = View.VISIBLE
        }
        else{
            binding.searchidSearchInfoLl.visibility = View.VISIBLE
            binding.searchidSearchNoresultLl.visibility = View.GONE
            binding.searchidSearchResultRecyclerview.visibility = View.GONE
        }
    }

    //커스텀 다이얼로그 띄우기
    private fun shoAddVocDialog(voc: VocData){
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


}