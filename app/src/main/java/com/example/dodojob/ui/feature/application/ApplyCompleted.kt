package com.example.dodojob.ui.feature.application

import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.dodojob.R

@Composable
fun ApplyCompletedScreen(
    onAnyClick: () -> Unit = {}
) {
    val Bg = Color(0xFFF1F5F7)
    val StatusBg = Color(0xFFEFEFEF)
    val BottomBg = Color(0xFFF4F5F7)
    val HandleColor = Color(0xFF909AA6)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .clickable { onAnyClick() },
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg)
        ) {
            // 상단 상태바 (24dp 높이)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(StatusBg)
                    .align(Alignment.TopCenter)
            )



            // 가운데 메인 컨텐츠 영역 (완전 중앙 정렬)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(328.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.complete_image),
                    contentDescription = "지원 완료 체크 표시",
                    modifier = Modifier
                        .size(69.dp)
                )

                Text(
                    text = "지원 완료!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 32.sp,
                        lineHeight = 48.sp,
                        letterSpacing = (-0.019).em,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
