package com.example.language.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.language.R
import com.example.language.api.ApiResponse
import com.example.language.api.login.LoginRepository
import com.example.language.api.login.UserPreference
import com.example.language.api.login.viewModel.LoginViewModel
import com.example.language.api.login.viewModel.LoginViewModelFactory
import com.example.language.databinding.ActivityLoginBinding
import com.example.language.ui.home.MainActivity
import com.kakao.sdk.auth.TokenManagerProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    //API 테스트
    private var loginRepository = LoginRepository()
    private val LoginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(loginRepository)
    }
    //sharedPref
    private lateinit var userPreference: UserPreference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //sharedPref 초기화
        userPreference = UserPreference(this)

        //옵저빙
        observeLoginResult()

        //해시 키 출력
        val keyHash = Utility.getKeyHash(this)
        Log.d("Hash", keyHash)

        //유효 체크
        checkKakaoValid()

        //카카오 로그인 버튼 터치 시 로그인 로직
        binding.loginBtn.setOnClickListener {
            kakaoLogin()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    //현재 토큰이 유효한지 체크(자동 로그인)
    private fun checkKakaoValid(){
        // 로그인 정보 확인
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (error != null) {
                Log.d("log_login", "토큰 정보 보기 실패")
            }
            else if (tokenInfo != null) {
                // 토큰 정보 보기
                Log.i("log_login", "accessTokenInfo 성공, 회원번호: ${tokenInfo.id}, 만료시간: ${tokenInfo.expiresIn}")
                // 토큰 그대로 꺼내서 로그 찍기
                val token = TokenManagerProvider
                    .instance
                    .manager
                    .getToken()
                    ?.accessToken
                Log.d("log_login", "기존 토큰 유효 AccessToken: $token")
                //그럼 유저 정보 get
                getInfoWithAPI()

            }
        }
    }

    //카카오 로그인 로직
    private fun kakaoLogin(){

        //콜백 함수 로직
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Toast.makeText(this, "접근이 거부 되었습니다(동의 취소)", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                        Toast.makeText(this, "유효하지 않은 앱", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                        Toast.makeText(this, "인증 수단이 유효하지 않아 인증할 수 없는 상태입니다", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                        Toast.makeText(this, "요청 파라미터 오류입니다", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                        Toast.makeText(this, "유효하지 않은 scope ID 입니다", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                        Toast.makeText(this, "설정이 올바르지 않습니다(android key hash)", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.ServerError.toString() -> {
                        Toast.makeText(this, "서버 내부 에러입니다", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                        Toast.makeText(this, "앱이 요청 권한이 없습니다", Toast.LENGTH_SHORT).show()
                    }
                    else -> { // Unknown
                        Toast.makeText(this, "기타 에러입니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if (token != null) {
                Log.d("log_login", "카카오 로그인 성공: ${token.accessToken}")

                //그럼 유저 정보 get
                getInfoWithAPI()

            }
        }

        val activityContext = this@LoginActivity

        //UserApiClient로 체크
        //카카오톡 이용
        if(UserApiClient.instance.isKakaoTalkLoginAvailable(activityContext)){
            UserApiClient.instance.loginWithKakaoTalk(
                activityContext,
                callback = callback)
        }
        //웹뷰로 가서 처리
        else{
            UserApiClient.instance.loginWithKakaoAccount(
                activityContext,
                callback = callback)
        }
    }

    //엑세스 토큰을 이용해 유저 정보 get
    private fun getInfoWithAPI(){
        UserApiClient.instance.me { user, error ->
            //에러로 유저 정보 불러오기 실패
            if(error != null){
                Log.i("log_login", "사용자 정보 요청 실패", error)
                Toast.makeText(this, "사용자 정보 요청 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
            //성공
            else if(user != null){
                val nickname = user.kakaoAccount?.profile?.nickname
                val email = user.kakaoAccount?.email
                val profileImageUrl = user.kakaoAccount?.profile?.profileImageUrl

                Log.d("log_login", "닉네임: $nickname")
                Log.d("log_login", "이메일: $email")
                Log.d("log_login", "프로필 이미지: $profileImageUrl")

                /**API 시작**/
                LoginViewModel.requestLogin(this, email!!, nickname!!)

                /*
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()

                 */
            }
        }
    }

    //API 요청 후 observe하기
    private fun observeLoginResult() {
        LoginViewModel.loginResult.observe(this) { response ->
            when (response) {
                is ApiResponse.Success -> {
                    //서버 인증 성공
                    val userData = response.data
                    Log.d("log_login", "서버 인증 성공: UID ${userData.uid}, 닉네임 ${userData.nickname}")

                    //sharedPref 저장
                    userPreference.saveUserInfo(userData.uid, userData.nickname, userData.email)

                    //메인 화면으로 이동
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is ApiResponse.Error -> {
                    //서버 인증/통신 실패
                    Log.e("log_login", "서버 인증 실패: 코드 ${response.code}, 메시지 ${response.message}")
                    Toast.makeText(this, "서버 인증 오류: ${response.message}", Toast.LENGTH_LONG).show()

                    /**메인 화면으로 이동(일단 로컬 임시)**/
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                }
            }
        }
    }


}