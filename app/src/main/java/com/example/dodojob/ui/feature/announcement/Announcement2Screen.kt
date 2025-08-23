package com.example.dodojob.ui.feature.announcement

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* -------- Colors -------- */
private val Blue = androidx.compose.ui.graphics.Color(0xFF005FFF)
private val TextGray = androidx.compose.ui.graphics.Color(0xFF828282)
private val BgGray = androidx.compose.ui.graphics.Color(0xFFF1F5F7)
private val CardBg = androidx.compose.ui.graphics.Color.White

/* -------- Screen: 공고등록/02 (Option 2: LazyVerticalGrid) -------- */
@Composable
fun Announcement2Screen(
    onBack: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    val scroll = rememberScrollState()

    var majorSelected by remember { mutableStateOf(3) }
    val detailChips = remember {
        mutableStateListOf("서빙", "바리스타", "주방", "설거지", "캐셔", "매니저")
    }
    var detailSelected by remember { mutableStateOf(1) }

    val workTypes = listOf("단기 알바", "장기 알바", "정규직")
    var workTypeSel by remember { mutableStateOf(0) }

    val days = listOf("월", "화", "수", "목", "금", "토", "일")
    val daySelected = remember { mutableStateListOf(false, false, false, false, false, false, false) }

    var timeNegotiable by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("오후 12:30") }
    var endTime by remember { mutableStateOf("오후 18:00") }

    val intensities = listOf("낮음", "보통", "높음")
    var intensitySel by remember { mutableStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
    ) {
        Column(Modifier.fillMaxSize()) {

            /* Header */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .background(CardBg),
                contentAlignment = Alignment.Center
            ) {
                Text("공고등록", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }

            /* Tabs (01~04, current 02) */
            TabBar(selected = 1, labels = listOf("01", "02", "03", "04"), onClick = {})

            /* Body */
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scroll)
            ) {
                /* 직종 카테고리 */
                SectionCard(padding = 20.dp) {
                    TitleRow("공고등록/설명")
                    Spacer(Modifier.height(10.dp))
                    LabelText("직종 카테고리")
                    Spacer(Modifier.height(10.dp))

                    FlowGrid(
                        items = listOf("요식/서빙", "매장/판매", "사무/회계", "생산/제조", "물류/배송", "기타"),
                        columns = 2,
                        hGap = 10.dp,
                        vGap = 10.dp
                    ) { idx, text ->
                        BigChoiceTile(
                            text = text,
                            selected = majorSelected == idx,
                            onClick = { majorSelected = idx }
                        )
                    }

                    Spacer(Modifier.height(15.dp))

                    /* 세부 직종 (Stable 대안: LazyVerticalGrid) */
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(androidx.compose.ui.graphics.Color(0xFFF0F0F0), RoundedCornerShape(10.dp))
                            .padding(vertical = 15.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(Modifier.widthIn(max = 288.dp)) {
                            Text(
                                "세부 직종을 선택해주세요",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(10.dp))
                            DetailChipsGrid(
                                chips = detailChips,
                                selected = detailSelected,
                                onClick = { detailSelected = it }
                            )
                        }
                    }
                }

                /* 공고등록 메모 */
                SectionCard(padding = 20.dp) {
                    TitleRow("공고등록")
                    Spacer(Modifier.height(10.dp))
                    MultilineOutlinedBox(
                        value = "",
                        placeholder = "내용 입력",
                        onValueChange = {},
                        minHeight = 120.dp
                    )
                }

                /* Primary 버튼 */
                SectionCard(padding = 20.dp) {
                    PrimaryButton("공고등록") { onNext() }
                }

                /* 근무 형태 */
                SectionCard(padding = 20.dp) {
                    LabelText("근무 형태")
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        workTypes.forEachIndexed { i, t ->
                            BigOutlineRowButton(
                                text = t,
                                supporting = "선택 시 안내가 표시될 수 있어요",
                                selected = workTypeSel == i,
                                onClick = { workTypeSel = i }
                            )
                        }
                    }
                }

                /* 근무 요일 + 시간협의 */
                SectionCard(padding = 20.dp) {
                    LabelText("근무요일")
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        days.forEachIndexed { i, d ->
                            DayBox(
                                label = d,
                                selected = daySelected[i],
                                onClick = { daySelected[i] = !daySelected[i] }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = timeNegotiable,
                            onCheckedChange = { timeNegotiable = it }
                        )
                        Text("시간협의", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                /* 근무 시간 */
                SectionCard(padding = 20.dp) {
                    LabelText("근무 시간")
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("시작시간", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(10.dp))
                            SingleLineOutlinedBox(value = startTime, onClick = { /* time picker */ })
                        }
                        Column(Modifier.weight(1f)) {
                            Text("종료시간", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(10.dp))
                            SingleLineOutlinedBox(value = endTime, onClick = { /* time picker */ })
                        }
                    }
                }

                /* 업무 강도 */
                SectionCard(padding = 20.dp) {
                    LabelText("업무 강도")
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        intensities.forEachIndexed { i, t ->
                            BigOutlineRowButton(
                                text = t,
                                supporting = "업무 난이도 선택",
                                selected = intensitySel == i,
                                onClick = { intensitySel = i }
                            )
                        }
                    }
                }

                /* 하단 버튼 2개 */
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBg)
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = 328.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        SecondaryButton(text = "임시저장", modifier = Modifier.weight(1f)) { }
                        PrimaryButton(text = "다음", modifier = Modifier.weight(1f)) { onNext() }
                    }
                }

                BottomNavPlaceholder()
            }
        }
    }
}

/* -------- TabBar with centered indicator -------- */
@Composable
private fun TabBar(
    selected: Int,
    labels: List<String>,
    onClick: (Int) -> Unit
) {
    val density = LocalDensity.current
    val centersPx = remember(labels.size) {
        mutableStateListOf<Float>().apply { repeat(labels.size) { add(0f) } }
    }
    val indicatorWidth = 41.dp
    val rowPaddingStart = 24.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = rowPaddingStart),
            horizontalArrangement = Arrangement.spacedBy(61.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            labels.forEachIndexed { idx, text ->
                Box(
                    modifier = Modifier
                        .onGloballyPositioned { c ->
                            val center = c.positionInParent().x + c.size.width / 2f
                            if (centersPx[idx] != center) centersPx[idx] = center
                        }
                        .clickable { onClick(idx) }
                ) {
                    val isSel = idx == selected
                    Text(
                        text = text,
                        fontSize = 16.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Blue else androidx.compose.ui.graphics.Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(24.dp)
                    )
                }
            }
        }

        val centerPx = centersPx.getOrNull(selected) ?: 0f
        if (centerPx > 0f) {
            val startInRow = with(density) { (centerPx - indicatorWidth.toPx() / 2f).toDp() }
            val targetX = rowPaddingStart + startInRow
            val animatedX by animateDpAsState(targetValue = targetX, label = "tab-indicator")

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = animatedX)
                    .width(indicatorWidth)
                    .height(4.dp)
                    .background(Blue)
            )
        }
    }
}

/* -------- Reusable Components -------- */
@Composable
private fun SectionCard(
    padding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .padding(vertical = padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(horizontal = 16.dp),
            content = content
        )
    }
}

@Composable private fun TitleRow(text: String) {
    Text(text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.38).sp)
}
@Composable private fun LabelText(text: String) {
    Text(text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.34).sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp))
}

@Composable
private fun BigChoiceTile(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    val bg = if (selected) Blue else CardBg
    val txtColor = if (selected) androidx.compose.ui.graphics.Color.White else Blue
    Column(
        modifier = Modifier
            .width(159.dp)
            .height(70.dp)
            .background(bg, shape)
            .border(BorderStroke(1.dp, Blue), shape)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = txtColor)
        Text(if (selected) "선택됨" else "설명 텍스트",
            fontSize = 12.sp, color = if (selected) androidx.compose.ui.graphics.Color(0xFFD9D9D9) else TextGray)
    }
}

@Composable
private fun PillChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .height(28.dp)
            .wrapContentWidth()
            .background(if (selected) Blue else CardBg, shape)
            .then(if (selected) Modifier else Modifier.border(1.dp, Blue, shape))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) androidx.compose.ui.graphics.Color.White else TextGray
        )
    }
}

@Composable
private fun BigOutlineRowButton(
    text: String,
    supporting: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(61.dp)
            .background(CardBg, shape)
            .border(BorderStroke(1.dp, Blue), shape)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Blue)
        Text(supporting, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextGray)
    }
}

@Composable
private fun DayBox(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .size(width = 38.dp, height = 48.dp)
            .background(CardBg, shape)
            .border(1.dp, Blue, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 15.sp, color = if (selected) Blue else TextGray, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SingleLineOutlinedBox(value: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(CardBg, shape)
            .border(1.dp, Blue, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { Text(value, fontSize = 15.sp) }
}

@Composable
private fun MultilineOutlinedBox(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    minHeight: Dp
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .border(1.dp, Blue, shape)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        var text by remember { mutableStateOf(value) }
        BasicTextField(
            value = text,
            onValueChange = { text = it; onValueChange(it) },
            textStyle = TextStyle(fontSize = 13.sp, color = if (text.isEmpty()) TextGray else androidx.compose.ui.graphics.Color.Black),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (text.isEmpty()) Text(placeholder, fontSize = 13.sp, color = TextGray)
                inner()
            }
        )
    }
}

@Composable
private fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(47.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = androidx.compose.ui.graphics.Color.White)
    ) { Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun SecondaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(47.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Blue),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue)
    ) { Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun BottomNavPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .background(androidx.compose.ui.graphics.Color(0xFFF4F5F7))
    )
}

/* 간단한 2열 그리드 */
@Composable
private fun FlowGrid(
    items: List<String>,
    columns: Int,
    hGap: Dp,
    vGap: Dp,
    item: @Composable (index: Int, text: String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(vGap)) {
        items.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(hGap)) {
                row.forEach { s -> item(items.indexOf(s), s) }
            }
        }
    }
}

/* Stable 대체: LazyVerticalGrid로 칩 렌더링 */
@Composable
private fun DetailChipsGrid(
    chips: List<String>,
    selected: Int,
    onClick: (Int) -> Unit
) {
    val state = rememberLazyGridState()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 81.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = state,
        userScrollEnabled = false,
        modifier = Modifier.heightIn(max = 128.dp) // 2~3줄 정도 표시
    ) {
        items(chips.size) { i ->
            PillChip(
                text = chips[i],
                selected = selected == i,
                onClick = { onClick(i) }
            )
        }
    }
}

/* -------- Preview -------- */
@Preview(showSystemUi = true, device = Devices.PIXEL_7, locale = "ko")
@Composable
private fun PreviewAnnouncement2() {
    Announcement2Screen()
}
