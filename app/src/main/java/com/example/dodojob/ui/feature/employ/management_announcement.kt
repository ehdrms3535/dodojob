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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
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
fun interface AnnouncementsProvider {
    suspend fun fetchAnnouncements(): List<AnnouncementUi>
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
                val sorted = provider.fetchAnnouncements()
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
                        "home"      -> nav.navigate(Route.EmployerHome.path)
                        "notice"    -> Unit
                        "applicant" -> nav.navigate(Route.EmployerApplicant.path)
                        "my"        -> nav.navigate(Route.EmployerMy.path)
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

                    Spacer(Modifier.height(8.dp))

                    FilterTabs(
                        tabs = TABS,
                        selected = state.selectedTab,
                        onSelect = vm::onTabSelect,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
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
        Text(title, fontSize = 30.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
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
            .padding(horizontal = 12.dp, vertical = 10.dp),
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

/* ===== Tabs (animated underline) ===== */
@Composable
private fun FilterTabs(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var rowWidthPx by remember { mutableStateOf(0) }
    val tabCount = tabs.size.coerceAtLeast(1)

    val segmentWidth by remember(rowWidthPx, tabCount) {
        derivedStateOf { with(density) { (rowWidthPx / tabCount).toDp() } }
    }
    val targetX by remember(selected, segmentWidth) { derivedStateOf { segmentWidth * selected } }
    val indicatorX by animateDpAsState(
        targetValue = targetX,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "indicatorX"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .onGloballyPositioned { rowWidthPx = it.size.width }
        ) {
            tabs.forEachIndexed { i, t ->
                val color by animateColorAsState(
                    targetValue = if (i == selected) BrandBlue else TextGray,
                    animationSpec = tween(180),
                    label = "tabColor"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelect(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t,
                        fontSize = 15.sp,
                        fontWeight = if (i == selected) FontWeight.Bold else FontWeight.Medium,
                        color = color
                    )
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(3.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.BottomCenter)
                    .background(LineGray)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = indicatorX)
                    .width(segmentWidth)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BrandBlue)
            )
        }
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

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            Row(
                modifier = Modifier.menuAnchor().height(24.dp).clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedSort,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextGray,
                    letterSpacing = (-0.019).em
                )
                Spacer(Modifier.width(6.dp))
                Icon(imageVector = Icons.Filled.ExpandMore, contentDescription = "정렬 선택", modifier = Modifier.size(20.dp), tint = TextGray)
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                sortOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSortChange(option) // UI 텍스트만 바뀜
                            expanded = false
                        }
                    )
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
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {

            // 제목 + 더보기
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    data.title,
                    fontSize = 15.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /* TODO: overflow menu */ }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "더 보기", tint = Color.Black)
                }
            }

            // 위치
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.location), contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(displayLocation, fontSize = 12.sp, color = TextGray)
            }

            // 메트릭: 지원자/조회수/마감일
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricItem(R.drawable.number_of_applicant, "지원자", "${data.applicants}명", BrandBlue)
                MetricItem(R.drawable.number_of_views, "조회수", "${data.views}", BrandBlue)
                MetricItem(R.drawable.due_date, "마감일", "${data.dueDate} 까지", BrandBlue)
            }

            // 구분선
            Spacer(Modifier.height(12.dp))
            Divider(color = LineGray, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            // 하단: 근무형태 칩 + 액션(수정/복사/통계)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 칩
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDEEAFF))
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        data.workType,
                        fontSize = 12.sp,
                        color = BrandBlue,
                        fontWeight = FontWeight.SemiBold
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
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, color = Color.Black)
        Spacer(Modifier.width(6.dp))
        Text(value, fontSize = 13.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ActionItem(iconRes: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(text, fontSize = 12.sp, color = TextGray)
    }
}
