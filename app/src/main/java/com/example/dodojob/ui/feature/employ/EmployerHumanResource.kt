package com.example.dodojob.ui.feature.employ

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import com.example.dodojob.ui.feature.main.EmployerBottomNavBar // 임포트 사용

/* ================= Font ================= */
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular,  FontWeight.Normal),
    Font(R.font.pretendard_medium,   FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold,     FontWeight.Bold)
)

/* ================= Colors ================= */
private val ScreenBg   = Color(0xFFF1F5F7)
private val BrandBlue  = Color(0xFF005FFF)
private val TextGray   = Color(0xFF828282)
private val CardBg     = Color(0xFFFFFFFF)
private val TitleBlack = Color(0xFF000000)
private val BarGray    = Color(0xFFE7EDF1)
private val DividerGray= Color(0xFFE6E9ED)

/* ================= Fake DB (확장) ================= */
object FakeTalentRepo {
    data class Talent(
        val name: String, val gender: String, val age: Int,
        val seniorLevel: Int, val intro: String, val expYears: Int,
        val location: String, val jobCategories: List<String>, val updatedMinutesAgo: Int
    )
    fun getTalents(): List<Talent> = listOf(
        Talent("안은영","여",70,3,"열심히 일 할 수 있습니다.", 34, "대구광역시 서구", listOf("고객응대","청결 관리","환경미화","사서 보조"), 5),
        Talent("김영수","남",62,3,"성실합니다", 8, "서울 전체", listOf("서비스 기타","보조출연","사무보조"), 12),
        Talent("이수정","여",28,2,"고객 응대에 자신", 3, "부산 전체", listOf("매장관리","CS"), 18),
        Talent("박민재","남",33,1,"빠른 적응, 꼼꼼함", 6, "대구 전체", listOf("사무보조"), 25),
        Talent("정다연","여",41,3,"책임감 있게 합니다", 10, "인천 전체", listOf("총무","행정"), 7),
        Talent("최우석","남",36,2,"팀워크 좋아요", 5, "경기 남부", listOf("물류","창고관리"), 15),
        Talent("한가을","여",24,1,"배우면서 성장할게요", 1, "서울 강북", listOf("카운터","매장보조"), 9),
        Talent("고상진","남",48,2,"정확하고 신속하게", 12, "대전 전체", listOf("배송","물류보조"), 3),
    )
}

/* ================= UI 모델 ================= */
data class TalentUi(
    val name: String,
    val gender: String,
    val age: Int,
    val seniorLevel: Int,
    val intro: String,
    val expYears: Int,
    val location: String,
    val jobCategories: List<String>,
    val updatedMinutesAgo: Int
)

/* ================= Screen ================= */
@Composable
fun EmployerHumanResourceScreen(nav: NavController) {
    val talents = remember {
        FakeTalentRepo.getTalents().map {
            TalentUi(it.name, it.gender, it.age, it.seniorLevel, it.intro, it.expYears, it.location, it.jobCategories, it.updatedMinutesAgo)
        }
    }

    var sort by remember { mutableStateOf("업데이트순") }
    val sortOptions = listOf("업데이트순", "이름순", "경력순")
    val totalCountForHeader = 23_400

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            EmployerBottomNavBar(
                current = "human_resource",
                onClick = { key ->
                    when (key) {
                        "home"           -> nav.safeNavigate(Route.EmployerHome.path)
                        "notice"         -> nav.safeNavigate(Route.EmployerNotice.path)
                        "applicant"      -> nav.safeNavigate(Route.EmployerApplicant.path)
                        "human_resource" -> nav.safeNavigate(Route.EmployerHumanResource.path)
                        "my"             -> nav.safeNavigate(Route.EmployerMy.path)
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
            item {
                TopSection(
                    totalCount = totalCountForHeader,
                    sortOptions = sortOptions,
                    sort = sort,
                    onSortChange = { sort = it },
                    onStarClick = { /* TODO */ },
                    onFilterClick = { /* TODO */ }
                )
            }

            items(talents.size) { idx ->
                TalentCard(data = talents[idx])
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/* ================= Top Section ================= */
@Composable
private fun TopSection(
    totalCount: Int,
    sortOptions: List<String>,
    sort: String,
    onSortChange: (String) -> Unit,
    onStarClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
    ) {
        // 1) 타이틀 + 빈 별
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "인재",
                fontFamily = Pretendard,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TitleBlack,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onStarClick) {
                Image(
                    painter = painterResource(R.drawable.empty_star),
                    contentDescription = "즐겨찾기",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // ───────── divider: 인재 ↔ 조건설정
        Divider(color = DividerGray, thickness = 0.5.dp)

        // 2) 맞춤 조건 + 슬라이더 (배경 흰색)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(CardBg)
                .padding(horizontal = 26.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "맞춤 조건을 설정해보세요.",
                fontFamily = Pretendard,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextGray,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onFilterClick() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_sliders),
                    contentDescription = "조건 설정",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // ───────── divider: 조건설정 ↔ 정렬바
        Divider(color = DividerGray, thickness = 0.5.dp)

        // 3) 요약바: 총 n개 · 정렬 드롭다운 (배경 ScreenBg)
        var expanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ScreenBg)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "총 ${formatWithComma(totalCount)}개",
                fontFamily = Pretendard,
                fontSize = 13.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { expanded = true }) {
                Text(
                    text = sort,
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    color = TextGray,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    painter = painterResource(R.drawable.caret_down),
                    contentDescription = null,
                    tint = TextGray
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                sortOptions.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt, fontFamily = Pretendard, fontSize = 14.sp) },
                        onClick = { onSortChange(opt); expanded = false }
                    )
                }
            }
        }
    }
}


/* ================= Talent Card ================= */
@Composable
private fun TalentCard(data: TalentUi) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽 프로필 + 경력(줄바꿈)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFDEEAFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.basic_profile),
                            contentDescription = "profile",
                            modifier = Modifier.size(75.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = BrandBlue, fontWeight = FontWeight.SemiBold)) {
                                append("경력")
                            }
                            append("\n")
                            withStyle(SpanStyle(color = BrandBlue, fontWeight = FontWeight.Medium)) {
                                append("${data.expYears}년")
                            }
                        },
                        fontFamily = Pretendard,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }

                Spacer(Modifier.width(20.dp))

                // 오른쪽 정보
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${maskName(data.name)} (${data.gender}, ${data.age}세)",
                            fontFamily = Pretendard,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Spacer(Modifier.width(6.dp))
                        Image(
                            painter = painterResource(id = medalResForLevel(data.seniorLevel)),
                            contentDescription = "medal",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "“${data.intro}”",
                        fontFamily = Pretendard,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.location),
                            contentDescription = "location",
                            modifier = Modifier.size(14.dp),
                            colorFilter = ColorFilter.tint(TextGray)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = data.location,
                            fontFamily = Pretendard,
                            fontSize = 13.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.cargo),
                            contentDescription = "jobs",
                            modifier = Modifier.size(14.dp),
                            colorFilter = ColorFilter.tint(TextGray)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = data.jobCategories.joinToString(", "),
                            fontFamily = Pretendard,
                            fontSize = 13.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Text(
                text = "${data.updatedMinutesAgo}분전",
                fontFamily = Pretendard,
                fontSize = 11.sp,
                color = TextGray,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 20.dp)
            )
        }
    }
}

/* ================= Utils ================= */
private fun formatWithComma(n: Int): String = "%,d".format(n)

private fun maskName(name: String): String =
    if (name.isNotEmpty()) name.first() + "**" else "**"

@DrawableRes
private fun medalResForLevel(level: Int): Int = when (level) {
    1 -> R.drawable.red_medal
    2 -> R.drawable.yellow_medal
    else -> R.drawable.blue_medal
}

private fun NavController.safeNavigate(
    route: String,
    builder: (NavOptionsBuilder.() -> Unit)? = {
        launchSingleTop = true
        restoreState = true
    }
) { navigate(route) { builder?.invoke(this) } }

/* ============ 리소스 체크 ============
res/font/
- pretendard_regular.ttf, pretendard_medium.ttf, pretendard_semibold.ttf, pretendard_bold.ttf

res/drawable/
- basic_profile.png
- red_medal.png, yellow_medal.png, blue_medal.png
- empty_star.png
- ic_sliders.png
- caret_down.png
- location.png
- cargo.png
*/
