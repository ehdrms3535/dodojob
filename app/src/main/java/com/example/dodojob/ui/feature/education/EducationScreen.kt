@file:OptIn(ExperimentalFoundationApi::class)
package com.example.dodojob.ui.feature.education

import java.time.DayOfWeek
import java.time.LocalDate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import com.example.dodojob.ui.components.AppBottomBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.dodojob.dao.fetchLectures
import com.example.dodojob.dao.LectureRow
import com.example.dodojob.dao.fetchDisplayNameByUsername
import com.example.dodojob.dao.fetchJobtypeByUsername
import com.example.dodojob.dao.buildInterestVectors
import com.example.dodojob.dao.recommendCoursesForUser
import com.example.dodojob.dao.InterestVectors
import com.example.dodojob.dao.fetchFavoriteIdsForUser
import com.example.dodojob.dao.setFavoriteForUser

/* ========================= Colors ========================= */
private val ScreenBg   = Color(0xFFF1F5F7)
private val TitleBlack = Color(0xFF000000)
private val BrandBlue  = Color(0xFF005FFF)

/* ========================= Data ========================= */
data class Course(
    val id: Long,
    val title: String,
    val tag: String,
    val sub: String,
    val imageUrl: String? = null,
    @DrawableRes val imageRes: Int? = null,
    val videoUrl: String? = null
)

private fun LectureRow.toCourse(): Course = Course(
    id = id,
    title = title.orEmpty(),
    tag = category.orEmpty(),
    sub = explain.orEmpty(),
    imageUrl = thumbnail,
    videoUrl = url
)

private val filterTabs = listOf("전체", "영어", "컴퓨터", "요리", "교육", "응대", "기타")

fun recommendedCourses() = listOf(
    Course(-101L, "영어 회화 입문", "영어", "일상 표현부터 차근차근", imageRes = R.drawable.edu_recom1),
    Course(-102L, "컴퓨터 기초 마스터", "컴퓨터", "문서·인터넷·이메일 한 번에", imageRes = R.drawable.edu_recom2),
    Course(-103L, "집에서 즐기는 홈쿠킹", "요리", "기초 재료 손질과 간단 레시피", imageRes = R.drawable.edu_recom3),
    Course(-104L, "그룹 스터디 튜터링", "교육", "주 1회 온라인 그룹 학습", imageRes = R.drawable.edu_recom4),
)

fun liveHotCoursesFallback() = listOf(
    Course(-201L, "고객 응대 스킬", "응대", "전화·대면 응대 기본", imageRes = R.drawable.edu_live1),
    Course(-202L, "스마트폰 200% 활용", "컴퓨터", "결제·사진·앱 활용 전반", imageRes = R.drawable.edu_live2),
    Course(-203L, "물감과 친해지는 수채화", "기타", "기초 드로잉과 색감 연습", imageRes = R.drawable.edu_live3),
    Course(-204L, "영어 뉴스 리스닝", "영어", "쉬운 뉴스로 리스닝 감 만들기", imageRes = R.drawable.edu_live4),
)

private fun pickLiveHotFromDb(dbCourses: List<Course>): List<Course> {
    if (dbCourses.isEmpty()) return liveHotCoursesFallback()
    val ids = dbCourses.map { it.id }.shuffled().take(3)
    val picked = dbCourses.filter { it.id in ids }
    return picked.ifEmpty { liveHotCoursesFallback() }
}

/* ========================= Entry ========================= */
@Composable
fun EducationHomeRoute(
    nav: NavController,
    userName: String? = null
) {
    // 바텀바 탭 클릭 시 네비게이션 콜백
    val handleBottomClick: (String) -> Unit = { key ->
        when (key) {
            "home" -> nav.navigate(Route.Main.path) { launchSingleTop = true }
            "edu"  -> { /* 현재 화면 */ }
            "welfare" -> nav.navigate("welfare/home") { launchSingleTop = true }
            "my"   -> nav.navigate(Route.My.path) { launchSingleTop = true }
        }
    }

    EducationHomeScreen(
        userName = userName,
        onCourseClick = { c ->
            nav.currentBackStackEntry?.savedStateHandle?.set(
                "lec_payload",
                LecturePayload(
                    lectureId = c.id,
                    title     = c.title,
                    subtitle  = c.sub,
                    thumbnail = c.imageUrl,
                    videoUrl  = c.videoUrl
                )
            )
            nav.navigate(Route.EduLectureInitial.of(c.id.toString()))
        },
        onOpenLibrary = { nav.navigate(Route.EduMy.path) },
        onBottomClick = handleBottomClick       // ✅ bottomBar 대신 콜백만 내려보냄
    )
}

/* ========================= Screen ========================= */
@Composable
fun EducationHomeScreen(
    userName: String?,
    onCourseClick: (Course) -> Unit,
    onOpenLibrary: () -> Unit,
    onBottomClick: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    // 닉네임
    var displayName by remember { mutableStateOf("회원") }
    LaunchedEffect(userName) {
        displayName = if (!userName.isNullOrBlank()) {
            try { withContext(Dispatchers.IO) { fetchDisplayNameByUsername(userName) } ?: userName }
            catch (_: Exception) { userName }
        } else "회원"
    }

    // 필터/강의 목록
    var pickedFilter by remember { mutableStateOf("전체") }
    var supaCourses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val recomLocal = remember { recommendedCourses() }

    LaunchedEffect(Unit) {
        loading = true; error = null
        try {
            val rows = withContext(Dispatchers.IO) { fetchLectures(limit = 30) }
            supaCourses = rows.map { it.toCourse() }
        } catch (e: Exception) {
            error = e.message
        } finally { loading = false }
    }

    fun List<Course>.applyFilter() =
        if (pickedFilter == "전체") this else filter { it.tag == pickedFilter }

    val heroCourse: Course? = (if (supaCourses.isNotEmpty()) supaCourses else recomLocal).firstOrNull()

    // 선호벡터
    var vectors by remember { mutableStateOf<InterestVectors?>(null) }
    var loadingVectors by remember { mutableStateOf(false) }
    LaunchedEffect(userName) {
        if (!userName.isNullOrBlank()) {
            loadingVectors = true
            try {
                val job = withContext(Dispatchers.IO) { fetchJobtypeByUsername(userName) }
                vectors = job?.let {
                    buildInterestVectors(
                        talentBits = it.job_talent,
                        serviceBits = it.job_service,
                        manageBits  = it.job_manage,
                        careBits    = it.job_care
                    )
                }
            } catch (_: Exception) {
                vectors = null
            } finally { loadingVectors = false }
        } else vectors = null
    }

    // ✅ ID 기반 즐겨찾기
    var favoriteIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var favLoading by remember { mutableStateOf(false) }

    // 최초 로드: 즐겨찾기 ID들
    LaunchedEffect(userName) {
        if (!userName.isNullOrBlank()) {
            favLoading = true
            try {
                favoriteIds = withContext(Dispatchers.IO) { fetchFavoriteIdsForUser(userName) }
            } catch (_: Exception) {
                favoriteIds = emptySet()
            } finally { favLoading = false }
        } else favoriteIds = emptySet()
    }

    // 토글 핸들러 (낙관적 UI → 서버 업서트 → 실패 시 롤백)
    fun toggleFavorite(course: Course, newFav: Boolean) {
        val before = favoriteIds
        val after = if (newFav) before + course.id else before - course.id
        favoriteIds = after
        if (!userName.isNullOrBlank()) {
            scope.launch {
                val ok = setFavoriteForUser(userName, course.id, newFav)
                if (!ok) favoriteIds = before // 롤백
            }
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            AppBottomBar(
                current = "edu",          // ✅ 교육 탭 활성
                onClick = onBottomClick   // "home","edu","welfare","my" 넘어옴
            )
        },
        topBar = { Spacer(modifier = Modifier.fillMaxWidth().statusBarsPadding()) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ===== Hero =====
            HeroSection(
                userName = displayName,
                heroImageRes = R.drawable.edu_recom4,
                onBellClick = { /* TODO */ },
                onProfileClick = onOpenLibrary,
                heroCourse = heroCourse,
            )

            // ===== 검색/필터 =====
            Spacer(Modifier.height(18.dp))
            SearchBar(Modifier.padding(horizontal = 16.dp).fillMaxWidth())

            Spacer(Modifier.height(14.dp))
            FilterRow(
                tabs = filterTabs,
                picked = pickedFilter,
                onPick = { pickedFilter = it },
                modifier = Modifier.padding(start = 16.dp),
                chipWidth = 76.dp,
                chipHeight = 34.dp
            )

            Spacer(Modifier.height(28.dp))

            AttendanceCard(
                userName = displayName,
                modifier = Modifier.padding(horizontal = 16.dp),
                onMyCourseClick = onOpenLibrary
            )

            // ===== 추천 섹션 =====
            Spacer(Modifier.height(16.dp))
            SectionTitle(
                text = "${displayName}님을 위한 추천 강의",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
            Spacer(Modifier.height(12.dp))

            val basePool = (if (supaCourses.isNotEmpty()) supaCourses else recomLocal).applyFilter()
            val recommendedList: List<Course> =
                vectors?.let { recommendCoursesForUser(basePool, it, topN = 3) } ?: basePool

            if ((loading && supaCourses.isEmpty()) || loadingVectors || favLoading) {
                Text("불러오는 중...", modifier = Modifier.padding(horizontal = 16.dp))
            } else if (error != null && supaCourses.isEmpty()) {
                Text("로드 실패: $error", color = Color.Red, modifier = Modifier.padding(horizontal = 16.dp))
            } else {
                CourseCarousel(
                    courses = recommendedList,
                    favIds = favoriteIds,
                    onToggleFav = ::toggleFavorite,
                    onClick = onCourseClick,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            // ===== 실시간 인기 =====
            Spacer(Modifier.height(24.dp))
            SectionTitle("실시간 인기 강의", modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
            Spacer(Modifier.height(12.dp))

            val liveHotDb = (if (supaCourses.isNotEmpty()) pickLiveHotFromDb(supaCourses) else liveHotCoursesFallback())
                .applyFilter()

            CourseCarousel(
                courses = liveHotDb,
                favIds = favoriteIds,
                onToggleFav = ::toggleFavorite,
                onClick = onCourseClick,
                modifier = Modifier.padding(start = 16.dp, bottom = 24.dp)
            )
        }
    }
}

/* ========================= Pieces ========================= */
@Composable
private fun HeroSection(
    userName: String?,
    @DrawableRes heroImageRes: Int,
    onBellClick: () -> Unit,
    onProfileClick: () -> Unit,
    heroCourse: Course? = null,
    topBarHorizontal: Dp = 16.dp,
    topBarTop: Dp = 12.dp,
    logoSize: Dp = 29.dp,
    rightIconSize: Dp = 26.dp,
    contentHorizontal: Dp = 16.dp,
    contentBottom: Dp = 18.dp,
    titleSpacing: Dp = 8.dp,
    linesSpacingSmall: Dp = 2.dp,
    titleMaxLines: Int = 1,
    headlineMaxLines: Int = 1,
    metaMaxLines: Int = 1,
    descMaxLines: Int = 2,
    titleVerticalOffset: Dp = 60.dp,
    headlineVerticalOffset: Dp = 16.dp
) {
    val headline = heroCourse?.title ?: "외국인 친구와 소통하는 즐거움, 온라인 한국어 회화"
    val meta     = heroCourse?.let { "${it.tag.ifBlank { "기타" }} | ${it.id}" } ?: "언어·문화 | 세종학당재단"
    val desc     = heroCourse?.sub ?: "실생활 중심 대화 연습으로 자연스러운 회화"

    Box(Modifier.fillMaxWidth().height(320.dp)) {
        Image(
            painter = painterResource(heroImageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(Modifier.matchParentSize().background(Color(0xB3000000)))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = topBarHorizontal, vertical = topBarTop),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painterResource(R.drawable.white_logo), "logo", Modifier.size(logoSize))
            Spacer(Modifier.weight(1f))
            Image(painterResource(R.drawable.white_bell), "알림",
                Modifier.size(rightIconSize).clickable { onBellClick() })
            Spacer(Modifier.width(10.dp))
            Image(painterResource(R.drawable.white_profile), "프로필",
                Modifier.size(rightIconSize).clickable { onProfileClick() })
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = contentHorizontal, vertical = contentBottom)
                .fillMaxWidth()
        ) {
            Text(
                "이번주 인기 강의에요!",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 45.sp,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().offset(y = -titleVerticalOffset)
            )
            Spacer(Modifier.height(titleSpacing))
            Text(
                headline,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = headlineMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().offset(y = -headlineVerticalOffset)
            )
            Spacer(Modifier.height(linesSpacingSmall))
            Text(meta, color = Color.White, fontSize = 15.sp, maxLines = metaMaxLines)
            Spacer(Modifier.height(6.dp))
            Text(desc, color = Color.White, fontSize = 15.sp, maxLines = descMaxLines)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(12.dp)
    Box(modifier.shadow(4.dp, shape = shape, clip = false)) {
        OutlinedTextField(
            value = "", onValueChange = {}, readOnly = true, singleLine = true, placeholder = null,
            trailingIcon = {
                Image(painterResource(R.drawable.black_search), "검색", Modifier.size(20.dp))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White, shape)
                .let { m -> if (onClick != null) m.clickable { onClick() } else m },
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor  = Color.White,
                focusedBorderColor      = Color(0xFFC1D2ED),
                unfocusedBorderColor    = Color(0xFFC1D2ED),
                cursorColor             = Color(0xFF005FFF)
            )
        )
    }
}

@Composable
private fun FilterRow(
    tabs: List<String>,
    picked: String,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
    chipWidth: Dp = 76.dp,
    chipHeight: Dp = 34.dp
) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tab ->
            FilterChip(tab, tab == picked, { onPick(tab) }, chipWidth, chipHeight)
        }
        Spacer(Modifier.width(16.dp))
    }
}

@Composable
private fun FilterChip(
    text: String, selected: Boolean, onClick: () -> Unit, width: Dp, height: Dp
) {
    val bg = if (selected) BrandBlue else Color.White
    val fg = if (selected) Color.White else TitleBlack
    val border = if (selected) null else BorderStroke(1.dp, Color(0xFFD1D1D1))
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(25.dp),
        color = bg, border = border,
        modifier = Modifier.width(width).height(height),
        shadowElevation = 0.dp
    ) {
        Box(Modifier.fillMaxSize().padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
            Text(text, color = fg, fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CourseCarousel(
    courses: List<Course>,
    favIds: Set<Long>,
    onToggleFav: (Course, Boolean) -> Unit,
    onClick: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    if (courses.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { courses.size }
    )

    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(start = 0.dp, end = 16.dp),
        pageSpacing = 10.dp
    ) { page ->
        val course = courses[page]

        CourseCard(
            data = course,
            initialIsFav = course.id in favIds,
            onToggleFav = { newFav -> onToggleFav(course, newFav) },
            onClick = { onClick(course) }
        )
    }
}

@Composable
private fun CourseCard(
    data: Course,
    initialIsFav: Boolean,
    onToggleFav: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    var isFav by remember { mutableStateOf(initialIsFav) }

    Column(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(225.dp).clip(RoundedCornerShape(10.dp))
        ) {
            if (!data.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = data.imageUrl,
                    contentDescription = data.title,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (data.imageRes != null) {
                Image(
                    painter = painterResource(data.imageRes),
                    contentDescription = data.title,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            }
            IconButton(
                onClick = {
                    val newFav = !isFav
                    isFav = newFav           // 낙관적
                    onToggleFav(newFav)      // 상위에서 서버 업서트 + 실패시 롤백
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).size(28.dp)
            ) {
                Icon(
                    imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "찜",
                    tint = Color.White
                )
            }
        }
        Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(data.title, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = TitleBlack, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            Text(data.sub, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                letterSpacing = (-0.019).em, color = TitleBlack,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AttendanceCard(
    userName: String?,
    modifier: Modifier = Modifier,
    days: List<String> = listOf("일","월","화","수","목","금","토"),
    initiallySelected: Set<Int> = emptySet(),
    onMyCourseClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    val selectedSet = remember { mutableStateOf(initiallySelected.toMutableSet()) }

    val today = remember { LocalDate.now() }
    val weekDates = remember(today) {
        // DayOfWeek: MONDAY(1) ~ SUNDAY(7)
        val offsetFromSunday = when (today.dayOfWeek) {
            DayOfWeek.SUNDAY    -> 0
            DayOfWeek.MONDAY    -> 1
            DayOfWeek.TUESDAY   -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY  -> 4
            DayOfWeek.FRIDAY    -> 5
            DayOfWeek.SATURDAY  -> 6
        }
        val sunday = today.minusDays(offsetFromSunday.toLong())
        (0..6).map { idx ->
            sunday.plusDays(idx.toLong()).dayOfMonth.toString()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 276.dp),
        shape = shape,
        elevation = cardElevation(6.dp),
        colors = cardColors(containerColor = Color.White)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "안녕하세요 ${userName}님\n매일 출석하고 성장해요!",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 36.sp,
                letterSpacing = (-0.019).em,
                color = Color(0xFF000000),
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 요일 줄 (일~토)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    days.forEach { d ->
                        Box(
                            Modifier.size(40.dp, 41.62.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(d, fontSize = 16.6.sp, color = Color(0xFF000000))
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekDates.forEachIndexed { idx, d ->
                        val selected = idx in selectedSet.value
                        Box(
                            modifier = Modifier
                                .size(40.dp, 41.62.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) BrandBlue else Color.Transparent)
                                .clickable {
                                    val s = selectedSet.value.toMutableSet()
                                    if (selected) s.remove(idx) else s.add(idx)
                                    selectedSet.value = s
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                d,
                                fontSize = 16.6.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.White else Color(0xFF000000)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(2.dp))
            MyCourseButton(
                onClick = onMyCourseClick,
                modifier = Modifier.fillMaxWidth(),
                height = 64.dp
            )
        }
    }
}


@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().heightIn(min = 36.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = TitleBlack)
    }
}

@Composable
fun MyCourseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "내 강좌 바로가기",
    height: Dp = 55.dp,
    corner: Dp = 10.dp,
    textSize: Int = 24,
    horizontalPadding: Dp = 39.dp,
    verticalPadding: Dp = 9.dp,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick, enabled = enabled,
        modifier = modifier.fillMaxWidth().defaultMinSize(minHeight = height),
        shape = RoundedCornerShape(corner),
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = verticalPadding),
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue, contentColor = Color.White)
    ) {
        Text(text, fontSize = textSize.sp, fontWeight = FontWeight.Medium,
            color = Color.White, lineHeight = (textSize * 1.5f).sp)
    }
}
