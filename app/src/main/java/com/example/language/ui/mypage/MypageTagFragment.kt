package com.example.language.ui.mypage

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.language.R
import com.example.language.databinding.FragmentMypageTagBinding

class MypageTagFragment : Fragment() {

    private var _binding: FragmentMypageTagBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMypageTagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setTagBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        val container = binding.tagListLayout
        val inflater = LayoutInflater.from(requireContext())

        container.removeAllViews()

        // 임시 데이터
        val tagList = mutableListOf("고등", "토익", "커스텀")

        for (tagText in tagList) {
            val itemView = inflater.inflate(R.layout.item_tag_with_btn, container, false)
            val textView = itemView.findViewById<TextView>(R.id.tag_id_tv)

            textView.text = tagText

            val editButton = itemView.findViewById<ImageButton>(R.id.edit_tag_btn)
            editButton.setOnClickListener {
                showEditTagDialog(tagText, textView)
                Toast.makeText(requireContext(), "$tagText 수정", Toast.LENGTH_SHORT).show()
            }

            // 삭제 버튼 찾기 및 리스너 설정
            val deleteButton = itemView.findViewById<ImageButton>(R.id.delete_tag_btn)
            deleteButton.setOnClickListener {

                // UI에서 해당 태그 뷰(itemView) 제거
                container.removeView(itemView)

                // 데이터 리스트(tagList)에서도 제거
                tagList.remove(tagText)

                // TODO: (중요) 실제로는 API를 호출해 서버에서도 삭제해야 함
                Toast.makeText(requireContext(), "$tagText 삭제", Toast.LENGTH_SHORT).show()
            }

            container.addView(itemView)
        }
    }

    private fun showEditTagDialog(originalTagText: String, textViewToUpdate: TextView) {
        // 1. 다이얼로그의 커스텀 뷰를 inflate 합니다.
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_edit_tag, null)

        // 2. 커스텀 뷰 내부의 EditText를 찾습니다.
        val editText = dialogView.findViewById<EditText>(R.id.tag_dialog_et)
        val okButton = dialogView.findViewById<LinearLayout>(R.id.tag_dialog_ok_ll)
        val cancelButton = dialogView.findViewById<LinearLayout>(R.id.tag_dialog_cancel_ll)

        // 3. EditText에 기존 텍스트(originalTagText)를 설정합니다.
        editText.setText(originalTagText)

        // 4. AlertDialog를 생성합니다.
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        okButton.setOnClickListener {
            val newTagText = editText.text.toString().trim()

            if (newTagText.isNotEmpty()) {
                // [UI 업데이트] 전달받은 TextView의 텍스트를 변경
                textViewToUpdate.text = newTagText

                // TODO: [데이터 업데이트] tagList의 실제 데이터도 변경
                // (val index = tagList.indexOf(originalTagText) ...)

                // ⭐️ [중요] 모든 작업 완료 후 수동으로 다이얼로그를 닫습니다.
                dialog.dismiss()

            } else {
                Toast.makeText(requireContext(), "태그명은 비워둘 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}