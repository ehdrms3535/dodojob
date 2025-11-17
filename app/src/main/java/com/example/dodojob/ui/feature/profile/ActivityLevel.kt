package com.example.dodojob.ui.feature.profile

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import com.example.dodojob.ui.components.AppBottomBar
import kotlinx.parcelize.Parcelize

/* ===================== 전달 페이로드 ===================== */
@Parcelize
data class ActivityLevelData(
    val name: String,
    val level: Long,             // DB/도메인과 동일하게 Long 유지
    val applyWithinYear: Long,
    val realWorkExpCount: Long,
    val eduCompleted: Boolean,
    val joinedDate: String,      // "2025년 9월 3일" 고정 전달
    val profileImageUrl: String?
) : Parcelable

/* =============== 팔레트 =============== */
data class LevelPalette(
    val primary: Color,
    val primaryLight: Color,
    val border: Color
)

private fun paletteFor(level: Int): LevelPalette = when (level) {
    1 -> LevelPalette(Color(0xFFFC4545), Color(0xFFFFB297), Color(0xFFF24822)) // 레벨1(레드)
    2 -> LevelPalette(Color(0xFFFFC527), Color(0xFFFFEB80), Color(0xFFDDB739)) // 레벨2(옐로)
    else -> LevelPalette(Color(0xFF6D69FE), Color(0xFF9997FF), Color(0xFF6D69FE)) // 레벨3(블루)
}

/* =============== 리소스 맵핑 =============== */
@DrawableRes
private fun badgeResFor(level: Int) = when (level) {
    1 -> R.drawable.red_medal
    2 -> R.drawable.yellow_medal
    else -> R.drawable.blue_medal
}

@DrawableRes
private fun levelBannerResFor(level: Int) = when (level) {
    1 -> R.drawable.level1_banner
    2 -> R.drawable.level2_banner
    else -> R.drawable.level3_banner
}

@DrawableRes
private fun benefitsImageResFor(level: Int) = when (level) {
    1 -> R.drawable.benefits_level1
    2 -> R.drawable.benefits_level2
    else -> R.drawable.benefits_level3
}

/* =============== Route =============== */
@Composable
fun ActivityLevelRoute(
    nav: NavController
) {
    val payload: ActivityLevelData? = remember(nav) {
        nav.previousBackStackEntry
            ?.savedStateHandle
            ?.get<ActivityLevelData>("activity_level_payload")
    }

    if (payload == null) {
        MissingPayloadScreen(
            onBackToProfile = { nav.navigate(Route.My.path) { launchSingleTop = true } }
        )
        return
    }

    val shortcut: (String) -> Unit = { key ->
        when (key) {
            "home"    -> nav.navigate("main") { launchSingleTop = true }
            "edu"     -> nav.navigate("edu")
            "welfare" -> nav.navigate("welfare")
            "my"      -> nav.navigate(Route.My.path) { launchSingleTop = true }
        }
    }

    ActivityLevelScreen(
        data = payload,
        onShortcut = { key ->
            when (key) {
                "home"      -> nav.navigate("main") { launchSingleTop = true }
                "edu"       -> nav.navigate("edu")
                "welfare"   -> nav.navigate("welfare")
                "my"        -> nav.navigate(Route.My.path) { launchSingleTop = true }
            }
        },
        onBackToProfile = { nav.navigate(Route.My.path) { launchSingleTop = true } },
        bottomBar = {
            AppBottomBar(
                current = "my",
                onClick = shortcut
            )
        }
    )
}

/* =============== Screen =============== */
@Composable
fun ActivityLevelScreen(
    data: ActivityLevelData,
    onShortcut: (String) -> Unit,
    onBackToProfile: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val levelInt = data.level.coerceIn(1, 3).toInt()
    val pal      = remember(levelInt) { paletteFor(levelInt) }

    val screenBg  = Color(0xFFF1F5F7)
    val brandBlue = Color(0xFF005FFF)
    val context   = LocalContext.current

    Scaffold(
        containerColor = screenBg,
        bottomBar = bottomBar
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            /* ----- 상단 그라데이션 (하단 둥근 모서리) ----- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(264.dp)
                    .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                    .background(Brush.verticalGradient(listOf(pal.primary, pal.primaryLight)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = onBackToProfile,
                        modifier = Modifier
                            .size(48.dp)
                            .offset(x = (-4).dp)
                    ) {
                        val backIconRes = if (levelInt == 2) R.drawable.back else R.drawable.white_back
                        Image(
                            painter = painterResource(backIconRes),
                            contentDescription = "뒤로가기",
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(data.profileImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "프로필 사진",
                            modifier = Modifier
                                .size(104.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.senior_id),
                            error = painterResource(R.drawable.senior_id)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = " ${data.name}님",
                                    color = if (levelInt == 2) Color.Black else Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Spacer(Modifier.width(8.dp))
                                Image(
                                    painter = painterResource(id = badgeResFor(levelInt)),
                                    contentDescription = "레벨 뱃지",
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height(36.dp)
                                        .align(Alignment.CenterVertically)
                                        .offset(y = 3.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            /* ----- 활동레벨 배너 카드 ----- */
            FloatingRoundedImageCard(
                resId = levelBannerResFor(levelInt),
                borderColor = pal.border,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-85).dp)
            )

            /* ----- 나의 활동 ----- */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-70).dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                ) {
                    Text(
                        "나의 활동",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActivityRow("1년 이내 일자리 지원", "${data.applyWithinYear}건", valueColor = brandBlue)
                        ActivityRow("실제 근무 경험", "${data.realWorkExpCount}건", valueColor = brandBlue)
                        ActivityRow(
                            "교육/강의 콘텐츠",
                            if (data.eduCompleted) "수강 완료" else "수강 중",
                            valueColor = brandBlue
                        )
                        ActivityRow("회원가입", data.joinedDate, valueColor = brandBlue)
                    }
                }
            }

            /* ----- 레벨 별 혜택 (이미지) ----- */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-60).dp)
            ) {
                Image(
                    painter = painterResource(id = benefitsImageResFor(levelInt)),
                    contentDescription = "레벨별 혜택 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }
}

/* =============== 소형 컴포넌트 =============== */
@Composable
private fun ActivityRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 20.sp, color = Color(0xFF9C9C9C), fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

/** 이미지 자체를 둥근-사각형 카드로 띄우기 */
@Composable
private fun FloatingRoundedImageCard(
    @DrawableRes resId: Int,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
    }
}

/* =============== 페이로드 누락 화면 =============== */
@Composable
private fun MissingPayloadScreen(
    onBackToProfile: () -> Unit
) {
    val screenBg = Color(0xFFF1F5F7)
    Scaffold(containerColor = screenBg) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("필요한 데이터가 없습니다.", fontSize = 18.sp, color = Color(0xFF222222))
                Spacer(Modifier.height(8.dp))
                Button(onClick = onBackToProfile) { Text("프로필로 돌아가기") }
            }
        }
    }
}
