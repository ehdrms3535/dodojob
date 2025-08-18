package com.example.dodojob.ui.feature.prefer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferWorkMapScreen(nav: NavController) {
    // 들어오면 곧바로 시트가 뜨게
    var showSheet by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 시트 닫기 동작
    fun dismiss() {
        showSheet = false
        nav.popBackStack()
    }

    // 바닥(뒤 배경)은 투명, 시트만 띄워서 "밑에서 서랍" 느낌
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { dismiss() },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = {
                // 상단 핸들바
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(width = 122.dp, height = 4.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFFB3B3B3))
                    )
                }
            }
        ) {
            // ===== 시트 본문 =====
            var query by remember { mutableStateOf("") }
            var slider by remember { mutableStateOf(0.5f) }
            var picked by remember { mutableStateOf("태전동") } // 데모용

            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                // 타이틀
                Text(
                    "일 할 지역을 설정해주세요",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
                )

                // 검색 입력 (회색 박스)
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("지역명", color = Color(0xFFA6A6A6), fontSize = 18.sp) },
                    trailingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFFA6A6A6))
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(57.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFEFEFEF),
                        unfocusedContainerColor = Color(0xFFEFEFEF),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF005FFF)
                    )
                )

                Spacer(Modifier.height(12.dp))
                Divider(color = Color(0xFFCFCFCF))
                Spacer(Modifier.height(12.dp))

                // 지도 컨테이너 (안쪽 GPS/현재지역)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE9E9E9))
                        .border(1.dp, Color(0xFFD0D0D0), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    // 중앙 조준 박스
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(44.dp)
                            .border(2.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
                    )
                    // 좌하단 GPS
                    Icon(
                        imageVector = Icons.Outlined.GpsFixed,
                        contentDescription = "GPS",
                        tint = Color(0xFF343330),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(32.dp)
                    )
                    // 우하단 현재지역 버튼
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x2B005FFF))
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Text("현재지역", color = Color(0xFF005FFF), fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(22.dp))

                // 언더라인 설명
                Text(
                    "$picked 과 근처 동네 102개",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    color = Color.Black
                )

                Spacer(Modifier.height(12.dp))

                // 근/원 슬라이더 (파란색)
                Slider(
                    value = slider,
                    onValueChange = { slider = it },
                    steps = 1,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF005FFF),
                        activeTrackColor = Color(0xFF005FFF),
                        inactiveTrackColor = Color(0xFFB0C4DE)
                    )
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("가까운 동", fontSize = 16.sp)
                    Text("먼 동", fontSize = 16.sp)
                }

                Spacer(Modifier.height(18.dp))

                // 하단 버튼 (적용하기 하나만)
                Button(
                    onClick = {
                        nav.previousBackStackEntry?.savedStateHandle?.set("pickedRegion", picked)
                        dismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FFF))
                ) {
                    Text("적용하기", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    } else {
        // 시트가 닫히면 아무 것도 렌더링하지 않음 (뒤 화면이 그대로 보이도록)
        Box(Modifier.fillMaxSize())
    }
}
