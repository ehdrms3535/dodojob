package com.example.dodojob.ui.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.dodojob.R
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.em


/* ===================== 데이터 모델 ===================== */

data class JobSummary(
    val id: String,
    val org: String,
    val tag: String,
    val title: String,
    val desc: String, // 상단 카드에서는 미표시(보관용)
    val dday: String,
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
    val tailoredJobs: List<JobDetail> = emptyList()
)

/* ===================== Fake Repository ===================== */

object MainFakeRepository {
    fun loadAiJobs(): List<JobSummary> = listOf(
        JobSummary("j1","대구 전통시장 상인회","[시장형사업단]","채소·과일 포장 및 판매","상품 포장·진열 및 간단한 판매 보조","D-12 | 경력"),
        JobSummary("j2","대구시립도서관","[도서관리 지원]","반납·대출 보조 업무","자료 정리·반납 정산·이용자 안내","D-4 | 경력,신입"),
        JobSummary("j3","한국노인인력 개발원","[미디어 전문 서비스]","미디어컨텐츠 제작 및 교육","촬영·편집 보조 및 교육 진행","D-18 | 경력"),
        JobSummary("j4","모던하우스 대구점","[매장운영·고객관리]","매장 정리, 고객 응대","매장 진열·청결 관리, 고객 안내","D-9 | 경력"),
    )

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

/* ===================== ViewModel ===================== */

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        MainUiState(
            aiJobs = MainFakeRepository.loadAiJobs(),
            banners = MainFakeRepository.loadBanners(),
            tailoredJobs = MainFakeRepository.loadTailored()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState

    fun onSearchChange(text: String) { _uiState.update { it.copy(searchText = text) } }
    fun refreshRecommendations() {
        _uiState.update { it.copy(aiJobs = it.aiJobs.shuffled(), tailoredJobs = it.tailoredJobs.shuffled()) }
    }
}

/* ===================== Route 진입점 ===================== */

@Composable
fun MainRoute(nav: NavController, vm: MainViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    MainScreen(
        state = state,
        onSearch = vm::onSearchChange,
        onJobClick = { /* nav.navigate("job_detail/$it") */ },
        onTailoredClick = { /* nav.navigate("job_detail/$it") */ },
        onMoreRecommend = vm::refreshRecommendations,
        onOpenCalendar = { /* nav.navigate("calendar") */ },
        onShortcut = { key ->
            when (key) {
                "home" -> nav.navigate("main") { launchSingleTop = true }
                "edu" -> nav.navigate("edu")
                "welfare" -> nav.navigate("welfare")
                "community" -> nav.navigate("community")
                "my" -> nav.navigate("my")
            }
        }
    )
}

/* ===================== UI ===================== */

@Composable
fun MainScreen(
    state: MainUiState,
    onSearch: (String) -> Unit,
    onJobClick: (String) -> Unit,
    onTailoredClick: (String) -> Unit,
    onMoreRecommend: () -> Unit,
    onOpenCalendar: () -> Unit,
    onShortcut: (String) -> Unit
) {
    val brandBlue = Color(0xFF005FFF)
    val screenBg = Color(0xFFF1F5F7)

    var bannerIndex by remember { mutableStateOf(0) }
    LaunchedEffect(state.banners.size) {
        while (isActive && state.banners.isNotEmpty()) {
            delay(10_000)
            bannerIndex = (bannerIndex + 1) % state.banners.size
        }
    }

    Scaffold(
        containerColor = screenBg,
        // topBar 제거: 헤더를 리스트 첫 아이템으로 넣어 스크롤 시 함께 사라짐
        bottomBar = { BottomNavBar(current = "home", onClick = onShortcut) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
                        text = "오늘도 좋은 하루입니다,\n${state.greetingName}님",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        lineHeight = 40.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 4.dp)
                    )
                }

            /* 2) 검색창 (둥근 + 연한테두리 + 그림자) */
            item {
                SearchBar(
                    value = state.searchText,
                    onValueChange = onSearch,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            /* 3) 면접 일정 달력 버튼 (그라데이션) */
            item {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    InterviewCalendarButton(onClick = onOpenCalendar)
                }
            }

            /* 4) (위) AI 추천 일자리 — 사진 없음 (2×2) */
            item {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    SectionTitle("${state.greetingName}님을 위한 AI 추천 일자리")
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
                        onClick = { onJobClick(row[0].id) },
                        modifier = Modifier.weight(1f)
                    )
                    if (row.size > 1) {
                        JobSummaryCard(
                            job = row[1],
                            onClick = { onJobClick(row[1].id) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            /* 5) 광고 (자동 회전) */
            if (state.banners.isNotEmpty()) {
                item {
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        AutoRotatingAd(banners = state.banners)  // ← 리스트 통째로 전달
                    }
                }
            }

            /* 6) (아래) 맞춤형 일자리 — 사진 + 설명(더 크게) */
            item {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    SectionTitle("${state.greetingName}님을 위한 맞춤형 일자리")
                }
            }

            items(state.tailoredJobs, key = { it.id }) { jd ->
                Box(Modifier.padding(horizontal = 16.dp)) {
                    JobDetailCard(job = jd, onClick = { onTailoredClick(jd.id) })
                }
            }

            /* 7) 다른 일자리 추천받기 */
            item {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    Button(
                        onClick = onMoreRecommend,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x2B005FFF))
                    ) {
                        Text("다른 일자리 추천받기", color = Color(0xFF005FFF), fontSize = 18.sp)
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
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ✅ 로고 버튼
            TextButton(onClick = onLogoClick, contentPadding = PaddingValues(0.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(chipBgColor, shape = RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo1), // ← 로고 리소스
                        contentDescription = "Logo",
                        modifier = Modifier.size(50.dp),
                        colorFilter = ColorFilter.tint(iconTintBlue) // 필요 시 색 적용
                    )
                }
            }

            // ✅ 알림 버튼
            IconButton(onClick = onNotifyClick) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(chipBgColor, shape = RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bell), // ← 알림 리소스
                        contentDescription = "Notifications",
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }
    }
}


/* ---------- 검색창: 둥근 + 연한테두리 + 그림자 + 돋보기 ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .shadow(4.dp, shape = shape, clip = false)
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

/* ---------- 캘린더 버튼: 보라→파랑 그라데이션 + 흰 텍스트 + 아이콘 ---------- */
@Composable
private fun InterviewCalendarButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Card(
        onClick = onClick,
        shape = shape,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF7169D8), Color(0xFF005FFF))
                    ),
                    shape = shape
                )
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("지원현황", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    Text(
                        "면접 일정 확인하기",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 📅 여기! → drawable 리소스 불러오기
                    Image(
                        painter = painterResource(id = R.drawable.calender),
                        contentDescription = "달력",
                        modifier = Modifier.size(125.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/* ---------- 하단 네비 ---------- */
data class NavItem(
    val key: String,
    val unselectedRes: Int,
    val selectedRes: Int? = null // 없으면 틴트 처리
)

@Composable
fun BottomNavBar(current: String, onClick: (String) -> Unit) {
    val brandBlue = Color(0xFF005FFF)

    val items = listOf(
        NavItem("home",      R.drawable.unselected_home,      R.drawable.selected_home),
        NavItem("edu",       R.drawable.unselected_education, null),
        NavItem("welfare",   R.drawable.unselected_welfare,   null),
        NavItem("community", R.drawable.unselected_talent,    null),
        NavItem("my",        R.drawable.unselected_my,        R.drawable.selected_my),
    )

    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            val isSelected = item.key == current

            // ✅ 선택 여부에 따라 아이콘 결정
            val iconRes = if (isSelected && item.selectedRes != null) {
                item.selectedRes
            } else {
                item.unselectedRes
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onClick(item.key) },
                icon = {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = item.key,
                        modifier = Modifier.size(55.dp),
                        // selectedRes 없고 선택된 탭만 파란 틴트
                        colorFilter = if (isSelected && item.selectedRes == null)
                            ColorFilter.tint(brandBlue)
                        else null
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

/* ---------- 유틸: D-day 남은 날짜 파싱 ---------- */
private fun parseDaysLeft(dday: String): Int? {
    val idx = dday.indexOf("D-")
    if (idx == -1) return null
    val start = idx + 2
    val digits = dday.drop(start).takeWhile { it.isDigit() }
    return digits.toIntOrNull()
}

/* ---------- (위) AI 추천 텍스트 카드: 사진 없음 + D-day 색상 ---------- */
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
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(job.org,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    style = LocalTextStyle.current.copy(
                        lineBreak = LineBreak.Paragraph // 단어/어절 단위 줄바꿈
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(job.tag,
                    fontSize = 15.sp,
                    color = Color.Black,
                    style = LocalTextStyle.current.copy(
                        lineBreak = LineBreak.Paragraph // 단어/어절 단위 줄바꿈
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(job.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    style = LocalTextStyle.current.copy(
                        lineBreak = LineBreak.Paragraph // 단어/어절 단위 줄바꿈
                    )
                )
            }
            Text(job.dday, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ddayColor)
        }
    }
}

/* ---------- (아래) 맞춤형 카드: 사진 + 설명 (더 크게) + D-day 색상 ---------- */
@Composable
private fun JobDetailCard(job: JobDetail, onClick: () -> Unit) {
    val daysLeft = remember(job.dday) { parseDaysLeft(job.dday) }
    val ddayColor = if (daysLeft != null && daysLeft <= 10) Color.Red else Color(0xFF005FFF)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 236.dp), // desc 보이도록 여유
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column {
            // 이미지 영역(Placeholder) — 필요 시 Coil로 AsyncImage 교체
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .background(Color(0xFFE9EEF8), RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier
                        .background(ddayColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(job.dday, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(job.org, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(Modifier.width(6.dp))
                    Text(job.condition, fontSize = 14.sp, color = Color(0xFFFF2F00))
                }
                Spacer(Modifier.height(6.dp))
                Text(job.desc, fontSize = 14.sp, color = Color.Black)
            }
        }
    }
}

/* ---------- 광고(자동 회전) ---------- */
@Composable
fun AutoRotatingAd(
    banners: List<AdBanner>,
    autoIntervalMs: Long = 5_000L,
) {
    val realCount = banners.size

    // 배너가 없으면 플레이스홀더 표시 후 종료
    if (realCount == 0) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(154.dp),
            shape = CardDefaults.shape,
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A77FF))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "광고가 없습니다.",
                    color = Color.White,
                    fontSize = 16.sp,
                    letterSpacing = (-0.019).em,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    // 무한 캐러셀: 매우 큰 가상 페이지 위에서 mod 매핑
    val virtualCount = Int.MAX_VALUE
    val startPage = remember(realCount) {
        val mid = virtualCount / 2
        mid - (mid % realCount)
    }
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { virtualCount }
    )

    // 자동 슬라이드
    LaunchedEffect(realCount) {
        while (isActive) {
            delay(autoIntervalMs)

            // 스와이프 중이면 대기
            while (pagerState.isScrollInProgress) {
                delay(80)
            }

            // 분수 오프셋 정리 후 다음 페이지로
            pagerState.scrollToPage(pagerState.currentPage)
            val next = pagerState.currentPage + 1
            pagerState.animateScrollToPage(next, pageOffsetFraction = 0f)
        }
    }

    // 현재 실제 인덱스
    val currentReal = (pagerState.currentPage % realCount + realCount) % realCount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp),
        shape = CardDefaults.shape,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val idx = (page % realCount + realCount) % realCount
                val banner = banners[idx]

                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = CardDefaults.shape,
                    colors = CardDefaults.cardColors(containerColor = banner.bg),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        // 오른쪽 이미지
                        Image(
                            painter = painterResource(id = banner.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                                .size(110.dp),            // 필요시 조절 (예: 100~140.dp)
                            contentScale = ContentScale.Fit
                        )

                        // 왼쪽 텍스트
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
                            Spacer(modifier = Modifier.height(4.dp))
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

            // ●●● 인디케이터
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
