package com.example.dodojob.ui.feature.employ

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R

/* =========================
 * Fonts
 * ========================= */
private val PretendardSemiBold = FontFamily(Font(R.font.pretendard_semibold))
private val PretendardMedium   = FontFamily(Font(R.font.pretendard_medium))

/* =========================
 * Colors
 * ========================= */
private val ScreenBg   = Color(0xFFF1F5F7)
private val White      = Color(0xFFFFFFFF)
private val LineGray   = Color(0xFFDDDDDD)
private val TextGray   = Color(0xFF828282)
private val TitleBlack = Color(0xFF000000)
private val BrandBlue  = Color(0xFF005FFF)

/* =========================
 * Layout
 * ========================= */
private val SIDE = 16.dp
private val CardShape = RoundedCornerShape(10.dp)
private val InfoLabelWidth: Dp = 80.dp

/* 고정 높이 계산용 상수 (필요 시 숫자만 조절) */
private val TitleRowHeight = 36.dp            // 아이콘+타이틀 라인 높이
private val InnerVerticalPadding = 16.dp * 2  // SectionCard 내부 위/아래 패딩 합
private val DividerThickness = 1.dp           // Divider 두께
private val BetweenItemsGap = 25.dp           // Divider 후 항목 사이 여백
private val FirstGapAfterTopDivider = BetweenItemsGap // Top Divider 밑 첫 간격
private val CareerItemHeight  = 56.dp         // 경력 항목 1개 고정 높이
private val LicenseItemHeight = 56.dp         // 자격증 항목 1개 고정 높이

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ApplicantInformationScreen(navController: NavController) {
    val data = remember { fakeApplicantDetail() }

    /* 카드 고정 높이 계산식
     * height =
     *   InnerVerticalPadding + TitleRowHeight
     * + DividerThickness + FirstGapAfterTopDivider
     * + (itemHeight * n)
     * + (DividerThickness + BetweenItemsGap) * (n - 1)
     */
    val careerItems = data.careers.size
    val licenseItems = data.licenses.size

    val careerCardHeight =
        InnerVerticalPadding + TitleRowHeight + DividerThickness + FirstGapAfterTopDivider +
                (CareerItemHeight * careerItems) +
                (if (careerItems > 1) (DividerThickness + BetweenItemsGap) * (careerItems - 1) else 0.dp)

    val licenseCardHeight =
        InnerVerticalPadding + TitleRowHeight + DividerThickness + FirstGapAfterTopDivider +
                (LicenseItemHeight * licenseItems) +
                (if (licenseItems > 1) (DividerThickness + BetweenItemsGap) * (licenseItems - 1) else 0.dp)

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(White)
                    .padding(horizontal = SIDE)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "뒤로가기", tint = Color.Black)
                }
                Text(
                    text = "지원자 정보",
                    fontFamily = PretendardSemiBold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    lineHeight = 36.sp,
                    letterSpacing = (-0.019).em,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = SIDE, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            /* 상단 Status Bar */
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(Color(0xFFEFEFEF))
                )
            }

            /* 인적사항 */
            item {
                SectionCard(
                    iconRes = R.drawable.identification,
                    iconBg = Color(0xFFFFDEDF),
                    title = "인적사항"
                ) {
                    // 사진은 좌측, 그 아래 Key-Value
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF3F3F3))
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Column(Modifier.fillMaxWidth()) {
                        InfoRow("이름", data.name)
                        InfoRow("생년월일", data.birth)
                        InfoRow("전화번호", data.phone)
                        InfoRow("주소", data.address)
                        InfoRow("이메일", data.email, bold = true)
                    }
                }
            }

            /* 경력 — 고정 높이 */
            item {
                SectionCard(
                    iconRes = R.drawable.career,
                    iconBg = Color(0xFFE6FFDE),
                    title = "경력",
                    modifier = Modifier.height(careerCardHeight)
                ) {
                    Divider(color = LineGray, thickness = DividerThickness)
                    Spacer(Modifier.height(FirstGapAfterTopDivider))  // 첫 항목도 동일 간격
                    Column(Modifier.fillMaxWidth()) {
                        data.careers.forEachIndexed { i, c ->
                            CareerItemBlock(c, CareerItemHeight)
                            if (i != data.careers.lastIndex) {
                                Divider(
                                    color = LineGray.copy(alpha = 0.6f),
                                    thickness = DividerThickness
                                )
                                Spacer(Modifier.height(BetweenItemsGap))
                            }
                        }
                    }
                }
            }

            /* 자격증 — 고정 높이 */
            item {
                SectionCard(
                    iconRes = R.drawable.license,
                    iconBg = Color(0xFFEDDEFF),
                    title = "자격증",
                    modifier = Modifier.height(licenseCardHeight)
                ) {
                    Divider(color = LineGray, thickness = DividerThickness)
                    Spacer(Modifier.height(FirstGapAfterTopDivider))  // 첫 항목도 동일 간격
                    Column(Modifier.fillMaxWidth()) {
                        data.licenses.forEachIndexed { i, l ->
                            LicenseItemBlock(l, LicenseItemHeight)
                            if (i != data.licenses.lastIndex) {
                                Divider(
                                    color = LineGray.copy(alpha = 0.6f),
                                    thickness = DividerThickness
                                )
                                Spacer(Modifier.height(BetweenItemsGap))
                            }
                        }
                    }
                }
            }

            /* 희망직무 */
            item {
                SectionCard(
                    iconRes = R.drawable.hope_work,
                    iconBg = Color(0xFFFFF0FA),
                    title = "희망직무"
                ) {
                    FlowRow(
                        maxItemsInEachRow = 2,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        data.hopeJobs.forEachIndexed { idx, job ->
                            HopeJobPill(
                                title = job.category,
                                subtitle = job.detail,
                                selected = idx == 0
                            )
                        }
                    }
                }
            }
        }
    }
}

/* =========================
 * 섹션 카드 공통
 * ========================= */
@Composable
private fun SectionCard(
    iconRes: Int,
    iconBg: Color,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // 타이틀 행 높이 고정
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TitleRowHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    fontFamily = PretendardSemiBold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = TitleBlack
                )
            }
            content()
        }
    }
}

/* =========================
 * 경력/자격증 아이템 블록 (고정 높이)
 * ========================= */
@Composable
private fun CareerItemBlock(item: ApplicantDetailUi.Career, height: Dp) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            item.org,
            fontFamily = PretendardSemiBold,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = TitleBlack,
            textAlign = TextAlign.Start
        )
        Spacer(Modifier.height(4.dp))
        Text(
            item.period,
            fontFamily = PretendardMedium,
            fontSize = 14.sp,
            color = BrandBlue,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun LicenseItemBlock(item: ApplicantDetailUi.License, height: Dp) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            item.issuer,
            fontFamily = PretendardMedium,
            fontSize = 13.sp,
            color = Color(0xFF9C9C9C),
            textAlign = TextAlign.Start
        )
        Spacer(Modifier.height(4.dp))
        Text(
            item.title,
            fontFamily = PretendardSemiBold,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = TitleBlack,
            textAlign = TextAlign.Start
        )
    }
}

/* =========================
 * 인적사항 Key–Value
 * ========================= */
@Composable
private fun InfoRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontFamily = PretendardMedium,
            fontSize = 14.sp,
            color = TextGray,
            modifier = Modifier.width(InfoLabelWidth),
            textAlign = TextAlign.Start
        )
        Text(
            value,
            fontFamily = if (bold) PretendardSemiBold else PretendardMedium,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 15.sp,
            color = if (bold) TitleBlack else Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

/* =========================
 * 희망직무 Pill
 * ========================= */
@Composable
private fun HopeJobPill(title: String, subtitle: String, selected: Boolean) {
    val bg = if (selected) BrandBlue else White
    val borderColor = if (selected) BrandBlue else Color(0xFF828282)
    val textColor = if (selected) White else Color(0xFF828282)

    Column(
        modifier = Modifier
            .width(138.dp)
            .height(70.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(title, fontFamily = PretendardSemiBold, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textColor)
        Text(subtitle, fontFamily = PretendardMedium, fontSize = 12.sp, color = textColor)
    }
}

/* =========================
 * 더미(DB 대체)
 * ========================= */
private data class ApplicantDetailUi(
    val name: String,
    val birth: String,
    val phone: String,
    val address: String,
    val email: String,
    val careers: List<Career>,
    val licenses: List<License>,
    val hopeJobs: List<HopeJob>
) {
    data class Career(val org: String, val period: String)
    data class License(val issuer: String, val title: String)
    data class HopeJob(val category: String, val detail: String)
}

private fun fakeApplicantDetail() = ApplicantDetailUi(
    name = "김말바",
    birth = "1962년 3월 15일",
    phone = "010-1234-1234",
    address = "경북 경산시",
    email = "Hong_11@naver.com",
    careers = listOf(
        ApplicantDetailUi.Career("프랜차이즈 카페 점장", "2008.03 ~ 2010.01"),
        ApplicantDetailUi.Career("기업체 인사/총무 담당 과장", "2010.01 ~ 2020.06")
    ),
    licenses = listOf(
        ApplicantDetailUi.License("한국서비스산업진흥원", "고객 서비스 매니저 1급 자격증"),
        ApplicantDetailUi.License("대구문화센터", "문화·여가 프로그램 지도사 자격증"),
        ApplicantDetailUi.License("대한시니어평생교육원", "시니어 케어 코디네이터 2급 자격증")
    ),
    hopeJobs = listOf(
        ApplicantDetailUi.HopeJob("서비스업", "매장관리, 고객 응대"),
        ApplicantDetailUi.HopeJob("교육/강의", "문화센터 강사"),
        ApplicantDetailUi.HopeJob("관리/운영", "시설, 인력관리"),
        ApplicantDetailUi.HopeJob("돌봄서비스", "노인, 시니어 돌봄")
    )
)
