package com.example.dodojob.ui.feature.employ

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import com.example.dodojob.session.CurrentUser
import com.example.dodojob.ui.feature.main.EmployerBottomNavBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random

/* ===== Colors ===== */
private val ScreenBg  = Color(0xFFF1F5F7)
private val BrandBlue = Color(0xFF005FFF)
private val TextGray  = Color(0xFF828282)
private val LineGray  = Color(0xFFD7D7D7)
private val White     = Color(0xFFFFFFFF)

/* ===== Tabs / Sort labels (UI만 유지) ===== */
private val TABS = listOf("전체", "활성중", "일시중지", "마감")
private val SORT_OPTIONS = listOf("최신순", "마감 임박", "지원자 많은순", "조회수 많은순", "제목 A-Z")

/* =======================================================================================
 * Public UI Model (외부 노출 OK)
 * =======================================================================================*/
data class AnnouncementUi(
    val id: Long,
    val title: String,
    val location: String,
    val createdDate: LocalDate,
    val applicants: Int = 0,
    val views: Int = 0,              // 비어오면 로드 시 10~20 랜덤으로 채움
    val dueDate: LocalDate = createdDate, // 기본값: createdDate
    val workType: String = "풀타임"
)

/* =======================================================================================
 * Data Provider (레포 대신 얇은 인터페이스만)
 * =======================================================================================*/
val user = CurrentUser.username

fun interface AnnouncementsProvider {
    suspend fun fetchAnnouncements(user : String?): List<AnnouncementUi>
}

/* =======================================================================================
 * State / ViewModel
 * =======================================================================================*/
data class AnnouncementUiState(
    val items: List<AnnouncementUi> = emptyList(),
    val selectedTab: Int = 0,
    val selectedSort: String = SORT_OPTIONS.first(), // UI 표시용. 실제 정렬엔 영향 없음.
    val loading: Boolean = false,
    val error: String? = null,
    // 간단 통계(전체/활성/임박/종료) — 현재 로컬 계산
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val dueSoonCount: Int = 0,
    val closedCount: Int = 0
)

class ManagementAnnouncementViewModel(
    private val provider: AnnouncementsProvider
) : ViewModel() {

    private val _state = MutableStateFlow(AnnouncementUiState())
    val state: StateFlow<AnnouncementUiState> = _state

    fun load() {
        val st = _state.value
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                // 항상 provider에서 불러오고, createdDate 기준 최신순으로만 정렬
                val sorted = provider.fetchAnnouncements(user)
                    .sortedByDescending { it.createdDate }

                // views가 0이면 10~20 랜덤으로 채움
                val base = sorted.map {
                    if (it.views <= 0) it.copy(views = Random.nextInt(10, 21)) else it
                }

                // 간단 통계 (필요 시 실제 로직으로 교체)
                val total = base.size
                val active = total          // 현재 모두 활성이라고 가정
                val dueSoon = 0
                val closed = 0

                // 탭 2(일시중지), 3(마감) 선택 시 빈 목록
                val displayed = when (st.selectedTab) {
                    2, 3 -> emptyList()
                    else -> base
                }

                _state.update {
                    it.copy(
                        items = displayed,
                        loading = false,
                        totalCount = total,
                        activeCount = active,
                        dueSoonCount = dueSoon,
                        closedCount = closed
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "알 수 없는 오류") }
            }
        }
    }

    fun onTabSelect(idx: Int) {
        _state.update { it.copy(selectedTab = idx) }
        load()
    }

    fun onSortChange(label: String) {
        // 드롭다운은 UI 텍스트만 변경, 실제 정렬은 항상 createdDate 최신순
        _state.update { it.copy(selectedSort = label) }
        // 재로드 불필요
    }
}

/* =======================================================================================
 * Screen
 * =======================================================================================*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementAnnouncementRoute(
    nav: NavController,
    provider: AnnouncementsProvider, // <- 외부에서 넘겨줌
    vm: ManagementAnnouncementViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ManagementAnnouncementViewModel(provider) as T
            }
        }
    )
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            EmployerBottomNavBar(
                current = "notice",
                onClick = { key ->
                    when (key) {
                        "home"           -> nav.safeNavigate(Route.EmployerHome.path)
                        "notice"         -> nav.safeNavigate(Route.EmployerNotice.path)
                        "applicant"      -> nav.safeNavigate(Route.EmployerApplicant.path)
                        "human_resource" -> nav.safeNavigate(Route.EmployerHumanResource.path)
                        "my"             -> nav.safeNavigate(Route.EmployerMy.path)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                ) {
                    TopNavigationBar(title = "공고관리", useOwnBackground = false)
                    Spacer(Modifier.height(8.dp))

                    val stats = listOf(
                        StatItem("전체 공고", state.totalCount, R.drawable.total_announcement),
                        StatItem("활성중", state.activeCount, R.drawable.active_announcement),
                        StatItem("마감임박", state.dueSoonCount, R.drawable.due_soon_announcement),
                        StatItem("종료", state.closedCount, R.drawable.closed_announcement),
                    )
                    StatGrid(
                        items = stats,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    FilterTabs(
                        tabs = TABS,
                        selected = state.selectedTab,
                        onSelect = vm::onTabSelect,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ScreenBg)
                ) {
                    ListControls(
                        total = state.items.size,
                        sortOptions = SORT_OPTIONS,
                        selectedSort = state.selectedSort,
                        onSortChange = vm::onSortChange, // UI 텍스트만 변경
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            when {
                state.loading -> {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                }
                state.error != null -> {
                    item {
                        Text(
                            text = "로드 실패: ${state.error}",
                            color = Color.Red,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
                else -> {
                    items(state.items) { ann ->
                        AnnouncementCard(
                            data = ann,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

/* ===== Top Bar ===== */
@Composable
private fun TopNavigationBar(
    title: String,
    useOwnBackground: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .then(if (useOwnBackground) Modifier.background(Color(0xFFF4F5F7)) else Modifier)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.Black, modifier = Modifier.padding(top = 6.dp))
    }
}

/* ===== Stats ===== */
data class StatItem(
    val label: String,
    val number: Int,
    val iconRes: Int
)

@Composable
private fun StatGrid(items: List<StatItem>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.take(2).forEach { item ->
                StatTile(item = item, isLast = false, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.drop(2).forEachIndexed { i, item ->
                StatTile(item = item, isLast = (i == items.drop(2).lastIndex), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(item: StatItem, isLast: Boolean, modifier: Modifier = Modifier) {
    val bgColor = if (isLast) Color(0xFFF2F2F2) else Color(0xFFF5F9FF)
    Row(
        modifier = modifier
            .height(73.dp)
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 22.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFDEEAFF)),
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(id = item.iconRes), contentDescription = null, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(item.label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Text("${item.number}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isLast) Color(0xFF414141) else BrandBlue)
        }
    }
}

/* ===== Tabs (지원완료/면접예정/합격결과 스타일 재사용) ===== */
@Composable
private fun FilterTabs(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, text ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    ManagementTabLabel(
                        text = text,
                        isSelected = selected == index,
                        onClick = { onSelect(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ManagementTabLabel(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()          // 🔹 Row 높이(45dp)를 꽉 채움
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom   // 🔹 내용 전체를 아래로 붙임
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = (-0.019).em,
            color = if (isSelected) BrandBlue else Color(0xFF848484), // ← Figma: #848484
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))              // 텍스트와 선 사이 간격
        Box(
            modifier = Modifier
                .width(66.dp)
                .height(3.dp)
                .background(if (isSelected) BrandBlue else Color.Transparent)
        )
    }
}

/* ===== Sort bar (UI만 바뀌고 실제 정렬엔 영향 없음) ===== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListControls(
    total: Int,
    sortOptions: List<String>,
    selectedSort: String,
    onSortChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 드롭다운에 실제로 보여줄 3개 (Figma 텍스트)
    val dropdownItems = listOf(
        "최신순",
        "지원자 많은 순",
        "마감 임박 순"
    )

    Row(
        modifier = modifier.height(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "총 ${total}개",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextGray,
            letterSpacing = (-0.019).em
        )

        var expanded by remember { mutableStateOf(false) }

        // 최신순이면 upper, 아니면 down
        val sortIconRes = if (expanded == true) {
            R.drawable.upper
        } else {
            R.drawable.down
        }

        Box {
            Row(
                modifier = Modifier
                    .clickable { expanded = true }
                    .height(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedSort,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextGray,
                    letterSpacing = (-0.019).em
                )
                Spacer(Modifier.width(4.dp))
                Image(
                    painter = painterResource(sortIconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    surface = Color.White,
                    surfaceVariant = Color.White,
                    surfaceTint = Color.Transparent
                ),
                typography = MaterialTheme.typography,
                shapes = MaterialTheme.shapes
            ) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    offset = DpOffset(x = (-50).dp, y = 0.dp),
                    modifier = Modifier
                        .width(113.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        dropdownItems.forEach { option ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .clickable {
                                        onSortChange(option)
                                        expanded = false
                                    },
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = (-0.019).em,
                                    color = TextGray,
                                    modifier = Modifier.padding(start = 20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ===== Card ===== */
@Composable
private fun AnnouncementCard(
    data: AnnouncementUi,
    modifier: Modifier = Modifier
) {
    fun String.firstTwoTokens(): String {
        val parts = trim().split(Regex("\\s+"), limit = 3)
        return when {
            parts.size >= 2 -> parts[0] + " " + parts[1]
            else -> this
        }
    }

// 사용
    val displayLocation = data.location.firstTwoTokens()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)
        ) {
            // 제목 + 더보기
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.title,
                    fontSize = 15.sp,
                    lineHeight = 22.sp, // 여기 줄이면 이제 확실히 차이 보일 거야
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.019).em,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(24.dp)                // 전체 터치 영역 (원하면 20.dp로 더 줄여도 됨)
                        .clickable { /* TODO: overflow menu */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "더 보기",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)   // 아이콘 자체 크기
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 위치 / 관리·운영 / 회사 내규
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)   // 세 그룹 사이 gap 18px
            ) {
                // 1) 위치
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.location),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)                // 12x12
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = displayLocation,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        letterSpacing = (-0.019).em,
                        color = TextGray
                    )
                }

                // 2) 관리/운영 (desk.png)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.desk),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "관리/운영",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        letterSpacing = (-0.019).em,
                        color = TextGray
                    )
                }

                // 3) 회사 내규 (db.png)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.db),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "회사 내규",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        letterSpacing = (-0.019).em,
                        color = TextGray
                    )
                }
            }

// 메트릭: 지원자/조회수/마감일
            Spacer(Modifier.height(20.dp))   // ← 12dp → 20dp 로 변경
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricItem(R.drawable.number_of_applicant, "지원자", "${data.applicants}명", BrandBlue)
                MetricItem(R.drawable.number_of_views, "조회수", "${data.views}", BrandBlue)
                MetricItem(R.drawable.due_date, "마감일", "${data.dueDate} 까지", BrandBlue)
            }

            // 구분선
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(thickness = 1.dp, color = LineGray)
            Spacer(Modifier.height(20.dp))

            // 하단: 근무형태 칩 + 액션(수정/복사/통계)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 칩
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))          // radius 5
                        .background(Color(0xFFDEEAFF))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = data.workType,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        letterSpacing = (-0.019).em,
                        color = BrandBlue,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 액션
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionItem(R.drawable.edit, "수정")
                    ActionItem(R.drawable.copy, "복사")
                    ActionItem(R.drawable.statistics, "통계")
                }
            }
        }
    }
}

/* ===== 보조 컴포넌트 ===== */
@Composable
private fun MetricItem(iconRes: Int, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)   // 24x24
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            letterSpacing = (-0.019).em,
            color = Color.Black
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            letterSpacing = (-0.019).em,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ActionItem(iconRes: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            modifier = Modifier.size(12.dp)      // 12x12
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            letterSpacing = (-0.019).em,
            color = TextGray
        )
    }
}

private fun NavController.safeNavigate(
    route: String,
    builder: (NavOptionsBuilder.() -> Unit)? = { launchSingleTop = true; restoreState = true }
) { navigate(route) { builder?.invoke(this) } }
