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
import androidx.navigation.fragment.findNavController
import com.example.language.R
import com.example.language.api.ApiResponse
import com.example.language.api.login.UserPreference
import com.example.language.api.mypage.MypageRepository
import com.example.language.api.mypage.viewModel.MypageViewModel
import com.example.language.api.mypage.viewModel.MypageViewModelFactory
import com.example.language.databinding.DialogCustomEditBinding
import com.example.language.databinding.FragmentMypageMyprofileBinding
import com.example.language.ui.home.MainActivity
import kotlin.getValue

class MypageMyprofileFragment : Fragment(), BottomSheetUserImageDialog.OnImageSelectedListener {

    private lateinit var binding: FragmentMypageMyprofileBinding

    // 현재 선택된 이미지 값을 저장하는 변수
    private var currentImageValue: String = "0"

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

        // 프로필 이미지 클릭 시 다이얼로그 띄우기
        binding.userProfileImage.setOnClickListener {
            showUserImageDialog()
        }

        binding.myprofileBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

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

            myPageViewModel.updateUserInfo(requireContext(),email, nickname, currentImageValue, oneline)


        }

    }

    private fun showUserImageDialog() {
        val dialog = BottomSheetUserImageDialog.newInstance()
        dialog.show(childFragmentManager, BottomSheetUserImageDialog.TAG)
    }

    // [인터페이스 구현] 변경 버튼 누르면 이 함수가 실행됨
    override fun onProfileImageUpdated(resId: Int) {
        // 1. 내 프로필 이미지 변경 (UI)
        binding.userProfileImage.setImageResource(resId)

        // 2. 서버 전송용 값("0"~"3")으로 변환하여 임시 저장
        currentImageValue = when(resId) {
            R.drawable.img_default_user1 -> "0"
            R.drawable.img_default_user2 -> "1"
            R.drawable.img_default_user3 -> "2"
            R.drawable.img_default_user4 -> "3"
            else -> "0"
        }
        // 2. 서버 통신 등 필요한 로직 수행
        // viewModel.updateProfile(resId)
    }

    private fun setUI(){
        (activity as MainActivity).setUIVisibility(false)
        binding.userProfileNicknameTv.text = userPreference.getName()
        binding.myIdTv.text = userPreference.getUid()
        binding.selfIntroduceEt.setText(userPreference.getOneline())

        // [이미지 초기화 로직] 저장된 값("0"~"3")을 가져와서 리소스 ID로 변환 후 세팅
        val savedImageValue = userPreference.getImage() ?: "0"
        currentImageValue = savedImageValue // 현재 값 동기화

        val imageResId = when(savedImageValue) {
            "0" -> R.drawable.img_default_user1
            "1" -> R.drawable.img_default_user2
            "2" -> R.drawable.img_default_user3
            "3" -> R.drawable.img_default_user4
            else -> R.drawable.img_default_user1
        }
        binding.userProfileImage.setImageResource(imageResId)
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
                    // 변경된 이미지 값 저장
                    userPreference.updateImage(currentImageValue)
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