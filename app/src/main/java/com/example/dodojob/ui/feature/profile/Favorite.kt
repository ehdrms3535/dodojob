package com.example.dodojob.ui.feature.profile

import com.example.dodojob.navigation.Route
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val announcement_pricing: List<AnnouncementPricingRow>? = null
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
    return pricings.maxByOrNull { it.created_at ?: "" }
}

private fun computeTotalDays(pricings: List<AnnouncementPricingRow>?): Int {
    val base = 7
    val pricing = pickLatestPricing(pricings)
    val extra = pricing?.date?.toInt() ?: 0
    return base + extra
}

private fun parseDateTimeFlexible(str: String): LocalDateTime {
    return try {
        OffsetDateTime.parse(str).toLocalDateTime()
    } catch (e: Exception) {
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
    val client = LocalSupabase.current
    val username = CurrentUser.username ?: return

    var all by remember { mutableStateOf<List<LikedItem>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) } // 0: 전체, 1: 모집중

    LaunchedEffect(username) {
        all = fetchLikedJobs(client, username)
    }

    val visible = remember(selectedTab, all) {
        when (selectedTab) {
            0 -> all
            1 -> all.filter { it.isOpen }
            else -> all
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        LikedTopSection(
            nav = nav,
            countText = countText(visible.size),
            onDeleteClosed = {
                // UI에서 마감된 것 제거
                all = all.filter { it.isOpen }
                // TODO: 실제 DB 업데이트는 필요시 추가
            }
        )

        LikedTabBar(
            tabs = listOf("전체", "모집중"),
            selectedIndex = selectedTab,
            onSelected = { selectedTab = it },
            underlineWidth = 68.dp
        )

        LikedList(
            items = visible,
            nav = nav
        )
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
        // 상태바 (회색 영역) – 최근본공고 동일
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color(0xFFEFEFEF))
        )

        // 뒤로가기 (back.png) – ChangePassword / 최근본공고 스타일
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 24.dp, start = 6.dp)
                    .size(48.dp)
                    .clickable { nav.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "뒤로가기",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // 타이틀
        Text(
            text = "좋아요한 일자리",   // 🔹 텍스트만 변경
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.019f).em,
            color = Color.Black,
            modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
        )

        // 타이틀 아래 간격
        Spacer(Modifier.height(16.dp))

        // count + 마감공고 삭제 – 최근본공고 스타일
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

            // 세로 구분선
            Box(
                modifier = Modifier
                    .height(16.dp)
                    .width(1.dp)
                    .offset(y = 1.dp)
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
                Image(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = "마감공고 삭제",
                    modifier = Modifier
                        .size(18.dp)
                        .offset(y = 1.dp)
                )
            }
        }

        Spacer(Modifier.height(22.dp))
    }
}

/* ===================== 탭바 – 최근본공고 스타일 ===================== */

@Composable
private fun LikedTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    underlineWidth: Dp = 68.dp,   // 파란선 길이
    indicatorHeight: Dp = 2.dp,   // 파란선 두께
    tabSpacing: Dp = 40.dp        // 전체 / 모집중 사이 간격
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { i, label ->
                val selected = i == selectedIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .clickable { onSelected(i) }
                ) {
                    Text(
                        text = label,
                        fontSize = 18.sp,
                        lineHeight = 20.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = (-0.5).sp,
                        color = if (selected) PrimaryBlue else Color(0xFF000000)
                    )

                    // 글자와 파란선 사이 간격
                    Spacer(modifier = Modifier.height(8.dp))

                    // 선택된 탭만 파란선 보이게
                    Box(
                        modifier = Modifier
                            .width(underlineWidth)
                            .height(if (selected) indicatorHeight else 0.dp)
                            .background(
                                color = if (selected) PrimaryBlue else Color.Transparent,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }

        // 탭 아래 흰색 여백
        Spacer(modifier = Modifier.height(12.dp))
    }
}

/* ===================== 리스트 ===================== */

@Composable
private fun LikedList(
    items: List<LikedItem>,
    nav: NavController
) {
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
            itemsIndexed(items, key = { _, it -> it.id }) { _, item ->
                LikedCard(
                    item = item,
                    nav = nav
                )
            }
        }
    }
}

/* ===================== 카드 – 최근본공고 카드 스타일 + 하트 ===================== */

@Composable
private fun LikedCard(
    item: LikedItem,
    nav: NavController
) {
    var liked by remember { mutableStateOf(item.isLiked) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .heightIn(min = 120.dp)
            .clickable {
                nav.navigate(
                    Route.JobDetail.path.replace(
                        "{id}",
                        item.id.toString()
                    )
                )
            }
            .padding(
                start = 24.dp,
                end = 16.dp,
                top = 20.dp,
                bottom = 20.dp
            ),
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
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.019).em,
                color = stateColor
            )

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = {
                    liked = !liked
                    // TODO: Supabase isliked 업데이트
                },
                modifier = Modifier.size(28.dp)
            ) {
                Image(
                    painter = painterResource(
                        id = if (liked) R.drawable.heart else R.drawable.empty_heart
                    ),
                    contentDescription = if (liked) "좋아요 취소" else "좋아요",
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Text(
            text = item.company,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = TextGray
        )

        Text(
            text = item.title,
            fontSize = 18.sp,
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
