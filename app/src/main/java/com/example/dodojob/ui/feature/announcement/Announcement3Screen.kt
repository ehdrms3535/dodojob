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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.dodojob.data.announcement.needlicense3.NeedlicenseDto
import com.example.dodojob.data.announcement.needlicense3.NeedlicenseRepo
import com.example.dodojob.data.announcement.needlicense3.NeedlisenceRepoSupabase
import com.example.dodojob.data.announcement.preferential3.PreferentialDto
import com.example.dodojob.data.announcement.preferential3.PreferentialRepo
import com.example.dodojob.data.announcement.preferential3.PreferentialRepoSuSupabase
import com.example.dodojob.data.announcement.salary3.SalaryDto
import com.example.dodojob.data.announcement.salary3.SalaryRepository
import com.example.dodojob.data.announcement.salary3.SalaryRepositorySupabase
import com.example.dodojob.data.supabase.LocalSupabase
import kotlinx.coroutines.launch

/* -------- Colors -------- */
private val Blue = Color(0xFF005FFF)
private val TextGray = Color(0xFF828282)
private val BgGray = Color(0xFFF1F5F7)
private val CardBg = Color.White

/* -------- Unified Sizes -------- */
private val CARD_HEIGHT = 70.dp            // 선택 카드/칩 통일 높이
private val CARD_CORNER = 10.dp            // 선택 카드/칩 라운드 통일
private val BOTTOM_BTN_HEIGHT = 44.dp      // 하단 버튼 통일 높이

/* ================== Route ================== */
@Composable
fun Announcement3Route(
    nav: NavController,
    onNext: () -> Unit = {
        nav.navigate(Route.Announcement4.path) {
            launchSingleTop = true
        }
    },
    onBack: () -> Unit = { nav.popBackStack() },
    onTabClick: (Int) -> Unit = { idx ->
        val target = when (idx) {
            0 -> Route.Announcement.path
            1 -> Route.Announcement2.path
            2 -> Route.Announcement3.path
            else -> Route.Announcement4.path
        }
        val current = nav.currentBackStackEntry?.destination?.route
        if (current != target) {
            nav.navigate(target) { launchSingleTop = true }
        }
    }
) {
    Announcement3Screen(
        onNext = onNext,
        onBack = onBack,
        onTabClick = onTabClick
    )
}

/* ====== Screen: 공고등록 / 03 (급여·복리·성별·경력) ====== */
@Composable
fun Announcement3Screen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    onTabClick: (Int) -> Unit
) {
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val client = LocalSupabase.current

    val repo: SalaryRepository = remember(client) { SalaryRepositorySupabase(client) }
    val needRepo: NeedlicenseRepo = remember(client) { NeedlisenceRepoSupabase(client) }
    val prefRepo: PreferentialRepo = remember(client) { PreferentialRepoSuSupabase(client) }

    var loading by rememberSaveable { mutableStateOf(false) }

    /* ----------- State ----------- */
    var payType by remember { mutableStateOf(PayType.Hourly) }
    var selectedPayType by remember { mutableStateOf("시급") }
    var hourlyWage by remember { mutableStateOf("") }  // "12,000" 등 문자열
    var companyName by remember { mutableStateOf("") }
    var selectedgender by remember { mutableStateOf("")}

    // 복리혜택 (bit 문자열 변환 대상)
    val benefits = remember { defaultBenefits().toMutableStateList() }

    var gender by remember { mutableStateOf(Gender.Any) }
    var exp by remember { mutableStateOf(Experience.None) }

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

        /* Tabs (03 선택) */
        TabBar3(selected = 2, labels = listOf("01", "02", "03", "04"), onClick = onTabClick)

        /* Body */
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
        ) {
            /* 안내 */
            SectionCard {
                TitleRow("03. 급여·복리·요건을 선택해주세요.")
            }

            /* 급여정보 */
            SectionCard {
                LabelText("급여정보")

                SubLabel("급여 금액")
                Spacer(Modifier.height(6.dp))

                // 급여 타입 2x2
                TwoByTwo(
                    {
                        PayCard(
                            title = "시급",
                            sub = "Hourly",
                            selected = payType == PayType.Hourly,
                            onClick = {
                                payType = PayType.Hourly
                                selectedPayType = "시급"
                            }
                        )
                    },
                    {
                        PayCard(
                            title = "일급",
                            sub = "Daily",
                            selected = payType == PayType.Daily,
                            onClick = {
                                payType = PayType.Daily
                                selectedPayType = "일급"
                            }
                        )
                    },
                    {
                        PayCard(
                            title = "월급",
                            sub = "Monthly",
                            selected = payType == PayType.Monthly,
                            onClick = {
                                payType = PayType.Monthly
                                selectedPayType = "월급"
                            }
                        )
                    },
                    {
                        PayCard(
                            title = "연봉",
                            sub = "Yearly",
                            selected = payType == PayType.Yearly,
                            onClick = {
                                payType = PayType.Yearly
                                selectedPayType = "연봉"
                            }
                        )
                    }
                )

                Spacer(Modifier.height(12.dp))
                SubLabel("시급")
                Spacer(Modifier.height(6.dp))
                MoneyLineField(
                    value = hourlyWage,
                    placeholder = "예: 12,000",
                    suffix = "원",
                    onChange = { hourlyWage = it }
                )
            }

            /* 근무회사명 (라인 인풋) */
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("근무회사명", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
                LineTextField(
                    value = companyName,
                    placeholder = "내용입력",
                    onChange = { companyName = it }
                )
            }

            /* 복리혜택 (버튼 래핑) */
            SectionCard {
                LabelText("복리혜택")
                Spacer(Modifier.height(6.dp))
                WrapChips(
                    items = benefits,
                    onToggle = { idx ->
                        val cur = benefits[idx]
                        benefits[idx] = cur.copy(selected = !cur.selected)
                    }
                )
            }

            /* 성별 */
            SectionCard {
                LabelText("성별")
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GenderCard("남", gender == Gender.Male, { gender = Gender.Male }, modifier = Modifier.weight(1f))
                    GenderCard("여", gender == Gender.Female, { gender = Gender.Female }, modifier = Modifier.weight(1f))
                    GenderCard("무관", gender == Gender.Any, { gender = Gender.Any }, modifier = Modifier.weight(1f))
                }
            }

            /* 경력 요구사항 */
            SectionCard {
                LabelText("경력 요구사항")
                Spacer(Modifier.height(6.dp))
                TwoByTwo(
                    { ExpCard("신입",   exp == Experience.Entry) { exp = Experience.Entry } },
                    { ExpCard("1년↑",  exp == Experience.Y1)    { exp = Experience.Y1 } },
                    { ExpCard("3년↑",  exp == Experience.Y3)    { exp = Experience.Y3 } },
                    { ExpCard("경력무관", exp == Experience.None) { exp = Experience.None } },
                )
            }

            /* 하단 버튼: 이전 / 다음 */
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
                        onClick = {
                            scope.launch {
                                loading = true
                                runCatching {
                                    val benefitBits = benefitsToBitString(benefits)

                                    val salary = SalaryDto(
                                        salary_type = selectedPayType,                          // 급여유형 ("시급" 등)
                                        salary_amount = hourlyWage.filter { it.isDigit() }
                                            .toLongOrNull() ?: 0L,                              // 시급 숫자 저장
                                        benefit = benefitBits,                                  // 복리혜택 bit 문자열
                                        career = experienceToCode(exp),                         // 경력 코드(또는 그대로 문자열)
                                        gender = genderToCode(gender)                           // 성별 코드(또는 그대로 문자열)
                                    )
                                    repo.insertSalary(salary)

                                    val preferential = PreferentialDto(
                                        preferential_treatment = ""   // 스키마에 맞춰 채워줘
                                    )
                                    prefRepo.insertPreferential(preferential)

                                    val needlicense = NeedlicenseDto(
                                        need1 = ""                     // 스키마에 맞춰 채워줘
                                    )
                                    needRepo.insertNeedlisence(needlicense)
                                }.onSuccess {
                                    onNext()    // 실제 호출
                                }.onFailure {
                                    // TODO: 에러 핸들링 (스낵바/토스트 등)
                                }
                                loading = false
                            }
                        },
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

/* ====== TabBar (03 선택) ====== */
@Composable
private fun TabBar3(
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
            val animatedX by animateDpAsState(targetValue = targetX, label = "tab-indicator-3")

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
        letterSpacing = (-0.38).sp
    )
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

@Composable
private fun SubLabel(text: String) {
    Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
}

@Composable
private fun LineTextField(
    value: String,
    placeholder: String,
    onChange: (String) -> Unit
) {
    val border = RoundedCornerShape(CARD_CORNER)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .border(1.dp, Blue, border)
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

/* --- 급여 카드 --- */
@Composable
private fun PayCard(
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
        Text(
            title,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else Blue
        )
        Text(
            sub,
            fontSize = 12.sp,
            color = if (selected) Color(0xFFD9D9D9) else TextGray
        )
    }
}

/* --- 성별 카드 --- */
@Composable
private fun GenderCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(CARD_CORNER)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            .clip(shape)
            .border(1.dp, Blue, shape)
            .background(if (selected) Blue else Color.White, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else Blue
        )
    }
}

/* --- 경력 카드 (selected만 반영하도록 단순화) --- */
@Composable
private fun ExpCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(CARD_CORNER)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            .clip(shape)
            .border(1.dp, Blue, shape)
            .background(if (selected) Blue else Color.White, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else Blue
        )
    }
}

/* --- 복리혜택 비트 인코딩용 모델/헬퍼 --- */
private enum class BenefitKey {
    FOUR_INS,      // 4대보험
    MEAL,          // 식사 제공
    TRANSPORT,     // 교통비
    HOLIDAY_BONUS, // 명절 보너스
    OVERTIME,      // 야근수당
    UNIFORM        // 근무복 지급
}

private data class Benefit(
    val key: BenefitKey,
    val name: String,
    val selected: Boolean
)

private fun defaultBenefits() = listOf(
    Benefit(BenefitKey.FOUR_INS,      "4대보험",    false),
    Benefit(BenefitKey.MEAL,          "식사 제공",  false),
    Benefit(BenefitKey.TRANSPORT,     "교통비",    false),
    Benefit(BenefitKey.HOLIDAY_BONUS, "명절 보너스", false),
    Benefit(BenefitKey.OVERTIME,      "야근수당",   false),
    Benefit(BenefitKey.UNIFORM,       "근무복 지급", false)
)

/** 선택 상태 → bit 문자열 (예: "101001") */
private fun benefitsToBitString(list: List<Benefit>): String {
    val byKey = list.associateBy { it.key }
    return BenefitKey.entries.joinToString("") { key ->
        if (byKey[key]?.selected == true) "1" else "0"
    }
}

/** bit 문자열 → 선택 상태 적용 (수정 화면 복원용, 필요 시 사용) */
@Suppress("unused")
private fun applyBitStringToBenefits(bit: String, list: MutableList<Benefit>) {
    val max = minOf(bit.length, BenefitKey.entries.size)
    val indexByKey = BenefitKey.entries.withIndex().associate { it.value to it.index }
    list.replaceAll { b ->
        val idx = indexByKey[b.key]!!
        val sel = if (idx < max) bit[idx] == '1' else false
        b.copy(selected = sel)
    }
}

/* --- 복리혜택 칩(2열 래핑) --- */
@Composable
private fun WrapChips(
    items: List<Benefit>,
    onToggle: (Int) -> Unit
) {
    val shape = RoundedCornerShape(CARD_CORNER)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEachIndexed { rowIdx, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEachIndexed { colIdx, item ->
                    val idx = rowIdx * 2 + colIdx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .height(CARD_HEIGHT)
                            .clip(shape)
                            .border(1.dp, Blue, shape)
                            .background(if (item.selected) Blue else Color.White, shape)
                            .clickable { onToggle(idx) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            item.name,
                            fontWeight = FontWeight.Bold,
                            color = if (item.selected) Color.White else Blue
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/* --- 기타 공용 --- */
@Composable
private fun BottomNavPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .background(Color(0xFFF4F5F7))
    )
}

@Composable
private fun MoneyLineField(
    value: String,
    placeholder: String,
    suffix: String = "원",
    onChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(CARD_CORNER)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .border(1.dp, Blue, shape)
            .clip(shape)
            .background(Color.White)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = { raw ->
                onChange(formatMoney(raw))
            },
            textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            singleLine = true,
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(placeholder, fontSize = 13.sp, color = TextGray)
                }
                inner()
            }
        )
        Spacer(Modifier.width(8.dp))
        Text(suffix, fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatMoney(input: String): String {
    val digits = input.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    val sb = StringBuilder()
    var cnt = 0
    for (i in digits.length - 1 downTo 0) {
        sb.append(digits[i])
        cnt++
        if (cnt % 3 == 0 && i != 0) sb.append(',')
    }
    return sb.reverse().toString()
}

/* --- enums & 코드 매핑 --- */
private enum class PayType { Hourly, Daily, Monthly, Yearly }
private enum class Gender { Male, Female, Any }
private enum class Experience { Entry, Y1, Y3, None }


private fun genderToCode(g: Gender): String = when (g) {
    Gender.Male -> "male"
    Gender.Female -> "female"
    Gender.Any -> "all"
}

private fun experienceToCode(e: Experience): String = when (e) {
    Experience.Entry -> "ENTRY"
    Experience.Y1 -> "Y1_PLUS"
    Experience.Y3 -> "Y3_PLUS"
    Experience.None -> "NONE"
}

/* -------- Preview -------- */
/*
@Preview(showSystemUi = true, device = Devices.PIXEL_7, locale = "ko")
@Composable
private fun PreviewAnnouncement3() {
    Announcement3Screen(
        onNext = {},
        onBack = {},
        onTabClick = {}
    )
}
*/
