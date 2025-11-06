package com.example.dodojob.ui.feature.education

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.core.view.WindowCompat
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import android.app.Activity

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
            // 상단 바
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 12.dp, bottom = 12.dp)
            ) {
                // 뒤로가기
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

                // 타이틀
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
                                onPlay = { /*nav.navigate(Route.EduLectureNormal.of(c.id)) */}
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
                                imageUrl = c.imageUrl, // 🔹 URL만 사용
                                onClick = { /*nav.navigate(Route.EduLectureNormal.of(c.id))*/ }
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
        }
    }
}

/** 🔹 상태바 색상/아이콘 설정 */
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

/** 🔹 썸네일 URL만 사용하는 찜한 강의 아이템 */
@Composable
private fun FavoriteRowItem(
    title: String,
    subtitle: String,
    imageUrl: String?,        // Supabase 썸네일 URL
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
        }
    }
}
