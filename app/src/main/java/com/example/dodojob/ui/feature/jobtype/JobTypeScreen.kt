package com.example.dodojob.ui.feature.jobtype

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun JobTypeScreen(nav: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F7))
    ) {
        // Status Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color(0xFFEFEFEF))
                .align(Alignment.TopCenter)
        )

        // Body
        Column(
            modifier = Modifier
                .padding(top = 25.dp, start = 16.dp, end = 16.dp, bottom = 10.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = "원하는 일자리가\n어떻게 되시나요?",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                lineHeight = 45.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 선택 옵션들
            OptionItem("급여형")
            OptionItem("단기알바")
            OptionItem("원격")
            OptionItem("봉사")

            Spacer(modifier = Modifier.height(40.dp))

            // 다음 버튼
            Button(
                onClick = { nav.navigate("nextScreen") }, // 원하는 화면 route 지정
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(10.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FFF))
            ) {
                Text(
                    text = "다음",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        // Navigation Bar (하단)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp)
                .background(Color(0xFFF4F5F7))
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun OptionItem(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            // 선택 버튼 (체크박스 모양)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFD9D9D9)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.White, shape = CircleShape)
                )
            }
        }
    }
}