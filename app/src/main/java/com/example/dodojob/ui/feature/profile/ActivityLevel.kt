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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route

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

/* =============== 데이터 모델 (실제에선 서버 응답 매핑) =============== */
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
        2 -> ActivityLevelData("홍길동", 2, 5, 1, true,  "2025.08.21")
        else -> ActivityLevelData("홍길동", 3, 11, 5, true, "2025.08.23")
    }
}

/* =============== 리소스 맵핑 (PNG 대체/재활용 지점) =============== */
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

/* =============== Route =============== */
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
        onBackToProfile = { nav.navigate(Route.My.path) { launchSingleTop = true } },
        bottomBar = {
            // 프로젝트에 이미 있는 실제 BottomNavBar 사용
            BottomNavBar(current = "my", onClick = { key ->
                when (key) {
                    "home","edu","welfare","community","my" -> {} // label 목적
                }
                // 위의 라벨링은 미리보기에서 IDE 인스펙션 경고 방지용
                // 실제 네비게이션은 onShortcut으로 전달
                // (아래에서 동일 키로 라우팅)
            })
        }
    )
}

/* =============== Screen =============== */
@Composable
fun ActivityLevelScreen(
    level: Int,
    onShortcut: (String) -> Unit,
    onBackToProfile: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val data = remember(level) { ActivityLevelFakeDb.get(level) }
    val pal  = remember(level) { paletteFor(level) }

    val screenBg  = Color(0xFFF1F5F7)
    val brandBlue = Color(0xFF005FFF)

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
                    .height(264.dp) // 디자인 스펙 반영
                    .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                    .background(Brush.verticalGradient(listOf(pal.primary, pal.primaryLight)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(R.drawable.senior_id), // drawable/senior_id.png
                            contentDescription = "프로필 사진",
                            modifier = Modifier
                                .size(104.dp) // 원 디자인 103.96
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = " ${data.name}님",
                                    color = if (level == 2) Color.Black else Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Spacer(Modifier.width(8.dp))
                                Image(
                                    painter = painterResource(id = badgeResFor(data.level)),
                                    contentDescription = "레벨 뱃지",
                                    modifier = Modifier.size(60.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }

            /* ----- 활동레벨 배너 카드 ----- */
            FloatingRoundedImageCard(
                resId = levelBannerResFor(data.level),
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
                    // 제목
                    Text(
                        "나의 활동",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    // 제목과 첫 번째 행 사이 간격 (예: 16dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 행들 (전체 간격은 Arrangement.spacedBy로)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp) // 전체 행 간격 (예: 12dp)
                    ) {
                        ActivityRow("1년 이내 일자리 지원", "${data.applyWithinYear}건", valueColor = brandBlue)
                        ActivityRow("실제 근무 경험", "${data.realWorkExpCount}건", valueColor = brandBlue)
                        ActivityRow("교육/강의 콘텐츠", if (data.eduCompleted) "수강 완료" else "수강 중", valueColor = brandBlue)
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
                    painter = painterResource(id = benefitsImageResFor(data.level)),
                    contentDescription = "레벨별 혜택 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    contentScale = ContentScale.FillWidth
                )
            }

            Spacer(Modifier.height(20.dp))
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

/* =============== 미리보기 (실구동과 동일하게 Route 호출) =============== */
@Preview(name = "Activity Level - Lv.1", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewActivityLevel_Lv1() {
    val nav = rememberNavController()
    ActivityLevelRoute(nav = nav, levelArg = 1)
}

@Preview(name = "Activity Level - Lv.2", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewActivityLevel_Lv2() {
    val nav = rememberNavController()
    ActivityLevelRoute(nav = nav, levelArg = 2)
}

@Preview(name = "Activity Level - Lv.3", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewActivityLevel_Lv3() {
    val nav = rememberNavController()
    ActivityLevelRoute(nav = nav, levelArg = 3)
}
