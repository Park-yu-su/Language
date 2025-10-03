package com.example.language.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.language.R
import com.example.language.databinding.ActivityMainBinding
import com.kakao.sdk.common.util.Utility

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var bottomInset = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //bottomNav 연결
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_fragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController
        binding.mainBnv.setupWithNavController(navController)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            bottomInset = systemBars.bottom
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    //특정 fragment에서 상단/하단 바 제어
    fun setUIVisibility(visible: Boolean) {
        val fragmentParams = binding.mainFragmentContainer.layoutParams as ConstraintLayout.LayoutParams

        if (visible) {
            // UI 복구: 툴바와 BNV를 다시 보이게 하고 높이와 제약 조건을 복구합니다.

            // 1. 높이를 복구하고 VISIBLE 설정
            binding.mainTopbar.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.mainBnv.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.mainTopbar.visibility = View.VISIBLE
            binding.mainBnv.visibility = View.VISIBLE

            // 2. 툴바와 BNV의 공간 확보를 즉시 요청
            binding.mainTopbar.requestLayout()
            binding.mainBnv.requestLayout()

            // 3. FragmentContainerView의 제약 조건을 복구 (툴바가 공간을 확보한 후)
            fragmentParams.topToBottom = binding.mainTopbar.id
            fragmentParams.bottomToTop = binding.mainBnv.id

            // 4. 하단 바 맞추기
            binding.mainFragmentContainer.setPadding(0, 0, 0, 0)

        } else {
            // UI 숨김 (기존 로직 유지)

            // 1. FragmentContainerView의 제약 조건을 부모 끝으로 확장
            fragmentParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID


            // 2. 뷰를 숨기고 높이를 0으로 설정하여 공간을 없앰
            binding.mainTopbar.visibility = View.GONE
            binding.mainBnv.visibility = View.GONE

            //3. 세팅
            binding.mainFragmentContainer.setPadding(0, 0, 0, bottomInset)
        }

        // 4. FragmentContainerView에 최종 제약 조건 적용
        binding.mainFragmentContainer.layoutParams = fragmentParams
    }


    //다른 Fragment에서 Topbar 제어를 위한 메소드
    fun setTopBar(mytitle: String, isBackVisible: Boolean, isBlue: Boolean){
        binding.mainTitleTv.text = mytitle
        binding.mainBackBtn.visibility = if (isBackVisible) View.VISIBLE else View.INVISIBLE

        binding.mainTopbar.background = if (isBlue) getDrawable(R.color.Main1_1) else getDrawable(R.color.white)

        val selectedColorStateList: ColorStateList? = ContextCompat.getColorStateList(
            this,
            if (isBlue) R.color.white else R.color.Black
        )

        binding.mainTitleTv.setTextColor(selectedColorStateList)

    }
    fun setTopBar(isBackVisible: Boolean, isShow: Boolean){
        binding.mainTitleTv.text = title
        binding.mainBackBtn.visibility = if (isBackVisible) View.VISIBLE else View.INVISIBLE

    }

}