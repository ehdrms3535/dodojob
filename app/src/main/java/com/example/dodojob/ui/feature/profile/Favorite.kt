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
import com.example.dodojob.data.supabase.LocalSupabase
import java.time.LocalDateTime
import com.example.dodojob.session.CurrentUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/* ===================== 색상 ===================== */
private val PrimaryBlue = Color(0xFF005FFF)
private val TextGray    = Color(0xFF848484)
private val ScreenBg    = Color(0xFFF1F5F7)
private val DangerRed   = Color(0xFFFF2F00)

/* ===================== UI 데이터 모델 ===================== */
data class LikedItem(
    val id: Long,          // announcement.id
    val title: String,     // 공고 제목 = announcement.company_name
    val company: String,   // 회사명 = 현재도 company_name 그대로 사용
    val isOpen: Boolean,   // 모집 중 / 마감
    val isLiked: Boolean   // 좋아요 여부
)

/* ===================== Supabase DTO ===================== */

@Serializable
private data class AnnouncementPricingRow(
    val date: Long? = null,
    val created_at: String? = null
)

@Serializable
private data class AnnouncementNested(
    val id: Long,
    val company_name: String? = null,
    val created_at: String,
    val announcement_pricing: List<AnnouncementPricingRow>? = null   // ✅ 리스트로 변경
)


@Serializable
private data class AnnouncementSeniorRow(
    val announcement_id: Long,
    val isliked: Boolean? = null,
    val announcement: AnnouncementNested? = null
)

/* ===================== 모집기간 / 상태 계산 ===================== */

private fun pickLatestPricing(pricings: List<AnnouncementPricingRow>?): AnnouncementPricingRow? {
    if (pricings.isNullOrEmpty()) return null
    // created_at 기준으로 가장 최근 것 하나 선택
    return pricings.maxByOrNull { it.created_at ?: "" }
}

private fun computeTotalDays(pricings: List<AnnouncementPricingRow>?): Int {
    val base = 7
    val pricing = pickLatestPricing(pricings)
    val extra = pricing?.date?.toInt() ?: 0
    return base + extra
}

// 문자열을 Offset + Local 형식 둘 다 받아주는 유틸
private fun parseDateTimeFlexible(str: String): LocalDateTime {
    return try {
        // 만약 "2025-11-11T12:31:38Z" 처럼 offset 있는 경우
        OffsetDateTime.parse(str).toLocalDateTime()
    } catch (e: Exception) {
        // 지금처럼 "2025-11-11T12:31:38" 인 경우
        LocalDateTime.parse(str)
    }
}

private fun computeIsOpen(
    announcementCreatedAt: String,
    pricings: List<AnnouncementPricingRow>?
): Boolean {
    val totalDays = computeTotalDays(pricings)

    val latest = pickLatestPricing(pricings)
    val baseDateStr = latest?.created_at ?: announcementCreatedAt

    val base = parseDateTimeFlexible(baseDateStr)
    val now = LocalDateTime.now()

    val passedDays = ChronoUnit.DAYS.between(base, now)
    val leftDays = totalDays - passedDays

    return leftDays >= 0
}


/* ===================== Supabase에서 좋아요 공고 가져오기 ===================== */

suspend fun fetchLikedJobs(
    client: SupabaseClient,
    seniorUsername: String
): List<LikedItem> {

    val rows = client.from("announcement_senior")
        .select(
            Columns.raw(
                """
                announcement_id,
                isliked,
                announcement:announcement_id (
                    id,
                    company_name,
                    created_at,
                    announcement_pricing (
                        date,
                        created_at
                    )
                )
                """.trimIndent()
            )
        ) {
            // ❗ 최신 supabase-kt 문법: filter { eq(...) }
            filter {
                eq("senior_username", seniorUsername)
                eq("isliked", true)
            }
        }
        .decodeList<AnnouncementSeniorRow>()

    return rows.mapNotNull { row ->
        val ann = row.announcement ?: return@mapNotNull null

        val isOpen = computeIsOpen(
            announcementCreatedAt = ann.created_at,
            pricings = ann.announcement_pricing
        )

        LikedItem(
            id = ann.id,
            title = ann.company_name ?: "-",
            company = ann.company_name ?: "-",
            isOpen = isOpen,
            isLiked = row.isliked == true
        )
    }
}

/* ===================== Route ===================== */

@Composable
fun LikedJobsRoute(
    nav: NavController
) {

    val client = LocalSupabase.current      // 예시: 네가 쓰는 헬퍼
    val username = CurrentUser.username ?: return  // 로그인 유저

    var all by remember { mutableStateOf<List<LikedItem>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) } // 0: 전체, 1: 모집중

    LaunchedEffect(username) {
        all = fetchLikedJobs(client, username)
    }

    val visible = remember(selectedTab, all) {
        when (selectedTab) {
            0 -> all
            1 -> all.filter { it.isOpen }   // 모집 중만
            else -> all
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LikedTopSection(
            nav = nav,
            countText = countText(visible.size),
            onDeleteClosed = {
                // "마감공고 삭제" → UI에서 마감된 것 제거
                all = all.filter { it.isOpen }
                // TODO: 실제 DB에서도 마감 + isliked=false 로 바꾸고 싶으면 여기서 Supabase update 호출
            }
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
        // 상태바 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color(0xFFEFEFEF))
        )

        // 타이틀 영역
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
                IconButton(
                    onClick = { nav.popBackStack() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "뒤로",
                        tint = Color.Black
                    )
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
        val animatedOffset by animateDpAsState(
            targetValue = targetOffset,
            label = "tabIndicatorOffset"
        )

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
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                Box(
                    modifier = Modifier
                        .padding(
                            top = if (index == 0) 0.dp else 12.dp,
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
    var liked by remember { mutableStateOf(item.isLiked) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // 상단: 모집 상태 + 하트
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val stateLabel = if (item.isOpen) "모집 중" else "마감"
            val stateColor = if (item.isOpen) PrimaryBlue else TextGray

            Text(
                text = stateLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.019).em,
                color = stateColor
            )
            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = {
                    liked = !liked
                    // TODO: 여기서 announcement_senior.isliked 업데이트 Supabase 호출
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.heart),
                    contentDescription = if (liked) "좋아요 취소" else "좋아요",
                    tint = if (liked) DangerRed else TextGray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.height(2.dp))

        Text(
            item.company,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextGray
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = item.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF000000),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryBlue)
                .clickable {
                    // TODO: 공고 상세 화면으로 이동
                },
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
