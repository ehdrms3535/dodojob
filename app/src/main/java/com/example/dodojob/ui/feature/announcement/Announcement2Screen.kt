package com.example.dodojob.ui.feature.announcement

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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

/* -------- Palette -------- */
private val Blue       = Color(0xFF005FFF) // 프라이머리
private val TextGray   = Color(0xFF828282) // 문구/보더 그레이
private val BorderGray = Color(0xFF828282)
private val BgGray     = Color(0xFFF1F5F7) // 화면 배경
private val CardBg     = Color.White

/* -------- Sizes -------- */
private val CARD_HEIGHT       = 70.dp        // 카테고리/재능 카드 159×70
private val CARD_CORNER       = 10.dp
private val BOTTOM_BTN_HEIGHT = 47.dp        // Prev/Next & 중간 버튼 높이

private val TALENT_CARD_HEIGHT = 64.dp
private val TIME_BOX_HEIGHT   = 55.dp        // 시간 박스 159×55
private val DAY_W             = 38.dp        // 요일 칩 38×48
private val DAY_H             = 48.dp

/* ================== Route ================== */
@Composable
fun Announcement2Route(
    nav: NavController,
    onNext: () -> Unit = { nav.navigate(Route.Announcement3.path) { launchSingleTop = true } },
    onBack: () -> Unit = { nav.popBackStack() },
    onTabClick: (Int) -> Unit = { idx ->
        val target = when (idx) {
            0 -> Route.Announcement.path
            1 -> Route.Announcement2.path
            2 -> Route.Announcement3.path
            else -> Route.Announcement4.path
        }
        val current = nav.currentBackStackEntry?.destination?.route
        if (current != target) nav.navigate(target) { launchSingleTop = true }
    }
) {
    Announcement2Screen(onNext, onBack, onTabClick)
}

/* ====== Screen: 공고등록 / 02 ====== */
@Composable
fun Announcement2Screen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    onTabClick: (Int) -> Unit
) {
    val scroll = rememberScrollState()

    // ---- 로컬 상태 (실사용시 ViewModel 바인딩) ----
    var majorCategory by remember { mutableStateOf(JobCategory.Service) }

    var workType by remember { mutableStateOf(WorkType.LongTerm) }
    var weekdays by remember { mutableStateOf(weekdayDefaults()) }
    var timeNegotiable by remember { mutableStateOf(false) }

    var startTime by remember { mutableStateOf("오후 12:30") }
    var endTime by remember { mutableStateOf("오후 18:00") }

    var intensity by remember { mutableStateOf(Intensity.Medium) }

    // 재능(다중 선택) – Figma 2열 버튼 그리드
    val talentOptions = listOf(
        "영어 회화", "악기 지도",
        "요리 강사", "역사 강의",
        "공예 강의", "예술 지도",
        "독서 지도", "관광 가이드",
        "상담 멘토링", "홍보 컨설팅"
    )
    var selectedTalents by remember { mutableStateOf(setOf<String>()) }
    fun toggleTalent(label: String) {
        selectedTalents = selectedTalents.toMutableSet().also {
            if (it.contains(label)) it.remove(label) else it.add(label)
        }
    }

    // 상세 설명(멀티라인) / 연락처·링크(싱글라인)
    var desc by remember { mutableStateOf("") }
    var contactOrLink by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
    ) {
        /* ✅ 상단 상태바 24dp (#EFEFEF) */
        StatusBarBar()

        /* Header 76dp */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .background(CardBg)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                "공고등록",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.019).em()
            )
        }

        /* Tabs (02 선택, 40dp) */
        TabBar02(selected = 1, labels = listOf("01", "02", "03", "04"), onClick = onTabClick)

        /* Body */
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
        ) {
            // --- 설명 블록 ---
            SectionCard {
                Spacer(modifier = Modifier.height(18.dp))
                TitleRow("02. 직종과 근무 조건을 설정해주세요!")
            }

            // --- 직종 카테고리 (2x2, 1개 선택) ---
            SectionCard {
                LabelText("직종 카테고리")
                Spacer(Modifier.height(10.dp))
                TwoByTwo(
                    {
                        SelectCardSingle(
                            title = "서비스업", sub = "매장관리, 고객 응대",
                            selected = majorCategory == JobCategory.Service
                        ) { majorCategory = JobCategory.Service }
                    },
                    {
                        SelectCardSingle(
                            title = "교육/강의", sub = "전문지식 전수",
                            selected = majorCategory == JobCategory.Retail
                        ) { majorCategory = JobCategory.Retail }
                    },
                    {
                        SelectCardSingle(
                            title = "관리/운영", sub = "시설, 인력관리",
                            selected = majorCategory == JobCategory.Office
                        ) { majorCategory = JobCategory.Office }
                    },
                    {
                        SelectCardSingle(
                            title = "돌봄서비스", sub = "아동, 시니어 돌봄",
                            selected = majorCategory == JobCategory.Etc
                        ) { majorCategory = JobCategory.Etc }
                    }
                )
            }
            SectionSpacer()

            // --- 재능 (다중선택, 2열 그리드) ---
            SectionCard {
                LabelText("재능")
                Spacer(Modifier.height(10.dp))
                GridTwoColumns(items = talentOptions) { label ->
                    val selected = selectedTalents.contains(label)
                    SelectCardToggle(
                        title = label,
                        selected = selected,
                        onClick = { toggleTalent(label) }
                    )
                }
            }
            SectionSpacer()

            // --- 상세 설명 (멀티라인 328×120, 보더 #828282, 13sp) ---
            SectionCard {
                LabelText("주요 업무 내용")
                Spacer(Modifier.height(10.dp))
                MultilineInputBox(
                    value = desc,
                    onValueChange = { desc = it },
                    placeholder = "예:\n" +
                            "• 고객 문의의 전화 응대 및 상담\n" +
                            "• 주문 접수 및 처리\n" +
                            "• 고객 정보 관리 및 데이터입력\n" +
                            "• 간단한 보고서 작성"
                )
            }
            SectionSpacer()

            // --- 연락처/링크 (싱글라인 328×40, 보더 #828282) + 중간 파란 버튼(328×47) ---
            SectionCard {
                LabelText("필요한 기술 또는 경험")
                Spacer(Modifier.height(10.dp))
                SinglelineInputBox(
                    value = contactOrLink,
                    onValueChange = { contactOrLink = it },
                    placeholder = "예 : 컴퓨터 활용, 영어회화, 운전가능"
                )
            }
            SectionCard(padding = 0.dp) {
                Button(
                    onClick = { /* TODO: 중간 액션 (예: 링크 검증/추가) */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BOTTOM_BTN_HEIGHT),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    Text(
                        "추가",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.019).em()
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
            SectionSpacer()

            // --- 근무 형태 (리스트 카드 3개, 각 328×61) ---
            SectionCard {
                LabelText("근무 형태")
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ListChoice(
                        title = "파트타임", sub = "주 3~4일, 4~5시간",
                        selected = workType == WorkType.ShortTerm
                    ) { workType = WorkType.ShortTerm }

                    ListChoice(
                        title = "풀타임", sub = "주 5일, 8시간",
                        selected = workType == WorkType.LongTerm
                    ) { workType = WorkType.LongTerm }

                    ListChoice(
                        title = "프로젝트", sub = "단기, 계약직",
                        selected = workType == WorkType.FullTime
                    ) { workType = WorkType.FullTime }
                }
            }
            SectionSpacer()

            // --- 근무 요일 + 시간협의 ---
            SectionCard(padding = 20.dp) {
                LabelText("근무요일")
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekdays.forEachIndexed { idx, w ->
                        Box(Modifier.weight(1f)) {
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CheckBoxLike(
                        checked = timeNegotiable,
                        onToggle = { timeNegotiable = !timeNegotiable }
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "시간협의",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.019).em()
                    )
                }
            }
            SectionSpacer()

            // --- 근무 시간 (시작/종료, 각 159×55) ---
            SectionCard {
                LabelText("근무 시간")
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "시작시간",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.019).em()
                        )
                        Spacer(Modifier.height(6.dp))
                        TimeBox(startTime) { /* TODO: TimePicker */ }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "종료시간",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.019).em()
                        )
                        Spacer(Modifier.height(6.dp))
                        TimeBox(endTime) { /* TODO: TimePicker */ }
                    }
                }
            }
            SectionSpacer()

            // --- 업무 강도 (리스트 카드 3개) ---
            SectionCard {
                LabelText("업무 강도")
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ListChoice(
                        title = "가벼움", sub = "책상 업무 위주, 체력 부담 적음",
                        selected = intensity == Intensity.Light
                    ) { intensity = Intensity.Light }
                    ListChoice(
                        title = "보통", sub = "적당한 활동량, 일반적 업무 강도",
                        selected = intensity == Intensity.Medium
                    ) { intensity = Intensity.Medium }
                    ListChoice(
                        title = "활동적", sub = "이동이나 서서 하는 업무 포함",
                        selected = intensity == Intensity.Hard
                    ) { intensity = Intensity.Hard }
                }
            }
            SectionSpacer()

            // --- 하단 Prev(88×47) / Next(235×47) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)   // ✅ 3·4와 동일 간격
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(0.6f)                               // ✅ 0.6 : 1.4 비율
                            .height(BOTTOM_BTN_HEIGHT),                 // ✅ 44.dp
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Blue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("이전", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .weight(1.4f)                               // ✅ 0.6 : 1.4 비율
                            .height(BOTTOM_BTN_HEIGHT),                 // ✅ 44.dp
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("다음 단계", fontSize = 16.sp, fontWeight = FontWeight.Bold) // 텍스트도 3·4와 동일
                    }
                }
            }

            BottomNavPlaceholder()
        }
    }
}

/* ====== 상단 Status 24dp ====== */
@Composable
private fun StatusBarBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Color(0xFFEFEFEF))
    )
}

@Composable
private fun SectionSpacer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(BgGray)
    )
}

/* ====== TabBar (02 선택, 높이 40dp, 간격 61dp, 인디케이터 41×4) ====== */
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
                        letterSpacing = (-0.5).sp, // Pretendard -0.5px
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
    Text(
        text,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.019).em()
    )
}

@Composable
private fun LabelText(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        letterSpacing = (-0.019).em(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 6.dp)
    )
}

/* 2x2 레이아웃 (칸 간격 10dp) */
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

/* 2열 그리드 (가변 아이템) */
@Composable
private fun GridTwoColumns(
    items: List<String>,
    itemContent: @Composable (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) { itemContent(row[0]) }
                if (row.size > 1) {
                    Box(Modifier.weight(1f)) { itemContent(row[1]) }
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/* 직종 단일선택 카드 (159×70, 선택 시 파란 배경/흰 텍스트) */
@Composable
private fun SelectCardSingle(
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
            .border(1.dp, if (selected) Blue else BorderGray, shape)
            .background(if (selected) Blue else Color.White, shape)
            .clickable { onClick() }
            .padding(start = 24.dp, top = 10.dp, bottom = 10.dp),   // ✅ 내부 왼쪽 패딩
        horizontalAlignment = Alignment.Start,                     // ✅ 왼쪽 정렬
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else TextGray,
            letterSpacing = (-0.019).em(),
            textAlign = TextAlign.Start                            // ✅ 텍스트 왼쪽 정렬
        )
        Text(
            sub,
            fontSize = 12.sp,
            color = if (selected) Color(0xFFD9D9D9) else TextGray,
            letterSpacing = (-0.019).em(),
            textAlign = TextAlign.Start
        )
    }

}

/* 재능 토글 카드 (159×70, 선택 시 파란 배경/흰 텍스트) */
@Composable
private fun SelectCardToggle(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(CARD_CORNER)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(TALENT_CARD_HEIGHT)
            .clip(shape)
            .border(1.dp, if (selected) Blue else BorderGray, shape)
            .background(if (selected) Blue else Color.White, shape)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            title,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else TextGray,
            letterSpacing = (-0.019).em()
        )
    }
}

/* 리스트형 선택 (61dp, 보더 #828282, 타이틀 15, 서브 12) */
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
            .height(63.dp)
            .clip(shape)
            .border(1.dp, BorderGray, shape)
            .background(if (selected) Color(0xFFEFF5FF) else Color.White, shape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {
        Text(
            title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Blue else TextGray,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.019).em()
        )
        Text(
            sub,
            fontSize = 12.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.019).em()
        )
    }
}

/* 근무 요일 칩 (38×48, 선택 시 파란 배경/흰 텍스트) */
@Composable
private fun DayChip(
    label: String,
    selected: Boolean,
    onToggle: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(DAY_H)
            .clip(shape)
            .border(1.dp, if (selected) Blue else BorderGray, shape)
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
            color = if (selected) Color.White else TextGray,
            letterSpacing = (-0.019).em()
        )
    }
}

/* 체크박스 스타일 네모 (외곽선 파랑) */
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
        // ✅ 외곽 박스
        Box(
            modifier = Modifier
                .size(16.dp)
                .border(width = 1.dp, color = Blue, shape = shape)
                .background(if (checked) Blue else Color.Transparent, shape = shape),
            contentAlignment = Alignment.Center
        ) {
            // ✅ 체크 표시
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check, // 머티리얼 체크 아이콘
                    contentDescription = "checked",
                    tint = Color.White,                // 흰색 체크
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/* 시간 박스 (159×55, 보더 블루, 텍스트 15 SemiBold) */
@Composable
private fun TimeBox(
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(CARD_CORNER)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(TIME_BOX_HEIGHT)
            .clip(shape)
            .border(1.dp, Blue, shape)
            .background(Color.White, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            letterSpacing = (-0.019).em()
        )
    }
}

/* 멀티라인 입력 (328×120) */
@Composable
private fun MultilineInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(shape)
            .border(1.dp, BorderGray, shape)
            .background(Color.White, shape)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 13.sp,
                color = Color.Black,
                letterSpacing = (-0.019).em()
            ),
            modifier = Modifier.fillMaxSize(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        fontSize = 13.sp,
                        color = TextGray,
                        letterSpacing = (-0.019).em()
                    )
                }
                inner()
            }
        )
    }
}

/* 싱글라인 입력 (328×40) */
@Composable
private fun SinglelineInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(shape)
            .border(1.dp, BorderGray, shape)
            .background(Color.White, shape)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 13.sp,
                color = Color.Black,
                letterSpacing = (-0.019).em()
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        fontSize = 13.sp,
                        color = TextGray,
                        letterSpacing = (-0.019).em()
                    )
                }
                inner()
            }
        )
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
private fun weekdayDefaults() =
    listOf("월", "화", "수", "목", "금", "토", "일").map { Weekday(it, false) }

/* ---- util: em letterSpacing helper ---- */
private fun Double.em() = (this * 16).sp // 대략 -0.019em ≈ -0.304px@16sp → Compose 보정용
private fun Float.em() = (this * 16).sp

