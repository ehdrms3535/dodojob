package com.example.dodojob.ui.feature.experience

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val Primary = Color(0xFF005FFF)

    // ✅ 이동 여부 관리 (중복 이동 방지)
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
        containerColor = Bg,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Bg)
                    .padding(horizontal = 18.dp, vertical = 50.dp)
            ) {
                Button(
                    onClick = {
                        if (!navigated) {
                            navigated = true
                            nav.navigate(Route.Main.path)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("완료", fontSize = 25.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .clickable { // 👆 화면 아무데나 터치 시 이동
                    if (!navigated) {
                        navigated = true
                        nav.navigate(Route.Main.path)
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ✅ 체크 이미지
            Image(
                painter = painterResource(id = R.drawable.complete_image),
                contentDescription = "완성 체크",
                modifier = Modifier.size(96.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "프로필 완성!",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}
