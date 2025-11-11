package com.example.dodojob.ui.feature.main

import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.dao.fetchDisplayNameByUsername
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.navigation.Route
import com.example.dodojob.session.CurrentUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewModelScope
import com.example.dodojob.data.recommend.RecoJob
import com.example.dodojob.data.recommend.fetchRecommendedJobs
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dodojob.dao.fetchCompanyImagesMap
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import com.example.dodojob.data.recommend.fetchAiRecommendedJobs
import com.example.dodojob.ui.components.AppBottomBar
import android.content.Context
import android.util.Log
import com.example.dodojob.data.recentwatch.RecentWatchSupabase
/* ===================== 데이터 모델 ===================== */

data class JobCardUi(
    val id: Long,
    val org: String,
    val condition: String,
    val desc: String,
    val dday: String,
    val imageUrl: String? = null
)

fun RecoJob.toJobCardUi(): JobCardUi = JobCardUi(
    id = this.id,
    org = this.company_name ?: "회사명 없음", //회사명
    condition =  "없음", // 필수 경력
    desc = this.major ?: "-", // 주요일
    dday = "없음", // 남은일
    imageUrl = "https://bswcjushfcwsxswufejm.supabase.co/storage/v1/object/public/company_images/workplace/2345/1759684464991_1daad090-6d3c-4ab7-a3d3-89eb01898561.jpg"
)

data class JobSummary(
    val id: Long,
    val org: String,
    val tag: String,
    val title: String,
    val desc: String, // 상단 카드에서는 미표시(보관용)
    val dday: String,// 남으닝ㄹ
)

data class JobDetail(
    val id: String,
    val org: String,
    val condition: String,
    val desc: String,
    val dday: String
)

data class AdBanner(
    val id: String,
    val bg: Color,
    val titleTop: String,
    val titleBottom: String,
    val imageRes: Int
)

data class MainUiState(
    val greetingName: String = "홍길동",
    val searchText: String = "",
    val aiJobs: List<JobSummary> = emptyList(),
    val banners: List<AdBanner> = emptyList(),
    val tailoredJobs: List<JobCardUi> = emptyList()
)

/* ===================== Fake Repository ===================== */

// "D-12 | 경력,신입" -> ("D-12", " | 경력,신입")
private fun splitDdayParts(dday: String): Pair<String, String> {
    val idx = dday.indexOf("D-")
    if (idx == -1) return dday to ""
    val digits = dday.drop(idx + 2).takeWhile { it.isDigit() }
    if (digits.isEmpty()) return dday to ""
    val dPart = "D-$digits"
    val rest = dday.drop(idx + 2 + digits.length) // 예: " | 경력,신입"
    return dPart to rest
}

object MainFakeRepository {

    fun loadBanners(): List<AdBanner> = listOf(
        AdBanner("b1", Color(0xFFFF8C00), "두 번째 커리어, 이제 시작해볼까요?", "교육 ~ 일자리까지\n원스톱 케어", R.drawable.first_banner),
        AdBanner("b2", Color(0xFFFFEA00), "교육 신청 시 사은품 제공", "스마트폰 배우고,\n일자리로 연결!",R.drawable.second_banner),
        AdBanner("b3", Color(0xFF505050), "신청만 해도 교육 + 사은품 제공", "병원 안내·행정 보조,\n경력 살려 재취업!",R.drawable.third_banner)
    )

    fun loadTailored(): List<JobDetail> = listOf(
        JobDetail("t1","수성시니어클럽","| 영양사 자격증, 경력","초등학교 급식 도우미로 급식 준비 및 배식 보조","D-8"),
        JobDetail("t2","대구광역시 평생교육진흥원","| 강사 경력","시니어 대상 평생교육 강좌 운영, 프로그램 기획","D-13"),
        JobDetail("t3","칠성시장 상인회","| 경영·회계 경력","상점 매출 관리, 회계 보조, 상인 교육 지원","D-15"),
        JobDetail("t4","달서구 어린이집","| 보육도우미 경험","아이들 하원 지도, 간단한 생활 보조","D-17"),
    )
}

fun computeDday(
    createdAtIso: String?,   // e.g. "2025-10-05T17:14:26.692965+00"
    isPaid: Boolean?,
    paidDays: Int?
): String {
    return try {
        if (createdAtIso == null) return "D-?"
        val createdAt = OffsetDateTime.parse(createdAtIso).toLocalDate()
        val today = LocalDate.now()

        val endDate = if (isPaid == true && paidDays != null)
            paidDays + 7
        else
            7 // 무료 공고는 그대로

        var left = ChronoUnit.DAYS.between(today, createdAt).toInt()
        left = endDate-left
        when {
            left < 0 -> "마감"
            else -> "D-$left"
        }
    } catch (e: Exception) {
        "D-?"
    }
}

fun List<RecoJob>.toJobDetailList(imageMap: Map<Long, String>): List<JobCardUi> =
    map { job ->
        val url = imageMap[job.id]
        val talents = listOf(
            "영어 회화", "악기 지도", "요리 강사", "역사 강의",
            "공예 강의", "예술 지도", "독서 지도", "관광 가이드",
            "상담·멘토링", "홍보 컨설팅"
        )

        val dday = computeDday(
            createdAtIso = job.created_at,
            isPaid = job.is_paid,
            paidDays = job.paid_days
        )

        val selected = job.talent
            ?.toList()
            ?.mapIndexedNotNull { index, c ->
                if (c == '1') talents.getOrNull(index) else null
            }
            ?.take(2)
            ?.joinToString(" | ") ?: "없음"

        JobCardUi(
            id = job.id,
            org = job.company_name ?: "회사명 없음",
            condition = "| $selected",
            desc = job.major ?: "-",
            dday = dday,
            imageUrl = url ?: ""
        )
    }

/* ===================== ViewModel ===================== */

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        MainUiState(
            aiJobs = emptyList(),
            banners = MainFakeRepository.loadBanners(),
            tailoredJobs =  emptyList()
        )
    )

    val uiState: StateFlow<MainUiState> = _uiState

    fun fetchRpcAiRecommendations() {
        viewModelScope.launch {
            val username = CurrentUser.username ?: return@launch
            try {
                val recoList = fetchAiRecommendedJobs(username)
                val aiList = recoList.map { job ->
                    val tag = if (job.career_required == true) "[경력]" else "[무관]"
                    val Dday = computeDday(
                        createdAtIso = job.created_at,
                        isPaid = job.is_paid,
                        paidDays = job.paid_days
                    )
                    JobSummary(
                        id = job.id,
                        org = job.company_name ?: "회사명 없음",
                        tag = "[${job.job_category ?: "일반"}]",
                        title = job.major ?: "-",
                        desc = job.form ?: "-",
                        dday = "$Dday | $tag"
                    )
                }

                _uiState.update { it.copy(aiJobs = aiList) }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchRpcRecommendations() {
        viewModelScope.launch {
            val recoList = fetchRecommendedJobs(
                category = null,
                days = null,
                startMin = null, endMin = null,
                region = null, years = 0, gender = null
            )

            val ids = recoList.map { it.id }.distinct()
            val imageMap = fetchCompanyImagesMap(ids)

            val tailored = recoList.toJobDetailList(imageMap)
            _uiState.update { it.copy(tailoredJobs = tailored) }
        }
    }

    fun onSearchChange(text: String) { _uiState.update { it.copy(searchText = text) } }

    fun refreshRecommendations() {
        _uiState.update { it.copy(aiJobs = it.aiJobs.shuffled(), tailoredJobs = it.tailoredJobs.shuffled()) }
    }


}

/* ===================== Route 진입점 ===================== */

@Composable
fun MainRoute(nav: NavController, vm: MainViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    LaunchedEffect(Unit) {
        vm.fetchRpcAiRecommendations()
        vm.fetchRpcRecommendations()
    }
    val client = LocalSupabase.current
    val username = CurrentUser.username
    val recentRepo = remember(client) { RecentWatchSupabase(client) }
    val scope = rememberCoroutineScope()

    MainScreen(
        state = state,
        onSearch = vm::onSearchChange,
        onJobClick = { id->
            if (!username.isNullOrBlank()) {
                scope.launch {
                    recentRepo.upsertRecentWatch(username, id)
                }
            }
            nav.navigate(Route.JobDetail.of(id))
        },
        onTailoredClick = { id->
            if (!username.isNullOrBlank()) {
                scope.launch {
                    recentRepo.upsertRecentWatch(username, id)
                }
            }
            nav.navigate(Route.JobDetail.of(id))
        },

        onOpenCalendar = { nav.navigate(Route.Map.path)  },
        onShortcut = { key ->
            when (key) {
                "home" -> nav.navigate("main") { launchSingleTop = true }
                "edu" -> nav.navigate("edu")
                "welfare", "welfare/home" -> nav.navigate("welfare/home")
                "my" -> nav.navigate("my")
            }
        },
        onRefreshTailored = vm::refreshRecommendations,
        onBannerClick = { idx ->
            when (idx) {
                0 -> nav.navigate("ad/1")
                1 -> nav.navigate("ad/2")
                2 -> nav.navigate("ad/3")
            }
        }
    )
}

/* ===================== UI ===================== */

@Composable
fun MainScreen(
    state: MainUiState,
    onSearch: (String) -> Unit,
    onJobClick: (Long) -> Unit,
    onTailoredClick: (Long) -> Unit,
    onOpenCalendar: () -> Unit,
    onShortcut: (String) -> Unit,
    onRefreshTailored: () -> Unit,
    onBannerClick: (Int) -> Unit,
) {

    val brandBlue = Color(0xFF005FFF)
    val screenBg = Color(0xFFF1F5F7)
    var user by remember { mutableStateOf<String?>(null) }
    val client = LocalSupabase.current
    val currentuser = CurrentUser.username

    LaunchedEffect(currentuser) {
        user = fetchDisplayNameByUsername(currentuser)
    }

    val limitedTailored = state.tailoredJobs.take(3)

    val context = LocalContext.current
    var showPopup by remember {
        mutableStateOf(shouldShowHomePopup(context))
    }

    var bannerIndex by remember { mutableStateOf(0) }
    LaunchedEffect(state.banners.size) {
        while (isActive && state.banners.isNotEmpty()) {
            delay(10_000)
            bannerIndex = (bannerIndex + 1) % state.banners.size
        }
    }

    Scaffold(
        containerColor = screenBg,
        bottomBar = {
            AppBottomBar(
                current = "home",
                onClick = { key ->
                    if (showPopup) showPopup = false
                    onShortcut(key)
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 메인 스크롤 리스트
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                /* 0) 스크롤되는 헤더(로고/알림) - 회색 배경줄 */
                item {
                    ScrollHeaderRow(
                        barBgColor = screenBg,
                        chipBgColor = screenBg,
                        iconTintBlue = brandBlue,
                        onLogoClick = {},
                        onNotifyClick = {}
                    )
                }

                /* 1) 상단 인사 */
                item {
                    Text(
                        text = "오늘도 좋은 하루입니다,\n${user}님",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        lineHeight = 40.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 4.dp)
                    )
                }

                /* 2) 검색창 */
                item {
                    SearchBar(
                        value = state.searchText,
                        onValueChange = onSearch,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                /* 3) 면접 일정 카드 */
                item {
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        InterviewCalendarCard(onClick = onOpenCalendar)
                    }
                }

                /* 4) (위) AI 추천 일자리 — 2×2 */
                item {
                    val sectionGap = 12.dp

                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Spacer(Modifier.height(sectionGap))
                        SectionTitle("${user}님을 위한 AI 추천 일자리")
                        Spacer(Modifier.height(sectionGap))
                    }
                }

                val gridItems = state.aiJobs.take(4).chunked(2)
                items(gridItems) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        JobSummaryCard(
                            job = row[0],
                            onClick = {
                                Log.d("MainScreen", "✅ AI Job 클릭됨 → id=${row[0].id}")
                                onJobClick(row[0].id)
                           },

                            modifier = Modifier.weight(1f)
                        )
                        if (row.size > 1) {
                            JobSummaryCard(
                                job = row[1],
                                onClick = {
                                    Log.d("MainScreen", "✅ AI Job 클릭됨 → id=${row[1].id}")
                                    onJobClick(row[1].id) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }

                /* 5) 광고 (자동 회전) */
                item {
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        BannerCarousel(
                            images = listOf(
                                R.drawable.main_banner1,
                                R.drawable.main_banner2,
                                R.drawable.main_banner3
                            ),
                            pageSpacing = 10.dp,
                            onClickIndex = { idx -> onBannerClick(idx) }
                        )
                    }
                }

                /* 6) (아래) 맞춤형 일자리 — 사진 + 설명 */
                item {
                    val sectionGap = 12.dp

                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Spacer(Modifier.height(sectionGap))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionTitle("${user}님을 위한 맞춤형 일자리")
                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = onRefreshTailored,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.refresh),
                                    contentDescription = "새로고침",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(sectionGap))
                    }
                }

                items(limitedTailored, key = { it.id }) { jd ->
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        JobDetailCard(job = jd, onClick = { onTailoredClick(jd.id) })
                    }
                }
            }

            // ✅ 하단 팝업
            if (showPopup) {
                HomePopupDialog(
                    onDismiss = { showPopup = false },
                    onCloseToday = {
                        hideHomePopupToday(context)  // 오늘 하루 숨김 저장
                        showPopup = false            // 즉시 닫기
                    }
                )
            }
        }
    }
}

@Composable
fun HomePopupDialog(
    onDismiss: () -> Unit,
    onCloseToday: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
                    .background(Color.White)
            ) {
                val painter = painterResource(R.drawable.ad_lifis)
                val ratio = remember(painter) {
                    val s = painter.intrinsicSize
                    val w = s.width; val h = s.height
                    if (w.isFinite() && h.isFinite() && h > 0f) w / h else 360f / 270f
                }

                Image(
                    painter = painter,
                    contentDescription = "홈 팝업 광고",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(ratio)
                        .clickable {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.lifis.co.kr/?c=shop&m=product&cate_cd=000002&sval=")
                            )
                            context.startActivity(intent)
                        },
                    contentScale = ContentScale.Fit
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(63.dp)
                        .background(Color.White)
                        .padding(horizontal = 25.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCloseToday) {
                        Text(
                            "오늘 그만보기",
                            fontSize = 18.sp,
                            color = Color(0xFF828282),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text(
                            "닫기",
                            fontSize = 18.sp,
                            color = Color(0xFF005FFF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/* ---------- 스크롤되는 헤더(로고/알림) ---------- */
@Composable
fun ScrollHeaderRow(
    barBgColor: Color,
    chipBgColor: Color,
    iconTintBlue: Color,
    onLogoClick: () -> Unit,
    onNotifyClick: () -> Unit
) {
    Surface(color = barBgColor, shadowElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(start = 0.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onLogoClick, contentPadding = PaddingValues(0.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(chipBgColor, shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(29.dp),
                        colorFilter = ColorFilter.tint(iconTintBlue)
                    )
                }
            }

            IconButton(onClick = onNotifyClick) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(chipBgColor, shape = RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bell),
                        contentDescription = "Notifications",
                        modifier = Modifier.size(29.dp)
                    )
                }
            }
        }
    }
}

/* ---------- 검색창 ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier.shadow(4.dp, shape = shape, clip = false)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("검색어를 입력하세요") },
            singleLine = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Color(0xFF62626D)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White, shape),
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor  = Color.White,
                focusedBorderColor      = Color(0xFFC1D2ED),
                unfocusedBorderColor    = Color(0xFFC1D2ED),
                cursorColor             = Color(0xFF005FFF)
            )
        )
    }
}

/* ---------- 캘린더 버튼 ---------- */
@Composable
private fun InterviewCalendarCard(onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    val painter = painterResource(R.drawable.main_schedule)

    val ratio = remember(painter) {
        val s = painter.intrinsicSize
        val w = s.width
        val h = s.height
        if (w.isFinite() && h.isFinite() && h > 0f) w / h else 340f / 148f
    }

    Card(
        onClick = onClick,
        shape = shape,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painter,
            contentDescription = "면접 일정 확인하기",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio)
                .background(Color.Transparent),
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.Center
        )
    }
}

/* ---------- 하단 네비 ---------- */
data class NavItem(
    val key: String,
    val unselectedRes: Int,
    val selectedRes: Int? = null
)

@Composable
fun BottomNavBar(current: String, onClick: (String) -> Unit) {
    val brandBlue = Color(0xFF005FFF)

    val items = listOf(
        NavItem("home",      R.drawable.unselected_home,      R.drawable.selected_home),
        NavItem("edu",       R.drawable.unselected_education, R.drawable.selected_education),
        NavItem("welfare/home",   R.drawable.unselected_welfare,   R.drawable.selected_welfare),
        NavItem("my",        R.drawable.unselected_my,        R.drawable.selected_my),
    )

    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            val isSelected = item.key == current
            val iconRes = if (isSelected && item.selectedRes != null) item.selectedRes else item.unselectedRes

            NavigationBarItem(
                selected = isSelected,
                onClick = { onClick(item.key) },
                icon = {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = item.key,
                        modifier = Modifier.size(55.dp),
                        colorFilter = if (isSelected && item.selectedRes == null)
                            ColorFilter.tint(brandBlue) else null
                    )
                },
                label = null,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Color.Unspecified,
                    selectedTextColor   = Color.Unspecified,
                    unselectedIconColor = Color.Unspecified,
                    unselectedTextColor = Color.Unspecified,
                    indicatorColor      = Color.Transparent
                )
            )
        }
    }
}

/* ---------- 공통 UI ---------- */
@Composable
private fun SectionTitle(text: String) {
    Text(text = text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
}

/* ---------- 유틸: D-day ---------- */
private fun parseDaysLeft(dday: String): Int? {
    val idx = dday.indexOf("D-")
    if (idx == -1) return null
    val start = idx + 2
    val digits = dday.drop(start).takeWhile { it.isDigit() }
    return digits.toIntOrNull()
}

/* ---------- (위) AI 추천 텍스트 카드 ---------- */
@Composable
private fun JobSummaryCard(
    job: JobSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val daysLeft = remember(job.dday) { parseDaysLeft(job.dday) }
    val ddayColor = if (daysLeft != null && daysLeft <= 10) Color.Red else Color(0xFF005FFF)

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column {
                Text(
                    job.org,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = LocalTextStyle.current.copy(lineBreak = LineBreak.Paragraph)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    job.tag,
                    fontSize = 15.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = LocalTextStyle.current.copy(lineBreak = LineBreak.Paragraph)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    job.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = LocalTextStyle.current.copy(lineBreak = LineBreak.Paragraph)
                )
            }

            Spacer(Modifier.weight(1f))

            val (dPart, rest) = remember(job.dday) { splitDdayParts(job.dday) }
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = ddayColor)) { append(dPart) }
                    append(rest)
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

/* ---------- (아래) 맞춤형 카드 ---------- */
@Composable
private fun JobDetailCard(job: JobCardUi, onClick: () -> Unit) {
    val daysLeft = remember(job.dday) { parseDaysLeft(job.dday) }
    val ddayColor = if (daysLeft != null && daysLeft <= 10) Color.Red else Color(0xFF005FFF)
    val (dPart, _) = remember(job.dday) { splitDdayParts(job.dday) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 236.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .background(Color(0xFFE9EEF8), RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(job.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                DdayBadge(
                    dday = job.dday,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(job.org, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(Modifier.width(6.dp))
                    Text(job.condition, fontSize = 15.sp, color = Color(0xFFFF2F00))
                }
                Spacer(Modifier.height(6.dp))
                Text(job.desc, fontSize = 14.sp, color = Color.Black)
            }
        }
    }
}

@Composable
private fun SectionTitleWithRefresh(
    text: String,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionTitle(text)
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onRefresh,
            modifier = Modifier.size(36.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.refresh),
                contentDescription = "새로고침",
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun DdayBadge(dday: String, modifier: Modifier = Modifier) {
    val daysLeft = parseDaysLeft(dday)
    val color = if (daysLeft != null && daysLeft <= 10)
        Color(0xFFFF2F00)
    else
        Color(0xFF005FFF)

    val (dPart, _) = splitDdayParts(dday)

    Row(
        modifier = modifier
            .height(24.dp)
            .background(color, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dPart,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 24.sp,
            letterSpacing = (-0.019).em
        )
    }
}

/* ---------- 광고(자동 회전) ---------- */
@Composable
fun AutoRotatingAd(
    banners: List<AdBanner>,
    autoIntervalMs: Long = 5_000L,
    height: Dp = 184.dp,
    pageSpacing: Dp = 10.dp
) {
    val realCount = banners.size
    if (realCount == 0) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            shape = CardDefaults.shape,
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A77FF))
        ) { /* ... */ }
        return
    }

    val virtualCount = Int.MAX_VALUE
    val startPage = remember(realCount) {
        val mid = virtualCount / 2
        mid - (mid % realCount)
    }
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { virtualCount }
    )

    LaunchedEffect(realCount) {
        while (isActive) {
            delay(autoIntervalMs)
            while (pagerState.isScrollInProgress) delay(80)
            pagerState.scrollToPage(pagerState.currentPage)
            pagerState.animateScrollToPage(pagerState.currentPage + 1, pageOffsetFraction = 0f)
        }
    }

    val currentReal = (pagerState.currentPage % realCount + realCount) % realCount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        shape = CardDefaults.shape,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = pageSpacing
            ) { page ->
                val idx = (page % realCount + realCount) % realCount
                val banner = banners[idx]

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 0.dp),
                    shape = CardDefaults.shape,
                    colors = CardDefaults.cardColors(containerColor = banner.bg),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = banner.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                                .size((height * 0.70f).coerceAtLeast(110.dp)),
                            contentScale = ContentScale.Fit
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                banner.titleTop,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                banner.titleBottom,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(realCount) { i ->
                    val active = i == currentReal
                    Box(
                        modifier = Modifier
                            .size(if (active) 10.dp else 8.dp)
                            .background(
                                if (active) Color.White else Color.White.copy(alpha = 0.5f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun BannerCarousel(
    images: List<Int>,
    autoIntervalMs: Long = 5_000L,
    pageSpacing: Dp = 8.dp,
    aspectRatioFallback: Float = 360f / 170f,
    onClickIndex: (Int) -> Unit
) {
    if (images.isEmpty()) return

    val firstPainter = painterResource(id = images.first())
    val ratio = run {
        val s = firstPainter.intrinsicSize
        val w = s.width; val h = s.height
        if (w.isFinite() && h.isFinite() && h > 0f) w / h else aspectRatioFallback
    }

    val virtualCount = Int.MAX_VALUE
    val realCount = images.size
    val startPage = remember(realCount) {
        val mid = virtualCount / 2
        mid - (mid % realCount)
    }
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { virtualCount }
    )

    LaunchedEffect(realCount) {
        while (isActive) {
            delay(autoIntervalMs)
            while (pagerState.isScrollInProgress) delay(80)
            pagerState.scrollToPage(pagerState.currentPage)
            pagerState.animateScrollToPage(pagerState.currentPage + 1, pageOffsetFraction = 0f)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ratio),
        shape = CardDefaults.shape,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = pageSpacing
            ) { page ->
                val idx = (page % realCount + realCount) % realCount
                val res = images[idx]
                Image(
                    painter = painterResource(res),
                    contentDescription = "banner ${idx + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onClickIndex(idx) },
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center
                )
            }

            val currentReal = (pagerState.currentPage % realCount + realCount) % realCount
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(realCount) { i ->
                    val active = i == currentReal
                    Box(
                        modifier = Modifier
                            .size(if (active) 10.dp else 8.dp)
                            .background(
                                if (active) Color.White else Color.White.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }
        }
    }
}

/* ---------- "오늘 하루 그만 보기" 상태 저장용 SharedPreferences ---------- */

private const val PREFS_HOME_POPUP = "home_popup_prefs"
private const val KEY_HIDE_UNTIL_EPOCH_DAY = "hide_until_epoch_day"

// 오늘 팝업을 보여줘야 하는지 여부
private fun shouldShowHomePopup(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_HOME_POPUP, Context.MODE_PRIVATE)
    val hideUntil = prefs.getLong(KEY_HIDE_UNTIL_EPOCH_DAY, -1L)
    if (hideUntil == -1L) return true

    val today = LocalDate.now().toEpochDay()
    return today > hideUntil
}

// "오늘 그만보기" 선택 시 오늘까지만 숨기도록 저장
private fun hideHomePopupToday(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_HOME_POPUP, Context.MODE_PRIVATE)
    val today = LocalDate.now().toEpochDay()
    prefs.edit()
        .putLong(KEY_HIDE_UNTIL_EPOCH_DAY, today)
        .apply()
}
