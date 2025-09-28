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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
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
import com.example.dodojob.navigation.Route

// ✅ 공용 BottomNavBar 임포트
import com.example.dodojob.ui.feature.main.EmployerBottomNavBar
// ✅ 얇은 구분선 임포트 (employ.ScrappedHumanResource.kt)
import com.example.dodojob.ui.feature.employ.ThinDivider

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

/* ================= Fake Repo (데모 데이터) ================= */
private object FakeMyRepo {
    val managerName = "홍길동 담당자님"
    data class StatRow(val title: String, val countText: String)
    fun activityStats() = listOf(
        StatRow("활성공고", "3건"),
        StatRow("총 지원자", "3건"),
        StatRow("채용완료", "1건"),
    )
}

/* ================= Entry ================= */
@Composable
fun EmployerMyRoute(nav: NavController) {
    val activityStats = remember { FakeMyRepo.activityStats() }

    // 기본 글꼴을 Pretendard SemiBold로 깔아두기
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
                /* ===== 상단 헤더 ===== */
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(White)
                    ) {
                        // 🔻 회색 빈칸(상단 24dp status bar) 제거 완료

                        // Title row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .padding(vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "마이페이지",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.019).em,
                                color = Color.Black
                            )
                        }

                        // 프로필/설정 줄
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 프로필 사진: employermyprofile
                            Image(
                                painter = painterResource(id = R.drawable.employermyprofile),
                                contentDescription = "프로필 사진",
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(Modifier.width(10.dp))

                            Column(Modifier.weight(1f)) {
                                // 이름(파랑) + 담당자님(검정)
                                val full = FakeMyRepo.managerName
                                val suffix = " 담당자님"
                                val namePart = if (full.endsWith(suffix)) full.removeSuffix(suffix) else full
                                val suffixPart = if (full.endsWith(suffix)) "담당자님" else ""
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(SpanStyle(color = BrandBlue, fontWeight = FontWeight.Bold)) {
                                                append(namePart)
                                            }
                                            if (suffixPart.isNotEmpty()) {
                                                append(" ")
                                                withStyle(SpanStyle(color = Color.Black, fontWeight = FontWeight.Bold)) {
                                                    append(suffixPart)
                                                }
                                            }
                                        },
                                        fontSize = 24.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.ChevronRight,
                                        contentDescription = null,
                                        tint = Color.Black
                                    )
                                }

                                Spacer(Modifier.height(6.dp))

                                // 기업정보 수정 (Pretendard Medium)
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

                        // 빠른 액션 4개 (라벨: Pretendard Medium)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QuickAction("공고 등록", R.drawable.register_announcement) {
                                nav.safeNavigate(Route.EmployerNotice.path)
                            }
                            QuickAction("공고 관리", R.drawable.manage_announcement) {
                                nav.safeNavigate(Route.EmployerNotice.path)
                            }
                            QuickAction("지원자 관리", R.drawable.manage_applicant) {
                                nav.safeNavigate(Route.EmployerApplicant.path)
                            }
                            QuickAction("인재관리", R.drawable.manage_resource) {
                                nav.safeNavigate(Route.EmployerHumanResource.path)
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
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "나의 활동",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                            )
                            activityStats.forEachIndexed { idx, row ->
                                ActivityRow(
                                    title = row.title,
                                    countText = row.countText,
                                    onClick = {
                                        when (row.title) {
                                            "활성공고" -> nav.safeNavigate(Route.EmployerNotice.path)
                                            "총 지원자" -> nav.safeNavigate(Route.EmployerApplicant.path)
                                            "채용완료" -> nav.safeNavigate(Route.EmployerNotice.path)
                                        }
                                    }
                                )
                                if (idx != activityStats.lastIndex) {
                                    // ThinDivider 위/아래 패딩 추가
                                    ThinDivider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                /* ===== 섹션 리스트 (기본정보 등) ===== */
                item {
                    InfoSectionList(
                        sections = listOf("기본정보", "보안설정", "알림설정", "결제 · 정산"),
                        onRowClick = { /* TODO: 각 섹션 상세 라우트 연결 */ }
                    )
                }
                item {
                    InfoSectionList(
                        sections = listOf("약관 및 정책", "고객센터", "앱 정보"),
                        onRowClick = { /* TODO: 라우트 연결 */ }
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
            fontWeight = FontWeight.Medium, // 라벨은 Medium
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
            color = BrandBlue
        )
        Spacer(Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Color.Black
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
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = TextGray
                    )
                }
                ThinDivider(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                )
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
