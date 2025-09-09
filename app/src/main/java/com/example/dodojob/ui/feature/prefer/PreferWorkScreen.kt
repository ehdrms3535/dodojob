// com/example/dodojob/ui/feature/prefer/PreferWorkSheetScreen.kt
package com.example.dodojob.ui.feature.prefer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.dodojob.navigation.Route

/* ---------- 유틸: 선택 리스트 → 비트 문자열("1010...") ---------- */
private fun toBits(options: List<String>, selected: List<String>): String =
    options.joinToString("") { if (it in selected) "1" else "0" }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferWorkSheetScreen(nav: NavController) {
    // ---- state (시트 + 폼) ----
    var showSheet by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var query by remember { mutableStateOf("") }
    var healthy by remember { mutableStateOf(false) }

    // ✅ 카테고리별 선택 상태 (각각 따로 보낼 것)
    val selectedTalent  = remember { mutableStateListOf<String>() }
    val selectedService = remember { mutableStateListOf<String>() }
    val selectedManage  = remember { mutableStateListOf<String>() }
    val selectedCare    = remember { mutableStateListOf<String>() }

    // 팔레트
    val Primary = Color(0xFF005FFF)
    val Divider = Color(0xFFCFCFCF)

    // ✅ 카테고리별 옵션
    val talentOptions = listOf(
        "영어 회화","악기 지도","요리 강사","역사 강의","공예 강의","예술 지도",
        "독서 지도","관광 가이드","상담·멘토링","홍보 컨설팅"
    )
    val serviceOptions = listOf(
        "고객 응대","카운터/계산","상품 진열","청결 관리","안내 데스크","주차 관리"
    )
    val manageOptions = listOf(
        "환경미화","인력 관리","사서 보조","사무 보조","경비/보안"
    )
    val careOptions = listOf(
        "등하원 도우미","가정 방문","보조 교사"
    )

    fun dismiss() {
        showSheet = false
        nav.popBackStack()
    }

    if (!showSheet) {
        Box(Modifier.fillMaxSize())
        return
    }

    ModalBottomSheet(
        onDismissRequest = { dismiss() },
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
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
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // 제목
            Text(
                "경험을 살릴 일을 설정해주세요",
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            // 검색창
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("직종 키워드", color = Color(0xFFA6A6A6), fontSize = 18.sp) },
                trailingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFFA6A6A6)) },
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
                    cursorColor = Primary
                )
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Divider)
            Spacer(Modifier.height(12.dp))

            // ───────── 4개 섹션으로 분리 ─────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                fun filtered(list: List<String>) =
                    if (query.isBlank()) list else list.filter { it.contains(query.trim(), ignoreCase = true) }

                // 1) 재능
                run {
                    val options = filtered(talentOptions)
                    if (options.isNotEmpty()) {
                        Text("재능", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        TwoColumnChipsEqualWidth(
                            options = options,
                            isSelected = { it in selectedTalent },
                            onToggle = { label ->
                                if (label in selectedTalent) selectedTalent.remove(label) else selectedTalent.add(label)
                            }
                        )
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(color = Divider)
                    }
                }

                // 2) 서비스업
                run {
                    val options = filtered(serviceOptions)
                    if (options.isNotEmpty()) {
                        Text("서비스업", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        TwoColumnChipsEqualWidth(
                            options = options,
                            isSelected = { it in selectedService },
                            onToggle = { label ->
                                if (label in selectedService) selectedService.remove(label) else selectedService.add(label)
                            }
                        )
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(color = Divider)
                    }
                }

                // 3) 관리/운영
                run {
                    val options = filtered(manageOptions)
                    if (options.isNotEmpty()) {
                        Text("관리/운영", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        TwoColumnChipsEqualWidth(
                            options = options,
                            isSelected = { it in selectedManage },
                            onToggle = { label ->
                                if (label in selectedManage) selectedManage.remove(label) else selectedManage.add(label)
                            }
                        )
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(color = Divider)
                    }
                }

                // 4) 돌봄
                run {
                    val options = filtered(careOptions)
                    if (options.isNotEmpty()) {
                        Text("돌봄", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        TwoColumnChipsEqualWidth(
                            options = options,
                            isSelected = { it in selectedCare },
                            onToggle = { label ->
                                if (label in selectedCare) selectedCare.remove(label) else selectedCare.add(label)
                            }
                        )
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(color = Divider)
                    }
                }
            }

            // 하단 안내 + 액션
            Spacer(Modifier.height(4.dp))
            Text("＊ 필수", color = Color(0xFFF24822), fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OptionCheckBox(
                    checked = healthy,
                    onCheckedChange = { healthy = it }
                )
                Spacer(Modifier.width(10.dp))
                Text("건강해서 일하는 데 지장이 없어요.", fontSize = 18.sp)
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        // 초기화
                        selectedTalent.clear()
                        selectedService.clear()
                        selectedManage.clear()
                        selectedCare.clear()
                        healthy = false
                        query = ""
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Primary
                    ),
                    border = BorderStroke(1.dp, Primary)
                ) { Text("초기화", fontSize = 18.sp) }

                val canApply = healthy // 건강 체크 시에만 가능(요구사항 유지)
                Button(
                    onClick = {
                        // ① 섹션별 비트 문자열 생성 (표시 순서 = 각 options 순서)
                        val talentBits  = toBits(talentOptions,  selectedTalent)
                        val serviceBits = toBits(serviceOptions, selectedService)
                        val manageBits  = toBits(manageOptions,  selectedManage)
                        val careBits    = toBits(careOptions,    selectedCare)

                        // ② 다음 화면에서 읽을 수 있게 저장
                        nav.currentBackStackEntry?.savedStateHandle?.apply {

                            // ✅ 섹션별 비트 문자열(문자열) 전달
                            set("sheet_talent_bits",  talentBits)   // 예: "1010000000"
                            set("sheet_service_bits", serviceBits)  // 예: "010001"
                            set("sheet_manage_bits",  manageBits)   // 예: "10010"
                            set("sheet_care_bits",    careBits)     // 예: "101"

                            set("sheet_healthy", healthy)
                        }

                        nav.navigate(Route.Hope.path)
                    },
                    enabled = canApply,
                    modifier = Modifier
                        .weight(2f)
                        .height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canApply) Primary else Color(0xFFBFC6D2),
                        disabledContainerColor = Color(0xFFBFC6D2)
                    )
                ) { Text("적용하기", color = Color.White, fontSize = 18.sp) }
            }
        }
    }
}

/* ---------- 칩 2열 ---------- */
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
                    SimpleChoiceChip(
                        text = label,
                        selected = isSelected(label),
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

/* ----- 커스텀 체크박스 ----- */
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
