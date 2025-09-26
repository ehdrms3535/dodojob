@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.dodojob.ui.feature.employ

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R

/* ===== 컬러 ===== */
private val BrandBlue = Color(0xFF005FFF)
private val TextGray  = Color(0xFF828282)
private val LineGray  = Color(0xFFDDDDDD)
private val LabelGray = Color(0xFF9C9C9C)
private val BgGray    = Color(0xFFF1F5F7)

/* ===== 폰트 ===== */
private val PretendardSemi = FontFamily(Font(R.font.pretendard_semibold, FontWeight.SemiBold))
private val PretendardMed  = FontFamily(Font(R.font.pretendard_medium,  FontWeight.Medium))

object ApplicantFakeDB {
    val name = "김알바"
    val birth = "1964년 3월 15일"
    val phone = "010-1234-1234"
    val address = "경북 경산시"
    val email = "hong_11@naver.com"

    val careers = listOf(
        Triple("프랜차이즈 카페 점장", "2008.03", "2010.01"),
        Triple("기업체 인사/총무 담당 과장", "2010.01", "2020.06")
    )

    val licenses = listOf(
        Triple("한국서비스산업진흥원", "고객 서비스 매니저 1급 자격증", "CSM-2018-1103-1023"),
        Triple("대구문화센터", "문화·여가 프로그램 지도사 자격증", "CPC-2021-0420-0789"),
        Triple("대한시니어평생교육원", "시니어 케어 코디네이터 2급 자격증", "SCC-2020-0612-0457")
    )

    // ✅ 기본 희망직무 값 추가
    val defaultJob = "서비스업"
}



/* ===== 공통 컴포넌트 ===== */
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
            Text(
                text = title,
                fontSize = 18.sp,
                fontFamily = PretendardSemi,
                color = Color.Black
            )
        }
        Spacer(Modifier.height(16.dp))
        ThinDivider(insetStart = 4.dp, insetEnd = 4.dp) // ⬅️ 타이틀 밑에 선
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
            Text(
                label,
                fontSize = 16.sp,
                fontFamily = PretendardSemi,
                color = LabelGray,
                modifier = Modifier.weight(1f)
            )
            Text(
                value,
                fontSize = 16.sp,
                fontFamily = PretendardSemi,
                color = valueColor,
                textAlign = TextAlign.Right
            )
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun ThinDivider(insetStart: Dp = 4.dp, insetEnd: Dp = 4.dp) {
    Spacer(Modifier.height(6.dp))
    Divider(
        modifier = Modifier.padding(start = insetStart, end = insetEnd),
        color = LineGray,
        thickness = 1.dp
    )
    Spacer(Modifier.height(6.dp))
}

/* ===== 스크롤되는 헤더(흰 배경, 중앙 정렬 제목, 좌측 뒤로가기) ===== */
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
            // 왼쪽: 뒤로가기
            Icon(
                imageVector = Icons.Outlined.ArrowBackIosNew,
                contentDescription = "뒤로가기",
                tint = Color.Unspecified,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(20.dp)
                    .clickable { onBack() }
            )
            // 가운데: 제목 (정중앙)
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

/* ===== 메인 진입점 ===== */
@Composable
fun ApplicantInformationScreen(navController: NavController) {
    val scroll = rememberScrollState()

    var personalExpanded by remember { mutableStateOf(true) }
    var careerExpanded   by remember { mutableStateOf(true) }
    var licenseExpanded  by remember { mutableStateOf(true) }
    var hopeExpanded     by remember { mutableStateOf(true) }

    var selectedJob by remember { mutableStateOf(ApplicantFakeDB.defaultJob) }

    Scaffold(
        containerColor = BgGray,
        topBar = { } // ← 고정 AppBar 비움(헤더는 본문 첫 줄에 배치하여 스크롤과 함께 이동)
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(scroll)
        ) {
            // 스크롤되는 헤더
            ScrollHeader(
                title = "지원자 정보",
                onBack = { navController.popBackStack() }
            )

            Spacer(Modifier.height(12.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                /* ===== 인적사항 ===== */
                SectionCard {
                    SectionTitle(
                        title = " 인적사항",
                        iconRes = R.drawable.identification,
                        expanded = personalExpanded,
                        onToggle = { personalExpanded = !personalExpanded }
                    )
                    if (personalExpanded) {
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFE0E0E0))
                            )
                            Spacer(Modifier.width(18.dp))
                        }
                        Spacer(Modifier.height(16.dp))

                        KeyValueRow("이름",     ApplicantFakeDB.name)
                        KeyValueRow("생년월일", ApplicantFakeDB.birth)
                        KeyValueRow("전화번호", ApplicantFakeDB.phone)
                        KeyValueRow("주소",     ApplicantFakeDB.address)
                        KeyValueRow("이메일",   ApplicantFakeDB.email)
                    }
                }

                Spacer(Modifier.height(28.dp))

                /* ===== 경력 ===== */
                SectionCard {
                    SectionTitle(
                        title = " 경력",
                        iconRes = R.drawable.career,
                        expanded = careerExpanded,
                        onToggle = { careerExpanded = !careerExpanded }
                    )
                    if (careerExpanded) {
                        ApplicantFakeDB.careers.forEachIndexed { i, (title, start, end) ->
                            if (i > 0) {
                                Spacer(Modifier.height(16.dp))
                                ThinDivider()
                                Spacer(Modifier.height(16.dp))
                            } else {
                                Spacer(Modifier.height(8.dp))
                            }
                            CareerItem(title, start, end)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }

                Spacer(Modifier.height(28.dp))

                /* ===== 자격증 ===== */
                SectionCard {
                    SectionTitle(
                        title = " 자격증",
                        iconRes = R.drawable.license,
                        expanded = licenseExpanded,
                        onToggle = { licenseExpanded = !licenseExpanded }
                    )
                    if (licenseExpanded) {
                        ApplicantFakeDB.licenses.forEachIndexed { i, (org, title, code) ->
                            if (i > 0) {
                                Spacer(Modifier.height(16.dp))
                                ThinDivider()
                                Spacer(Modifier.height(16.dp))
                            } else {
                                Spacer(Modifier.height(8.dp))
                            }
                            LicenseItem(org, title, code)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }

                Spacer(Modifier.height(28.dp))

                /* ===== 희망직무 ===== */
                SectionCard {
                    // 드롭다운 아이콘 없이 제목 터치로 토글
                    SectionTitle(
                        title = " 희망직무",
                        iconRes = R.drawable.hope_work,
                        expanded = hopeExpanded,
                        onToggle = { hopeExpanded = !hopeExpanded }
                    )
                    if (hopeExpanded) {
                        Spacer(Modifier.height(20.dp))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            JobChip(
                                title = "서비스업",
                                desc = "매장관리, 고객 응대",
                                selected = selectedJob == "서비스업",
                                onClick = { selectedJob = "서비스업" },
                                modifier = Modifier.weight(1f)
                            )
                            JobChip(
                                title = "교육/강의",
                                desc = "강사, 지도사",
                                selected = selectedJob == "교육/강의",
                                onClick = { selectedJob = "교육/강의" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            JobChip(
                                title = "관리/운영",
                                desc = "시설, 인력관리",
                                selected = selectedJob == "관리/운영",
                                onClick = { selectedJob = "관리/운영" },
                                modifier = Modifier.weight(1f)
                            )
                            JobChip(
                                title = "돌봄서비스",
                                desc = "방문, 요양, 돌봄",
                                selected = selectedJob == "돌봄서비스",
                                onClick = { selectedJob = "돌봄서비스" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))
            }
        }
    }
}

/* ===== 보조 컴포넌트 ===== */
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
        Spacer(Modifier.height(2.dp))
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
                withStyle(SpanStyle(color = LabelGray,  fontFamily = PretendardMed, fontSize = 14.sp)) {
                    append("자격번호 ")
                }
                withStyle(SpanStyle(color = BrandBlue, fontFamily = PretendardMed, fontSize = 14.sp)) {
                    append(code)
                }
            }
        )
        Spacer(Modifier.height(2.dp))
    }
}

/* ===== 칩(설명 1~2줄) ===== */
private val ChipRadius = 14.dp
private val ChipBorder = Color(0xFFD9D9D9)
private val ChipTitleGray = Color(0xFF6B7280)
private val ChipDescGray  = Color(0xFF9CA3AF)

@Composable
private fun JobChip(
    title: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(ChipRadius)
    val bg   = if (selected) BrandBlue else Color.White
    val tCol = if (selected) Color.White else ChipTitleGray
    val dCol = if (selected) Color.White.copy(alpha = 0.9f) else ChipDescGray

    Column(
        modifier = modifier
            .border(
                width = 1.5.dp,
                color = if (selected) Color.Transparent else ChipBorder,
                shape = shape
            )
            .clip(shape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .heightIn(min = 64.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontFamily = PretendardSemi,
            color = tCol,
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = desc,
            fontSize = 12.sp,
            fontFamily = PretendardMed,
            color = dCol,
            lineHeight = 14.sp,
            maxLines = 2
        )
    }
}
