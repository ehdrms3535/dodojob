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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dodojob.R
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.data.welfare.*
import com.example.dodojob.session.CurrentUser
import com.example.dodojob.ui.feature.profile.BottomNavBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.dodojob.dao.getSeniorInformation

/* =========================================================
 * 팔레트/토큰
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
 * 카테고리 모델
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
 * Route
 * ========================================================= */
object WelfareRoutes {
    const val Home = "welfare/home"
    private const val CategoryBase = "welfare/category"
    const val ArgTab = "tab"
    const val Category = "$CategoryBase/{$ArgTab}"

    fun categoryOf(tab: CategoryTab): String =
        "$CategoryBase/${if (tab == CategoryTab.Health) "health" else "leisure"}"

    fun parseTab(raw: String?): CategoryTab =
        if (raw.equals("leisure", ignoreCase = true)) CategoryTab.Leisure else CategoryTab.Health
}

/* =========================================================
 * ViewModel + UiState
 * ========================================================= */
private data class WelfareHomeUi(
    val applications: List<WelfareItem> = emptyList(),
    val loading: Boolean = false
)

private data class WelfareCategoryUi(
    val health: List<WelfareInfo> = emptyList(),
    val leisure: List<WelfareInfo> = emptyList(),
    val loading: Boolean = false
)

private class WelfareVm(private val repo: WelfareRepository) : ViewModel() {

    private val _home = MutableStateFlow(WelfareHomeUi())
    val home: StateFlow<WelfareHomeUi> = _home

    private val _category = MutableStateFlow(WelfareCategoryUi())
    val category: StateFlow<WelfareCategoryUi> = _category

    fun loadHome(username: String) = viewModelScope.launch {
        _home.value = _home.value.copy(loading = true)

        val rows = repo.myApplicationsJoined(username)

        val items = rows.map { row ->
            val date = (row.createdAt ?: "").take(10).replace("-", ".").let {
                if (it.isNotBlank()) "$it 지원" else "지원"
            }
            val status = (row.status ?: WelfareStatus.REVIEW)
            val title  = row.welfare?.title ?: "복지 신청 #${row.welfareId}"

            WelfareItem(
                date = date,
                title = title,
                status = status.toStatusKind(),
                progress = status.progressPercent()
            )
        }
        _home.value = WelfareHomeUi(applications = items, loading = false)
    }

    fun loadCategories(query: String? = null) = viewModelScope.launch {
        _category.value = _category.value.copy(loading = true)

        val health = repo.list(category = WelfareCategory.HEALTH, query = query).map { it.toWelfareInfo() }
        val leisure = repo.list(category = WelfareCategory.LEISURE, query = query).map { it.toWelfareInfo() }

        _category.value = WelfareCategoryUi(health = health, leisure = leisure, loading = false)
    }
}

private class WelfareVmFactory(
    private val repo: WelfareRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = WelfareVm(repo) as T
}

/* =========================================================
 * 매핑 Helper
 * ========================================================= */
private fun WelfareStatus.toStatusKind(): StatusKind = when (this) {
    WelfareStatus.REVIEW   -> StatusKind.REVIEW
    WelfareStatus.APPROVED -> StatusKind.APPROVED
    WelfareStatus.PENDING  -> StatusKind.PENDING
}

private fun WelfareStatus.progressPercent(): Int = when (this) {
    WelfareStatus.PENDING  -> 5
    WelfareStatus.REVIEW   -> 60
    WelfareStatus.APPROVED -> 100
}

private fun Welfare.toWelfareInfo(): WelfareInfo {
    val (bg, fg) = when (category) {
        WelfareCategory.HEALTH  -> PillPinkBG to PillPinkFG
        WelfareCategory.LEISURE -> PillBlueBG to PillBlueFG
        null -> PillYelBG to PillYelFG
    }
    return WelfareInfo(
        supportType = supportType ?: "지원유형 정보 없음",
        title = title,
        agencyLabel = AgencyLabel(agency ?: "기관 미상", bg, fg),
        description = description ?: "상세 설명이 없습니다."
    )
}

/* =========================================================
 * 홈 Route
 * ========================================================= */
@Composable
fun WelfareHomeRoute(
    nav: NavController,
    userName: String
) {
    val client = LocalSupabase.current
    val repo = remember(client) { WelfareRepositoryImpl(client) }
    val vm: WelfareVm = androidx.lifecycle.viewmodel.compose.viewModel(factory = WelfareVmFactory(repo))
    val homeUi by vm.home.collectAsState()

    val username = remember { CurrentUser.username ?: "guest" }

    var displayName by remember { mutableStateOf(userName) }

    LaunchedEffect(username) {
        vm.loadHome(username)
        vm.loadCategories()


        runCatching {
            getSeniorInformation(username)
        }.onSuccess { info ->
            val name = info?.user?.name
            if (!name.isNullOrBlank()) {
                displayName = name
            } else {
                displayName = username
            }
        }.onFailure {
            displayName = username
        }
    }

    WelfareHomeScreen(
        userName = displayName,
        onBottomClick = { key ->
            when (key) {
                "home"      -> nav.navigate("main") { launchSingleTop = true }
                "edu"       -> nav.navigate("edu") { launchSingleTop = true }
                "welfare"   -> {}
                "community" -> nav.navigate("community") { launchSingleTop = true }
                "my"        -> nav.navigate("my") { launchSingleTop = true }
            }
        },
        bottomBar = { BottomNavBar(current = "welfare", onClick = { }) },
        onCardClick = { /* 상세 이동 */ },
        onClickHealth = { nav.navigate(WelfareRoutes.categoryOf(CategoryTab.Health)) },
        onClickLeisure = { nav.navigate(WelfareRoutes.categoryOf(CategoryTab.Leisure)) },
        applications = homeUi.applications,
        loading = homeUi.loading
    )
}


/* =========================================================
 * 홈 Screen
 * ========================================================= */
@Composable
private fun WelfareHomeScreen(
    userName: String,
    onBottomClick: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
    onCardClick: () -> Unit,
    onClickHealth: () -> Unit,
    onClickLeisure: () -> Unit,
    applications: List<WelfareItem>,
    loading: Boolean
) {
    var selectedFilter by remember { mutableStateOf(Filter.ALL) }

    val filteredItems = remember(selectedFilter, applications) {
        when (selectedFilter) {
            Filter.ALL      -> applications
            Filter.PENDING  -> applications.filter { it.status == StatusKind.PENDING }
            Filter.APPROVED -> applications.filter { it.status == StatusKind.APPROVED }
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
                total = applications.size,
                selected = selectedFilter,
                onSelect = { selectedFilter = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(10.dp))

            when {
                loading -> {
                    Box(
                        Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
                filteredItems.isEmpty() -> {
                    Box(
                        Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("신청 내역이 없습니다.") }
                }
                else -> {
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
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* =========================================================
 * 공통 UI
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
 * 카테고리 Route (실데이터)
 * ========================================================= */
@Composable
fun HealthLeisureRoute(nav: NavController, startTabArg: String?) {
    val client = LocalSupabase.current
    val repo = remember(client) { WelfareRepositoryImpl(client) }
    val vm: WelfareVm = androidx.lifecycle.viewmodel.compose.viewModel(factory = WelfareVmFactory(repo))
    val ui by vm.category.collectAsState()

    val start = remember(startTabArg) { WelfareRoutes.parseTab(startTabArg) }
    LaunchedEffect(Unit) { vm.loadCategories() }

    HealthLeisureScreenWithData(
        startTab = start,
        ui = ui,
        onBack = { nav.popBackStack() }
    )
}

@Composable
private fun HealthLeisureScreenWithData(
    startTab: CategoryTab,
    ui: WelfareCategoryUi,
    onBack: () -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(startTab) }

    Scaffold(containerColor = ScreenBg) { padding ->
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

            val items = when (tab) {
                CategoryTab.Health  -> ui.health
                CategoryTab.Leisure -> ui.leisure
            }

            when {
                ui.loading -> {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                items.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("검색 결과가 없습니다.")
                    }
                }
                else -> {
                    items.forEachIndexed { i, item ->
                        WelfareInfoCard(item)
                        if (i != items.lastIndex) Spacer(Modifier.height(4.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* 상단 토글/카드/공통 */
@Composable
private fun BackButtonBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Image(
                painter = painterResource(R.drawable.back),
                contentDescription = "뒤로가기",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

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
            resId = if (selected == CategoryTab.Health) R.drawable.health_manage_button else R.drawable.unselected_health_button,
            onClick = onLeftClick,
            modifier = Modifier.weight(1f).height(67.dp)
        )
        CategoryImageButton(
            resId = if (selected == CategoryTab.Leisure) R.drawable.leisure_education_button else R.drawable.unselected_leisure_button,
            onClick = onRightClick,
            modifier = Modifier.weight(1f).height(67.dp)
        )
    }
}

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

/* =========================================================
 * Preview (미리보기는 더미/로딩만 표시)
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
    HealthLeisureScreenWithData(
        startTab = CategoryTab.Health,
        ui = WelfareCategoryUi(loading = true),
        onBack = {}
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "Category - Leisure Start")
@Composable
private fun PreviewLeisureStart() {
    HealthLeisureScreenWithData(
        startTab = CategoryTab.Leisure,
        ui = WelfareCategoryUi(loading = false, leisure = listOf(
            WelfareInfo("지원유형", "예시 타이틀", AgencyLabel("기관", PillBlueBG, PillBlueFG), "설명")
        )),
        onBack = {}
    )
}
