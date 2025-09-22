package com.example.dodojob.ui.feature.welfare

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.*
import androidx.navigation.compose.rememberNavController
import com.example.dodojob.R
import com.example.dodojob.ui.feature.profile.BottomNavBar

/* =========================================================
 * 공통 팔레트/토큰
 * ========================================================= */
private val ScreenBg   = Color(0xFFF1F5F7)
private val CardBg     = Color(0xFFFFFFFF)
private val SubGray    = Color(0xFF848484)
private val TitleBlack = Color(0xFF000000)

private val BrandBlue  = Color(0xFF005FFF)
private val BorderGray = Color(0xFFC1D2ED)
private val TextGray   = Color(0xFF9C9C9C)

/* 라벨 칩 (기관) 색상 */
private val PillPinkBG = Color(0xFFFFEBE6)
private val PillPinkFG = Color(0xFFFF2F00)
private val PillBlueBG = Color(0xFFDEEBFF)
private val PillBlueFG = Color(0xFF005FFF)
private val PillYelBG  = Color(0xFFFFFDD8)
private val PillYelFG  = Color(0xFFFF9900)
private val PillPurpBG = Color(0xFFF3E8FF)
private val PillPurpFG = Color(0xFF7169D8)

/* =========================================================
 * 홈 화면용 데이터/필터
 * ========================================================= */
private enum class StatusKind { REVIEW, APPROVED, PENDING }
private enum class Filter { ALL, PENDING, APPROVED }

private data class WelfareItem(
    val date: String,
    val title: String,
    val status: StatusKind,
    val progress: Int
)

/* =========================================================
 * 카테고리 화면(건강관리/여가교육) 모델
 * ========================================================= */
enum class CategoryTab { Health, Leisure }

data class AgencyLabel(val text: String, val bg: Color, val fg: Color)
data class WelfareInfo(
    val supportType: String,
    val title: String,
    val agencyLabel: AgencyLabel,
    val description: String
)

/* =========================================================
 * Route constants
 * ========================================================= */
object WelfareRoutes {
    // 홈
    const val Home = "welfare/home"

    // 카테고리 화면: welfare/category/{tab}, tab in ["health","leisure"]
    private const val CategoryBase = "welfare/category"
    const val ArgTab = "tab"
    const val Category = "$CategoryBase/{$ArgTab}"

    fun categoryOf(tab: CategoryTab): String =
        "$CategoryBase/${if (tab == CategoryTab.Health) "health" else "leisure"}"

    fun parseTab(raw: String?): CategoryTab =
        if (raw.equals("leisure", ignoreCase = true)) CategoryTab.Leisure else CategoryTab.Health
}

/* =========================================================
 * Welfare Home Route
 * ========================================================= */
@Composable
fun WelfareHomeRoute(
    nav: NavController,
    userName: String = "홍길동"
) {
    WelfareHomeScreen(
        userName = userName,
        onBottomClick = { key ->
            when (key) {
                "home"      -> nav.navigate("main") { launchSingleTop = true }
                "edu"       -> nav.navigate("edu") { launchSingleTop = true }
                "welfare"   -> {} // 현재 화면
                "community" -> nav.navigate("community") { launchSingleTop = true }
                "my"        -> nav.navigate("my") { launchSingleTop = true }
            }
        },
        bottomBar = { BottomNavBar(current = "welfare", onClick = { /* 라벨 처리 */ }) },
        onCardClick = { /* TODO: 상세 이동 */ },
        onClickHealth = { nav.navigate(WelfareRoutes.categoryOf(CategoryTab.Health)) },
        onClickLeisure = { nav.navigate(WelfareRoutes.categoryOf(CategoryTab.Leisure)) }
    )
}

/* =========================================================
 * Welfare Home Screen
 * ========================================================= */
@Composable
fun WelfareHomeScreen(
    userName: String,
    onBottomClick: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
    onCardClick: () -> Unit,
    onClickHealth: () -> Unit,
    onClickLeisure: () -> Unit
) {
    // 샘플 데이터 (서버 연동 시 치환)
    val allItems = remember {
        listOf(
            WelfareItem("2025.08.25 지원", "건강검진 지원 프로그램", StatusKind.REVIEW,    75),
            WelfareItem("2025.08.25 지원", "온라인 취미 강좌 (수강권 지원)", StatusKind.APPROVED, 100),
            WelfareItem("2025.08.25 지원", "돌봄 서비스 (가정 방문 지원)", StatusKind.PENDING,    5)
        )
    }
    var selectedFilter by remember { mutableStateOf(Filter.ALL) }

    val filteredItems = remember(selectedFilter, allItems) {
        when (selectedFilter) {
            Filter.ALL      -> allItems
            Filter.PENDING  -> allItems.filter { it.status == StatusKind.PENDING }
            Filter.APPROVED -> allItems.filter { it.status == StatusKind.APPROVED }
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = bottomBar,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "앱 로고",
                    modifier = Modifier.size(29.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { /* 알림센터 이동 */ }) {
                    Image(
                        painter = painterResource(id = R.drawable.bell),
                        contentDescription = "알림",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            GreetingHeader(userName)

            Spacer(Modifier.height(12.dp))

            SearchBox(
                placeholder = "원하는 복지를 검색해보세요",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ✅ 여기서 각 버튼 클릭 시 카테고리 라우트로 이동
            CategoryButtons(
                leftImage = R.drawable.health_manage_button,
                rightImage = R.drawable.leisure_education_button,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                onLeftClick = onClickHealth,
                onRightClick = onClickLeisure
            )

            Spacer(Modifier.height(20.dp))

            ApplicationSummary(
                total = allItems.size,
                selected = selectedFilter,
                onSelect = { selectedFilter = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(10.dp))
            filteredItems.forEach { item ->
                WelfareCard(
                    date = item.date,
                    title = item.title,
                    statusText = when (item.status) {
                        StatusKind.REVIEW   -> "심사중"
                        StatusKind.APPROVED -> "승인"
                        StatusKind.PENDING  -> "접수대기"
                    },
                    statusKind = item.status,
                    progressPercent = item.progress,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 10.dp),
                    onClick = onCardClick
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* =========================================================
 * 공통 UI 조각 - 홈
 * ========================================================= */
@Composable
private fun GreetingHeader(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${userName}님\n어떤 복지를 찾으세요?",
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            lineHeight = 45.sp
        )
    }
}

@Composable
private fun SearchBox(
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var value by remember { mutableStateOf(TextFieldValue("")) }

    Box(
        modifier = modifier
            .height(57.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "검색",
                tint = Color(0xFF62626D),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            if (value.text.isEmpty()) {
                Text(text = placeholder, color = Color(0xFF9C9C9C), fontSize = 16.sp)
            }
        }
        BasicTextField(
            value = value,
            onValueChange = { value = it },
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
            modifier = Modifier
                .matchParentSize()
                .padding(start = 48.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
        )
    }
}

@Composable
private fun CategoryButtons(
    @DrawableRes leftImage: Int,
    @DrawableRes rightImage: Int,
    modifier: Modifier = Modifier,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryImageButton(resId = leftImage, onClick = onLeftClick, modifier = Modifier.weight(1f))
        CategoryImageButton(resId = rightImage, onClick = onRightClick, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CategoryImageButton(
    @DrawableRes resId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(67.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ApplicationSummary(
    total: Int,
    selected: Filter,
    onSelect: (Filter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                append("총 ")
                withStyle(SpanStyle(color = BrandBlue, fontWeight = FontWeight.SemiBold)) {
                    append("${total}건")
                }
                append(" 신청")
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChipPill("전체", selected == Filter.ALL) { onSelect(Filter.ALL) }
            FilterChipPill("접수대기", selected == Filter.PENDING) { onSelect(Filter.PENDING) }
            FilterChipPill("승인", selected == Filter.APPROVED) { onSelect(Filter.APPROVED) }
        }
    }
}

@Composable
private fun FilterChipPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) BrandBlue else Color.White
    val fg = if (selected) Color.White else Color.Black
    val border = if (selected) null else BorderStroke(1.dp, Color(0xFFD1D1D1))

    Surface(
        modifier = Modifier.height(34.dp),
        shape = RoundedCornerShape(24.dp),
        color = bg,
        shadowElevation = 0.dp,
        border = border,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = fg,
                fontSize = 18.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WelfareCard(
    date: String,
    title: String,
    statusText: String,
    statusKind: StatusKind,
    progressPercent: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 27.dp, vertical = 20.dp)) {
            Text(text = date, fontSize = 13.sp, color = Color(0xFF848484))
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StatusBadge(text = statusText, kind = statusKind)
            }
            Spacer(Modifier.height(14.dp))
            ProgressLine(percent = progressPercent)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "진행률 (${progressPercent}%)",
                fontSize = 18.sp,
                color = TextGray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }
    }
}

@Composable
private fun StatusBadge(text: String, kind: StatusKind) {
    val (bg, fg) = when (kind) {
        StatusKind.REVIEW   -> Color(0xFFFFEBE6) to Color(0xFFFF2F00)
        StatusKind.APPROVED -> Color(0xFFDEFFE1) to Color(0xFF1E7428)
        StatusKind.PENDING  -> Color(0xFFEFEFEF) to Color(0xFFA6A6A6)
    }
    Box(
        modifier = Modifier
            .width(85.dp)
            .clip(RoundedCornerShape(31.dp))
            .background(bg)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = fg, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
private fun ProgressLine(percent: Int) {
    val p = percent.coerceIn(0, 100) / 100f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFFEEF2F4))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(p)
                .background(BrandBlue)
        )
    }
}

/* =========================================================
 * 카테고리 화면 (건강관리/여가교육)
 * ========================================================= */
@Composable
private fun BackButtonBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier, // 배경은 부모 Surface가 흰색으로 통일
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Image(
                painter = painterResource(R.drawable.back),
                contentDescription = "뒤로가기",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/** NavRoute:  welfare/category/{tab} → 화면 진입용 */
@Composable
fun HealthLeisureRoute(nav: NavController, startTabArg: String?) {
    val start = remember(startTabArg) { WelfareRoutes.parseTab(startTabArg) }
    HealthLeisureScreen(
        startTab = start,
        onBack = { nav.popBackStack() }
    )
}

@Composable
fun HealthLeisureScreen(
    startTab: CategoryTab,
    onBack: () -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(startTab) }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {}
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Surface(color = Color.White) {
                Column {
                    BackButtonBar(
                        onBack = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                    )
                    CategoryToggleHeader(
                        selected = tab,
                        onLeftClick  = { tab = CategoryTab.Health },
                        onRightClick = { tab = CategoryTab.Leisure },
                    )
                }
            }

            val items = if (tab == CategoryTab.Health) healthItems() else leisureItems()

            items.forEachIndexed { i, item ->
                WelfareInfoCard(item)
                if (i != items.lastIndex) Spacer(Modifier.height(4.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* 상단 토글 */
@Composable
private fun CategoryToggleHeader(
    selected: CategoryTab,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryImageButton(
            resId = if (selected == CategoryTab.Health)
                R.drawable.health_manage_button
            else
                R.drawable.unselected_health_button,
            onClick = onLeftClick,
            modifier = Modifier.weight(1f).height(67.dp)
        )
        CategoryImageButton(
            resId = if (selected == CategoryTab.Leisure)
                R.drawable.leisure_education_button
            else
                R.drawable.unselected_leisure_button,
            onClick = onRightClick,
            modifier = Modifier.weight(1f).height(67.dp)
        )
    }
}

/* 카드 */
@Composable
private fun WelfareInfoCard(data: WelfareInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(151.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(27.dp),
        ) {
            Text(
                text = data.supportType,
                color = SubGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                maxLines = 1
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.title,
                    color = TitleBlack,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 30.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                AgencyPill(data.agencyLabel)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = data.description,
                color = SubGray,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 27.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AgencyPill(label: AgencyLabel) {
    Box(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(31.dp))
            .background(label.bg)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.text,
            color = label.fg,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/* 더미 데이터 */
private fun healthItems() = listOf(
    WelfareInfo(
        "지원유형 : 서비스(의료), 의료지원",
        "인플루엔자 국가예방접종 지원",
        AgencyLabel("질병관리청", PillPinkBG, PillPinkFG),
        "인플루엔자 접종 대상자(어린이, 임신부, 어르신)의 예방접종률 향상과 질병 부담 감소"
    ),
    WelfareInfo(
        "지원유형 : 서비스(의료), 의료지원",
        "무료 건강검진 지원 안내",
        AgencyLabel("질병관리청", PillPinkBG, PillPinkFG),
        "생활에 꼭 필요한 기본 의약품을 정기적으로 지원합니다"
    ),
    WelfareInfo(
        "지원유형 : 서비스(의료), 의료지원",
        "노인성 질환 의료비 지원",
        AgencyLabel("질병관리청", PillPinkBG, PillPinkFG),
        "노인성 질환을 앓고 있는 사람은 경제적 부담 능력에 따라 국가 또는 지방자치단체로부터 노인성 질환의 예방교육,"
    ),
    WelfareInfo(
        "지원유형 : 서비스(의료), 의료지원",
        "치매 조기검진 지원",
        AgencyLabel("질병관리청", PillPinkBG, PillPinkFG),
        "치매를 빠르게 발견하고 맞춤형 상담을 받을 수 있습니다."
    ),
    WelfareInfo(
        "지원유형 : 서비스(의료), 의료지원",
        "재활·물리치료 지원",
        AgencyLabel("질병관리청", PillPinkBG, PillPinkFG),
        "노약자, 수술 후 회복 대상자"
    )
)

private fun leisureItems() = listOf(
    WelfareInfo(
        "11월 개강, 4주 과정",
        "수채화 기초 강좌 지원",
        AgencyLabel("문화체육관광", PillBlueBG, PillBlueFG),
        "온라인 강의 + 기초 교재 PDF 제공"
    ),
    WelfareInfo(
        "무료 온라인 강의 + 교재 제공",
        "평생직업 교육 강좌 (컴퓨터 기초반)",
        AgencyLabel("고용노동부", PillYelBG, PillYelFG),
        "한글 문서 작성부터 인터넷 활용까지 차근차근 배웁니다."
    ),
    WelfareInfo(
        "창업 교육 + 초기 자금 일부 지원",
        "중장년 창업 지원 프로그램",
        AgencyLabel("고용노동부", PillYelBG, PillYelFG),
        "창업 교육 + 초기 자금 일부 지원으로 은퇴 후 제2의 삶을 위한 창업 준비를 돕습니다."
    ),
    WelfareInfo(
        "사회활동 지원사업",
        "현장실습훈련(시니어인턴십) 지원사업",
        AgencyLabel("보건복지부", PillPurpBG, PillPurpFG),
        "2025년 현장실습훈련(시니어인턴십) 지원사업 운영안내"
    ),
    WelfareInfo(
        "지원유형 : 서비스(의료), 의료지원",
        "낙상 예방 운동 프로그램",
        AgencyLabel("질병관리청", PillPinkBG, PillPinkFG),
        "넘어짐을 예방하는 근력·균형 운동을 배워 안전한 생활을 돕습니다."
    )
)

/* =========================================================
 * Preview
 * ========================================================= */
@Preview(showBackground = true, showSystemUi = true, name = "Welfare Home")
@Composable
private fun PreviewWelfareHome() {
    val nav = rememberNavController()
    WelfareHomeRoute(nav = nav, userName = "홍길동")
}

@Preview(showBackground = true, showSystemUi = true, name = "Category - Health Start")
@Composable
private fun PreviewHealthStart() {
    HealthLeisureScreen(startTab = CategoryTab.Health, onBack = {})
}

@Preview(showBackground = true, showSystemUi = true, name = "Category - Leisure Start")
@Composable
private fun PreviewLeisureStart() {
    HealthLeisureScreen(startTab = CategoryTab.Leisure, onBack = {})
}
