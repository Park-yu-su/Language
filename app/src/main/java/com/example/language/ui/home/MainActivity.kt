package com.example.language.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.language.R
import com.example.language.api.friend.viewModel.FriendViewModel
import com.example.language.api.study.viewModel.StudyViewModel
import com.example.language.databinding.ActivityMainBinding
import com.kakao.sdk.common.util.Utility

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var bottomInset = 0

    //검색용 ViewModel
    private val searchViewModel: StudyViewModel by viewModels()
    private val friendViewModel: FriendViewModel by viewModels()
    private var showMode = 0

    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //bottomNav 연결
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController
        //binding.mainBnv.setupWithNavController(navController)
        setupBottomNavListener(navController)

        //우상단 버튼을 누를 경우, showMode의 값에 따라 다른 ViewMoel 값을 변경한다.
        binding.mainSearchBtn.setOnClickListener {
            if(showMode == 1) {
                searchViewModel.searchEventStart.value = true
            }
            else if(showMode == 2){
                friendViewModel.friendEventStart.value = true
            }
        }

        setBackPressHandle()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            bottomInset = systemBars.bottom
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    private fun setupBottomNavListener(navController: NavController){
        binding.mainBnv.setOnItemSelectedListener { item ->
            val selectedId = item.itemId
            // 현재 위치와 이동하려는 위치가 다를 때만 실행
            if (selectedId != navController.currentDestination?.id) {
                //NavOptions 설정: 스택 초기화 옵션
                val navOptions = NavOptions.Builder()
                    // 메인 NavGraph의 시작 지점(nav_graph_home)으로 돌아갑니다.
                    // inclusive = false: nav_graph_home은 그대로 두고, 그 위에 쌓인 것만 제거합니다.
                    //                  -> 이렇게 해야 탭 복귀 시 해당 탭의 시작점으로 돌아갑니다.
                    // R.id.nav_graph는 최상위 NavGraph의 ID입니다.
                    .setPopUpTo(navController.graph.id, false)

                    // 새로운 탭을 시작할 때 스택 최상단에 하나만 유지하도록 함
                    .setLaunchSingleTop(true)
                    .build()

                // 새로운 탭의 NavGraph ID로 이동
                navController.navigate(selectedId, null, navOptions)
                return@setOnItemSelectedListener true
            }

            // 같은 탭을 재클릭했을 때 스택 맨 위로 돌아가도록 처리 (선택 사항)
            // navController.popBackStack(selectedId, false)

            return@setOnItemSelectedListener false
        }
    }

    //뒤로 가기 세팅
    private fun setBackPressHandle() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                val isPoppedLocally = navController.popBackStack()

                if (!isPoppedLocally) {
                    Log.d("log_back", "맨 끝임")

                    // ⭐ 1. 현재 선택된 BNV 항목의 ID를 가져옵니다. ⭐
                    val selectedTabId = binding.mainBnv.selectedItemId

                    // 2. 현재 선택된 ID가 Home 탭의 ID와 같지 않은지 확인합니다.
                    if (selectedTabId != R.id.nav_graph_home) { // R.id.nav_graph_home은 BottomNav 메뉴의 ID
                        Log.d("log_back", "홈으로 (탭 전환 필요)")

                        // Home 탭으로 이동 (nav_graph_home ID는 BottomNav 메뉴 ID이자, NavGraph ID)
                        navController.navigate(R.id.nav_graph_home)

                        // BNV 아이콘 동기화 (이미 눌러져 있지만, 명시적으로)
                        binding.mainBnv.selectedItemId = R.id.nav_graph_home

                    } else {
                        // ⭐ 3. 현재 BNV 선택 항목이 R.id.nav_graph_home이고, pop할 스택이 없을 때 ⭐
                        Log.d("log_back", "죽어 (종료)")

                        // 콜백 비활성화 후 Activity 종료
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }


    //상단 바만 안보이게 하기
    fun setUIVisibilityOnlyTopbar(){
        val fragmentParams = binding.mainFragmentContainer.layoutParams as ConstraintLayout.LayoutParams
        binding.mainTopbar.visibility = View.GONE
        binding.mainBnv.visibility = View.VISIBLE

        binding.mainFragmentContainer.layoutParams = fragmentParams
    }

    /** 상단 바 관련 로직 **/
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
    //mytitle: topbar에 적힐 이름 / isBackVisible: 뒤로가기 버튼 보여주기 / isBule: topbar 색깔 (파랑/흰색)
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

    //우상단 버튼 보여주기 및 로직
    fun showToprightIcon(boolean: Boolean, mode: Int){
        binding.mainSearchBtn.visibility = if (boolean) View.VISIBLE else View.INVISIBLE

        if(mode == 1){
            showMode = 1
            binding.mainSearchBtn.setImageResource(R.drawable.ic_search)
        }
        else if(mode == 2){
            showMode = 2
            binding.mainSearchBtn.setImageResource(R.drawable.ic_friend_handle)
        }
    }

}