package com.example.dodojob.ui.feature.education

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.core.view.WindowCompat
import com.example.dodojob.R
import com.example.dodojob.dao.fetchAssignedCourses
import com.example.dodojob.dao.fetchDisplayNameByUsername
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/* ====================== 색상 ====================== */
private val ScreenBg = Color(0xFFF1F5F7)
private val BrandBlue = Color(0xFF005FFF)
private val SubGray = Color(0xFF848484)

/* ====================== UI 모델 ====================== */


data class CoursewithFavorite(
    val id: Long,
    val title: String,
    val sub: String? = null,        // lecture.explain
    val imageUrl: String? = null,   // lecture.thumbnail
    val buy: Boolean? = null,       // lecture_assign_user.buy
    val favorite: Boolean? = null   // lecture_assign_user.favorite
)

enum class EduTab { Continue, Favorites }

/* ====================== 화면 ====================== */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EducationLibraryScreen(
    nav: NavController,
    userName: String?,
) {
    SetStatusBar(color = Color.Transparent, darkIcons = true)

    // 강의 로딩
    var allCourses by remember { mutableStateOf<List<CoursewithFavorite>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userName) {
        if (!userName.isNullOrBlank()) {
            loading = true
            loadError = null
            try {
                // DTO(배정 + 강의 조인) → UI Course 매핑
                val rows = withContext(Dispatchers.IO) {
                    // /lecture_assign_user?select=buy,favorite,lecture(id,title,explain,thumbnail)&user=eq.{username}
                    fetchAssignedCourses(userName)
                }
                allCourses = rows.mapNotNull { r ->
                    val lec = r.lecture ?: return@mapNotNull null
                    CoursewithFavorite(
                        id       = lec.id,
                        title    = lec.title ?: "(제목 없음)",
                        sub      = lec.explain,
                        imageUrl = lec.thumbnail,
                        buy      = r.buy,
                        favorite = r.favorite
                    )
                }
            } catch (e: Exception) {
                loadError = e.message
                allCourses = emptyList()
            } finally {
                loading = false
            }
        } else {
            allCourses = emptyList()
        }
    }

    var tab by remember { mutableStateOf(EduTab.Continue) }

    // ✅ 요구사항 필터
    // 이어보기: buy == true
    val continueCourses = remember(allCourses) {
        allCourses.filter { it.buy == true }
    }
    // 찜: favorite == true && buy != true
    val favCourses = remember(allCourses) {
        allCourses.filter { it.favorite == true && it.buy != true }
    }
    // 카운트
    val count = if (tab == EduTab.Continue) continueCourses.size else favCourses.size

    // 닉네임
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

    Scaffold(containerColor = ScreenBg) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            /* ───── 상단 바 ───── */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 12.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Image(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "뒤로",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${displayName}님 강의",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            /* ───── 로딩/에러 ───── */
            if (loading) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            }
            if (loadError != null) {
                Text(
                    text = "강의 불러오기 실패: $loadError",
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            /* ───── 총 건수 + 탭바 ───── */
            Column(Modifier.background(Color.White)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("총 ")
                            withStyle(style = SpanStyle(color = BrandBlue)) { append("${count}건") }
                            if (tab == EduTab.Continue) append("의 강좌 수강 중")
                            else append("의 강좌 찜")
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.019).em,
                        color = Color.Black
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .background(Color.White),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(60.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UnderlineTab(
                            text = "이어보기",
                            selected = tab == EduTab.Continue,
                            onClick = { tab = EduTab.Continue }
                        )
                        UnderlineTab(
                            text = "찜한 강의",
                            selected = tab == EduTab.Favorites,
                            onClick = { tab = EduTab.Favorites }
                        )
                    }
                }
            }

            /* ───── 콘텐츠 ───── */
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(Color.White)
                    .padding(top = 32.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
            ) {
                if (tab == EduTab.Continue) {
                    // ✅ buy == true 만
                    if (continueCourses.isEmpty()) {
                        EmptyHint("아직 학습 중인 강의가 없어요.")
                    } else {
                        continueCourses.forEach { c ->
                            MyCourseRowCard(
                                title = c.title,
                                subtitle = c.sub ?: "",
                                imageUrl = c.imageUrl,
                                onPlay = { /* nav.navigate(Route.EduLectureNormal.of(c.id)) */ }
                            )
                            Spacer(Modifier.height(28.dp))
                        }
                    }
                } else {
                    // ✅ favorite == true && buy != true 만
                    if (favCourses.isEmpty()) {
                        EmptyHint("아직 찜한 강의가 없어요.")
                    } else {
                        favCourses.forEach { c ->
                            FavoriteRowItem(
                                title = c.title,
                                subtitle = c.sub ?: "",
                                imageUrl = c.imageUrl,
                                onClick = { /* nav.navigate(Route.EduLectureNormal.of(c.id)) */ }
                            )
                            Spacer(Modifier.height(28.dp))
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

/* ====================== 공용 컴포넌트 ====================== */

@Composable
private fun UnderlineTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val lineWidth = 68.dp
    val lineThickness = 4.dp

    val textColor by animateColorAsState(
        targetValue = if (selected) BrandBlue else Color.Black,
        label = "tabTextColor"
    )
    val underlineHeight by animateDpAsState(
        targetValue = if (selected) lineThickness else 0.dp,
        label = "underlineHeight"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
        Spacer(Modifier.height(5.dp))
        Box(
            Modifier
                .width(lineWidth)
                .height(underlineHeight)
                .background(BrandBlue)
        )
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(text = text, fontSize = 16.sp, color = SubGray)
}

@Composable
private fun MyCourseRowCard(
    title: String,
    subtitle: String,
    imageUrl: String?,   // 썸네일 URL
    onPlay: () -> Unit
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(195.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFD9D9D9))
                .clickable(onClick = onPlay)
        ) {
            // 썸네일
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "강의 썸네일",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            }
            // 어두운 오버레이
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )
            // 재생 아이콘
            Image(
                painter = painterResource(R.drawable.play_button),
                contentDescription = "재생",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(60.dp)
            )
        }

        Column {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.019).em,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = subtitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.019).em,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FavoriteRowItem(
    title: String,
    subtitle: String,
    imageUrl: String?,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier
                .size(width = 105.dp, height = 80.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEFEFEF))
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.019).em,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFF2F00)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = subtitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.019).em,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* 상태바 설정 */
@Composable
private fun SetStatusBar(color: Color, darkIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = color.toArgb()
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = darkIcons
        }
    }
}
