package com.example.language.ui.mypage

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.language.R
import com.example.language.api.ApiResponse
import com.example.language.api.login.UserPreference
import com.example.language.api.mypage.MypageRepository
import com.example.language.api.mypage.viewModel.MypageViewModel
import com.example.language.api.mypage.viewModel.MypageViewModelFactory
import com.example.language.data.VocData
import com.example.language.databinding.DialogCustomEditBinding
import com.example.language.databinding.DialogCustomSelectBinding
import com.example.language.databinding.FragmentMypageMyprofileBinding
import com.example.language.ui.home.MainActivity
import kotlin.getValue

class MypageMyprofileFragment : Fragment() {

    private lateinit var binding: FragmentMypageMyprofileBinding


    private lateinit var userPreference : UserPreference

    private val myPageRepository = MypageRepository()
    private val myPageViewModel: MypageViewModel by activityViewModels() {
        MypageViewModelFactory(myPageRepository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMypageMyprofileBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference(requireContext())

        //초기 UI 설정
        setUI()
        observeChange()

        //UID 복사
        binding.idCopyBtn.setOnClickListener {
            copyUID()
        }

        //닉네임 수정
        binding.userNicknameEditBtn.setOnClickListener {
            showRenameDialog()
        }

        //확인
        binding.myInfoSaveBtn.setOnClickListener {
            var popanim = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.button_pop)
            binding.myInfoSaveBtn.startAnimation(popanim)

            var stringUid = userPreference.getUid() ?: "0"
            var uid = stringUid.toInt()
            var email = userPreference.getEmail() ?: ""
            var nickname = binding.userProfileNicknameTv.text.toString()
            var oneline = binding.selfIntroduceEt.text.toString()
            /**차후 설정**/
            var image = "0"

            myPageViewModel.updateUserInfo(requireContext(),email, nickname, image, oneline)


        }

    }

    private fun setUI(){
        (activity as MainActivity).setUIVisibility(false)
        binding.userProfileNicknameTv.text = userPreference.getName()
        binding.myIdTv.text = userPreference.getUid()
        binding.selfIntroduceEt.setText(userPreference.getOneline())

        /**이미지 타입 설정 (차후)**/
        var imageType = userPreference.getImage()
    }

    private fun copyUID(){
        //1. Context에서 ClipboardManager 서비스 가져오기
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        //2. 복사할 TextView의 텍스트를 가져옵니다.
        val textToCopy = binding.myIdTv.text.toString()

        //3. 텍스트를 ClipData 객체로 래핑
        //ClipData.newPlainText(레이블, 복사할 텍스트)
        val clip = ClipData.newPlainText("Copied Text", textToCopy)

        //4. 클립보드에 데이터 설정
        clipboard.setPrimaryClip(clip)

        Toast.makeText(requireContext(), "클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show()
    }


    private fun observeChange(){
        myPageViewModel.loginResult.observe(viewLifecycleOwner) { response ->
            when(response){
                is ApiResponse.Success -> {
                    Log.d("log_mypage", "정보 수정 성공")
                    Toast.makeText(requireContext(), "정보가 수정되었습니다", Toast.LENGTH_SHORT).show()
                    userPreference.updateName(binding.userProfileNicknameTv.text.toString())
                    userPreference.updateOneline(binding.selfIntroduceEt.text.toString())
                    /**차후 수정**/
                    userPreference.updateImage("0")
                }
                is ApiResponse.Error -> {
                    Log.d("log_mypage", "정보 수정 실패")
                    Toast.makeText(requireContext(), "정보 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }


    //커스텀 다이얼로그 띄우기
    private fun showRenameDialog(){
        //1. 바인딩 생성
        val dialogBinding = DialogCustomEditBinding.inflate(layoutInflater)

        //2. 다이얼로그 생성
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        //다이얼로그 투명
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        //3. 버튼 리스너
        dialogBinding.dialogCancelCdv.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.dialogOkCdv.setOnClickListener {
            //여기서 실질적인 추가 로직 (API)
            var stringUid = userPreference.getUid() ?: "0"
            var uid = stringUid.toInt()
            var newNickName = dialogBinding.dialogMessageTv.text.toString()
            binding.userProfileNicknameTv.text = newNickName
            
            dialog.dismiss()
        }

        dialog.show()

    }

}