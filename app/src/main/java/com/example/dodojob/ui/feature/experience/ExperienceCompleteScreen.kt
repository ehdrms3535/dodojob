package com.example.dodojob.ui.feature.experience

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.navigation.Route
import com.example.dodojob.R
import kotlinx.coroutines.delay

@Composable
fun ExperienceCompleteScreen(nav: NavController) {
    val Bg = Color(0xFFF1F5F7)

    var navigated by remember { mutableStateOf(false) }

    // ⏱ 8초 뒤 자동 이동
    LaunchedEffect(Unit) {
        delay(8000)
        if (!navigated) {
            navigated = true
            nav.navigate(Route.Main.path)
        }
    }

    Scaffold(
        containerColor = Bg
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(Bg)
                .clickable {
                    if (!navigated) {
                        navigated = true
                        nav.navigate(Route.Main.path)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ✅ 중앙 체크 이미지
                Image(
                    painter = painterResource(id = R.drawable.complete_image),
                    contentDescription = "완성 체크",
                    modifier = Modifier.size(69.dp)
                )

                Spacer(Modifier.height(10.dp))

                // ✅ 중앙 텍스트
                Text(
                    text = "프로필 완성!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }
    }
}
