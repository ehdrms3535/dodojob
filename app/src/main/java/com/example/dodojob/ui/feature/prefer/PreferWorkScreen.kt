package com.example.dodojob.ui.feature.prefer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PreferWorkScreen(nav: NavController) {
    val Bg = Color(0xFFF1F5F7)
    val Primary = Color(0xFF005FFF)

    val categories = mapOf(
        "취미" to listOf("요리","음악","글쓰기","공예","원예","사진","바느질","그림"),
        "안전시설 관리" to listOf("경비","출입통제","환경관리","청소","방역","소독")
    )

    var query by remember { mutableStateOf("") }
    val selected = remember { mutableStateListOf<String>() }
    var healthy by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Bg)
                    .navigationBarsPadding()
                    .padding(top = 8.dp, bottom = 12.dp)
            ) {
                Divider(color = Color(0xFFCFCFCF))
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("＊ 필수", color = Color(0xFFF24822), fontSize = 16.sp)
                }
                Spacer(Modifier.height(8.dp))

                // ✅ 커스텀 체크박스 스타일
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OptionCheckBox(
                        checked = healthy,
                        onCheckedChange = { healthy = it }
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("건강해서 일하는 데 지장이 없어요.", fontSize = 18.sp)
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 🔵 초기화 버튼 (파란색 테두리 + 흰 배경)
                    Button(
                        onClick = { selected.clear(); healthy = false; query = "" },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Primary
                        ),
                        border = BorderStroke(1.dp, Primary)
                    ) { Text("초기화", fontSize = 18.sp) }

                    Button(
                        onClick = {
                            nav.previousBackStackEntry?.savedStateHandle
                                ?.set("pickedJobs", selected.toSet())
                            nav.popBackStack()
                        },
                        enabled = healthy,
                        modifier = Modifier.weight(2f).height(54.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (healthy) Primary else Color(0xFFBFC6D2),
                            disabledContainerColor = Color(0xFFBFC6D2)
                        )
                    ) { Text("적용하기", color = Color.White, fontSize = 18.sp) }
                }
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentPadding = PaddingValues(bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SheetDragHandle()
                Spacer(Modifier.height(4.dp))
                Text(
                    "경험을 살릴 일을 설정해주세요",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    placeholder = { Text("직종 키워드") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFF9AA2A9))
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            categories.forEach { (title, list) ->
                val filtered = if (query.isBlank()) list
                else list.filter { it.contains(query.trim(), ignoreCase = true) }

                item {
                    Text(title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    TwoColumnChipsEqualWidth(
                        options = filtered,
                        isSelected = { it in selected },
                        onToggle = { label ->
                            if (label in selected) selected.remove(label) else selected.add(label)
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    Divider(color = Color(0xFFCFCFCF))
                }
            }
        }
    }
}

/* ---------- 칩 2열 (글씨 가운데 정렬) ---------- */
@Composable
private fun TwoColumnChipsEqualWidth(
    options: List<String>,
    isSelected: (String) -> Boolean,
    onToggle: (String) -> Unit,
    itemHeight: Dp = 56.dp,
    radius: Dp = 12.dp
) {
    val rows = options.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { label ->
                    val selected = isSelected(label)
                    SimpleChoiceChip(
                        text = label,
                        selected = selected,
                        onClick = { onToggle(label) },
                        modifier = Modifier
                            .weight(1f)
                            .height(itemHeight),
                        radius = radius
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SimpleChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    radius: Dp = 12.dp
) {
    val border = if (selected) Color(0xFF005FFF) else Color(0xFFE0E0E0)
    val bg = if (selected) Color(0xFFE9F0FF) else Color.White
    val textColor = if (selected) Color(0xFF005FFF) else Color(0xFF111111)

    Box(
        modifier = modifier
            .border(1.dp, border, RoundedCornerShape(radius))
            .background(bg, RoundedCornerShape(radius))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontWeight = FontWeight.Medium, fontSize = 18.sp)
    }
}

/* ----- JobTypeScreen 체크박스 스타일 ----- */
@Composable
fun OptionCheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val badgeSize = 28.dp
    val iconSize = 18.dp

    Box(
        modifier = Modifier
            .size(badgeSize)
            .clip(CircleShape)
            .background(if (checked) Color(0xFF2A77FF) else Color(0xFFE6E6E6))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = if (checked) Color.White else Color(0xFFBDBDBD),
            modifier = Modifier.size(iconSize)
        )
    }
}

/* ----- 상단 드래그 핸들 ----- */
@Composable
fun SheetDragHandle() {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .width(92.dp)
                .height(6.dp)
                .background(Color(0xFFB3B3B3), RoundedCornerShape(100))
        )
    }
}
