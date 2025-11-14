package com.example.dodojob.ui.feature.main

import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.example.dodojob.R
import com.example.dodojob.dao.fetchDisplayNameByUsername
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.data.user.UserRepository
import com.example.dodojob.data.user.UserRepositorySupabase
import com.example.dodojob.navigation.Route
import com.example.dodojob.session.CurrentUser
import kotlinx.coroutines.delay
import com.example.dodojob.dao.getCompanyIdByUsername
import com.example.dodojob.dao.getCompanyRowCount
import com.example.dodojob.dao.getannounce
import com.example.dodojob.dao.getannounce24
import com.example.dodojob.dao.getRecentApplicantsByCompany
import com.example.dodojob.data.career.CareerRepositoryImpl
import com.example.dodojob.dao.http
import android.content.Context
import java.time.LocalDate

/* ================= Colors ================= */
private val ScreenBg  = Color(0xFFF1F5F7)
private val BrandBlue = Color(0xFF005FFF)
private val TextGray  = Color(0xFF828282)
private val LineGray  = Color(0xFFDDDDDD)
private val White     = Color(0xFFFFFFFF)

/* ================= Layout Const ================= */
// StatCard의 number/subtitle을 아이콘 바로 아래에서 시작시키는 고정 패딩
private val STATCARD_TEXT_START = 20.dp   // 아이콘(24) + 간격(5) + 여유(15) ≈ 44

/* ================= Fake DB ================= */
object FakeEmployerRepo {

    val employerName = "홍길동"

    data class DashboardStats(
        val newApplicantsToday: Int,   // 신규 지원자 수
        val unreadResumes: Int,        // 미열람 이력서 수
        val activeNotices: Int         // 진행 중 공고 수
    )

    data class Applicant(
        val name: String,
        val jobTitle: String,          // 지원 직종
        val experience: String,        // 경력
        val location: String,          // 사는 곳
        val appliedHoursAgo: Int,      // 몇 시간 전 지원
        val medalRes: Int,             // 메달 리소스
        val age: Int                   // 나이
    )

    fun getDashboardStats(): DashboardStats =
        DashboardStats(
            newApplicantsToday = 7,
            unreadResumes = 12,
            activeNotices = 3
        )

    fun getRecentApplicants(): List<Applicant> {
        val commonJob = "매장 매니저"
        return listOf(
            Applicant("홍길동", commonJob, "경력 3년", "서울", 2, R.drawable.blue_medal,   age = 29),
            Applicant("김철수", commonJob, "경력 1년", "부산", 5, R.drawable.red_medal,    age = 25),
            Applicant("이영희", commonJob, "신입",     "대구", 9, R.drawable.yellow_medal, age = 23),
        )
    }
}

/* ================= Data for UI ================= */
data class ApplicantUi(
    val name: String,
    val jobTitle: String,
    val experience: String,
    val location: String,
    val appliedHoursAgo: Int,
    val medalRes: Int,
    val age: Int
)

/* ---------- 광고 배너 데이터 ---------- */
data class EmployerAdBanner(
    @DrawableRes val imageRes: Int,
    val onClick: () -> Unit = {}
)

/* ---------- 광고(자동 회전) ---------- */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmployerAutoRotatingAd(
    banners: List<EmployerAdBanner>,
    autoIntervalMs: Long = 5_000L,
) {
    val realCount = banners.size

    // 배너가 없으면 플레이스홀더
    if (realCount == 0) {
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(154.dp),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A77FF))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "광고가 없습니다.",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { realCount })

    // 자동 넘김
    LaunchedEffect(realCount, autoIntervalMs) {
        while (true) {
            delay(autoIntervalMs)
            val next = (pagerState.currentPage + 1) % realCount
            pagerState.animateScrollToPage(next)
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(154.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            HorizontalPager(state = pagerState) { page ->
                val banner = banners[page]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { banner.onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = banner.imageRes),
                        contentDescription = "ad_banner_$page",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // 인디케이터
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val current = pagerState.currentPage
            repeat(realCount) { i ->
                val isSelected = i == current
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isSelected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) BrandBlue else LineGray)
                )
            }
        }
    }
}

/* ================= Route Entry ================= */
@Composable
fun EmployerHomeRoute(nav: NavController) {
    val stats = remember { FakeEmployerRepo.getDashboardStats() }
    var user by remember { mutableStateOf<String?>(null) }
    val client = LocalSupabase.current
    val repo: UserRepository = remember(client) { UserRepositorySupabase(client) }

    var newApplicantsToday by remember { mutableStateOf(0)}
    var unreadResumes by remember { mutableStateOf(0)}
    var activeNotices by remember { mutableStateOf(0) }

    val currentuser = CurrentUser.username
    var applicantsUi by remember { mutableStateOf<List<ApplicantUi>>(emptyList()) }

    val careerRepo = remember(client) { CareerRepositoryImpl(client) }

    LaunchedEffect(currentuser)
    {
        user = fetchDisplayNameByUsername(currentuser)
        val companyId = getCompanyIdByUsername(currentuser)
        activeNotices = getCompanyRowCount(currentuser)
        newApplicantsToday = getannounce24(currentuser)
        unreadResumes = getannounce(currentuser)
        applicantsUi = getRecentApplicantsByCompany(
            username = currentuser,
            careerRepo = careerRepo,
            http = http
        )
    }

    // 광고 배너들
    val adBanners = remember {
        listOf(
            EmployerAdBanner(R.drawable.employeradvertisement1) {
                nav.navigate("employer/ad/1")
            },
            EmployerAdBanner(R.drawable.employeradvertisement2) {
                nav.navigate("employer/ad/2")
            },
            EmployerAdBanner(R.drawable.employeradvertisement3) {
                nav.navigate("employer/ad/3")
            },
        )
    }

    // 🔹 팝업 노출 여부: SharedPreferences 기반
    val context = LocalContext.current
    var showPopup by remember {
        mutableStateOf(shouldShowEmployerPopup(context))
    }

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            EmployerBottomNavBar(
                current = "home",
                onClick = { key ->
                    when (key) {
                        "home"      -> nav.safeNavigate(Route.EmployerHome.path)
                        "notice"    -> nav.safeNavigate(Route.EmployerNotice.path)
                        "applicant" -> nav.safeNavigate(Route.EmployerApplicant.path)
                        "human_resource" -> nav.safeNavigate(Route.EmployerHumanResource.path)
                        "my"        -> nav.safeNavigate(Route.EmployerMy.path)
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
            /* 0) 스크롤되는 헤더 */
            item {
                ScrollHeaderRow(
                    barBgColor   = ScreenBg,
                    chipBgColor  = ScreenBg,
                    iconTintBlue = BrandBlue,
                    onLogoClick  = { nav.safeNavigate(Route.EmployerHome.path) },
                    onNotifyClick= { /* 알림 라우트 연결 시 교체 */ }
                )
            }

            /* 인사말: 이름만 파란색 */
            item {
                val employerName = user
                Text(
                    text = buildAnnotatedString {
                        append("안녕하세요, ")
                        withStyle(
                            style = SpanStyle(
                                color = BrandBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) { append(employerName) }
                        append(" 담당자님")
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    lineHeight = 40.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 4.dp)
                )
            }

            /* 1) 카드 3개 */
            item {
                StatCard(
                    leading = { SmallIconBox(resId = R.drawable.new_applicant, contentDescription = "신규 지원자") },
                    title = "신규 지원자",
                    number = newApplicantsToday.toString(),
                    subtitle = "오늘 ${newApplicantsToday}명이 지원했습니다.",
                    onClickChevron = {}
                )
            }
            item {
                StatCard(
                    leading = { SmallIconBox(resId = R.drawable.unread_resume, contentDescription = "미열람 이력서") },
                    title = "미열람 이력서",
                    number = unreadResumes.toString(),
                    subtitle = "총 ${unreadResumes}개의 이력서를 확인해보세요",
                    onClickChevron = {}
                )
            }
            item {
                StatCard(
                    leading = { SmallIconBox(resId = R.drawable.processing_announ, contentDescription = "진행 중인 공고") },
                    title = "진행 중인 공고",
                    number = activeNotices.toString(),
                    subtitle = "현재 ${activeNotices}개의 공고가 진행 중입니다",
                    onClickChevron = {}
                )
            }

            /* 2) 공고등록 버튼 */
            item {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    PrimaryButton(
                        text = "공고등록 하기",
                        height = 43.dp,
                        onClick = { nav.safeNavigate(Route.Announcement.path) }
                    )
                }
            }

            /* 2.5) 광고 배너 */
            item {
                EmployerAutoRotatingAd(banners = adBanners, autoIntervalMs = 5_000L)
            }

            /* 3) 최근 지원자 리스트 카드 */
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Spacer(Modifier.height(20.dp)) // 제목 위 여백

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "최근 지원자",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { nav.safeNavigate(Route.EmployerApplicant.path) }
                            ) {
                                Text(
                                    "전체보기",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandBlue
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.blue_right_back),
                                    contentDescription = "blue_arrow",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp)) // 제목 아래 여백
                    }
                    ApplicantList(applicants = applicantsUi)
                }
            }
        }

        if (showPopup) {
            EmployerMainPopupDialog(
                onDismiss = { showPopup = false },
                onCloseToday = {
                    hideEmployerPopupToday(context)   // 오늘 날짜까지 숨김 저장
                    showPopup = false                // 즉시 닫기
                }
            )
        }
    }
}

/* ================= Bottom Nav ================= */
data class EmployerNavItem(
    val key: String,
    val unselectedRes: Int,
    val selectedRes: Int? = null,
    val size: Dp = 55.dp
)

@Composable
fun EmployerBottomNavBar(current: String, onClick: (String) -> Unit) {
    val items = listOf(
        EmployerNavItem("home",      R.drawable.unselected_employer_home,      R.drawable.selected_employer_home, 75.dp),
        EmployerNavItem("notice",    R.drawable.unselected_notice,    R.drawable.selected_notice, 75.dp),
        EmployerNavItem("applicant", R.drawable.unselected_applicant, R.drawable.selected_applicant, 75.dp),
        EmployerNavItem("human_resource",R.drawable.unselected_human_resource, R.drawable.selected_human_resource, 75.dp),
        EmployerNavItem("my",        R.drawable.unselected_employer_my,        R.drawable.selected_employer_my,    75.dp),
    )

    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            val isSelected = item.key == current
            val iconRes = if (isSelected && item.selectedRes != null) item.selectedRes else item.unselectedRes

            NavigationBarItem(
                selected = isSelected,
                onClick = { onClick(item.key) },
                icon = {
                    Image(
                        painter = painterResource(id = iconRes!!),
                        contentDescription = item.key,
                        modifier = Modifier.size(item.size),
                        colorFilter = if (isSelected && item.selectedRes == null) ColorFilter.tint(BrandBlue) else null
                    )
                },
                label = null,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Color.Unspecified,
                    selectedTextColor   = Color.Unspecified,
                    unselectedIconColor = Color.Unspecified,
                    unselectedTextColor = Color.Unspecified,
                    indicatorColor      = Color.Transparent
                )
            )
        }
    }
}

/* ================= Cards & List ================= */
@Composable
private fun SmallIconBox(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFFDEEAFF)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun StatCard(
    leading: @Composable () -> Unit,
    title: String,
    number: String,
    subtitle: String,
    onClickChevron: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 15.dp, bottom = 2.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .height(27.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leading()
                Spacer(Modifier.width(5.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = painterResource(id = R.drawable.right_back),
                    contentDescription = "chevron_right",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onClickChevron() },
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = number,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandBlue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = STATCARD_TEXT_START, top = 7.dp)
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = STATCARD_TEXT_START, top = 4.dp)
            )
        }
    }
}

/** 공용 Primary 버튼 */
@Composable
fun PrimaryButton(text: String, height: Dp, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = White
        )
    }
}

/* ======= Applicants ======= */
@Composable
fun ApplicantList(applicants: List<ApplicantUi>) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
    ) {
        Separator() // 맨 위 선

        applicants.forEachIndexed { idx, ap ->
            Spacer(Modifier.height(6.dp))   // 행 위 여백
            ApplicantRow(ap)
            Spacer(Modifier.height(6.dp))   // 행 아래 여백

            if (idx != applicants.lastIndex) {
                Separator()                 // 사이사이 선
            }
        }
    }
}

@Composable
fun EmployerMainPopupDialog(
    onDismiss: () -> Unit,
    onCloseToday: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 바깥을 한 번 더 감싸서 전체 모서리도 안전하게 자르기
            Surface(
                shape = RoundedCornerShape(15.dp),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    // 상단 이미지
                    val painter = painterResource(R.drawable.employer_main_popup)
                    val ratio = remember(painter) {
                        val s = painter.intrinsicSize
                        val w = s.width; val h = s.height
                        if (w.isFinite() && h.isFinite() && h > 0f) w / h else 360f / 270f
                    }

                    Image(
                        painter = painter,
                        contentDescription = "Employer main popup",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
                            .aspectRatio(ratio)
                            .clickable {
                                val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://shiftee.io/ko"))
                                context.startActivity(i)
                            },
                        contentScale = ContentScale.Fit
                    )

                    // 하단 버튼 바
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(63.dp)
                            .background(Color.White)
                            .clip(RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp))
                            .padding(horizontal = 25.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onCloseToday) {
                            Text("오늘 그만보기", fontSize = 18.sp, color = Color(0xFF828282), fontWeight = FontWeight.Medium)
                        }
                        TextButton(onClick = onDismiss) {
                            Text("닫기", fontSize = 18.sp, color = Color(0xFF005FFF), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicantRow(ap: ApplicantUi) {

    val lineSpacing = 2.dp   // 이름 / 직무 / 경력 줄 간격 통일

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),          // 높이는 패딩으로
        verticalAlignment = Alignment.CenterVertically // 기본은 가운데
    ) {
        // 🔹 프로필 (맨 위 정렬)
        Image(
            painter = painterResource(id = R.drawable.basic_profile),
            contentDescription = "user",
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.Top)
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(lineSpacing)
        ) {
            // 1줄: 이름 + 나이 + 메달
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    ap.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(Modifier.width(4.dp))
                Text("(${ap.age}세)", fontSize = 13.sp, color = TextGray)
                Spacer(Modifier.width(2.dp))
                Image(
                    painter = painterResource(id = ap.medalRes),
                    contentDescription = "medal",
                    modifier = Modifier
                        .size(18.dp)
                        .offset(y = 1.dp)
                )
            }

            // 2줄: 직무
            Text(
                ap.jobTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextGray,
                maxLines = 1
            )

            // 3줄: 경력 / 위치 / 시간
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ap.experience, fontSize = 12.sp, color = BrandBlue)

                Spacer(Modifier.width(8.dp))
                Text("·", fontSize = 12.sp, color = TextGray)
                Spacer(Modifier.width(8.dp))

                Image(
                    painter = painterResource(id = R.drawable.location),
                    contentDescription = "location",
                    modifier = Modifier.size(12.dp),
                    colorFilter = ColorFilter.tint(TextGray)
                )
                Spacer(Modifier.width(4.dp))

                Text(ap.location, fontSize = 12.sp, color = TextGray)

                Spacer(Modifier.width(8.dp))

                Text("${ap.appliedHoursAgo}시간 전", fontSize = 12.sp, color = TextGray)
            }
        }

        Spacer(Modifier.width(12.dp))

        Image(
            painter = painterResource(id = R.drawable.right_back),
            contentDescription = "go_detail",
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(BrandBlue)
        )
    }
}

@Composable
private fun Separator() {
    Divider(
        color = LineGray,
        thickness = 1.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun HorizontalLine() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.dp)
            .border(1.dp, LineGray, RoundedCornerShape(0.dp))
    )
}

/* ================= Nav Helper ================= */
private fun NavController.safeNavigate(
    route: String,
    builder: (NavOptionsBuilder.() -> Unit)? = {
        launchSingleTop = true
        restoreState = true
    }
) {
    navigate(route) {
        builder?.invoke(this)
    }
}

/* ====== "오늘 그만보기" 상태 저장용 SharedPreferences ====== */

private const val PREFS_EMPLOYER_POPUP = "employer_home_popup_prefs"
private const val KEY_EMPLOYER_HIDE_UNTIL_EPOCH_DAY = "hide_until_epoch_day"

private fun shouldShowEmployerPopup(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_EMPLOYER_POPUP, Context.MODE_PRIVATE)
    val hideUntil = prefs.getLong(KEY_EMPLOYER_HIDE_UNTIL_EPOCH_DAY, -1L)
    if (hideUntil == -1L) return true

    val today = LocalDate.now().toEpochDay()
    return today > hideUntil
}

private fun hideEmployerPopupToday(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_EMPLOYER_POPUP, Context.MODE_PRIVATE)
    val today = LocalDate.now().toEpochDay()
    prefs.edit()
        .putLong(KEY_EMPLOYER_HIDE_UNTIL_EPOCH_DAY, today)
        .apply()
}

fun formatExperienceYearsOnly(totalMonths: Int): String {
    val years = totalMonths / 12
    return if (years == 0) "신입" else "경력 ${years}년"
}

/* ============ 리소스 체크 ============

drawable/
- logo1.png, bell.png
- user_with_circle.png
- red_medal.png, yellow_medal.png, blue_medal.png
- new_applicant.png, unread_resume.png, processing_announ.png
- unselected_home.png, selected_home.png
- unselected_notice.png, unselected_applicant.png
- unselected_my.png, selected_my.png
- ic_location.png
- employeradvertisement1.png, employeradvertisement2.png, employeradvertisement3.png

*/
