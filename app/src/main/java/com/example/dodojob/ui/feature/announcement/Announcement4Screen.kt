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
import com.example.dodojob.dao.getCompanyIdByUsername
import com.example.dodojob.dao.getannouncebycom
import com.example.dodojob.navigation.Route   // ✅ Route 사용
import com.example.dodojob.data.announcement.fullannouncement.*
import com.example.dodojob.session.CurrentUser
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/* -------- Colors -------- */
private val Blue = Color(0xFF005FFF)
private val TextGray = Color(0xFF828282)
private val BgGray = Color(0xFFF1F5F7)
private val CardBg = Color.White

// package com.example.dodojob.ui.feature.announcement

private val BENEFIT_LABELS = listOf("식대 지원", "교통비 지원", "4대 보험", "교육비 지원")

private fun benefitBitsToText(bits: String?): String {
    if (bits.isNullOrBlank()) return "없음"
    val first4 = bits.padEnd(4, '0').substring(0, 4)
    val onLabels = buildList {
        first4.forEachIndexed { idx, c ->
            if (c == '1' && idx < BENEFIT_LABELS.size) add(BENEFIT_LABELS[idx])
        }
    }
    return if (onLabels.isEmpty()) "없음" else onLabels.joinToString(" / ")
}

private fun weekBitsToText(bits: String?): String {
    // "1111111" → "주 7일 (월/화/수/목/금/토/일)"
    if (bits.isNullOrBlank()) return "협의"
    val days = listOf("월","화","수","목","금","토","일")
    val on = days.indices.filter { i -> i < bits.length && bits[i] == '1' }.map { days[it] }
    val count = on.size
    return if (count == 0) "협의" else "주 ${count}일 (${on.joinToString("/")})"
}

private fun timeRangeText(start: String?, end: String?): String {
    val s = start ?: "-"
    val e = end ?: "-"
    return if (s == "-" && e == "-") "협의" else "$s ~ $e"
}

private fun salaryText(type: String?, amount: Int?): String {
    if (type.isNullOrBlank() || amount == null) return "협의"
    val pretty = "%,d".format(amount)
    return "$type $pretty 원"
}

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

    var row by remember { mutableStateOf<AnnouncementFullRow?>(null) }
    var applyMethod by remember { mutableStateOf(ApplyMethod.PhoneSms) }

    LaunchedEffect(Unit) {
        val username = CurrentUser.username
        if (username.isNullOrBlank()) return@LaunchedEffect

        runCatching {
            withContext(Dispatchers.IO) {
                val companyId = CurrentUser.companyid           // suspend 가정
                val announceId = getannouncebycom(companyId)
                    ?: error("해당 회사의 공고가 없습니다.")
                val announce = announceId.id

                fetchAnnouncementFull( id = announce)
            }
        }.onSuccess { fetched ->
            row = fetched                                                  // state 업데이트
        }.onFailure { e ->
            // TODO: 스낵바/로그 처리
            e.printStackTrace()
        }
    }

    // -------- 화면에 꽂기 위한 문자열 준비 --------
    val companyName = row?.company_name ?: "-"
    val companyLocation = listOfNotNull(row?.company_locate, row?.detail_locate?.takeIf { it.isNotBlank() })
        .joinToString(" ")
    val contactName = "-"        // 이전 단계 상태 연결 전까지 placeholder
    val contactPhone = "-"       // 이전 단계 상태 연결 전까지 placeholder

    val majorJob = row?.work_category ?: "-"
    val headCount = "-"                                 // (뷰에 없음) 추후 02~03 단계 상태와 연결
    val jobDesc = row?.major ?: "-"

    val workType = row?.form ?: "-"
    val workTime = timeRangeText(row?.starttime, row?.endtime)
    val workDaysCount = weekBitsToText(row?.week)
    val intensity = row?.intensity ?: "-"

    val hourlyWage = salaryText(row?.salary_type, row?.salary_amount)
    val monthlyEstimate = "-"                            // (필요시 계산로직 추가)
    val benefits = benefitBitsToText(row?.benefit)

    val reqGender = row?.gender ?: "무관"
    val reqMust = row?.license_requirement ?: "-"        // 필수조건으로 매핑
    val reqPrefer = buildList {
        if (!row?.preferential_treatment.isNullOrBlank()) add(row!!.preferential_treatment!!)
        if (!row?.skill.isNullOrBlank()) add("스킬: ${row!!.skill}")
    }.joinToString(" / ").ifBlank { "-" }



    val scroll = rememberScrollState()
/*
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
*/
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
                Spacer(modifier = Modifier.height(18.dp))
                TitleRow("04. 최종 검토 후 공고를 게시해주세요!")
            }

            /* 기본정보 */
            SectionCard {
                SectionHeader(title = "기본정보", onEdit = onEditBasic)
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConfirmItem(label = "근무회사명", value = companyName)
                    ConfirmItem(label = "담당자명", value = contactName)
                    ConfirmItem(label = "담당자 연락처", value = contactPhone)
                    ConfirmItem(label = "회사 위치", value = companyLocation)
                }
            }
            SectionSpacer()

            /* 모집 직종 */
            SectionCard {
                SectionHeader(title = "모집 직종", onEdit = onEditJob)
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConfirmItem(label = "직종 카테고리", value = majorJob)
                    ConfirmItem(label = "모집 인원", value = headCount)
                    ConfirmItem(label = "업무 내용", value = jobDesc)
                }
            }
            SectionSpacer()

            /* 근무 조건 */
            SectionCard {
                SectionHeader(title = "근무 조건", onEdit = onEditWorkCond)
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConfirmItem(label = "근무 형태", value = workType)
                    ConfirmItem(label = "근무 시간", value = workTime)
                    ConfirmItem(label = "근무 일수", value = workDaysCount)
                    ConfirmItem(label = "체력 강도", value = intensity)
                }
            }
            SectionSpacer()

            /* 급여 및 혜택 */
            SectionCard {
                SectionHeader(title = "급여 및 혜택", onEdit = onEditPayBenefit)
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConfirmItem(label = "시급", value = hourlyWage)
                    ConfirmItem(label = "월 예상 급여", value = monthlyEstimate)
                    ConfirmItem(label = "복리 혜택", value = benefits)
                }
            }
            SectionSpacer()

            /* 지원자 요건 */
            SectionCard {
                SectionHeader(title = "지원자 요건", onEdit = onEditRequirements)
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConfirmItem(label = "성별", value = reqGender)
                    ConfirmItem(label = "필수 조건", value = reqMust)
                    ConfirmItem(label = "우대사항", value = reqPrefer)
                }
            }
            SectionSpacer()

            /* 지원 방식 - 카드형 2개 (체크박스 제거) */
            SectionCard {
                LabelText("지원 방식")
                Spacer(Modifier.height(10.dp))
                ApplyMethodSection(
                    selected = applyMethod,
                    onSelect = { applyMethod = it }
                )
            }
            SectionSpacer()

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
                    horizontalArrangement = Arrangement.spacedBy(5.dp) // ← 5.dp로
                ) {
                    OutlinedButton(
                        onClick = { onBack() },
                        modifier = Modifier
                            .width(88.dp)                    // ← 고정 88.dp
                            .height(47.dp),                  // ← 47.dp
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Blue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue),
                        contentPadding = PaddingValues(horizontal = 10.dp) // ← 10.dp
                    ) {
                        Text(
                            "이전",
                            fontSize = 18.sp,                 // ← 18.sp
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp,        // ← 동일 letterSpacing
                            color = Blue
                        )
                    }

                    Button(
                        onClick = { onSubmit() },
                        modifier = Modifier
                            .weight(1f)                       // ← 나머지 전부
                            .height(47.dp),                   // ← 47.dp
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            "다음 단계",
                            fontSize = 18.sp,                 // ← 18.sp
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp         // ← 동일 letterSpacing
                        )
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
private fun SectionSpacer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(BgGray)
    )
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
            .height(64.dp)
            .border(stroke, shape)
            .background(Color.White, shape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Blue else Color(0xFF1A1A1A),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(0.dp)) // ✅ 간격을 줄임 (기존 6~8dp → 2dp)
        Text(
            text = subtitle,
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

