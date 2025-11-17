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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun IntroScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(1500)
        navController.navigate(Route.Onboarding.path) {
            popUpTo(Route.Intro.path) { inclusive = true }
        }
    }

    // 🔵 전체 화면 이미지 스플래시
    Box(
        Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash), // ← 전체 이미지 넣기
            contentDescription = "두두잡 인트로",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
