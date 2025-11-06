@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dodojob.ui.feature.employ

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.example.dodojob.R

/* =============== Colors =============== */
private val ScreenBg   = Color(0xFFF1F5F7)
private val BrandBlue  = Color(0xFF005FFF)
private val TextGray   = Color(0xFF828282)
private val CardBg     = Color(0xFFFFFFFF)

private val TitleBlack = Color(0xFF000000)
private val DividerGray= Color(0xFFE6E9ED)

/* =============== Fake Repo (list) =============== */
object FakeTalentRepoforScrapped {
    data class Talent(
        val name: String, val gender: String, val age: Int,
        val seniorLevel: Int, val intro: String, val expYears: String,
        val location: String, val jobCategories: List<String>, val updatedMinutesAgo: String
    )
    fun getTalents(): List<Talent> = listOf(
        Talent("안은영","여",70,3,"열심히 일 할 수 있습니다.", "34년", "대구광역시 서구", listOf("고객응대","청결 관리","환경미화","사서 보조"), "5"),
        Talent("김영수","남",62,3,"성실합니다", "8년","서울 전체", listOf("서비스 기타","보조출연","사무보조"), "12"),
        Talent("이수정","여",28,2,"고객 응대에 자신", "3년", "부산 전체", listOf("매장관리","CS"), "18")
    )
}

/* =============== Screen: List =============== */
@Composable
fun ScrappedHumanResourceScreen(nav: NavController) {
    val talents = remember {
        FakeTalentRepoforScrapped.getTalents().map {
            TalentUi(
                it.name, it.gender, it.age, it.seniorLevel,
                it.intro, it.expYears, it.location, it.jobCategories, it.updatedMinutesAgo
            )
        }
    }

    var sort by remember { mutableStateOf("업데이트순") }
    val sortOptions = listOf("업데이트순", "이름순", "경력순")
    val totalCountForHeader = talents.size

    Scaffold(containerColor = ScreenBg) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 👇 ScrollHeader + SortBar 를 하나의 item으로 묶음
            item {
                Column {
                    ScrollHeader(
                        title = "스크랩한 인재",
                        onBack = { nav.popBackStack() }
                    )
                    SortBar(
                        totalCount = totalCountForHeader,
                        sort = sort,
                        sortOptions = sortOptions,
                        onSortChange = { sort = it },
                        onFilterClick = { /* TODO */ }
                    )
                }
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

/* =============== ScrollHeader =============== */
@Composable
private fun ScrollHeader(
    title: String,
    onBack: () -> Unit
) {
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .height(70.dp)
                .padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBackIosNew,
                contentDescription = "뒤로가기",
                tint = Color.Unspecified,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(20.dp)
                    .clickable { onBack() }
            )
            Text(
                text = title,
                fontSize = 28.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center),
                maxLines = 1
            )
        }
        Divider(color = DividerGray, thickness = 1.dp)
    }
}

/* =============== Sort Bar (여백 축소 버전) =============== */
@Composable
private fun SortBar(
    totalCount: Int,
    sort: String,
    sortOptions: List<String>,
    onSortChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ScreenBg)
            .padding(horizontal = 16.dp, vertical = 6.dp), // 상하 여백 축소
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
        TextButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            modifier = Modifier.heightIn(min = 32.dp)
        ) {
            Text(
                sort,
                fontFamily = Pretendard,
                fontSize = 14.sp,
                color = TextGray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.caret_down),
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(4.dp))

    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        sortOptions.forEach { opt ->
            DropdownMenuItem(
                text = { Text(opt, fontFamily = Pretendard, fontSize = 14.sp) },
                onClick = { onSortChange(opt); expanded = false }
            )
        }
    }
}

/* =============== Talent Card (원래 모양 + 하단 3버튼) =============== */
@Composable
private fun TalentCard(
    data: TalentUi,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .padding(start = 16.dp, end=16.dp,bottom = 12.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        onClick = onClick
    ) {
        Column(Modifier.fillMaxWidth()) {
            // --- 원래 본문 유지 ---
            Box(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            Text("(${data.gender}, ${data.age}세)", fontSize = 14.sp, fontFamily = Pretendard, color = TextGray)
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                painterResource(medalResForLevel(data.seniorLevel)),
                                contentDescription = "medal",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text("“${data.intro}”", fontFamily = Pretendard, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
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

            }

            // --- 하단 버튼 3개 + 구분선 ---
            ThinDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton("면접제안", Modifier.weight(1f)) { /* TODO */ }
                ThinDivider(
                    modifier = Modifier
                        .height(30.dp)
                        .align(Alignment.CenterVertically)
                        .width(1.dp)
                )
                ActionButton("문자", Modifier.weight(1f)) { /* TODO */ }
                ThinDivider(
                    modifier = Modifier
                        .height(30.dp)
                        .align(Alignment.CenterVertically)
                        .width(1.dp)
                )
                ActionButton("전화", Modifier.weight(1f)) { /* TODO */ }
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier.clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = Pretendard,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextGray
        )
    }
}

/* =============== Utils =============== */
    @Composable
    fun ThinDivider(
        modifier: Modifier = Modifier,
        color: Color = DividerGray,
        thickness: Dp = 1.dp
    ) {
        Divider(color = color, modifier = modifier, thickness = thickness)
    }

private fun formatWithComma(n: Int): String = "%,d".format(n)
private fun maskName(name: String) = if (name.isNotEmpty()) name.first() + "**" else "**"
@DrawableRes
private fun medalResForLevel(level: Int): Int = when (level) {
    1 -> R.drawable.red_medal
    2 -> R.drawable.yellow_medal
    else -> R.drawable.blue_medal
}

private fun NavController.safeNavigate(
    route: String,
    builder: (NavOptionsBuilder.() -> Unit)? = { launchSingleTop = true; restoreState = true }
) {
    navigate(route) { builder?.invoke(this) }
}
