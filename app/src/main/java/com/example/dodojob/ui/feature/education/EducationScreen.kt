package com.example.dodojob.ui.feature.education

import com.example.dodojob.ui.feature.education.EducationViewModel
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import com.example.dodojob.ui.feature.profile.BottomNavBar
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController

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
    val id: String, //강의 클릭하면 해당 강의로 가게 할려고 둿음
    @DrawableRes val imageRes: Int,
    val title: String,
    val tag: String,    // 필터용
    val sub: String,    // 중간 라인
    val desc: String
)

/** 필터 탭 라벨 */
private val filterTabs = listOf("전체", "영어", "컴퓨터", "요리", "교육", "응대", "기타")

/** 추천 강의 */
fun recommendedCourses() = listOf(
    Course(
        id = "eng-conv-basic",
        imageRes = R.drawable.edu_recom1,
        title = "영어 회화 입문",
        tag   = "영어",
        sub   = "일상 표현부터 차근차근",
        desc  = "기초 패턴과 상황별 회화로 부담없이 시작"
    ),
    Course(
        id = "pc-basic-master",
        imageRes = R.drawable.edu_recom2,
        title = "컴퓨터 기초 마스터",
        tag   = "컴퓨터",
        sub   = "문서·인터넷·이메일 한 번에",
        desc  = "실습 위주로 바로 따라하는 필수 기능"
    ),
    Course(
        id = "home-cooking",
        imageRes = R.drawable.edu_recom3,
        title = "집에서 즐기는 홈쿠킹",
        tag   = "요리",
        sub   = "기초 재료 손질과 간단 레시피",
        desc  = "매일 먹는 반찬부터 근사한 일품요리까지"
    ),
    Course(
        id = "group-tutoring",
        imageRes = R.drawable.edu_recom4,
        title = "그룹 스터디 튜터링",
        tag   = "교육",
        sub   = "주 1회 온라인 그룹 학습",
        desc  = "함께 공부하며 동기부여 얻기"
    )
)

// ✅ 실시간 인기 강의
fun liveHotCourses() = listOf(
    Course(
        id = "cs-customer",
        imageRes = R.drawable.edu_live1,
        title = "고객 응대 스킬",
        tag   = "응대",
        sub   = "전화·대면 응대 기본",
        desc  = "상황별 말하기와 친절한 커뮤니케이션"
    ),
    Course(
        id = "smartphone-pro",
        imageRes = R.drawable.edu_live2,
        title = "스마트폰 200% 활용",
        tag   = "컴퓨터",
        sub   = "결제·사진·앱 활용 전반",
        desc  = "초보도 쉽게 따라하는 실전 가이드"
    ),
    Course(
        id = "watercolor-begin",
        imageRes = R.drawable.edu_live3,
        title = "물감과 친해지는 수채화",
        tag   = "기타",
        sub   = "기초 드로잉과 색감 연습",
        desc  = "간단한 소묘부터 분위기 있는 채색까지"
    ),
    Course(
        id = "english-news-listening",
        imageRes = R.drawable.edu_live4,
        title = "영어 뉴스 리스닝",
        tag   = "영어",
        sub   = "쉬운 뉴스로 리스닝 감 만들기",
        desc  = "핵심 단어·표현으로 이해력 향상"
    )
)
/* =========================
 * Entry
 * ========================= */
@Composable
fun EducationHomeRoute(
    nav: NavController,
    userName: String = "홍길동",
    eduVm: EducationViewModel
) {
    EducationHomeScreen(
        userName = userName,
        onCourseClick = { course ->
            nav.navigate(Route.EduLectureInitial.of(course.id))
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
                        "community" -> nav.navigate("community") { launchSingleTop = true }
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
    userName: String,
    onCourseClick: (Course) -> Unit,
    onOpenLibrary: () -> Unit,
    bottomBar: @Composable () -> Unit,
    favorites: Set<String>,
    onToggleFavorite: (String) -> Unit
) {
    var pickedFilter by remember { mutableStateOf("전체") }

    val recom = remember { recommendedCourses() }
    val live  = remember { liveHotCourses() }

    fun List<Course>.applyFilter(): List<Course> =
        if (pickedFilter == "전체") this else this.filter { it.tag == pickedFilter }

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
            // ===== Hero =====
            HeroSection(
                userName = userName,
                heroImageRes = R.drawable.edu_recom4,
                onBellClick = { /* TODO */ },
                onProfileClick = onOpenLibrary, // 프로필 아이콘 → 단일 화면
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
                userName = userName,
                modifier = Modifier.padding(horizontal = 16.dp),
                onMyCourseClick = onOpenLibrary // 버튼 → 단일 화면
            )

            Spacer(Modifier.height(16.dp))
            SectionTitle(
                text = "${userName}님을 위한 추천 강의",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
            Spacer(Modifier.height(12.dp))

            CourseCarousel(
                courses = recom.applyFilter(),
                favs = favorites,
                onToggleFav = onToggleFavorite,
                onClick = onCourseClick,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(Modifier.height(24.dp))
            SectionTitle(
                text = "실시간 인기 강의",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
            Spacer(Modifier.height(12.dp))

            CourseCarousel(
                courses = live.applyFilter(),
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
    userName: String,
    @DrawableRes heroImageRes: Int,
    onBellClick: () -> Unit,
    onProfileClick: () -> Unit,
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
                text = "외국인 친구와 소통하는 즐거움, 온라인 한국어 회화",
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
                text = "언어·문화 | 세종학당재단",
                color = Color.White,
                fontSize = 15.sp,
                maxLines = metaMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "실생활 중심 대화 연습으로 자연스러운 회화",
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
            Image(
                painter = painterResource(data.imageRes),
                contentDescription = data.title,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
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
            Spacer(Modifier.height(6.dp))
            Text(
                text = data.desc,
                fontSize = 15.sp,
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
    userName: String,
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
        modifier = modifier.fillMaxWidth().heightIn(min = 36.dp),
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

