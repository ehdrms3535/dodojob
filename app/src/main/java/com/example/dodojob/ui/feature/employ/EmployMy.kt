package com.example.dodojob.ui.feature.employ

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.example.dodojob.R
import com.example.dodojob.dao.fetchDisplayNameByUsername
import com.example.dodojob.dao.fetchEmployerStatsByUsername
import com.example.dodojob.navigation.Route
import com.example.dodojob.session.CurrentUser
import com.example.dodojob.ui.feature.main.EmployerBottomNavBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

/* ================= Font ================= */
private val PretendardFamily = FontFamily(
    Font(R.font.pretendard_medium,  FontWeight.Medium),
    Font(R.font.pretendard_semibold,FontWeight.SemiBold),
    Font(R.font.pretendard_bold,    FontWeight.Bold),
)

/* ================= Colors ================= */
private val ScreenBg  = Color(0xFFF1F5F7)
private val BrandBlue = Color(0xFF005FFF)
private val TextGray  = Color(0xFF828282)
private val White     = Color(0xFFFFFFFF)

/* ============ Divider ============ */
@Composable
private fun ThinDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 0.3.dp,
        color = Color(0xFF969696)
    )
}

/* ================= Entry ================= */
@Composable
fun EmployerMyRoute(nav: NavController) {

    val username = CurrentUser.username

    var managerDisplay by remember { mutableStateOf("담당자님") }
    var statActive by remember { mutableStateOf(0) }
    var statApplicants by remember { mutableStateOf(0) }
    var statCompleted by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(username) {
        managerDisplay = "담당자님"
        statActive = 0; statApplicants = 0; statCompleted = 0

        val u = username
        if (!u.isNullOrBlank()) {
            val (name, stats) = withContext(Dispatchers.IO) {
                val nm = runCatching { fetchDisplayNameByUsername(u) }.getOrNull()
                val st = runCatching { fetchEmployerStatsByUsername(u) }.getOrNull()
                nm to st
            }
            managerDisplay = (name?.takeIf { it.isNotBlank() }?.plus(" 담당자님")) ?: "담당자님"
            stats?.let {
                statActive = it.activeCount
                statApplicants = it.applicantsCount
                statCompleted = it.completedCount
            }
        }
    }

    CompositionLocalProvider(
        LocalTextStyle provides TextStyle(
            fontFamily = PretendardFamily,
            fontWeight = FontWeight.SemiBold
        )
    ) {
        Scaffold(
            containerColor = ScreenBg,
            bottomBar = {
                EmployerBottomNavBar(
                    current = "my",
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
                /* ===== 상단 헤더 + 담당자 카드 ===== */
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(White)
                    ) {

                        // 타이틀 영역 (72dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "마이페이지",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.019).em,
                                color = Color.Black
                            )
                        }

                        // 담당자 + 빠른 액션 카드 (Frame 1707480277 느낌)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 25.dp),
                            verticalArrangement = Arrangement.spacedBy(30.dp)
                        ) {
                            // 프로필 + 이름 + 기업정보 수정
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        nav.navigate(Route.EditEmployerInformation.path)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.employermyprofile),
                                    contentDescription = "프로필 사진",
                                    modifier = Modifier
                                        .offset(y = (-10).dp)
                                        .size(38.dp)
                                        .clip(CircleShape)
                                )

                                Spacer(Modifier.width(10.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // 이름(파랑) + 담당자님(검정)
                                    val full = managerDisplay
                                    val suffix = " 담당자님"
                                    val namePart = if (full.endsWith(suffix)) full.removeSuffix(suffix) else full
                                    val suffixPart = if (full.endsWith(suffix)) "담당자님" else ""

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(
                                                    SpanStyle(
                                                        color = BrandBlue,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                ) {
                                                    append(namePart)
                                                }
                                                if (suffixPart.isNotEmpty()) {
                                                    append(" ")
                                                    withStyle(
                                                        SpanStyle(
                                                            color = Color.Black,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    ) {
                                                        append(suffixPart)
                                                    }
                                                }
                                            },
                                            fontSize = 24.sp,
                                            letterSpacing = (-0.5).sp,
                                            lineHeight = 32.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Image(
                                            painter = painterResource(id = R.drawable.right_back),
                                            contentDescription = "detail",
                                            modifier = Modifier.size(24.dp),
                                            colorFilter = ColorFilter.tint(Color.Black)
                                        )
                                    }

                                    Spacer(Modifier.height(6.dp))

                                    // 기업정보 수정
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = R.drawable.setting),
                                            contentDescription = "setting",
                                            modifier = Modifier.size(16.dp),
                                            colorFilter = ColorFilter.tint(Color.Black)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "기업정보 수정",
                                            fontSize = 16.sp,
                                            fontFamily = PretendardFamily,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = (-0.5).sp,
                                            color = Color(0xFF787878)
                                        )
                                    }
                                }
                            }

                            // 빠른 액션 4개 (공고 등록 / 공고 관리 / 지원자 관리 / 인재관리)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                QuickAction("공고 등록", R.drawable.register_announcement) {
                                    nav.safeNavigate(Route.Announcement.path)
                                }
                                QuickAction("공고 관리", R.drawable.manage_announcement) {
                                    nav.safeNavigate(Route.EmployerNotice.path)
                                }
                                QuickAction("지원자 관리", R.drawable.manage_applicant) {
                                    nav.safeNavigate(Route.EmployerApplicant.path)
                                }
                                QuickAction("인재관리", R.drawable.manage_resource) {
                                    nav.safeNavigate(Route.ScrrapedHumanResource.path)
                                }
                            }
                        }
                    }
                }

                /* ===== 나의 활동 카드 ===== */
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 16.dp, bottom = 20.dp)
                        ) {
                            Text(
                                text = "나의 활동",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.019).em,
                                color = Color.Black,
                                modifier = Modifier
                                    .padding(top = 20.dp, bottom = 20.dp)
                            )

                            val stats = listOf(
                                "활성공고" to "${statActive}건",
                                "총 지원자" to "${statApplicants}건",
                                "채용완료" to "${statCompleted}건",
                            )

                            stats.forEachIndexed { idx, (title, countText) ->
                                ActivityRow(
                                    title = title,
                                    countText = countText,
                                    onClick = {
                                        nav.safeNavigate(Route.EmployerApplicant.path)
                                    }
                                )
                                if (idx < stats.lastIndex) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }

                /* ===== 섹션 리스트 1 ===== */
                item {
                    InfoSectionList(
                        sections = listOf("기본정보", "인증 및 보안", "알림 설정", "기업 인증"),
                        onRowClick = { /* TODO: 화면 이동 */ }
                    )
                }

                /* ===== 섹션 리스트 2 ===== */
                item {
                    InfoSectionList(
                        sections = listOf("공지 사항", "도움말 & 문의", "로그아웃"),
                        onRowClick = { idx ->
                            when (idx) {
                                0 -> { /* 공지 사항 화면 이동 */ }
                                1 -> { /* 도움말 & 문의 화면 이동 */ }
                                2 -> {
                                    if (busy) return@InfoSectionList
                                    busy = true
                                    scope.launch {
                                        runCatching {
                                            CurrentUser.clear()
                                        }

                                        nav.navigate("prelogin") {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                        busy = false
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

/* ============ 빠른 액션 ============ */
@Composable
private fun QuickAction(
    label: String,
    @DrawableRes res: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(82.dp)
            .height(45.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = res),
            contentDescription = label,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 13.sp,
            lineHeight = 23.sp,
            letterSpacing = (-0.5).sp,
            fontFamily = PretendardFamily,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF262626),
            textAlign = TextAlign.Center
        )
    }
}

/* ============ 활동 한 줄 ============ */
@Composable
private fun ActivityRow(
    title: String,
    countText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(30.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.019).em,
            color = Color(0xFF9C9C9C),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = countText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.019).em,
            color = BrandBlue
        )
        Spacer(Modifier.width(6.dp))
        Image(
            painter = painterResource(id = R.drawable.right_back),
            contentDescription = "detail",
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(Color.Black)
        )
    }
}

/* ============ 섹션 리스트 ============ */
@Composable
private fun InfoSectionList(
    sections: List<String>,
    onRowClick: (index: Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
    ) {
        sections.forEachIndexed { index, title ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .padding(horizontal = 16.dp)
                        .clickable { onRowClick(index) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.right_back),
                        contentDescription = "setting",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                }
                if (index < sections.lastIndex) {
                    ThinDivider(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

/* ============ 네비게이션 헬퍼 ============ */
private fun NavController.safeNavigate(
    route: String,
    builder: (NavOptionsBuilder.() -> Unit)? = {
        launchSingleTop = true
        restoreState = true
    }
) {
    navigate(route) { builder?.invoke(this) }
}
