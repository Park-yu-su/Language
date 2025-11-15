package com.example.language.ui.mypage

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.language.R
import com.example.language.adapter.WordListAdapter
import com.example.language.data.WordData
import com.example.language.databinding.DialogCustomSelectBinding
import com.example.language.databinding.FragmentMypageMyvocDetailBinding
import androidx.core.graphics.drawable.toDrawable

class MypageMyvocDetailFragment : Fragment() {

    private var _binding: FragmentMypageMyvocDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WordListAdapter
    private var fragmentWordList: MutableList<WordData> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMypageMyvocDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.manageMyVocDetailBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        val container = binding.tagLayout
        val inflater = LayoutInflater.from(requireContext())

        container.removeAllViews()

        // 임시 데이터
        val tagList = listOf("고등", "토익", "커스텀")

        for (tagText in tagList) {
            val itemView = inflater.inflate(R.layout.item_tag, container, false)
            val textView = itemView.findViewById<TextView>(R.id.tag_id_tv)

            textView.text = tagText

            container.addView(itemView)
        }

        // [ ✨ RecyclerView 초기 설정 ✨ ]
        // (fragmentWordList는 현재 비어있음)
        setupRecyclerView()

        // TODO: (중요) 실제 데이터를 가져온 후 어댑터에 알려야 합니다.
        // 예: viewModel.wordList.observe(viewLifecycleOwner) { words ->
        //    fragmentWordList.clear()
        //    fragmentWordList.addAll(words)
        //    adapter.notifyDataSetChanged() // 어댑터 새로고침
        // }

        binding.deleteVocBtn.setOnClickListener {
            // showDeleteVocDialog()
        }

        binding.setTagBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mypageMyVocDetailFragment_to_mypageTagFragment)
        }

        binding.addWordBtn.setOnClickListener {
            // 1. 전달할 인자("isReturnMode")를 Bundle로 만듭니다.
            val bundle = bundleOf("isReturnMode" to true)

            findNavController().navigate(
                R.id.action_mypageMyvocDetailFragment_to_selectWayAddVocFragment,
                bundle
            )
        }

        binding.deleteWordBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mypageMyvocDetailFragment_to_mypageDeleteWordFragment)
        }

        // ⭐️ 'SelectWay...'에서 보낸 결과를 리스닝
        // (결과를 받을 Key 이름은 "dialog_completed"로 정합니다)
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.mypageMyvocDetailFragment)

        navBackStackEntry.savedStateHandle.getLiveData<Boolean>("dialog_completed")
            .observe(viewLifecycleOwner) { result ->
                if (result == true) {
                    // 다이얼로그 작업이 완료되어 복귀함
                    Toast.makeText(requireContext(), "단어 추가 완료", Toast.LENGTH_SHORT).show()
                    // TODO: (선택) 여기서 단어장 상세 화면 데이터를 새로고침 (예: viewModel.loadDetails())

                    // ⭐️ 중요: 결과를 사용한 후에는 반드시 제거해야 재실행 방지
                    navBackStackEntry.savedStateHandle.remove<Boolean>("dialog_completed")
                }
            }
    }

    /**
     * RecyclerView와 어댑터를 초기화합니다.
     */
    private fun setupRecyclerView() {
        adapter = WordListAdapter(
            fragmentWordList, // 비어있는 로컬 리스트로 어댑터 생성

            onItemClicked = { wordData, position ->
                // (클릭 로직)
                // 예: Toast.makeText(requireContext(), "${wordData.word} 클릭됨", Toast.LENGTH_SHORT).show()
            },
            onTTSRequest = { }
        )

        binding.mypageWordListRecyclerview.adapter = adapter
        binding.mypageWordListRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        // ⭐️ [핵심] RecyclerView에 리스너를 추가하여 자식 View가 생성될 때마다 개입합니다.
        binding.mypageWordListRecyclerview.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {

            // 이 함수는 각 아이템 View가 RecyclerView에 붙을 때 호출됩니다.
            override fun onChildViewAttachedToWindow(view: View) {
                // view는 item_word.xml 레이아웃 전체를 의미합니다.
                // 여기서 숨기고 싶은 버튼(ttsBtn)을 찾습니다.
                val ttsButton = view.findViewById<View>(R.id.study_listen_imv)

                // 찾았다면, 해당 버튼을 숨깁니다.
                ttsButton?.visibility = View.GONE
            }

            // 이 함수는 아이템 View가 떨어져 나갈 때 호출됩니다. (여기서는 필요 없음)
            override fun onChildViewDetachedFromWindow(view: View) {
                // 필요시 로직 추가
            }
        })
    }

    /**
     * 단어장 삭제 여부를 묻는 커스텀 다이얼로그를 띄웁니다.
     */
    private fun showDeleteVocDialog(vocName: String) {
        // 1. 바인딩 생성 (XML과 일치)
        val dialogBinding = DialogCustomSelectBinding.inflate(layoutInflater)

        // ⭐️ 2. [수정] 내용 채우기 (요청하신 텍스트 형식으로)
        val message = "${vocName}를 \n삭제하시겠습니까?"
        dialogBinding.dialogMessageTv.text = message

        // ⭐️ [수정] 버튼 텍스트 (XML 레이아웃에 맞게)
        dialogBinding.dialogOkTv.text = "삭제"
        dialogBinding.dialogCancelTv.text = "취소"

        // 3. 다이얼로그 생성
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // 다이얼로그 투명
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        // 4. [수정] 취소 버튼 리스너
        dialogBinding.dialogCancelCdv.setOnClickListener {
            dialog.dismiss()
        }

        // 5. ⭐️ [수정] 'OK' (추가) 버튼 리스너
        dialogBinding.dialogOkCdv.setOnClickListener {
            // TODO: 여기서 실질적인 단어장 삭제 로직 (API)
            Toast.makeText(requireContext(), "${vocName} 삭제 완료", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}