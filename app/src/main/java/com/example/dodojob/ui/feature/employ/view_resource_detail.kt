@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.dodojob.ui.feature.employ

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R

/* ===== Colors / Fonts ===== */
private val BrandBlue = Color(0xFF005FFF)
private val TextGray  = Color(0xFF828282)
private val LineGray  = Color(0xFFDDDDDD)
private val LabelGray = Color(0xFF9C9C9C)
private val BgGray    = Color(0xFFF1F5F7)

private val PretendardSemi = FontFamily(Font(R.font.pretendard_semibold, FontWeight.SemiBold))
private val PretendardMed  = FontFamily(Font(R.font.pretendard_medium,  FontWeight.Medium))

/* ===== 상세 섹션용 FakeDB ===== */
// information_of_applicants에 정의되어 있다고 가정

/* ===== 상세 화면 ===== */
@Composable
fun ViewResourceDetailScreen(navController: NavController) {
    // 목록에서 넘겨준 TalentUi 사용 (없으면 기본 값)
    val passedTalent = navController.previousBackStackEntry?.savedStateHandle?.get<TalentUi>("talent")
    var talent by rememberSaveable { mutableStateOf(passedTalent) }
    val safeTalent = talent ?: TalentUi(
        name = "홍길동", gender = "여", age = 70, seniorLevel = 3,
        intro = "열심히 일 할 수 있습니다.", expYears = "34년",
        location = "서울 전체", jobCategories = listOf("매장관리", "고객 응대"), updatedMinutesAgo = "0"
    )

    val scroll = rememberScrollState()
    var personalExpanded by remember { mutableStateOf(true) }
    var careerExpanded   by remember { mutableStateOf(true) }
    var licenseExpanded  by remember { mutableStateOf(true) }
    var isFavorite       by remember { mutableStateOf(false) }

    Scaffold(containerColor = BgGray, topBar = { }) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(scroll)
        ) {
            ScrollHeader(title = "인재 상세보기", onBack = { navController.popBackStack() })
            Spacer(Modifier.height(12.dp))

            /* 헤더 카드 */
            TalentHeaderCard(
                name = safeTalent.name,
                gender = safeTalent.gender,
                age = safeTalent.age,
                seniorLevel = safeTalent.seniorLevel,
                intro = safeTalent.intro,
                expYears = safeTalent.expYears
            )

            Spacer(Modifier.height(16.dp))

            // ===== 패딩 있는 컨텐츠 영역 =====
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                /* 인적사항 */
                SectionCard {
                    SectionTitle(
                        title = " 인적사항",
                        iconRes = R.drawable.identification,
                        expanded = personalExpanded,
                        onToggle = { personalExpanded = !personalExpanded }
                    )
                    if (personalExpanded) {
                        KeyValueRow("이름",     ApplicantFakeDB.name)
                        KeyValueRow("생년월일", ApplicantFakeDB.birth)
                        KeyValueRow("전화번호", ApplicantFakeDB.phone)
                        KeyValueRow("주소",     ApplicantFakeDB.address)
                        KeyValueRow("이메일",   ApplicantFakeDB.email)
                    }
                }

                Spacer(Modifier.height(28.dp))

                /* 경력 */
                SectionCard {
                    SectionTitle(
                        title = " 경력",
                        iconRes = R.drawable.career,
                        expanded = careerExpanded,
                        onToggle = { careerExpanded = !careerExpanded }
                    )
                    if (careerExpanded) {
                        ApplicantFakeDB.careers.forEachIndexed { i, (title, start, end) ->
                            if (i > 0) { Spacer(Modifier.height(16.dp)); ThinDivider(); Spacer(Modifier.height(16.dp)) }
                            CareerItem(title, start, end)
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                /* 자격증 */
                SectionCard {
                    SectionTitle(
                        title = " 자격증",
                        iconRes = R.drawable.license,
                        expanded = licenseExpanded,
                        onToggle = { licenseExpanded = !licenseExpanded }
                    )
                    if (licenseExpanded) {
                        ApplicantFakeDB.licenses.forEachIndexed { i, (org, title, code) ->
                            if (i > 0) { Spacer(Modifier.height(16.dp)); ThinDivider(); Spacer(Modifier.height(16.dp)) }
                            LicenseItem(org, title, code)
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                /* 희망직무 카드 (흰 박스 안 / 파란 아웃라인 칩) */
                HopeJobCard(
                    jobs = safeTalent.jobCategories,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))
                // ⛔️ 하단 바는 여기(패딩 Column) 안에 넣지 않음
            }

            // ===== edge-to-edge 하단 바: 패딩 없는 루트 Column 레벨 =====
            BottomActionBar(
                isFavorite = isFavorite,
                onFavoriteToggle = { isFavorite = it },
                onInviteClick = { /* TODO: 면접 제의하기 */ },
                modifier = Modifier.fillMaxWidth()
            )
            // ⛔️ 아래 불필요 스페이서 제거 (회색 여백 원인)
        }
    }
}

/* ===== 헤더 카드 ===== */
@Composable
private fun TalentHeaderCard(
    name: String,
    gender: String,
    age: Int,
    seniorLevel: Int,
    intro: String,
    expYears: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .heightIn(min = 96.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top
        ) {
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

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(maskName(name), fontSize = 16.sp, fontFamily = PretendardSemi, color = Color.Black)
                    Spacer(Modifier.width(6.dp))
                    Text("(${gender}, ${age}세)", fontSize = 13.sp, fontFamily = PretendardMed, color = TextGray)
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = medalResForLevel(seniorLevel)),
                        contentDescription = "medal",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text("“$intro”", fontSize = 14.sp, fontFamily = PretendardSemi, color = Color.Black)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.weight(1f))
                    Text("경력 ${expYears}년", fontSize = 13.sp, fontFamily = PretendardMed, color = BrandBlue)
                }
            }
        }
    }
}

/* ===== 희망직무 카드 ===== */
@Composable
private fun HopeJobCard(
    jobs: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 20.dp)   // SectionCard와 동일 패딩
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.hope_work),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("희망직무", fontSize = 18.sp, fontFamily = PretendardSemi, color = Color.Black)
        }

        Spacer(Modifier.height(16.dp))
        ThinDivider(insetStart = 4.dp, insetEnd = 4.dp)
        Spacer(Modifier.height(16.dp))


        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            jobs.forEach { HopeJobChipOutlined(it) }
        }
    }
}

@Composable
private fun HopeJobChipOutlined(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(width = 1.5.dp, color = BrandBlue, shape = RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = text, fontSize = 14.sp, fontFamily = PretendardMed, color = BrandBlue)
    }
}

/* ===== 하단 액션바 (edge-to-edge, 회색 여백 제거) ===== */
@Composable
private fun BottomActionBar(
    isFavorite: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    onInviteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White) // 배경을 Box에 깔아 내비 영역까지 흰색 유지
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp) // 내부 여백만
                .navigationBarsPadding(),                      // 제스처/내비 영역 보정 (배경은 그대로 흰색)
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onFavoriteToggle(!isFavorite) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (isFavorite) R.drawable.full_star else R.drawable.empty_grey_star
                    ),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(28.dp)
                )
            }

            Button(
                onClick = onInviteClick,
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("면접 제의하기", fontSize = 16.sp, fontFamily = PretendardSemi, color = Color.White)
            }
        }
    }
}

/* ===== 공통 섹션 ===== */
@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) { content() }
}

@Composable
private fun SectionTitle(
    title: String,
    iconRes: Int,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(title, fontSize = 18.sp, fontFamily = PretendardSemi, color = Color.Black)
        }
        Spacer(Modifier.height(16.dp))
        ThinDivider(insetStart = 4.dp, insetEnd = 4.dp)
    }
}

@Composable
private fun KeyValueRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    startPadding: Dp = 8.dp,
    endPadding: Dp = 8.dp
) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = startPadding, end = endPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 16.sp, fontFamily = PretendardSemi, color = LabelGray, modifier = Modifier.weight(1f))
            Text(value, fontSize = 16.sp, fontFamily = PretendardSemi, color = valueColor, textAlign = TextAlign.Right)
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun ThinDivider(insetStart: Dp = 4.dp, insetEnd: Dp = 4.dp) {
    Divider(
        modifier = Modifier.padding(start = insetStart, end = insetEnd),
        color = LineGray,
        thickness = 1.dp
    )
}

@Composable
private fun ScrollHeader(title: String, onBack: () -> Unit) {
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
                fontFamily = PretendardSemi,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center),
                maxLines = 1
            )
        }
        Divider(color = LineGray, thickness = 1.dp)
    }
}

/* ===== 섹션 아이템 ===== */
@Composable
private fun CareerItem(title: String, start: String, end: String) {
    Column(Modifier.padding(horizontal = 8.dp)) {
        Text(title, fontSize = 18.sp, fontFamily = PretendardSemi, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(start, color = BrandBlue, fontSize = 14.sp, fontFamily = PretendardMed)
            Text(" ~ ",  color = TextGray,  fontSize = 14.sp, fontFamily = PretendardMed)
            Text(end,   color = BrandBlue, fontSize = 14.sp, fontFamily = PretendardMed)
        }
    }
}

@Composable
private fun LicenseItem(org: String, title: String, code: String) {
    Column(Modifier.padding(horizontal = 8.dp)) {
        Text(org, fontSize = 12.sp, color = Color(0xFF616161), fontFamily = PretendardMed)
        Spacer(Modifier.height(6.dp))
        Text(title, fontSize = 18.sp, fontFamily = PretendardSemi, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = LabelGray, fontFamily = PretendardMed, fontSize = 14.sp)) { append("자격번호 ") }
                withStyle(SpanStyle(color = BrandBlue, fontFamily = PretendardMed, fontSize = 14.sp)) { append(code) }
            }
        )
    }
}

/* ===== Utils ===== */
private fun maskName(name: String) = if (name.isNotEmpty()) name.first() + "**" else "**"
private fun medalResForLevel(level: Int): Int = when (level) {
    1 -> R.drawable.red_medal
    2 -> R.drawable.yellow_medal
    else -> R.drawable.blue_medal
}
