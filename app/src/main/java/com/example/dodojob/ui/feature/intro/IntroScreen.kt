package com.example.dodojob.ui.feature.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dodojob.R
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.navigation.NavController
import com.example.dodojob.navigation.Route
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

@Composable
fun IntroScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate(Route.Onboarding.path) {
            popUpTo(Route.Intro.path) { inclusive = true }
        }
    }

    val paperlogy = FontFamily(
        Font(R.font.paperlogy_bold, FontWeight.Bold)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F7))
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // ✅ 이미지 로고
            Image(
                painter = painterResource(id = R.drawable.intro),
                contentDescription = "앱 로고",
                modifier = Modifier.size(120.dp)
            )

            // ✅ 커스텀 폰트 적용된 텍스트
            Text(
                text = "하고싶은 일을,\n다시 할 수 있게 두두잡",
                fontFamily = paperlogy,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 36.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
    }
}
