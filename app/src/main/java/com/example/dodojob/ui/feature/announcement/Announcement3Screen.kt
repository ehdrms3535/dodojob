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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.dao.getCompanyIdByUsername
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
import com.example.dodojob.session.CurrentUser
import kotlinx.coroutines.launch
import com.example.dodojob.dao.getannouncebycom
/* -------- Design tokens (01/02와 동일) -------- */
private val Blue = Color(0xFF005FFF)
private val TextGray = Color(0xFF828282)
private val BorderGray = Color(0xFF828282)
private val BgGray = Color(0xFFF1F5F7)
private val CardBg = Color.White

/* -------- Unified Sizes -------- */
private val CARD_HEIGHT = 70.dp     // 카드/칩 높이
private val CARD_CORNER = 10.dp
private val GRID_GAP = 10.dp
private val BOTTOM_BTN_HEIGHT = 47.dp

/* ================== Route ================== */
@Composable
fun Announcement3Route(
    nav: NavController,
    onNext: () -> Unit = {
        nav.navigate(Route.Announcement4.path) { launchSingleTop = true }
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
        if (current != target) nav.navigate(target) { launchSingleTop = true }
    }
) {
    Announcement3Screen(
        onNext = onNext,
        onBack = onBack,
        onTabClick = onTabClick
    )
}

/* ====== Screen: 공고등록 / 03 (급여·복리·성별·경력·자격/우대) ====== */
@Composable
fun Announcement3Screen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    onTabClick: (Int) -> Unit
) {
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val client = LocalSupabase.current

    val salaryRepo: SalaryRepository = remember(client) { SalaryRepositorySupabase(client) }
    val needRepo: NeedlicenseRepo = remember(client) { NeedlisenceRepoSupabase(client) }
    val prefRepo: PreferentialRepo = remember(client) { PreferentialRepoSuSupabase(client) }

    var loading by rememberSaveable { mutableStateOf(false) }

    /* ----------- State ----------- */
    var payType by remember { mutableStateOf(PayType.Hourly) }
    var selectedPayType by remember { mutableStateOf("시급") }
    var payAmount by remember { mutableStateOf("") } // 공통 입력(피그마: “급여 금액” 아래 라인 인풋)

    // 복리혜택
    val benefits = remember { defaultBenefits().toMutableStateList() }

    // 성별
    var gender by remember { mutableStateOf(Gender.Any) }

    // 경력
    var exp by remember { mutableStateOf(Experience.None) }

    // 자격/우대 입력
    var licenseText by remember { mutableStateOf("") }
    var preferText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
    ) {
        /* StatusBar (24dp) 는 /01에서 공통 처리했다고 가정 */

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
                color = Color.Black,
                letterSpacing = (-0.3).sp
            )
        }

        /* Tabs (03 선택) */
        TabBar3(
            selected = 2,
            labels = listOf("01", "02", "03", "04"),
            onClick = onTabClick
        )

        /* Body */
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
        ) {

            /* 안내 */
            SectionCard {
                Spacer(modifier = Modifier.height(18.dp))
                TitleRow("03. 급여와 조건을 알려주세요!")
            }

            /* 급여정보 */
            SectionCard {
                LabelText("급여정보")
                Spacer(Modifier.height(6.dp))

                SubLabel("급여 금액")
                Spacer(Modifier.height(8.dp))

                // 급여 유형 2x2 (피그마 문구 그대로)
                TwoByTwo(
                    {
                        PayCard(
                            title = "연봉",
                            sub = "세전 기준",
                            selected = payType == PayType.Yearly,
                            onClick = {
                                payType = PayType.Yearly
                                selectedPayType = "연봉"
                            }
                        )
                    },
                    {
                        PayCard(
                            title = "월급",
                            sub = "세전 기준",
                            selected = payType == PayType.Monthly,
                            onClick = {
                                payType = PayType.Monthly
                                selectedPayType = "월급"
                            }
                        )
                    },
                    {
                        PayCard(
                            title = "일급",
                            sub = "하루 기준",
                            selected = payType == PayType.Daily,
                            onClick = {
                                payType = PayType.Daily
                                selectedPayType = "일급"
                            }
                        )
                    },
                    {
                        PayCard(
                            title = "시급",
                            sub = "시간 기준",
                            selected = payType == PayType.Hourly,
                            onClick = {
                                payType = PayType.Hourly
                                selectedPayType = "시급"
                            }
                        )
                    }
                )

                Spacer(Modifier.height(14.dp))
                TitleSubLabel("급여 금액")
                Spacer(Modifier.height(8.dp))
                UnderlineField(
                    value = payAmount,
                    onValueChange = { payAmount = it },
                    placeholder = "내용입력"
                )
            }

            SectionSpacer()

            /* 복리혜택 */
            SectionCard {
                LabelText("복리혜택")
                Spacer(Modifier.height(6.dp))
                BenefitGrid(
                    items = benefits,
                    onToggle = { idx ->
                        val cur = benefits[idx]
                        benefits[idx] = cur.copy(selected = !cur.selected)
                    }
                )
            }
            SectionSpacer()

            /* 성별 */
            SectionCard {
                LabelText("성별")
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GRID_GAP)
                ) {
                    GenderCard("성별무관", gender == Gender.Any, { gender = Gender.Any }, modifier = Modifier.weight(1f))
                    GenderCard("남자", gender == Gender.Male, { gender = Gender.Male }, modifier = Modifier.weight(1f))
                    GenderCard("여자", gender == Gender.Female, { gender = Gender.Female }, modifier = Modifier.weight(1f))
                }
            }
            SectionSpacer()

            /* 경력 요구사항 (피그마 문구) */
            SectionCard {
                LabelText("경력 요구사항")
                Spacer(Modifier.height(6.dp))
                TwoByTwo(
                    { ExpCard("경력 무관", exp == Experience.None) { exp = Experience.None } },
                    { ExpCard("1년 이상", exp == Experience.Y1) { exp = Experience.Y1 } },
                    { ExpCard("5년 이상", exp == Experience.Y5) { exp = Experience.Y5 } },
                    { ExpCard("10년 이상", exp == Experience.Y10) { exp = Experience.Y10 } },
                )
            }
            SectionSpacer()

            /* 관련 자격증 */
            SectionCard {
                LabelText("관련 자격증")
                Spacer(Modifier.height(6.dp))
                BlueOutlinedField(
                    value = licenseText,
                    placeholder = "예 : 컴퓨터 활용, 영어회화, 운전가능",
                    onChange = { licenseText = it }
                )
                Spacer(Modifier.height(10.dp))
                PrimaryButton(text = "추가") {
                    // 필요시 리스트에 accumulate / 저장 처리
                }
            }
            SectionSpacer()

            /* 기타 우대사항 */
            SectionCard {
                LabelText("기타 우대사항")
                Spacer(Modifier.height(6.dp))
                BlueOutlinedField(
                    value = preferText,
                    placeholder = "",
                    onChange = { preferText = it }
                )
                Spacer(Modifier.height(10.dp))
                PrimaryButton(text = "추가") {
                    // 필요시 accumulate / 저장 처리
                }
            }
            SectionSpacer()

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
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .width(88.dp)
                            .height(BOTTOM_BTN_HEIGHT),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Blue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) { Text("이전", fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp, color = Blue) }

                    Button(
                        onClick = {
                            scope.launch {
                                loading = true
                                runCatching {
                                    val benefitBits = benefitsToBitString(benefits)
                                    val amountNum = payAmount.filter { it.isDigit() }.toLongOrNull() ?: 0L
                                    val companyid = CurrentUser.companyid
                                    val announceid = getannouncebycom(companyid)?: error("해당 회사의 공고가 없습니다.")
                                    val announce =announceid.id
                                    val salary = SalaryDto(
                                        id = announce,
                                        salary_type = selectedPayType,
                                        salary_amount = amountNum,
                                        benefit = benefitBits,
                                        career = experienceToCode(exp),
                                        gender = genderToCode(gender)
                                    )
                                    salaryRepo.insertSalary(salary)

                                    // 아래 두 개는 스키마에 맞게 필요 시 값 세팅
                                    prefRepo.insertPreferential(PreferentialDto(announce,preferential_treatment = preferText))
                                    needRepo.insertNeedlisence(NeedlicenseDto(announce, need1 = licenseText))
                                }.onSuccess { onNext() }
                                loading = false
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(BOTTOM_BTN_HEIGHT),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) { Text("다음 단계", fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp) }
                }
            }

            BottomNavPlaceholder()
        }
    }
}

/* ====== TabBar (03 선택) – /01, /02 스타일 ====== */
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
                        letterSpacing = (-0.5).sp,
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
        letterSpacing = (-0.3).sp
    )
}

@Composable
private fun LabelText(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        letterSpacing = (-0.3).sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 6.dp)
    )
}

@Composable
private fun SubLabel(text: String) {
    Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
}

/* 라인 인풋 (회색 보더/밑줄 느낌) */
@Composable
private fun LineTextField(
    value: String,
    placeholder: String,
    onChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .border(1.dp, Blue, shape)
            .clip(shape)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
            singleLine = true,
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text("내용입력", fontSize = 13.sp, color = TextGray)
                }
                inner()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun Double.em() = (this * 16).sp

@Composable
private fun UnderlineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 15.sp,
                color = if (value.isEmpty()) TextGray else Color.Black,
                letterSpacing = (-0.019).em()
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            decorationBox = { inner ->
                Box(
                    modifier = Modifier.heightIn(min = 23.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            fontSize = 15.sp,
                            color = TextGray,
                            letterSpacing = (-0.019).em()
                        )
                    }
                    inner()
                }
            }
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFC0C0C0))
        )
    }
}

@Composable
private fun TitleSubLabel(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,                    // ✅ 18sp
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        letterSpacing = (-0.019).em()
    )
}

/* 파란 보더 인풋 (자격/우대) */
@Composable
private fun BlueOutlinedField(
    value: String,
    placeholder: String,
    onChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .border(1.dp, Blue, shape)
            .clip(shape)
            .background(Color.White)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
            singleLine = true,
            decorationBox = { inner ->
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(placeholder, fontSize = 13.sp, color = TextGray)
                }
                inner()
            },
            modifier = Modifier.weight(1f)
        )
    }
}

/* 2x2 그리드 */
@Composable
private fun TwoByTwo(
    a: @Composable () -> Unit,
    b: @Composable () -> Unit,
    c: @Composable () -> Unit,
    d: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(GRID_GAP)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GRID_GAP)) {
            Box(Modifier.weight(1f)) { a() }
            Box(Modifier.weight(1f)) { b() }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GRID_GAP)) {
            Box(Modifier.weight(1f)) { c() }
            Box(Modifier.weight(1f)) { d() }
        }
    }
}

/* 급여 카드 – 피그마 타이틀/서브텍스트 */
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
            .border(1.dp, if (selected) Blue else BorderGray, shape)
            .background(Color.White, shape)
            .clickable { onClick() }
            .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp), // ← 왼쪽 패딩 20dp
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Blue else Color(0xFF616161)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            sub,
            fontSize = 12.sp,
            color = TextGray
        )
    }
}

/* 성별 카드 */
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
            .border(1.dp, if (selected) Blue else BorderGray, shape)
            .background(Color.White, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Blue else Color(0xFF616161)
        )
    }
}

/* 경력 카드 */
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
            .border(1.dp, if (selected) Blue else BorderGray, shape)
            .background(Color.White, shape)
            .clickable { onClick() }
            .padding(start = 20.dp, end = 12.dp), // ← 동일하게 적용
        contentAlignment = Alignment.CenterStart // 가운데가 아니라 왼쪽 정렬!
    ) {
        Text(
            text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Blue else Color(0xFF616161)
        )
    }
}


/* 복리혜택 데이터/그리드 (피그마 문구) */
private data class BenefitItem(
    val title: String,
    val sub: String,
    val selected: Boolean = false
)

private fun defaultBenefits() = listOf(
    BenefitItem("식대 지원", "점심 식사 제공"),
    BenefitItem("교통비 지원", "대중교통비 지급"),
    BenefitItem("4대 보험", "국민연금, 건강보험"),
    BenefitItem("교육지원", "업무 관련 교육")
)

@Composable
private fun BenefitGrid(
    items: List<BenefitItem>,
    onToggle: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(GRID_GAP)) {
        items.chunked(2).forEachIndexed { rowIdx, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GRID_GAP)
            ) {
                row.forEachIndexed { colIdx, item ->
                    val idx = rowIdx * 2 + colIdx
                    BenefitCard(item = item, onClick = { onToggle(idx) }, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
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
private fun BenefitCard(
    item: BenefitItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(CARD_CORNER)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            .clip(shape)
            .border(1.dp, if (item.selected) Blue else BorderGray, shape)
            .background(Color.White, shape)
            .clickable { onClick() }
            .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp), // ← 동일하게 적용
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            item.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (item.selected) Blue else Color(0xFF616161)
        )
        Spacer(Modifier.height(2.dp))
        Text(item.sub, fontSize = 12.sp, color = TextGray)
    }
}

/* 하단 플레이스홀더 */
@Composable
private fun BottomNavPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .background(Color(0xFFF4F5F7))
    )
}

/* 포맷/코드 매핑 */
private enum class PayType { Hourly, Daily, Monthly, Yearly }
private enum class Gender { Male, Female, Any }
private enum class Experience { None, Y1, Y5, Y10 }

private fun benefitsToBitString(list: List<BenefitItem>): String {
    // 선택 여부만 필요하면 간단히 1/0 로 변환(순서 고정)
    return list.joinToString("") { if (it.selected) "1" else "0" }
}

private fun genderToCode(g: Gender): String = when (g) {
    Gender.Male -> "male"
    Gender.Female -> "female"
    Gender.Any -> "all"
}

private fun experienceToCode(e: Experience): String = when (e) {
    Experience.None -> "NONE"
    Experience.Y1 -> "Y1_PLUS"
    Experience.Y5 -> "Y5_PLUS"
    Experience.Y10 -> "Y10_PLUS"
}

/* 기본 파란 버튼 – /01/02 동일 */
@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(BOTTOM_BTN_HEIGHT),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Blue,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFE0E6EE),
            disabledContentColor = Color(0xFF98A2B3)
        )
    ) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp)
    }
}
