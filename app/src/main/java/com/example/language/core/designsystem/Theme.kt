package com.example.language.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 라이트 모드 색상표
private val LightColorScheme = lightColorScheme(
    primary = MainBlue,
    onPrimary = PureWhite,
    secondary = SubYellow,
    onSecondary = Black,
    background = BackgroundWhite,
    surface = PureWhite,
    onSurface = Black,
    outline = Gray3,
    error = RedStroke
)

// (다크 모드는 추후 필요하면 설정)
// private val DarkColorScheme = darkColorScheme(...)

@Composable
fun LanguageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // 현재는 Light 모드 고정 (원하면 분기 처리 가능)

    // 상태바(Status Bar) 색상 설정
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // 아이콘 색상만 테마에 맞게 설정 (배경이 밝으면 아이콘을 어둡게)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LanguageTypography,
        content = content
    )
}