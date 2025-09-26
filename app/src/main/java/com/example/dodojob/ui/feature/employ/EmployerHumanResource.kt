@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dodojob.ui.feature.employ

import android.os.Parcelable
import androidx.annotation.DrawableRes
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
import com.example.dodojob.ui.feature.main.EmployerBottomNavBar
import kotlinx.parcelize.Parcelize

/* =============== Fonts/Colors =============== */
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular,  FontWeight.Normal),
    Font(R.font.pretendard_medium,   FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold,     FontWeight.Bold)
)
private val ScreenBg   = Color(0xFFF1F5F7)
private val BrandBlue  = Color(0xFF005FFF)
private val TextGray   = Color(0xFF828282)
private val CardBg     = Color(0xFFFFFFFF)
private val TitleBlack = Color(0xFF000000)
private val DividerGray= Color(0xFFE6E9ED)

/* =============== Model =============== */
@Parcelize
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
) : Parcelable

/* =============== Fake Repo (list) =============== */
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

/* =============== Screen: List =============== */
@Composable
fun EmployerHumanResourceScreen(nav: NavController) {
    val talents = remember {
        FakeTalentRepo.getTalents().map {
            TalentUi(it.name, it.gender, it.age, it.seniorLevel, it.intro, it.expYears, it.location, it.jobCategories, it.updatedMinutesAgo)
        }
    }

    var sort by remember { mutableStateOf("업데이트순") }
    val sortOptions = listOf("업데이트순", "이름순", "경력순")
    val totalCountForHeader = talents.size

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
                    onStarClick = { nav.safeNavigate("scrapped_human_resource") },
                    onFilterClick = { /* TODO */ }
                )
            }

            items(talents.size) { idx ->
                val t = talents[idx]
                TalentCard(
                    data = t,
                    onClick = {
                        nav.currentBackStackEntry?.savedStateHandle?.set("talent", t)
                        nav.safeNavigate("view_resource_detail")
                    }
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/* =============== Top Section =============== */
@Composable
private fun TopSection(
    totalCount: Int,
    sortOptions: List<String>,
    sort: String,
    onSortChange: (String) -> Unit,
    onStarClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Column(Modifier.fillMaxWidth().background(CardBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("인재", fontFamily = Pretendard, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TitleBlack, modifier = Modifier.weight(1f))
            IconButton(onClick = onStarClick) {
                Icon(painterResource(R.drawable.empty_star), contentDescription = "즐겨찾기", tint = Color.Unspecified)
            }
        }
        Divider(color = DividerGray, thickness = 0.5.dp)
        Row(
            modifier = Modifier.fillMaxWidth().height(54.dp).background(CardBg).padding(horizontal = 26.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("맞춤 조건을 설정해보세요.", fontFamily = Pretendard, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextGray, modifier = Modifier.weight(1f))
            Box(Modifier.size(24.dp).clickable { onFilterClick() }, contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_sliders), contentDescription = "조건 설정", tint = Color.Unspecified)
            }
        }
        Divider(color = DividerGray, thickness = 0.5.dp)
        var expanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.fillMaxWidth().background(ScreenBg).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("총 ${formatWithComma(totalCount)}개", fontFamily = Pretendard, fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            TextButton(onClick = { expanded = true }) {
                Text(sort, fontFamily = Pretendard, fontSize = 14.sp, color = TextGray, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(4.dp))
                Icon(painterResource(R.drawable.caret_down), contentDescription = null, tint = TextGray)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                sortOptions.forEach { opt ->
                    DropdownMenuItem(text = { Text(opt, fontFamily = Pretendard, fontSize = 14.sp) }, onClick = { onSortChange(opt); expanded = false })
                }
            }
        }
    }
}

/* =============== Talent Card (list item) =============== */
@Composable
private fun TalentCard(
    data: TalentUi,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        onClick = onClick
    ) {
        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFDEEAFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.basic_profile),
                            contentDescription = "profile",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = BrandBlue, fontWeight = FontWeight.SemiBold)) { append("경력") }
                            append("\n")
                            withStyle(SpanStyle(color = BrandBlue, fontWeight = FontWeight.Medium)) { append("${data.expYears}년") }
                        },
                        fontFamily = Pretendard, fontSize = 11.sp, lineHeight = 14.sp
                    )
                }
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${maskName(data.name)}", fontFamily = Pretendard, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(6.dp))
                        Text("(${data.gender}, ${data.age}세)", fontSize = 15.sp, fontFamily = Pretendard, color = TextGray)
                        Spacer(Modifier.width(6.dp))
                        Icon(painterResource(medalResForLevel(data.seniorLevel)), contentDescription = "medal", tint = Color.Unspecified, modifier = Modifier.size(18.dp))
                    }
                    Text("“${data.intro}”", fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.location), null, tint = TextGray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(data.location, fontFamily = Pretendard, fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.Medium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.cargo), null, tint = TextGray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(data.jobCategories.joinToString(", "), fontFamily = Pretendard, fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Text("${data.updatedMinutesAgo}분전", fontFamily = Pretendard, fontSize = 11.sp, color = TextGray, modifier = Modifier.align(Alignment.TopEnd).padding(end = 20.dp))
        }
    }
}

/* =============== Utils =============== */
private fun formatWithComma(n: Int): String = "%,d".format(n)
private fun maskName(name: String) = if (name.isNotEmpty()) name.first() + "**" else "**"
@DrawableRes private fun medalResForLevel(level: Int): Int = when (level) {
    1 -> R.drawable.red_medal
    2 -> R.drawable.yellow_medal
    else -> R.drawable.blue_medal
}
private fun NavController.safeNavigate(
    route: String,
    builder: (NavOptionsBuilder.() -> Unit)? = { launchSingleTop = true; restoreState = true }
) { navigate(route) { builder?.invoke(this) } }
