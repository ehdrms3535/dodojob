package com.example.dodojob.ui.feature.profile

import android.os.Parcelable
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dodojob.R
import com.example.dodojob.data.career.CareerRepositoryImpl
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.session.CurrentUser
import com.example.dodojob.session.JobBits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random
import com.example.dodojob.data.recentwatch.RecentWatchSupabase
import com.example.dodojob.dao.getRecentWatchList
import com.example.dodojob.data.greatuser.GreatUser
import com.example.dodojob.ui.feature.employ.TalentUi
import kotlinx.parcelize.Parcelize
import com.example.dodojob.data.announcement.fullannouncement.fetchAnnouncementFull
import android.util.Log
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/* ===================== 색상 토큰 ===================== */
private val PrimaryBlue = Color(0xFF005FFF)
private val TextGray    = Color(0xFF848484)
private val ScreenBg    = Color(0xFFF1F5F7)

/* ===================== 데이터 모델 ===================== */
@Parcelize
data class ViewedItem(
    val id: String,
    val company: String,
    val title: String,
    val isOpen: Boolean,
    val viewedAt: String
) : Parcelable

/* ===================== Fake DB ===================== */
private object RecentFakeDb {
    fun items(): List<ViewedItem> = listOf(
        ViewedItem("v1", "모던하우스", "매장운영 및 고객관리 하는 일에 적합한 분 구해요", true, "2025.08.25"),
        ViewedItem("v2", "대구동구 어린이도서관", "아이들 책 읽어주기, 독서 습관 형성 프로그램 지원", false, "2025.08.20"),
        ViewedItem("v3", "수성구 체육센터", "회원 운동 지도 보조, 센터 관리 가능하신 분 지원 요망", true, "2025.08.14"),
        ViewedItem("v4", "대구도시철도공사", "지하철 역사 안전 순찰, 이용객 안내, 분실물 관리", false, "2025.08.10"),
    )
}

data class RecentWatchUiState(
    val isLoading: Boolean = false,
    val Recentwatch: List<ViewedItem> = emptyList(),
    val error: String? = null
)

class RecentWatchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RecentWatchUiState())
    val uiState: StateFlow<RecentWatchUiState> = _uiState

    fun loadUserData(username: String?, repo: RecentWatchSupabase) {
        val TAG = "RecentWatchVM"

        if (username.isNullOrBlank()) {
            Log.d(TAG, "❌ username이 비어있습니다.")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "username이 비어있습니다."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // 1) 최근 본 공고 목록 가져오기
                val users = getRecentWatchList(username)
                Log.d(TAG, "🔥 getRecentWatchList($username) size = ${users.size}")

                // 비어있으면 바로 반환
                if (users.isEmpty()) {
                    Log.d(TAG, "⚠ 최근 본 공고가 없습니다.")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        Recentwatch = emptyList(),
                        error = "최근 본 공고가 없습니다."
                    )
                    return@launch
                }

                // 2) 공고 상세 매핑
                val recents = users.mapNotNull { user ->
                    val a = fetchAnnouncementFull(user.announceid)

                    if (a == null) {
                        Log.w(TAG, "⚠ fetchAnnouncementFull(${user.announceid}) == null, 스킵함")
                        return@mapNotNull null
                    }

                    val company = a.company_name
                    val title = a.major
                    val viewAt = user.created_at
                    val isPaid = a.is_paid ?: false

                    val baseDuration = 7
                    val extraDays = if (isPaid) {
                        a.paid_days?.toInt() ?: 0
                    } else 0
                    val duration = baseDuration + extraDays

                    val createdAt = OffsetDateTime.parse(a.created_at)   // "2025-11-11T12:34:56Z" 형식 가정
                    val now = OffsetDateTime.now()

                    val daysDiff = ChronoUnit.DAYS.between(createdAt, now)
                    val isWithinDuration = daysDiff <= duration
                    val isOpen = isWithinDuration

                    Log.d(TAG, "✅ ${user.announceid} → company=$company, title=$title, isOpen=$isOpen, daysDiff=$daysDiff, duration=$duration")

                    ViewedItem(
                        id = user.announceid.toString(),
                        company = company.toString(),
                        title = "${title}에 적합한 분 구해요",
                        isOpen = isOpen,
                        viewedAt = viewAt
                    )
                }

                Log.d(TAG, "🔥 recents 최종 size = ${recents.size}")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    Recentwatch = recents,
                    error = null
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ 데이터 로드 실패", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "데이터 로드 실패"
                )
            }
        }
    }
}

@Composable
fun RecentViewedRoute(nav: NavController,viewModel: RecentWatchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {

    //var all by remember { mutableStateOf(RecentFakeDb.items()) }
    var selectedTab by remember { mutableStateOf(0) }

    val client = LocalSupabase.current
    val repo = remember { RecentWatchSupabase(client) }
    LaunchedEffect(Unit) {
        viewModel.loadUserData(CurrentUser.username,repo)
    }

    val uiState by viewModel.uiState.collectAsState()
    var all = uiState.Recentwatch

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
