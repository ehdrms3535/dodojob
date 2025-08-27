// app/src/main/java/com/example/dodojob/ui/feature/profile/ActivityLevelRoute.kt
package com.example.dodojob.ui.feature.profile

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route

/* =============== 팔레트 =============== */
data class LevelPalette(
    val primary: Color,
    val primaryLight: Color,
    val border: Color
)
private fun paletteFor(level: Int): LevelPalette = when (level) {
    1 -> LevelPalette(Color(0xFFFC4545), Color(0xFFFFD1D1), Color(0xFFF24822)) // 빨강
    2 -> LevelPalette(Color(0xFFFCC845), Color(0xFFFFF1C2), Color(0xFFDDB739)) // 노랑
    else -> LevelPalette(Color(0xFF4945FC), Color(0xFFD7D6FF), Color(0xFF6D69FE)) // 파랑
}

/* =============== Fake DB (요청 스키마) =============== */
data class ActivityLevelData(
    val name: String,
    val level: Int,
    val applyWithinYear: Int,
    val realWorkExpCount: Int,
    val eduCompleted: Boolean,
    val joinedDate: String
)
object ActivityLevelFakeDb {
    fun get(level: Int): ActivityLevelData = when (level) {
        1 -> ActivityLevelData("홍길동", 1, 2, 0, false, "2025.08.20")
        2 -> ActivityLevelData("홍길동", 2, 5, 2, true,  "2025.08.21")
        else -> ActivityLevelData("홍길동", 3, 11, 5, true, "2025.08.23")
    }
}

/* =============== 리소스 맵핑 =============== */
@DrawableRes private fun badgeResFor(level: Int) = when (level) {
    1 -> R.drawable.red_medal
    2 -> R.drawable.yellow_medal
    else -> R.drawable.blue_medal
}
@DrawableRes private fun levelBannerResFor(level: Int) = when (level) {
    1 -> R.drawable.level1_banner
    2 -> R.drawable.level2_banner
    else -> R.drawable.level3_banner
}
@DrawableRes private fun benefitsImageResFor(level: Int) = when (level) {
    1 -> R.drawable.benefits_level1
    2 -> R.drawable.benefits_level2
    else -> R.drawable.benefits_level3
}

/* =============== Route & Screen =============== */
@Composable
fun ActivityLevelRoute(
    nav: NavController,
    levelArg: Int = 3
) {
    ActivityLevelScreen(
        level = levelArg,
        onShortcut = { key ->
            when (key) {
                "home"      -> nav.navigate("main") { launchSingleTop = true }
                "edu"       -> nav.navigate("edu")
                "welfare"   -> nav.navigate("welfare")
                "community" -> nav.navigate("community")
                "my"        -> nav.navigate(Route.My.path) { launchSingleTop = true }
            }
        },
        onBackToProfile = { nav.navigate(Route.My.path) { launchSingleTop = true } }
    )
}

@Composable
fun ActivityLevelScreen(
    level: Int,
    onShortcut: (String) -> Unit,
    onBackToProfile: () -> Unit
) {
    val data = remember(level) { ActivityLevelFakeDb.get(level) }
    val pal  = remember(level) { paletteFor(level) }

    val screenBg  = Color(0xFFF1F5F7)
    val brandBlue = Color(0xFF005FFF)

    val gradientHeight = 220.dp
    val floatOffset = (-48).dp

    Scaffold(
        containerColor = screenBg,
        bottomBar = { BottomNavBar(current = "my", onClick = onShortcut) }
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
                    .height(gradientHeight)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Brush.verticalGradient(listOf(pal.primary, pal.primaryLight)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp) // ⬅ 줄임
                ) {
                    IconButton(onClick = onBackToProfile) {
                        Icon(
                            imageVector = Icons.Outlined.ChevronLeft,
                            contentDescription = "뒤로가기",
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp), // ⬅ 12→10
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(R.drawable.senior_id),
                            contentDescription = "프로필 사진",
                            modifier = Modifier
                                .size(80.dp) // 살짝 축소
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${data.name}님",
                                    color = Color.White,
                                    fontSize = 30.sp, // 32→30
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(Modifier.width(6.dp)) // 8→6
                                Image(
                                    painter = painterResource(id = badgeResFor(data.level)),
                                    contentDescription = "레벨 뱃지",
                                    modifier = Modifier.size(40.dp), // 50→40
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }

            /* ----- 활동레벨: 이미지 플로팅 카드 (둥근 사각형 + 그림자) ----- */
            FloatingRoundedImageCard(
                resId = levelBannerResFor(data.level),
                borderColor = pal.border,
                modifier = Modifier
                    .padding(horizontal = 16.dp) // ⬅ 16→12
                    .offset(y = (-70).dp)
            )

            /* ----- 나의 활동 (제목=검정, 라벨=회색, 값=항상 파란색) ----- */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 0.dp) // ⬅ 16→12 / 2→0
                    .offset(y = (-60).dp),                         // ⬅ -20→-16
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), // ⬅ 16→12/10
                    verticalArrangement = Arrangement.spacedBy(8.dp)                    // ⬅ 10→8
                ) {
                    Text("나의 활동", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)

                    ActivityRow("1년 이내 일자리 지원", "${data.applyWithinYear}건", valueColor = brandBlue)
                    Divider(thickness = 1.dp, color = Color(0xFFF0F0F0))
                    ActivityRow("실제 근무 경험", "${data.realWorkExpCount}건", valueColor = brandBlue)
                    Divider(thickness = 1.dp, color = Color(0xFFF0F0F0))
                    ActivityRow("교육/강의 콘텐츠", if (data.eduCompleted) "수강 완료" else "수강 중", valueColor = brandBlue)
                    Divider(thickness = 1.dp, color = Color(0xFFF0F0F0))
                    ActivityRow("회원가입", data.joinedDate, valueColor = brandBlue)
                }
            }

            /* ----- 레벨 별 혜택 (카드 제거: 이미지 단독) ----- */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp) // ⬅ 16→12
                    .offset(y=(-50).dp)
            ) {
                Image(
                    painter = painterResource(id = benefitsImageResFor(data.level)),
                    contentDescription = "레벨별 혜택 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    contentScale = ContentScale.FillWidth
                )
            }

            Spacer(Modifier.height(90.dp)) // ⬅ 90→64
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
        Text(label, fontSize = 14.sp, color = Color(0xFF7A7A7A)) // 라벨 회색
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = valueColor) // 값 파란색
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
        shape = RoundedCornerShape(16.dp), // 16→14
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp), // 4→3
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
