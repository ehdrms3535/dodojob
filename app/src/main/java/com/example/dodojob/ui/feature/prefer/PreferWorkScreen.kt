@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dodojob.ui.feature.prefer

import androidx.compose.foundation.Image
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.dodojob.R

/* ---------- 유틸: 선택 리스트 → 비트 문자열("1010...") ---------- */
private fun toBits(options: List<String>, selected: List<String>): String =
    options.joinToString("") { if (it in selected) "1" else "0" }

@Composable
fun PreferWorkSheetBottomSheet(
    onApply: (
        selectedLabels: List<String>,
        talentBits: String,
        serviceBits: String,
        manageBits: String,
        careBits: String,
        healthy: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val maxSheetFraction = 0.88f
    var showSheet by remember { mutableStateOf(true) }
    if (!showSheet) return

    val Brand = Color(0xFF005FFF)
    val FieldBg = Color(0xFFEFEFEF)
    val Divider = Color(0xFFCFCFCF)
    val ChipBase = Color(0xFFF7F7F7)
    val ChipSelBg = Color(0xFFDEEBFF)
    val ChipUnselBorder = Color(0xFFE0E0E0)

    var query by remember { mutableStateOf("") }
    var healthy by remember { mutableStateOf(false) }

    val selectedTalent = remember { mutableStateListOf<String>() }
    val selectedService = remember { mutableStateListOf<String>() }
    val selectedManage = remember { mutableStateListOf<String>() }
    val selectedCare = remember { mutableStateListOf<String>() }

    val talentOptions = listOf("영어 회화","악기 지도","요리 강사","역사 강의","공예 강의","예술 지도","독서 지도","관광 가이드","상담·멘토링","홍보 컨설팅")
    val serviceOptions = listOf("고객 응대","카운터/계산","상품 진열","청결 관리","안내 데스크","주차 관리")
    val manageOptions = listOf("환경미화","인력 관리","사서 보조","사무 보조","경비/보안")
    val careOptions = listOf("등하원 도우미","가정 방문","보조 교사")

    fun filtered(list: List<String>) =
        if (query.isBlank()) list else list.filter { it.contains(query.trim(), ignoreCase = true) }

    ModalBottomSheet(
        onDismissRequest = { showSheet = false; onDismiss() },
        sheetState = sheetState,
        containerColor = Color.White,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = { }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(maxSheetFraction)
                .navigationBarsPadding()
                .imePadding()
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val W = maxWidth
                fun frac(h: Float) = (W * h)
                val handleW = frac(122.89f / 360f)
                val handleH = frac(4.16f / 360f)
                val radius10 = (W * (10f / 360f))
                val fieldH = frac(57f / 360f)
                val chipH = 64.dp
                val btnH = frac(54f / 360f)
                val gap12 = frac(12f / 360f)
                val gap16 = frac(16f / 360f)
                val gap20 = frac(20f / 360f)

                // ⬇️ RegionPicker와 동일한 패딩 적용
                Column(
                    Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 18.dp)
                        .heightIn(min = 0.dp, max = 882.dp)
                ) {
                    // 핸들
                    Box(
                        Modifier.fillMaxWidth().padding(bottom = gap12),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .size(width = handleW, height = handleH)
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color(0xFFB3B3B3))
                        )
                    }

                    val tightLS = (-0.019).em
                    // 타이틀
                    Text(
                        "경험을 살릴 일을 설정해주세요",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = tightLS,
                        lineHeight = 39.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Spacer(Modifier.height(8.dp))

                    // 검색창
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = {
                            Text(
                                "직종 키워드",
                                color = Color(0xFFA6A6A6),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = (-0.019f).em
                            )
                        },
                        trailingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = "검색",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = fieldH, max = fieldH),
                        shape = RoundedCornerShape(radius10),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = FieldBg,
                            unfocusedContainerColor = FieldBg,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Brand
                        )
                    )

                    Spacer(Modifier.height(22.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = Color(0xFFCFCFCF)
                    )
                    Spacer(Modifier.height(20.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(gap16)
                    ) {
                        // 1) 재능
                        val sections = listOf(
                            "재능" to talentOptions to selectedTalent,
                            "서비스업" to serviceOptions to selectedService,
                            "관리/운영" to manageOptions to selectedManage,
                            "돌봄" to careOptions to selectedCare
                        )
                        sections.forEach { triple ->
                            val title = triple.first.first
                            val opts = filtered(triple.first.second)
                            val sel = triple.second
                            if (opts.isNotEmpty()) {
                                SectionHeader(title)
                                TwoColumnChipsResponsive(
                                    options = opts,
                                    chipHeight = chipH,
                                    radius = radius10,
                                    baseBg = ChipBase,
                                    selectedBg = ChipSelBg,
                                    brand = Brand,
                                    unselectedBorder = ChipUnselBorder,
                                    isSelected = { it in sel },
                                    onToggle = { label ->
                                        if (label in sel) sel.remove(label) else sel.add(label)
                                    }
                                )
                                Spacer(Modifier.height(1.dp))
                                HorizontalDivider(color = Divider)
                                Spacer(Modifier.height(0.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("＊ 필수", color = Color(0xFFF24822), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(gap12 / 2))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(
                                id = if (healthy) R.drawable.autologin_checked else R.drawable.autologin_unchecked
                            ),
                            contentDescription = "체크박스",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { healthy = !healthy }
                        )
                        Spacer(Modifier.width(gap12))
                        Text("건강해서 일하는 데 지장이 없어요.", fontSize = 22.sp)
                    }

                    Spacer(Modifier.height(gap20))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap12)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedTalent.clear()
                                selectedService.clear()
                                selectedManage.clear()
                                selectedCare.clear()
                                healthy = false
                                query = ""
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = btnH, max = btnH),
                            shape = RoundedCornerShape(radius10),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp, brush = SolidColor(Brand)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Brand
                            )
                        ) { Text("초기화", fontSize = 24.sp, fontWeight = FontWeight.Medium) }

                        val canApply = healthy
                        Button(
                            onClick = {
                                val talentBits = toBits(talentOptions, selectedTalent)
                                val serviceBits = toBits(serviceOptions, selectedService)
                                val manageBits = toBits(manageOptions, selectedManage)
                                val careBits = toBits(careOptions, selectedCare)

                                val labels = buildList {
                                    addAll(selectedTalent)
                                    addAll(selectedService)
                                    addAll(selectedManage)
                                    addAll(selectedCare)
                                }

                                showSheet = false
                                onApply(labels, talentBits, serviceBits, manageBits, careBits, healthy)
                            },
                            enabled = canApply,
                            modifier = Modifier
                                .weight(2f)
                                .heightIn(min = btnH, max = btnH),
                            shape = RoundedCornerShape(radius10),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canApply) Brand else Color(0xFFBFC6D2),
                                disabledContainerColor = Color(0xFFBFC6D2)
                            )
                        ) { Text("적용하기", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Medium) }
                    }
                }
            }
        }
    }
}

/* ---------- 섹션 헤더 ---------- */
@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        letterSpacing = (-0.019f).em,
        lineHeight = 33.sp
    )
}

/* ---------- 칩 ---------- */
@Composable
private fun TwoColumnChipsResponsive(
    options: List<String>,
    chipHeight: Dp,
    radius: Dp,
    baseBg: Color,
    selectedBg: Color,
    brand: Color,
    unselectedBorder: Color,
    isSelected: (String) -> Boolean,
    onToggle: (String) -> Unit
) {
    val rows = remember(options) { options.chunked(2) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { label ->
                    ChoiceChipResponsive(
                        text = label,
                        selected = isSelected(label),
                        onClick = { onToggle(label) },
                        height = chipHeight,
                        radius = radius,
                        baseBg = baseBg,
                        selectedBg = selectedBg,
                        brand = brand,
                        unselectedBorder = unselectedBorder,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ChoiceChipResponsive(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    height: Dp,
    radius: Dp,
    baseBg: Color,
    selectedBg: Color,
    brand: Color,
    unselectedBorder: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = height, max = height)
            .clip(RoundedCornerShape(radius))
            .background(if (selected) selectedBg else baseBg)
            .border(1.dp, if (selected) brand else unselectedBorder, RoundedCornerShape(radius))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) brand else Color.Black,
            letterSpacing = (-0.019f).em,
            maxLines = 1
        )
    }
}

/* ---------- 체크박스 ---------- */
@Composable
private fun OptionCheckBoxResponsive(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    boxSize: Dp,
    iconSize: Dp
) {
    Box(
        modifier = Modifier
            .size(boxSize)
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
