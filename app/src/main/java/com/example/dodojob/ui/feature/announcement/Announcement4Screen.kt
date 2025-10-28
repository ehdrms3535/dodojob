package com.example.dodojob.ui.feature.announcement

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.navigation.Route   // ✅ Route 사용

/* -------- Colors -------- */
private val Blue = Color(0xFF005FFF)
private val TextGray = Color(0xFF828282)
private val BgGray = Color(0xFFF1F5F7)
private val CardBg = Color.White

/* ====== Route Entrypoint ====== */
@Composable
fun Announcement4Route(
    nav: NavController,
    onSubmit: () -> Unit = {
        nav.navigate(Route.Announcement5.path) {    // ✅ 다음 단계 → 05로 이동
            launchSingleTop = true
        }
    },
    onBack: () -> Unit = { nav.popBackStack() },
    onEditBasic: () -> Unit = {},
    onEditJob: () -> Unit = {},
    onEditWorkCond: () -> Unit = {},
    onEditPayBenefit: () -> Unit = {},
    onEditRequirements: () -> Unit = {},
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
    Announcement4Screen(
        onSubmit = onSubmit,
        onBack = onBack,
        onEditBasic = onEditBasic,
        onEditJob = onEditJob,
        onEditWorkCond = onEditWorkCond,
        onEditPayBenefit = onEditPayBenefit,
        onEditRequirements = onEditRequirements,
        onTabClick = onTabClick
    )
}

/* ====== Screen: 공고등록 / 04 최종확인 ====== */
@Composable
fun Announcement4Screen(
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onEditBasic: () -> Unit,
    onEditJob: () -> Unit,
    onEditWorkCond: () -> Unit,
    onEditPayBenefit: () -> Unit,
    onEditRequirements: () -> Unit,
    onTabClick: (Int) -> Unit
) {
    val scroll = rememberScrollState()

    // 데모 데이터 (1~3단계에서 넘어오는 상태 바인딩 예정)
    val companyName by remember { mutableStateOf("모던하우스") }
    val contactName by remember { mutableStateOf("홍길동") }
    val contactPhone by remember { mutableStateOf("010-1234-5678") }
    val companyLocation by remember { mutableStateOf("서울시 강남구 테헤란로 123") }

    val majorJob by remember { mutableStateOf("요식/서빙") }
    val headCount by remember { mutableStateOf("3명") }
    val jobDesc by remember { mutableStateOf("음료 제조, 매장 응대, 마감 정리") }

    val workType by remember { mutableStateOf("장기 알바") }
    val workTime by remember { mutableStateOf("12:30 ~ 18:00 (협의 가능)") }
    val workDaysCount by remember { mutableStateOf("주 3일 (월/수/금)") }
    val intensity by remember { mutableStateOf("보통") }

    val hourlyWage by remember { mutableStateOf("시급 10,500원") }
    val monthlyEstimate by remember { mutableStateOf("월 약 820,000원") }
    val benefits by remember { mutableStateOf("식대 제공, 명절 보너스") }

    val reqGender by remember { mutableStateOf("무관") }
    val reqMust by remember { mutableStateOf("주말 1회 이상 근무 가능") }
    val reqPrefer by remember { mutableStateOf("바리스타 자격증 보유, 인근 거주자") }

    // 지원 방식: 카드 2개 중 택1
    var applyMethod by remember { mutableStateOf(ApplyMethod.PhoneSms) }

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
        ) {
            Text("공고등록", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        }

        /* Tabs (04 선택) */
        TabBar4(selected = 3, labels = listOf("01", "02", "03", "04"), onClick = onTabClick)

        /* Body */
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
        ) {
            /* 상단 설명 */
            SectionCard {
                TitleRow("04. 최종 검토 후 공고를 게시해주세요!")
            }

            /* 기본정보 */
            SectionCard {
                SectionHeader(title = "기본정보", onEdit = onEditBasic)
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConfirmItem(label = "근무회사명", value = companyName)
                    ConfirmItem(label = "담당자명", value = contactName)
                    ConfirmItem(label = "담당자 연락처", value = contactPhone)
                    ConfirmItem(label = "회사 위치", value = companyLocation)
                }
            }

            /* 모집 직종 */
            SectionCard {
                SectionHeader(title = "모집 직종", onEdit = onEditJob)
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConfirmItem(label = "직종 카테고리", value = majorJob)
                    ConfirmItem(label = "모집 인원", value = headCount)
                    ConfirmItem(label = "업무 내용", value = jobDesc)
                }
            }

            /* 근무 조건 */
            SectionCard {
                SectionHeader(title = "근무 조건", onEdit = onEditWorkCond)
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConfirmItem(label = "근무 형태", value = workType)
                    ConfirmItem(label = "근무 시간", value = workTime)
                    ConfirmItem(label = "근무 일수", value = workDaysCount)
                    ConfirmItem(label = "체력 강도", value = intensity)
                }
            }

            /* 급여 및 혜택 */
            SectionCard {
                SectionHeader(title = "급여 및 혜택", onEdit = onEditPayBenefit)
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConfirmItem(label = "시급", value = hourlyWage)
                    ConfirmItem(label = "월 예상 급여", value = monthlyEstimate)
                    ConfirmItem(label = "복리 혜택", value = benefits)
                }
            }

            /* 지원자 요건 */
            SectionCard {
                SectionHeader(title = "지원자 요건", onEdit = onEditRequirements)
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConfirmItem(label = "성별", value = reqGender)
                    ConfirmItem(label = "필수 조건", value = reqMust)
                    ConfirmItem(label = "우대사항", value = reqPrefer)
                }
            }

            /* 지원 방식 - 카드형 2개 (체크박스 제거) */
            SectionCard {
                LabelText("지원 방식")
                Spacer(Modifier.height(6.dp))
                ApplyMethodSection(
                    selected = applyMethod,
                    onSelect = { applyMethod = it }
                )
            }

            /* 하단 버튼 2개 */
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
                        onClick = { onBack() },
                        modifier = Modifier
                            .weight(0.6f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Blue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("이전", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { onSubmit() },     // ✅ Route 기본값에서 05로 navigate
                        modifier = Modifier
                            .weight(1.4f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("다음 단계", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            BottomNavPlaceholder()
        }
    }
}

/* ====== TabBar (centered indicator, 04 선택) ====== */
@Composable
private fun TabBar4(
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
            val animatedX by animateDpAsState(targetValue = targetX, label = "tab-indicator-4")

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

/* ====== Reusable UI ====== */
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
        letterSpacing = (-0.38).sp
    )
}

/* 제목 옆 "수정" 배치 */
@Composable
private fun SectionHeader(title: String, onEdit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.34).sp
        )
        Spacer(Modifier.width(8.dp))
        EditPill(onClick = onEdit)
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun EditPill(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(25.dp)
            .wrapContentWidth()
            .background(Blue, RoundedCornerShape(25.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("수정", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun ConfirmItem(
    label: String,
    value: String
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .border(1.dp, Blue, shape)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextGray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

/* ---------- 지원 방식: 카드형 2개 ---------- */
private enum class ApplyMethod { PhoneSms, OnlineForm }

@Composable
private fun ApplyMethodSection(
    selected: ApplyMethod,
    onSelect: (ApplyMethod) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ApplyChoiceCard(
            title = "전화 / 문자",
            subtitle = "지원자가 직접 전화/문자",
            selected = selected == ApplyMethod.PhoneSms,
            onClick = { onSelect(ApplyMethod.PhoneSms) }
        )
        ApplyChoiceCard(
            title = "온라인 지원서",
            subtitle = "구조화된 지원서 양식",
            selected = selected == ApplyMethod.OnlineForm,
            onClick = { onSelect(ApplyMethod.OnlineForm) }
        )
    }
}

@Composable
private fun ApplyChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val stroke = BorderStroke(1.dp, if (selected) Blue else Color(0xFFCFDBFF))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(61.dp)
            .border(stroke, shape)
            .background(Color.White, shape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Blue else Color(0xFF1A1A1A),
            textAlign = TextAlign.Center
        )
        Text(
            subtitle,
            fontSize = 12.sp,
            color = TextGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(47.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White)
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
private fun LabelText(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        letterSpacing = (-0.34).sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 6.dp)
    )
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

