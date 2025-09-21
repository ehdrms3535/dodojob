package com.example.dodojob.ui.feature.welfare

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import com.example.dodojob.ui.feature.profile.BottomNavBar

/* ------------------ 팔레트/토큰 ------------------ */
private val ScreenBg = Color(0xFFF1F5F7)
private val BrandBlue = Color(0xFF005FFF)
private val BorderGray = Color(0xFFC1D2ED)
private val TextGray = Color(0xFF9C9C9C)

/* ------------------ 데이터 모델 ------------------ */
private enum class StatusKind { REVIEW, APPROVED, PENDING }
private enum class Filter { ALL, PENDING, APPROVED }

private data class WelfareItem(
    val date: String,
    val title: String,
    val status: StatusKind,
    val progress: Int
)

/* ------------------ Route ------------------ */
@Composable
fun WelfareHomeRoute(
    nav: NavController,
    userName: String = "홍길동"
) {
    WelfareHomeScreen(
        userName = userName,
        onBottomClick = { key ->
            when (key) {
                "home"      -> nav.navigate("main") { launchSingleTop = true }
                "edu"       -> nav.navigate("edu") { launchSingleTop = true }
                "welfare"   -> {}
                "community" -> nav.navigate("community") { launchSingleTop = true }
                "my"        -> nav.navigate(Route.My.path) { launchSingleTop = true }
            }
        },
        bottomBar = {
            BottomNavBar(current = "welfare", onClick = { /* 라벨 클릭 처리만 */ })
        },
        onCardClick = { /* 상세로 이동 예정 */ }
    )
}

/* ------------------ Screen ------------------ */
@Composable
fun WelfareHomeScreen(
    userName: String,
    onBottomClick: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
    onCardClick: () -> Unit
) {
    // 샘플 데이터 (서버 연동 시 치환)
    val allItems = remember {
        listOf(
            WelfareItem("2025.08.25 지원", "건강검진 지원 프로그램", StatusKind.REVIEW,    75),
            WelfareItem("2025.08.25 지원", "온라인 취미 강좌 (수강권 지원)", StatusKind.APPROVED, 100),
            WelfareItem("2025.08.25 지원", "돌봄 서비스 (가정 방문 지원)", StatusKind.PENDING,    5)
        )
    }
    var selectedFilter by remember { mutableStateOf(Filter.ALL) }

    val filteredItems = remember(selectedFilter, allItems) {
        when (selectedFilter) {
            Filter.ALL      -> allItems
            Filter.PENDING  -> allItems.filter { it.status == StatusKind.PENDING }
            Filter.APPROVED -> allItems.filter { it.status == StatusKind.APPROVED }
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = bottomBar,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)                // 앱바 높이 (원하면 조절)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 로고/벨 사이즈는 네가 직접 size(dp)로 조절
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "앱 로고",
                    modifier = Modifier.size(29.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { /* 알림센터 이동 */ }) {
                    Image(
                        painter = painterResource(id = R.drawable.bell),
                        contentDescription = "알림",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 인사 헤더
            GreetingHeader(userName)

            Spacer(Modifier.height(12.dp))

            // 검색창
            SearchBox(
                placeholder = "원하는 복지를 검색해보세요",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // 카테고리 2 버튼
            CategoryButtons(
                leftImage = R.drawable.health_manage_button,
                rightImage = R.drawable.leisure_education_button,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )

            // 건강관리 ↔ 신청현황 간격
            Spacer(Modifier.height(20.dp))

            // 신청 현황 + 필터 칩
            ApplicationSummary(
                total = allItems.size,                // 총 건수는 전체 기준 (필터 기준으로 바꾸려면 filteredItems.size)
                selected = selectedFilter,
                onSelect = { selectedFilter = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // 카드 리스트
            Spacer(Modifier.height(10.dp))
            filteredItems.forEach { item ->
                WelfareCard(
                    date = item.date,
                    title = item.title,
                    statusText = when (item.status) {
                        StatusKind.REVIEW   -> "심사중"
                        StatusKind.APPROVED -> "승인"
                        StatusKind.PENDING  -> "접수대기"
                    },
                    statusKind = item.status,
                    progressPercent = item.progress,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 10.dp),
                    onClick = onCardClick
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ------------------ Greeting ------------------ */
@Composable
private fun GreetingHeader(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${userName}님\n어떤 복지를 찾으세요?",
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            lineHeight = 45.sp
        )
    }
}

/* ------------------ Search Box ------------------ */
@Composable
private fun SearchBox(
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var value by remember { mutableStateOf(TextFieldValue("")) }

    Box(
        modifier = modifier
            .height(57.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "검색",
                tint = Color(0xFF62626D),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            if (value.text.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color(0xFF9C9C9C),
                    fontSize = 16.sp
                )
            }
        }
        BasicTextField(
            value = value,
            onValueChange = { value = it },
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
            modifier = Modifier
                .matchParentSize()
                .padding(start = 48.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
        )
    }
}

/* ------------------ Category Buttons ------------------ */
@Composable
private fun CategoryButtons(
    @DrawableRes leftImage: Int,
    @DrawableRes rightImage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryImageButton(resId = leftImage, onClick = { /* 건강관리 이동 */ }, modifier = Modifier.weight(1f))
        CategoryImageButton(resId = rightImage, onClick = { /* 여가/교육 이동 */ }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CategoryImageButton(
    @DrawableRes resId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(67.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = null
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/* ------------------ Summary + Filter Chips ------------------ */
@Composable
private fun ApplicationSummary(
    total: Int,
    selected: Filter,
    onSelect: (Filter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // "총 n건 신청" (n은 전체 건수)
        Text(
            text = buildAnnotatedString {
                append("총 ")
                withStyle(SpanStyle(color = BrandBlue, fontWeight = FontWeight.SemiBold)) {
                    append("${total}건")
                }
                append(" 신청")
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        // 버튼: 전체, 접수대기, 승인
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChipPill(
                text = "전체",
                selected = (selected == Filter.ALL),
                onClick = { onSelect(Filter.ALL) }
            )
            FilterChipPill(
                text = "접수대기",
                selected = (selected == Filter.PENDING),
                onClick = { onSelect(Filter.PENDING) }
            )
            FilterChipPill(
                text = "승인",
                selected = (selected == Filter.APPROVED),
                onClick = { onSelect(Filter.APPROVED) }
            )
        }
    }
}

@Composable
private fun FilterChipPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) BrandBlue else Color.White
    val fg = if (selected) Color.White else Color.Black
    val border = if (selected) null else BorderStroke(1.dp, Color(0xFFD1D1D1))

    Surface(
        modifier = Modifier.height(34.dp),
        shape = RoundedCornerShape(24.dp),
        color = bg,
        shadowElevation = 0.dp,
        border = border,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = fg,
                fontSize = 18.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/* ------------------ Welfare Card ------------------ */
@Composable
private fun WelfareCard(
    date: String,
    title: String,
    statusText: String,
    statusKind: StatusKind,
    progressPercent: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 27.dp, vertical = 20.dp)) {
            // 날짜
            Text(
                text = date,
                fontSize = 13.sp,
                color = Color(0xFF848484)
            )
            Spacer(Modifier.height(6.dp))
            // 타이틀 + 상태뱃지
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StatusBadge(text = statusText, kind = statusKind)
            }
            // 진행률 바
            Spacer(Modifier.height(14.dp))
            ProgressLine(percent = progressPercent)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "진행률 (${progressPercent}%)",
                fontSize = 18.sp,
                color = TextGray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Right
            )
        }
    }
}

@Composable
private fun StatusBadge(text: String, kind: StatusKind) {
    val (bg, fg) = when (kind) {
        StatusKind.REVIEW   -> Color(0xFFFFEBE6) to Color(0xFFFF2F00)
        StatusKind.APPROVED -> Color(0xFFDEFFE1) to Color(0xFF1E7428)
        StatusKind.PENDING  -> Color(0xFFEFEFEF) to Color(0xFFA6A6A6)
    }
    Box(
        modifier = Modifier
            .width(85.dp)                         // 고정 폭 pill
            .clip(RoundedCornerShape(31.dp))
            .background(bg)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun ProgressLine(percent: Int) {
    val p = percent.coerceIn(0, 100) / 100f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFFEEF2F4))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(p)
                .background(BrandBlue)
        )
    }
}

/* ------------------ Preview ------------------ */
@Preview(showBackground = true, showSystemUi = true, name = "Welfare Home")
@Composable
private fun PreviewWelfareHome() {
    val nav = rememberNavController()
    WelfareHomeRoute(nav = nav, userName = "홍길동")
}
