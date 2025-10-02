package com.example.dodojob.ui.feature.education

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dodojob.R
import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.core.view.WindowCompat
import com.example.dodojob.navigation.Route

private val ScreenBg = Color(0xFFF1F5F7)
private val BrandBlue = Color(0xFF005FFF)
private val SubGray = Color(0xFF848484)

enum class EduTab { Continue, Favorites }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationLibraryScreen(
    nav: NavController,
    userName: String,
    favorites: Set<String>,
    allCourses: List<Course>
) {
    SetStatusBar(color = Color.Transparent, darkIcons = true)
    var tab by remember { mutableStateOf(EduTab.Continue) }
    val favCourses = remember(favorites, allCourses) {
        allCourses.filter { it.title in favorites }
    }
    val count = if (tab == EduTab.Continue) allCourses.size else favCourses.size

    Scaffold(
        containerColor = ScreenBg
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 🔹 상단 바 커스텀 (수정됨: 세로 Column로 쌓기)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 12.dp, bottom = 12.dp)
            ) {
                // 1) 뒤로가기 (맨 위)
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

                // 2) 타이틀 (뒤로가기 '아래')
                Text(
                    text = "${userName}님 강의",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            // 총 건수 + 탭바
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
                            withStyle(style = SpanStyle(color = BrandBlue)) {
                                append("${count}건")
                            }
                            if (tab == EduTab.Continue) {
                                append("의 강좌 수강 중")
                            } else {
                                append("의 강좌 찜")
                            }
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

            // 콘텐츠
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(Color.White)
                    .padding(top = 32.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
            ) {
                if (tab == EduTab.Continue) {
                    if (allCourses.isEmpty()) {
                        EmptyHint("아직 학습 중인 강의가 없어요.")
                    } else {
                        allCourses.forEach { c ->
                            MyCourseRowCard(
                                title = c.title,
                                subtitle = c.sub,
                                desc = c.desc,
                                onPlay = { nav.navigate(Route.EduLectureNormal.of(c.id)) }
                            )
                            Spacer(Modifier.height(28.dp))
                        }
                    }
                } else {
                    if (favCourses.isEmpty()) {
                        EmptyHint("아직 찜한 강의가 없어요.")
                    } else {
                        favCourses.forEach { c ->
                            FavoriteRowItem(
                                title = c.title,
                                subtitle = c.sub,
                                desc = c.desc,
                                thumbnailRes = c.imageRes,
                                onClick = { nav.navigate(com.example.dodojob.navigation.Route.EduLectureNormal.of(c.id)) }
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

@Composable
private fun UnderlineTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    // 선 길이/두께 Figma 스펙
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
            .padding(horizontal = 4.dp) // 터치 여유
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
private fun TabLabel(text: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) BrandBlue else Color.Black
        )
        Spacer(Modifier.height(5.dp))
        if (selected) {
            Box(
                Modifier
                    .width(68.dp)
                    .height(0.dp)
                    .border(4.dp, BrandBlue)
            )
        } else {
            Spacer(Modifier.height(4.dp))
        }
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
    desc: String,
    onPlay: () -> Unit
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(195.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFD9D9D9))
                .clickable(onClick = onPlay),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.play_button),
                contentDescription = "재생",
                modifier = Modifier.size(60.dp)
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
            Spacer(Modifier.height(4.dp))
            Text(
                text = desc,
                fontSize = 15.sp,
                letterSpacing = (-0.019).em,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SetStatusBar(color: Color, darkIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) { // Preview에서 window 접근 피함
        SideEffect {
            val window = (view.context as Activity).window
            // 컨텐츠를 시스템바 뒤로 배치 (히어로 이미지가 상태바까지 꽉 차도록)
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // 상태바 색상 및 아이콘 밝기
            window.statusBarColor = color.toArgb()
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = darkIcons
        }
    }
}

@Composable
private fun FavoriteRowItem(
    title: String,
    subtitle: String,
    desc: String,
    @DrawableRes thumbnailRes: Int,
    onClick: () -> Unit // ← 추가
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick), // ← 클릭 가능
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier
                .size(width = 105.dp, height = 80.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Image(
                painter = painterResource(thumbnailRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
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
                Icon(
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
            Text(
                text = desc,
                fontSize = 15.sp,
                letterSpacing = (-0.019).em,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* =========================
 * Preview
 * ========================= */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewEducationLibrary() {
    val nav = rememberNavController()

    val all = recommendedCourses() + liveHotCourses()
    val sampleFavorites = setOf("영어 회화 입문", "고객 응대 스킬")

    EducationLibraryScreen(
        nav = nav,
        userName = "홍길동",
        favorites = sampleFavorites,
        allCourses = all
    )
}
