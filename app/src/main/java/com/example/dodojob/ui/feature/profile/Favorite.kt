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
import androidx.compose.material3.*
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

/* ===================== 색상 ===================== */
private val PrimaryBlue = Color(0xFF005FFF)
private val TextGray    = Color(0xFF848484)
private val ScreenBg    = Color(0xFFF1F5F7)
private val DangerRed   = Color(0xFFFF2F00)

/* ===================== 데이터 모델 ===================== */
data class LikedItem(
    val id: String,
    val company: String,
    val title: String,
    val isOpen: Boolean,
    val likedAt: String // (표시는 안 하지만 유지)
)

/* ===================== Fake DB ===================== */
private object LikedFakeDb {
    fun items(): List<LikedItem> = listOf(
        LikedItem("l1", "모던하우스", "매장운영 및 고객관리 하는 일에 적합한 분 구해요", true,  "2025.08.25"),
        LikedItem("l2", "대구동구 어린이도서관", "아이들 책 읽어주기, 독서 습관 형성 프로그램 지원", false, "2025.08.20"),
        LikedItem("l3", "수성구 체육센터", "회원 운동 지도 보조, 센터 관리 가능하신 분 지원 요망", true,  "2025.08.14"),
    )
}

/* ===================== Route ===================== */
@Composable
fun LikedJobsRoute(nav: NavController) {
    var all by remember { mutableStateOf(LikedFakeDb.items()) }
    var selectedTab by remember { mutableStateOf(0) } // 0: 전체, 1: 모집중

    val visible = remember(selectedTab, all) {
        if (selectedTab == 0) all else all.filter { it.isOpen }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LikedTopSection(
            nav = nav,
            countText = countText(visible.size),
            onDeleteClosed = { all = all.filter { it.isOpen } }
        )

        LikedTabBar(
            tabs = listOf("전체", "모집중"),
            selectedIndex = selectedTab,
            onSelected = { selectedTab = it },
            underlineWidth = 68.dp
        )

        LikedList(visible)
    }
}

/* ===================== 상단 섹션 ===================== */
@Composable
private fun LikedTopSection(
    nav: NavController,
    countText: AnnotatedString,
    onDeleteClosed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // 상태바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color(0xFFEFEFEF))
        )

        // 타이틀 (프로필/최근 본 과 동일 규격)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 16.dp, top = 14.dp)
        ) {
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
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "좋아요한 일자리",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.019).em,
                    color = Color.Black
                )
            }
        }

        // 타이틀 아래 간격 축소
        Spacer(Modifier.height(6.dp))

        // 카운트 + 마감공고 삭제
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onDeleteClosed() }
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
private fun LikedTabBar(
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

/* ===================== 리스트 ===================== */
@Composable
private fun LikedList(items: List<LikedItem>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 12.dp) // 👈 위쪽 패딩 제거
        ) {
            itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                Box(
                    modifier = Modifier
                        .padding(
                            top = if (index == 0) 0.dp else 12.dp, // 첫 번째는 위 간격 0
                            start = 0.dp,
                            end = 0.dp
                        )
                ) {
                    LikedCard(item)
                }
            }
        }
    }
}


/* ===================== 카드 ===================== */
@Composable
private fun LikedCard(item: LikedItem) {
    var liked by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // 🔹 상단 Row
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

            IconButton(onClick = { liked = !liked }) {
                Icon(
                    painter = painterResource(id = R.drawable.heart),
                    contentDescription = if (liked) "좋아요 취소" else "좋아요",
                    tint = if (liked) DangerRed else TextGray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 👇 Row와 회사명 사이만 좁게 (예: 6dp)
        Spacer(Modifier.height(2.dp))

        Text(
            item.company,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextGray
        )

        Spacer(Modifier.height(12.dp)) // 회사명 ↔ 제목 간격 유지

        Text(
            text = item.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF000000),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(16.dp)) // 제목 ↔ 버튼 간격 유지

        // 지원하기 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryBlue)
                .clickable { /* TODO */ },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "지원하기",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
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
