package com.example.dodojob.ui.feature.jobtype

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.dodojob.navigation.Route
import com.example.dodojob.session.CurrentUser

@Composable
fun JobTypeScreen(nav: NavController) {
    val Bg = Color(0xFFF1F5F7)
    val Primary = Color(0xFF005FFF)
    val options = listOf(" 급여형", " 단기알바", " 원격", " 봉사")
    var selected by remember { mutableStateOf(options.first()) } // 스샷처럼 기본 '급여형' 선택

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
                        CurrentUser.setJob(selected)
                        nav.navigate(Route.Hope.path)
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("다음", fontSize = 25.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    ) { inner ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            val W = maxWidth
            val H = maxHeight

            val hPad = (W * 0.045f)
            val titleTop = (H * 0.03f)
            val backSp = (W.value * 0.065f).sp
            val titleSp = (W.value * 0.09f).sp
            val itemGap = 12.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = hPad)
            ) {
                Spacer(Modifier.height(titleTop))

                // 🔙 뒤로가기 "<"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "<",
                        fontSize = backSp,
                        color = Color.Black,
                        modifier = Modifier.clickable { nav.popBackStack() }
                    )
                }

                // 제목
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "원하는 일자리가\n어떻게 되시나요?",
                    fontSize = titleSp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    lineHeight = 45.sp
                )

                Spacer(Modifier.height(60.dp))

                // 옵션 리스트
                options.forEach { title ->
                    OptionItemSelectable(
                        title = title,
                        selected = selected == title,
                        onClick = { selected = title }
                    )
                    Spacer(Modifier.height(itemGap))
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun OptionItemSelectable(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val badgeSize = 40.dp      // ⬅️ 체크 원 크기
    val iconSize  = 30.dp      // ⬅️ 체크 아이콘 크기
    val textSp    = 25.sp      // ⬅️ 글자 크기
    // 카드형 + 우측 원형 체크 (선택: 파랑/흰 체크, 미선택: 연회색/회색 체크)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = Color.White,
        shadowElevation = 1.dp // 살짝 떠 보이는 느낌
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = textSp,
                color = Color(0xFF111315),
                fontWeight = FontWeight.Medium
            )

            // 체크 뱃지
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .clip(CircleShape)
                    .background(if (selected) Color(0xFF2A77FF) else Color(0xFFE6E6E6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = if (selected) Color.White else Color(0xFFBDBDBD),
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}