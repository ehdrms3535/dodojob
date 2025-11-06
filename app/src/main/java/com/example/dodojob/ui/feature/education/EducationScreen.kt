package com.example.dodojob.ui.feature.education

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
import com.example.dodojob.ui.feature.main.BottomNavBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.dodojob.dao.fetchLectures
import com.example.dodojob.dao.LectureRow
import com.example.dodojob.dao.fetchDisplayNameByUsername

/* =========================
 * Colors
 * ========================= */
private val ScreenBg   = Color(0xFFF1F5F7)
private val TitleBlack = Color(0xFF000000)
private val BrandBlue  = Color(0xFF005FFF)

/* =========================
 * Data
 * ========================= */
data class Course(
    val id: Long,                       // 강의 클릭시 전달할 ID(Long)
    val title: String,
    val tag: String,                    // category
    val sub: String,                    // explain
    val imageUrl: String? = null,       // 썸네일 URL
    @DrawableRes val imageRes: Int? = null // 로컬 이미지
)

/** Supabase Row → UI 모델 매핑 */
private fun LectureRow.toCourse(): Course = Course(
    id = id, // Long
    title = title.orEmpty(),
    tag = category.orEmpty(),
    sub = explain.orEmpty(),
    imageUrl = thumbnail
)

/** 필터 탭 라벨 */
private val filterTabs = listOf("전체", "영어", "컴퓨터", "요리", "교육", "응대", "기타")

/** 추천(로컬 폴백) — DB 비었을 때만 사용. id는 음수로 충돌 방지 */
fun recommendedCourses() = listOf(
    Course(
        id = -101L,
        imageRes = R.drawable.edu_recom1,
        title = "영어 회화 입문",
        tag   = "영어",
        sub   = "일상 표현부터 차근차근"
    ),
    Course(
        id = -102L,
        imageRes = R.drawable.edu_recom2,
        title = "컴퓨터 기초 마스터",
        tag   = "컴퓨터",
        sub   = "문서·인터넷·이메일 한 번에"
    ),
    Course(
        id = -103L,
        imageRes = R.drawable.edu_recom3,
        title = "집에서 즐기는 홈쿠킹",
        tag   = "요리",
        sub   = "기초 재료 손질과 간단 레시피"
    ),
    Course(
        id = -104L,
        imageRes = R.drawable.edu_recom4,
        title = "그룹 스터디 튜터링",
        tag   = "교육",
        sub   = "주 1회 온라인 그룹 학습"
    )
)

/** 더미(로컬) 실시간 인기 강의 — DB 매칭 실패/비었을 때 폴백. id 음수 */
fun liveHotCoursesFallback() = listOf(
    Course(
        id = -201L,
        imageRes = R.drawable.edu_live1,
        title = "고객 응대 스킬",
        tag   = "응대",
        sub   = "전화·대면 응대 기본"
    ),
    Course(
        id = -202L,
        imageRes = R.drawable.edu_live2,
        title = "스마트폰 200% 활용",
        tag   = "컴퓨터",
        sub   = "결제·사진·앱 활용 전반"
    ),
    Course(
        id = -203L,
        imageRes = R.drawable.edu_live3,
        title = "물감과 친해지는 수채화",
        tag   = "기타",
        sub   = "기초 드로잉과 색감 연습"
    ),
    Course(
        id = -204L,
        imageRes = R.drawable.edu_live4,
        title = "영어 뉴스 리스닝",
        tag   = "영어",
        sub   = "쉬운 뉴스로 리스닝 감 만들기"
    )
)

/* ===== DB 강의에서 1..6 중 3개 랜덤 Long id와 매칭하여 3개 선별 ===== */
private fun pickLiveHotFromDb(dbCourses: List<Course>): List<Course> {
    if (dbCourses.isEmpty()) return liveHotCoursesFallback() // 폴백

    // 1~6 중 3개 뽑기 → Long 세트
    val picks: Set<Long> = (1L..6L).shuffled().take(3).toSet()

    // id(Long)가 picks에 포함된 강의 매칭
    val matched = dbCourses.filter { it.id in picks }

    // 3개 미만이면 DB에서 나머지 채우기(중복 방지)
    val need = 3 - matched.size
    val filled = if (need > 0) {
        val remains = dbCourses.filter { it !in matched }
        matched + remains.take(need)
    } else matched.take(3)

    return filled.ifEmpty { liveHotCoursesFallback() }
}

/* =========================
 * Entry
 * ========================= */
@Composable
fun EducationHomeRoute(
    nav: NavController,
    userName: String? = null,    // ⚠️ 여기 들어오는 값은 'username(=ID)'
    eduVm: EducationViewModel
) {
    EducationHomeScreen(
        userName = userName,      // ID를 그대로 전달하고, 화면 안에서 이름 조회함
        onCourseClick = { course ->
            // Long id → String으로 변환해서 네비게이션 전달
            nav.navigate(Route.EduLectureInitial.of(course.id.toString()))
        },
        onOpenLibrary = { nav.navigate(Route.EduMy.path) }, // 내 강좌/프로필 → 단일 화면
        bottomBar = {
            BottomNavBar(
                current = "edu",
                onClick = { key ->
                    when (key) {
                        "home"      -> nav.navigate(Route.Main.path) { launchSingleTop = true }
                        "edu"       -> {} // 현재
                        "welfare"   -> nav.navigate("welfare/home") { launchSingleTop = true }
                        "my"        -> nav.navigate(Route.My.path) { launchSingleTop = true }
                    }
                }
            )
        },
        favorites = eduVm.favorites,
        onToggleFavorite = { title -> eduVm.toggleFavorite(title) }
    )
}

/* =========================
 * Screen
 * ========================= */
@Composable
fun EducationHomeScreen(
    userName: String?,
    onCourseClick: (Course) -> Unit,
    onOpenLibrary: () -> Unit,
    bottomBar: @Composable (() -> Unit),
    favorites: Set<String>,
    onToggleFavorite: (String) -> Unit
) {
    // ── 1) username(=ID) → name 조회해서 화면 표시용으로 사용 ─────────────
    var displayName by remember { mutableStateOf("회원") }
    var loadingName by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userName) {
        if (!userName.isNullOrBlank()) {
            loadingName = true
            nameError = null
            try {
                val fetched = withContext(Dispatchers.IO) {
                    fetchDisplayNameByUsername(userName)
                }
                displayName = fetched ?: userName
            } catch (e: Exception) {
                nameError = e.message
                displayName = userName
            } finally {
                loadingName = false
            }
        } else {
            displayName = "회원"
        }
    }
    // ───────────────────────────────────────────────────────────────

    var pickedFilter by remember { mutableStateOf("전체") }

    var supaCourses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // 로컬 fallback
    val recomLocal = remember { recommendedCourses() }

    // 최초 로드
    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            val rows = withContext(Dispatchers.IO) { fetchLectures(limit = 30) }
            supaCourses = rows.map { it.toCourse() }
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    fun List<Course>.applyFilter(): List<Course> =
        if (pickedFilter == "전체") this else this.filter { it.tag == pickedFilter }

    // ▼ 배너에 바인딩할 대표 강의 (DB 우선, 없으면 로컬)
    val heroCourse: Course? = (if (supaCourses.isNotEmpty()) supaCourses else recomLocal).firstOrNull()

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = bottomBar,
        topBar = { Spacer(modifier = Modifier.fillMaxWidth().statusBarsPadding()) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ===== Hero ===== (▼ heroCourse 전달)
            HeroSection(
                userName = displayName,
                heroImageRes = R.drawable.edu_recom4,
                onBellClick = { /* TODO */ },
                onProfileClick = onOpenLibrary,
                heroCourse = heroCourse,              // DB 데이터 바인딩
                topBarHorizontal = 16.dp,
                topBarTop = 0.dp,
                logoSize = 29.dp,
                rightIconSize = 26.dp,
                contentHorizontal = 18.dp,
                contentBottom = 40.dp,
                titleSpacing = 8.dp,
                linesSpacingSmall = 2.dp,
                titleMaxLines = 1,
                headlineMaxLines = 1,
                metaMaxLines = 1,
                descMaxLines = 2
            )

            // ===== 검색/필터 =====
            Spacer(Modifier.height(18.dp))
            SearchBar(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )

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

            Spacer(Modifier.height(16.dp))
            SectionTitle(
                text = "${displayName}님을 위한 추천 강의",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
            Spacer(Modifier.height(12.dp))

            val recommendedList = (if (supaCourses.isNotEmpty()) supaCourses else recomLocal)
                .applyFilter()


            if (loading && supaCourses.isEmpty()) {
                Text("불러오는 중...", modifier = Modifier.padding(horizontal = 16.dp))
            } else if (error != null && supaCourses.isEmpty()) {
                Text("로드 실패: $error", color = Color.Red, modifier = Modifier.padding(horizontal = 16.dp))
            } else {
                CourseCarousel(
                    courses = recommendedList,
                    favs = favorites,
                    onToggleFav = onToggleFavorite,
                    onClick = onCourseClick,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle(
                text = "실시간 인기 강의",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
            Spacer(Modifier.height(12.dp))

            // ▼ DB 기반으로 3개 뽑은 리스트 사용 (필터 적용)
            val liveHotDb = if (supaCourses.isNotEmpty()) {
                pickLiveHotFromDb(supaCourses)
            } else {
                liveHotCoursesFallback()
            }.applyFilter()

            CourseCarousel(
                courses = liveHotDb,
                favs = favorites,
                onToggleFav = onToggleFavorite,
                onClick = onCourseClick,
                modifier = Modifier.padding(start = 16.dp, bottom = 24.dp)
            )
        }
    }
}

/* =========================
 * Pieces
 * ========================= */
@Composable
private fun HeroSection(
    userName: String?,
    @DrawableRes heroImageRes: Int,
    onBellClick: () -> Unit,
    onProfileClick: () -> Unit,
    // ▼ 추가: DB에서 가져온 대표 강의(없으면 null)
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
    descMaxLines: Int = 1,
    titleVerticalOffset: Dp = 60.dp,
    headlineVerticalOffset: Dp = 16.dp
) {
    // ▼ DB 값이 있으면 해당 값 사용, 없으면 기존 더미 문구
    val headline = heroCourse?.title ?: "외국인 친구와 소통하는 즐거움, 온라인 한국어 회화"
    val meta     = heroCourse?.let { "${it.tag.ifBlank { "기타" }} | ${it.id}" } ?: "언어·문화 | 세종학당재단"
    val desc     = heroCourse?.sub ?: "실생활 중심 대화 연습으로 자연스러운 회화"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        Image(
            painter = painterResource(heroImageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xB3000000))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = topBarHorizontal, vertical = topBarTop),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.white_logo),
                contentDescription = "logo",
                modifier = Modifier.size(logoSize)
            )
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.white_bell),
                contentDescription = "알림",
                modifier = Modifier
                    .size(rightIconSize)
                    .clickable { onBellClick() }
            )
            Spacer(Modifier.width(10.dp))
            Image(
                painter = painterResource(R.drawable.white_profile),
                contentDescription = "프로필",
                modifier = Modifier
                    .size(rightIconSize)
                    .clickable { onProfileClick() }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = contentHorizontal, vertical = contentBottom)
                .fillMaxWidth()
        ) {
            Text(
                text = "이번주 인기 강의에요!",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 45.sp,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = -titleVerticalOffset)
            )
            Spacer(Modifier.height(titleSpacing))
            Text(
                text = headline,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = headlineMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = -headlineVerticalOffset)
            )
            Spacer(Modifier.height(linesSpacingSmall))
            Text(
                text = meta,
                color = Color.White,
                fontSize = 15.sp,
                maxLines = metaMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = desc,
                color = Color.White,
                fontSize = 15.sp,
                maxLines = descMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** 검색창 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .shadow(4.dp, shape = shape, clip = false)
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            placeholder = null,
            trailingIcon = {
                Image(
                    painter = painterResource(R.drawable.black_search),
                    contentDescription = "검색",
                    modifier = Modifier.size(20.dp)
                )
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
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tab ->
            FilterChip(
                text = tab,
                selected = tab == picked,
                onClick = { onPick(tab) },
                width = chipWidth,
                height = chipHeight
            )
        }
        Spacer(Modifier.width(16.dp))
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    width: Dp,
    height: Dp
) {
    val bg = if (selected) BrandBlue else Color.White
    val fg = if (selected) Color.White else TitleBlack
    val border = if (selected) null else BorderStroke(1.dp, Color(0xFFD1D1D1))

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(25.dp),
        color = bg,
        border = border,
        modifier = Modifier
            .width(width)
            .height(height),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = fg,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CourseCarousel(
    courses: List<Course>,
    favs: Set<String>,
    onToggleFav: (String) -> Unit,
    onClick: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(end = 16.dp)
    ) {
        items(courses) { course ->
            CourseCard(
                data = course,
                isFav = course.title in favs,
                onToggleFav = { onToggleFav(course.title) },
                onClick = { onClick(course) }
            )
        }
    }
}

@Composable
private fun CourseCard(
    data: Course,
    isFav: Boolean,
    onToggleFav: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(375.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(225.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            // 🔹 URL > 로컬 순서로 이미지 렌더
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
                onClick = onToggleFav,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "찜",
                    tint = Color.White
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                text = data.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TitleBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = data.sub,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.019).em,
                color = TitleBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* ---------- 출석/주간 카드 ---------- */
@Composable
private fun AttendanceCard(
    userName: String?,
    modifier: Modifier = Modifier,
    days: List<String> = listOf("일","월","화","수","목","금","토"),
    dates: List<String> = listOf("1","2","3","4","5","6","7"),
    initiallySelected: Set<Int> = emptySet(),
    onMyCourseClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    val selectedSet = remember { mutableStateOf(initiallySelected.toMutableSet()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 276.dp),
        shape = shape,
        elevation = cardElevation(6.dp),
        colors = cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    days.forEach { d ->
                        Box(
                            modifier = Modifier.size(width = 40.dp, height = 41.62.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(d, fontSize = 16.6.sp, color = Color(0xFF000000))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dates.forEachIndexed { idx, d ->
                        val selected = idx in selectedSet.value
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = 41.62.dp)
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = TitleBlack)
    }
}

/* ---------- 독립 버튼 ---------- */
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
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = height),
        shape = RoundedCornerShape(corner),
        contentPadding = PaddingValues(
            horizontal = horizontalPadding,
            vertical = verticalPadding
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandBlue,
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = textSize.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            lineHeight = (textSize * 1.5f).sp
        )
    }
}
