package com.example.dodojob.ui.feature.profile

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.BoxWithConstraints
import com.example.dodojob.R

/* ===================== 색상 토큰 ===================== */
private val PrimaryBlue = Color(0xFF005FFF)
private val TextGray    = Color(0xFF848484)
private val ScreenBg    = Color(0xFFF1F5F7)

/* ===================== 데이터 모델 ===================== */
data class ViewedItem(
    val id: String,
    val company: String,
    val title: String,
    val isOpen: Boolean,
    val viewedAt: String
)

/* ===================== Fake DB ===================== */
private object RecentFakeDb {
    fun items(): List<ViewedItem> = listOf(
        ViewedItem("v1", "모던하우스", "매장운영 및 고객관리 하는 일에 적합한 분 구해요", true, "2025.08.25"),
        ViewedItem("v2", "대구동구 어린이도서관", "아이들 책 읽어주기, 독서 습관 형성 프로그램 지원", false, "2025.08.20"),
        ViewedItem("v3", "수성구 체육센터", "회원 운동 지도 보조, 센터 관리 가능하신 분 지원 요망", true, "2025.08.14"),
        ViewedItem("v4", "대구도시철도공사", "지하철 역사 안전 순찰, 이용객 안내, 분실물 관리", false, "2025.08.10"),
    )
}

/* ===================== Route + Screen ===================== */
@Composable
fun RecentViewedRoute(nav: NavController) {
    var all by remember { mutableStateOf(RecentFakeDb.items()) }
    var selectedTab by remember { mutableStateOf(0) }

    val visible = remember(selectedTab, all) {
        if (selectedTab == 0) all else all.filter { it.isOpen }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        RecentTopSection(
            nav = nav,
            countText = countText(visible.size),
            onDeleteClosed = { all = all.filter { it.isOpen } }
        )

        RecentTabBar(
            tabs = listOf("전체", "모집중"),
            selectedIndex = selectedTab,
            onSelected = { selectedTab = it },
            underlineWidth = 68.dp
        )

        RecentList(visible)
    }
}

/* ===================== 상단 섹션 ===================== */
@Composable
private fun RecentTopSection(
    nav: NavController,
    countText: AnnotatedString,
    onDeleteClosed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // 상태바 (회색 영역)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color(0xFFEFEFEF))
        )

        // 🔻 min 높이 제거 → wrap content 로
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 16.dp, top = 14.dp)
        ) {
            // 아이콘
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { nav.popBackStack() }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.ArrowBackIosNew, contentDescription = "뒤로", tint = Color.Black)
                }
            }
            Spacer(Modifier.height(10.dp)) // chevron ↔ 타이틀

            // 타이틀
            Row(
                modifier = Modifier.height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "최근 본 공고",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.019).em,
                    color = Color.Black
                )
            }
        }

        // 🔻 타이틀 아래 간격을 고정 6dp로만 부여 (더 줄였음)
        Spacer(Modifier.height(6.dp))

        // count + 마감공고 삭제
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = countText,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.019).em,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color(0xFF828282))
            )
            Spacer(Modifier.width(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onDeleteClosed() }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    "마감공고 삭제",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF828282)
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    painter = painterResource(id = R.drawable.trash),
                    contentDescription = "마감공고 삭제",
                    tint = Color(0xFF828282),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/* ===================== 탭바 ===================== */
@Composable
private fun RecentTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    underlineWidth: Dp
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(Color.White)
    ) {
        val tabWidth = maxWidth / tabs.size
        val targetOffset = tabWidth * selectedIndex + (tabWidth - underlineWidth) / 2
        val animatedOffset by animateDpAsState(targetValue = targetOffset, label = "tabIndicatorOffset")

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { i, label ->
                val selected = i == selectedIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(tabWidth)
                        .clickable { onSelected(i) }
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 18.sp,
                        lineHeight = 20.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = (-0.2).sp,
                        color = if (selected) PrimaryBlue else Color(0xFF000000)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = animatedOffset)
                .width(underlineWidth)
                .height(4.dp)
                .background(PrimaryBlue)
        )
    }
}

/* ===================== 리스트 & 카드 ===================== */
@Composable
private fun RecentList(items: List<ViewedItem>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                if (index == 0) {
                    RecentCard(item) // 첫 카드 위 회색 제거
                } else {
                    RecentCard(item)
                }
            }
        }
    }
}

@Composable
private fun RecentCard(item: ViewedItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .heightIn(min = 120.dp)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val stateLabel = if (item.isOpen) "모집중" else "마감"
            val stateColor = if (item.isOpen) PrimaryBlue else TextGray
            Text(
                text = stateLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.019).em,
                color = stateColor
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Outlined.MoreHoriz, contentDescription = "더보기", tint = Color(0xFF343330))
            }
        }
        Text(item.company, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextGray)
        Text(
            text = item.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF000000),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* ===================== Util ===================== */
private fun countText(count: Int): AnnotatedString = buildAnnotatedString {
    append("총 ")
    pushStyle(SpanStyle(color = PrimaryBlue))
    append("${count}건")
    pop()
    append("의 알바가 있어요")
}
