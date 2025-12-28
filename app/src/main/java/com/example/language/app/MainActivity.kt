package com.example.language.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.language.core.designsystem.LanguageTheme
import com.example.language.core.designsystem.LanguageTypography
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() // Android 15부터는 강제 적용, 그 밑 버전은 X
        super.onCreate(savedInstanceState)
        setContent {
            // 나중에 여기에 Navigation이 들어갑니다.
            LanguageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background // 내가 설정한 테마의 background 색상으로 설정됨
                ) {
                    Text(
                        text = "Hello, World!",
                        style = LanguageTypography.titleLarge // 폰트 사용 예시
                    )
                }
            }
        }
    }
}