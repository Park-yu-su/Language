package com.example.language.ui.study

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.language.R
import com.example.language.adapter.VocSearchIdAdapter
import com.example.language.data.VocData
import com.example.language.databinding.DialogCustomSelectBinding
import com.example.language.databinding.FragmentStudySearchTagBinding

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


    private var hasResult = mutableListOf<VocData>()
    //임시 data
    private var tmpResult = mutableListOf<VocData>()

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

        settingRecyclerView()
        /*
        tmpResult.add(VocData("단어장 이름1", mutableListOf("태그1", "태그2"), "7130"))
        tmpResult.add(VocData("단어장 이름2", mutableListOf("태그1", "태그2"), "7130"))


         */
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


    }


    private fun settingRecyclerView(){
        adapter = VocSearchIdAdapter(hasResult,
            onItemClicked = { vocData ->
                shoAddVocDialog(vocData.title)
            })
        binding.searchtagSearchResultRecyclerview.adapter = adapter
        binding.searchtagSearchResultRecyclerview.layoutManager =
            LinearLayoutManager(requireContext())
    }

    //커스텀 다이얼로그 띄우기
    private fun shoAddVocDialog(vocName: String){
        //1. 바인딩 생성
        val dialogBinding = DialogCustomSelectBinding.inflate(layoutInflater)

        //2. 내용 채우기
        val message = "${vocName}를\n 내 단어장에 추가하시겠습니까?"
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
            Toast.makeText(requireContext(), "단어장 추가 완료", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()

    }



    override fun onDestroyView() {
        super.onDestroyView()
    }
}