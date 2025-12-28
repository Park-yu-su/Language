package com.example.language.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.language.R

// 폰트 패밀리 정의
val Pretendard = FontFamily(
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold)
)

val Freesentation = FontFamily(
    Font(R.font.freesentation_4regular, FontWeight.Normal),
    Font(R.font.freesentation_5medium, FontWeight.Medium),
    Font(R.font.freesentation_6semibold, FontWeight.SemiBold),
    Font(R.font.freesentation_7bold, FontWeight.Bold)
)

// 타이포그래피 매핑
val LanguageTypography = Typography(
    // 큰 제목 (22sp Semibold)
    titleLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = Black
    ),
    // 중간 제목 (20sp Semibold)
    titleMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = Black
    ),
    // 본문 강조 (16sp Semibold)
    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = Black
    ),
    // 본문 일반 (16sp Medium)
    bodyMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = Black
    ),
    // 작은 본문 강조 (14sp Semibold)
    bodySmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = Black
    ),
    // 라벨/캡션 (12sp Medium)
    labelSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = Gray2
    )
)

