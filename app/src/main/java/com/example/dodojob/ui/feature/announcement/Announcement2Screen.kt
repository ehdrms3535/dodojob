package com.example.dodojob.ui.feature.announcement

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import androidx.navigation.NavController
import com.example.dodojob.navigation.Route
/* -------- Palette (공통) -------- */
private val Blue = Color(0xFF005FFF)
private val TextGray = Color(0xFF828282)
private val BgGray = Color(0xFFF1F5F7)
private val CardBg = Color.White

/* -------- Unified Sizes -------- */
private val CARD_HEIGHT = 70.dp
private val CARD_CORNER = 10.dp
private val BOTTOM_BTN_HEIGHT = 44.dp



/* ================== Route ================== */
@Composable
fun Announcement2Route(
    nav: NavController,
    onNext: () -> Unit = {nav.navigate(Route.Announcement3.path) {    // ✅ 다음 단계 → 05로 이동
        launchSingleTop = true
    }},
    onBack: () -> Unit = { nav.popBackStack() },
    onTabClick: (Int) -> Unit = {idx ->
        val target = when (idx) {
            0 -> Route.Announcement.path
            1 -> Route.Announcement2.path
            2 -> Route.Announcement3.path
            else -> Route.Announcement4.path
        }

        // 같은 화면이면 무시(선택 사항)
        val current = nav.currentBackStackEntry?.destination?.route
        if (current != target) {
            nav.navigate(target) {
                launchSingleTop = true
            }
        }
    }
) {
    Announcement2Screen(
        onNext = onNext,
        onBack = onBack,
        onTabClick = onTabClick
    )
}

/* ====== Screen: 공고등록 / 02 ====== */
@Composable
fun Announcement2Screen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    onTabClick: (Int) -> Unit
) {
    val scroll = rememberScrollState()

    /* ----- Demo States (실사용 시 외부 상태연결) ----- */
    var majorCategory by remember { mutableStateOf(JobCategory.Service) }
    var jobDesc by remember { mutableStateOf("") }
    var headCount by remember { mutableStateOf("") }

    var workType by remember { mutableStateOf(WorkType.LongTerm) }
    var weekdays by remember { mutableStateOf(weekdayDefaults()) }
    var timeNegotiable by remember { mutableStateOf(false) }

    var startTime by remember { mutableStateOf("오후 12:30") }
    var endTime by remember { mutableStateOf("오후 18:00") }

    var intensity by remember { mutableStateOf(Intensity.Medium) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
    ) {
        /* Header */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .background(CardBg)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) { Text("공고등록", fontSize = 24.sp, fontWeight = FontWeight.SemiBold) }

        /* Tabs (02 선택) */
        TabBar02(selected = 1, labels = listOf("01", "02", "03", "04"), onClick = onTabClick)

        /* Body */
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
        ) {
            /* 설명 */
            SectionCard {
                TitleRow("02. 직종과 근무 조건을 입력해주세요.")
            }

            /* 직종 카테고리 (2x2, 1개 선택) */
            SectionCard {
                LabelText("직종 카테고리")
                Spacer(Modifier.height(6.dp))
                TwoByTwo(
                    {
                        SelectCard(
                            title = "요식/서빙",
                            sub = "Service",
                            selected = majorCategory == JobCategory.Service
                        ) { majorCategory = JobCategory.Service }
                    },
                    {
                        SelectCard(
                            title = "매장/관리",
                            sub = "Retail",
                            selected = majorCategory == JobCategory.Retail
                        ) { majorCategory = JobCategory.Retail }
                    },
                    {
                        SelectCard(
                            title = "사무/회계",
                            sub = "Office",
                            selected = majorCategory == JobCategory.Office
                        ) { majorCategory = JobCategory.Office }
                    },
                    {
                        SelectCard(
                            title = "기타",
                            sub = "Others",
                            selected = majorCategory == JobCategory.Etc
                        ) { majorCategory = JobCategory.Etc }
                    }
                )
            }

            /* 업무 내용 (멀티라인) */
            SectionCard {
                LabelText("업무 내용")
                Spacer(Modifier.height(6.dp))
                MultilineField(
                    value = jobDesc,
                    placeholder = "예) 음료 제조, 고객 응대, 매장 정리",
                    minHeight = 120.dp,
                    onChange = { jobDesc = it }
                )
            }

            /* 모집 인원 (싱글라인) */
            SectionCard {
                LabelText("모집 인원")
                Spacer(Modifier.height(6.dp))
                LineField(
                    value = headCount,
                    placeholder = "예) 3명",
                    onChange = { headCount = it }
                )
            }

            /* 근무 형태 (리스트 카드 3개) */
            SectionCard {
                LabelText("근무 형태")
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ListChoice(
                        title = "단기 알바",
                        sub = "1개월 미만",
                        selected = workType == WorkType.ShortTerm
                    ) { workType = WorkType.ShortTerm }
                    ListChoice(
                        title = "장기 알바",
                        sub = "1개월 이상",
                        selected = workType == WorkType.LongTerm
                    ) { workType = WorkType.LongTerm }
                    ListChoice(
                        title = "정규직",
                        sub = "풀타임",
                        selected = workType == WorkType.FullTime
                    ) { workType = WorkType.FullTime }
                }
            }

            /* 근무 요일 + 시간 협의 */
            SectionCard(padding = 20.dp) {
                LabelText("근무요일")
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekdays.forEachIndexed { idx, w ->
                        Box(
                            modifier = Modifier
                                .weight(1f)                  // 🔹 동일 폭 차지
                                .padding(horizontal = 2.dp), // 🔹 약간의 여백
                            contentAlignment = Alignment.Center
                        ) {
                            DayChip(
                                label = w.label,
                                selected = w.selected
                            ) {
                                weekdays = weekdays.toMutableList()
                                    .also { it[idx] = w.copy(selected = !w.selected) }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CheckBoxLike(
                        checked = timeNegotiable,
                        onToggle = { timeNegotiable = !timeNegotiable }
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("시간협의", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            /* 근무 시간 (시작/종료) */
            SectionCard {
                LabelText("근무 시간")
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("시작시간", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        TimeBox(startTime) { /* TODO: TimePicker */ }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("종료시간", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        TimeBox(endTime) { /* TODO: TimePicker */ }
                    }
                }
            }

            /* 업무 강도 (리스트 카드 3개) */
            SectionCard {
                LabelText("업무 강도")
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ListChoice(
                        title = "가벼움",
                        sub = "서서 근무, 단순업무 위주",
                        selected = intensity == Intensity.Light
                    ) { intensity = Intensity.Light }
                    ListChoice(
                        title = "보통",
                        sub = "상시 이동/정리 등",
                        selected = intensity == Intensity.Medium
                    ) { intensity = Intensity.Medium }
                    ListChoice(
                        title = "힘듦",
                        sub = "무거운 물건 취급/고강도",
                        selected = intensity == Intensity.Hard
                    ) { intensity = Intensity.Hard }
                }
            }

            /* 하단 버튼 */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(0.6f)
                            .height(BOTTOM_BTN_HEIGHT),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Blue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) { Text("이전", fontSize = 15.sp, fontWeight = FontWeight.Medium) }

                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .weight(1.4f)
                            .height(BOTTOM_BTN_HEIGHT),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) { Text("다음 단계", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                }
            }

            BottomNavPlaceholder()
        }
    }
}

/* ====== TabBar (02 선택) ====== */
@Composable
private fun TabBar02(
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
                        color = if (isSel) Blue else Color.Black,
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
            val animatedX by animateDpAsState(targetValue = targetX, label = "tab-indicator-2")

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

/* ====== 재사용 컴포넌트 ====== */
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

@Composable
private fun TitleRow(text: String) {
    Text(text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.38).sp)
}

@Composable
private fun LabelText(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        letterSpacing = (-0.34).sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 6.dp)
    )
}

/* 2x2 레이아웃 */
@Composable
private fun TwoByTwo(
    a: @Composable () -> Unit,
    b: @Composable () -> Unit,
    c: @Composable () -> Unit,
    d: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { a() }
            Box(Modifier.weight(1f)) { b() }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { c() }
            Box(Modifier.weight(1f)) { d() }
        }
    }
}

/* 선택 카드 (카테고리용) */
@Composable
private fun SelectCard(
    title: String,
    sub: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(CARD_CORNER)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            .clip(shape)
            .border(1.dp, Blue, shape)
            .background(if (selected) Blue else Color.White, shape)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, fontWeight = FontWeight.Bold, color = if (selected) Color.White else Blue)
        Text(sub, fontSize = 12.sp, color = if (selected) Color(0xFFD9D9D9) else TextGray)
    }
}

/* 멀티라인 입력 (Figma 120dp 높이 박스) */
@Composable
private fun MultilineField(
    value: String,
    placeholder: String,
    minHeight: Dp,
    onChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(CARD_CORNER)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .border(1.dp, Blue, shape)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        if (value.isEmpty()) {
            Text(placeholder, fontSize = 13.sp, color = TextGray)
        }
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/* 싱글라인 (40dp) */
@Composable
private fun LineField(
    value: String,
    placeholder: String,
    onChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(CARD_CORNER)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .border(1.dp, Blue, shape)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (value.isEmpty()) {
            Text(placeholder, fontSize = 13.sp, color = TextGray)
        }
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/* 리스트형 선택 (높이 61dp 사양 반영) */
@Composable
private fun ListChoice(
    title: String,
    sub: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(CARD_CORNER)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(61.dp)
            .clip(shape)
            .border(1.dp, Blue, shape)
            .background(if (selected) Color(0xFFEFF5FF) else Color.White, shape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Blue else Color(0xFF1A1A1A),
            textAlign = TextAlign.Center
        )
        Text(sub, fontSize = 12.sp, color = TextGray, textAlign = TextAlign.Center)
    }
}

/* 근무 요일 칩 (38x48 사양) */
@Composable
private fun DayChip(
    label: String,
    selected: Boolean,
    onToggle: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .width(38.dp)
            .height(48.dp)
            .clip(shape)
            .border(1.dp, Blue, shape)
            .background(if (selected) Blue else Color.White, shape)
            .clickable { onToggle() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else TextGray
        )
    }
}

/* 체크박스 느낌의 사각 틱 */
@Composable
private fun CheckBoxLike(
    checked: Boolean,
    onToggle: () -> Unit
) {
    val shape = RoundedCornerShape(3.dp)
    Box(
        modifier = Modifier
            .size(24.dp)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .border(width = 1.dp, color = Blue, shape = shape)
                .background(if (checked) Blue else Color.Transparent, shape = shape)
        )
    }
}

/* 시간 박스 (55dp) */
@Composable
private fun TimeBox(
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(CARD_CORNER)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .clip(shape)
            .border(1.dp, Blue, shape)
            .background(Color.White, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}

@Composable
private fun BottomNavPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .background(Color(0xFFF4F5F7))
    )
}

/* ====== Models ====== */
private enum class JobCategory { Service, Retail, Office, Etc }
private enum class WorkType { ShortTerm, LongTerm, FullTime }
private enum class Intensity { Light, Medium, Hard }
private data class Weekday(val label: String, val selected: Boolean)
private fun weekdayDefaults() = listOf("월", "화", "수", "목", "금", "토", "일").map { Weekday(it, false) }

/* ====== Tab Bar Helpers ====== */
@Composable
private fun TabBar02Preview() { TabBar02(1, listOf("01","02","03","04")){} }

/* -------- Preview -------- */
@Preview(showSystemUi = true, device = Devices.PIXEL_7, locale = "ko")
@Composable
private fun PreviewAnnouncement2() {
    Announcement2Screen(
        onNext = {},
        onBack = {},
        onTabClick = {}
    )
}
