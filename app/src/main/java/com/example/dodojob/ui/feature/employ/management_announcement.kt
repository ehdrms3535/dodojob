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
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import com.example.dodojob.ui.feature.main.EmployerBottomNavBar
import java.time.LocalDate

/* ===== Colors ===== */
private val ScreenBg  = Color(0xFFF1F5F7) // 회색 배경
private val BrandBlue = Color(0xFF005FFF)
private val TextGray  = Color(0xFF828282)
private val LineGray  = Color(0xFFD7D7D7)
private val White     = Color(0xFFFFFFFF)

/* ===== UI Data ===== */
private data class AnnouncementUi(
    val title: String,
    val location: String,
    val tag: String,
    val pay: String,
    val applicants: Int,
    val views: Int,
    val dueDate: LocalDate,
    val workType: String
)

/* ===== 상단 통계 카드용 ===== */
data class StatItem(
    val label: String,
    val number: Int,
    val iconRes: Int
)

/* ===== Route Entry ===== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementAnnouncementRoute(nav: NavController) {
    val tabs = listOf("전체", "활성중", "일시중지", "마감")
    var selectedTab by remember { mutableStateOf(0) }

    // 정렬 드롭다운
    val sortOptions = listOf("최신순", "마감 임박", "지원자 많은순", "조회수 많은순", "제목 A-Z")
    var selectedSort by remember { mutableStateOf(sortOptions.first()) }

    // 더미 DB
    val today = LocalDate.now()
    val list = remember {
        listOf(
            AnnouncementUi("현대백화점 대구점 주간 미화원 모집", "대구 중구", "관리/운영", "시급 9,620원", 8, 142, today.plusDays(3), "풀타임"),
            AnnouncementUi("강남 사무실 야간 청소", "서울 강남구", "청소", "월급 200만원", 5, 80, today.plusDays(1), "아르바이트"),
            AnnouncementUi("식당 서빙 아르바이트", "부산 해운대구", "서빙", "시급 10,000원", 12, 240, today.minusDays(1), "아르바이트"),
            AnnouncementUi("물류센터 포장 인력", "인천 남동구", "물류", "월급 220만원", 3, 50, today.plusDays(10), "풀타임")
        )
    }

    // 탭 필터링
    val filtered = remember(list, selectedTab, today) {
        when (selectedTab) {
            0 -> list // 전체
            1 -> list.filter { !it.dueDate.isBefore(today) } // 활성중: 오늘 포함 이후
            2 -> emptyList() // 일시중지: 데이터 없으니 비움
            else -> list.filter { it.dueDate.isBefore(today) } // 마감
        }
    }

    // 정렬
    val displayed = remember(filtered, selectedSort, today) {
        when (selectedSort) {
            "최신순" -> filtered.sortedByDescending { it.dueDate } // createdAt 없어서 dueDate로 근사
            "마감 임박" -> filtered.sortedBy { it.dueDate }
            "지원자 많은순" -> filtered.sortedByDescending { it.applicants }
            "조회수 많은순" -> filtered.sortedByDescending { it.views }
            "제목 A-Z" -> filtered.sortedBy { it.title }
            else -> filtered
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            EmployerBottomNavBar(
                current = "notice",
                onClick = { key ->
                    when (key) {
                        "home"      -> nav.navigate(Route.EmployerHome.path)
                        "notice"    -> { /* 현재 */ }
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
            item { TopStatusBar() }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)   // ← 흰 배경
                        .padding(bottom = 0.dp)
                ) {
                    TopNavigationBar(
                        title = "공고관리",
                        useOwnBackground = false
                    )

                    Spacer(Modifier.height(8.dp))

                    val stats = listOf(
                        StatItem("전체 공고", list.size, R.drawable.total_announcement),
                        StatItem("활성중", list.count { !it.dueDate.isBefore(today) }, R.drawable.active_announcement),
                        StatItem("마감임박", list.count { !it.dueDate.isBefore(today) && it.dueDate.isBefore(today.plusDays(3)) }, R.drawable.due_soon_announcement),
                        StatItem("종료", list.count { it.dueDate.isBefore(today) }, R.drawable.closed_announcement),
                    )
                    StatGrid(
                        items = stats,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    FilterTabs(
                        tabs = tabs,
                        selected = selectedTab,
                        onSelect = { selectedTab = it },
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
                        total = displayed.size,
                        sortOptions = sortOptions,
                        selectedSort = selectedSort,
                        onSortChange = { selectedSort = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }


            items(displayed) { ann ->
                AnnouncementCard(
                    data = ann,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

/* ===== 상단 바 ===== */
@Composable
private fun TopStatusBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Color(0xFFEFEFEF))
    )
}

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

/* ===== 상단 통계: 2x2 Grid ===== */
@Composable
private fun StatGrid(items: List<StatItem>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.take(2).forEach { item ->
                StatTile(
                    item = item,
                    isLast = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.drop(2).forEachIndexed { i, item ->
                val isLast = (i == items.drop(2).lastIndex)
                StatTile(
                    item = item,
                    isLast = isLast,
                    modifier = Modifier.weight(1f)
                )
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
        // 아이콘
        Box(
            modifier = Modifier
                .size(30.dp) // 24 → 30
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFDEEAFF)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                item.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                "${item.number}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLast) Color(0xFF414141) else BrandBlue
            )
        }
    }
}

/* ===== Tabs ===== */
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

    // 1/4 영역(세그먼트) 너비
    val segmentWidth by remember(rowWidthPx, tabCount) {
        derivedStateOf { with(density) { (rowWidthPx / tabCount).toDp() } }
    }
    // 선택된 탭의 시작 X
    val targetX by remember(selected, segmentWidth) {
        derivedStateOf { segmentWidth * selected }
    }
    // 밑줄 이동 애니메이션
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


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
        ) {
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

/* ===== 탭 하단 컨트롤: 총 개수 + 정렬 드롭다운 ===== */
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
        modifier = modifier
            .height(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 좌측: "총 N개"
        Text(
            text = "총 ${total}개",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextGray,
            letterSpacing = (-0.019).em
        )

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .menuAnchor()
                    .height(24.dp)
                    .clickable { expanded = true },
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
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = "정렬 선택",
                    modifier = Modifier.size(20.dp),
                    tint = TextGray
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                sortOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSortChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/* ===== 공고 카드 ===== */
@Composable
private fun AnnouncementCard(data: AnnouncementUi, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            /* 제목 + 더보기 */
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

            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetaItem(R.drawable.location, data.location)
                MetaItem(R.drawable.tag, data.tag)
                MetaItem(R.drawable.pay, data.pay)
            }

            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricItem(R.drawable.number_of_applicant, "지원자", "${data.applicants}명", BrandBlue)
                MetricItem(R.drawable.number_of_views, "조회수", "${data.views}", BrandBlue)
                MetricItem(R.drawable.due_date, "마감일", "${data.dueDate} 까지", BrandBlue)
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = LineGray, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            /* 근무형태 칩 + 액션 (칩 높이 축소) */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDEEAFF))
                        .padding(horizontal = 10.dp, vertical = 2.dp) // 6dp → 2dp
                ) {
                    Text(
                        data.workType,
                        fontSize = 12.sp,
                        color = BrandBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
private fun MetaItem(iconRes: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, color = TextGray)
    }
}

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
