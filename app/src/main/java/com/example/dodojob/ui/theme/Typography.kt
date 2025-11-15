package com.example.dodojob.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.dodojob.R

val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

fun Typography.usePretendard(): Typography {
    return Typography(
        displayLarge = displayLarge.copy(fontFamily = Pretendard),
        displayMedium = displayMedium.copy(fontFamily = Pretendard),
        displaySmall = displaySmall.copy(fontFamily = Pretendard),

        headlineLarge = headlineLarge.copy(fontFamily = Pretendard),
        headlineMedium = headlineMedium.copy(fontFamily = Pretendard),
        headlineSmall = headlineSmall.copy(fontFamily = Pretendard),

        titleLarge = titleLarge.copy(fontFamily = Pretendard),
        titleMedium = titleMedium.copy(fontFamily = Pretendard),
        titleSmall = titleSmall.copy(fontFamily = Pretendard),

        bodyLarge = bodyLarge.copy(fontFamily = Pretendard),
        bodyMedium = bodyMedium.copy(fontFamily = Pretendard),
        bodySmall = bodySmall.copy(fontFamily = Pretendard),

        labelLarge = labelLarge.copy(fontFamily = Pretendard),
        labelMedium = labelMedium.copy(fontFamily = Pretendard),
        labelSmall = labelSmall.copy(fontFamily = Pretendard),
    )
}

val AppTypography = Typography().usePretendard()