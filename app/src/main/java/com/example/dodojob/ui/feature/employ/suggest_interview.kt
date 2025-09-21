package com.example.dodojob.ui.feature.employ

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R

/* =========================
 *  Fonts
 * ========================= */
private val PretendardMedium   = FontFamily(Font(R.font.pretendard_medium))
private val PretendardSemiBold = FontFamily(Font(R.font.pretendard_semibold))
private val PretendardBold     = FontFamily(Font(R.font.pretendard_bold))

/* =========================
 *  Spacing / Colors
 * ========================= */
private val SIDE       = 12.dp   // 좌우 패딩 축소
private val ScreenBg   = Color(0xFFF1F5F7)
private val White      = Color(0xFFFFFFFF)
private val BrandBlue  = Color(0xFF005FFF)
private val TextGray   = Color(0xFF828282)
private val LineGray   = Color(0xFFE5E7EB)

/* =========================
 *  모델
 * ========================= */
data class ApplicantUi(
    val id: Long,
    val name: String,
    val gender: String,
    val age: Int,
    val headline: String,
    val address: String,
    val careerYears: Int,
    val method: String,
    val postingTitle: String,
    val status: ApplicantStatus,
    val activityLevel: Int,
    val profileRes: Int = R.drawable.basic_profile
)
enum class ApplicantStatus { UNREAD, READ, SUGGESTING }
fun medalRes(level: Int): Int = when (level) {
    1 -> R.drawable.red_medal
    2 -> R.drawable.yellow_medal
    3 -> R.drawable.blue_medal
    else -> R.drawable.blue_medal
}

/* =========================
 *  폼 상태
 * ========================= */
enum class InterviewMethod { InPerson, Remote }
data class SuggestInterviewFormState(
    val method: InterviewMethod,
    val date: String,
    val time: String,
    val address: String,
    val addressDetail: String,
    val note: String
)

/* ======================================================================
 *  화면
 * ====================================================================== */
@Composable
fun SuggestInterviewScreen(navController: NavController) {
    val applicant = remember {
        ApplicantUi(
            id = 1,
            name = "김알바",
            gender = "여",
            age = 62,
            headline = "열정 넘치는 인재입니다!",
            address = "대구광역시 서구",
            careerYears = 5,
            method = "온라인지원",
            postingTitle = "[현대백화점 대구점] 주간 미화원 모집",
            status = ApplicantStatus.SUGGESTING,
            activityLevel = 3
        )
    }

    var method by remember { mutableStateOf(InterviewMethod.InPerson) }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var addressDetail by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        // 상단 상태바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color(0xFFEFEFEF))
        )

        // 앱바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(White)
                .padding(horizontal = SIDE),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "뒤로가기", tint = Color.Black)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "면접 제안",
                fontFamily = PretendardSemiBold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.019).em,
                color = Color.Black
            )
        }

        // 본문
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ===== 지원자 정보 =====
            SectionContainer {
                SectionHeader(
                    iconRes = R.drawable.information_of_applicants,
                    title = "지원자 정보"
                )
                Spacer(Modifier.height(10.dp))
                ApplicantInfoBox(
                    data = applicant,
                    modifier = Modifier.fillMaxWidth(),
                    onViewPostingClick = { /* TODO */ }
                )
            }

            SectionDivider()

            // ===== 면접 일정 =====
            SectionContainer {
                SectionHeader(
                    iconRes = R.drawable.schedule_of_interview,
                    title = "면접 일정"
                )
                Spacer(Modifier.height(10.dp))
                InputBlock(
                    label = "면접 날짜",
                    value = date,
                    onValueChange = { date = it },
                    placeholder = "2025.01.01"
                )
                Spacer(Modifier.height(10.dp))
                InputBlock(
                    label = "면접 시간",
                    value = time,
                    onValueChange = { time = it },
                    placeholder = "오전 9:00"
                )
            }

            SectionDivider()

            // ===== 면접 장소 =====
            SectionContainer {
                SectionHeader(
                    iconRes = R.drawable.location_of_interview,
                    title = "면접 장소"
                )
                Spacer(Modifier.height(10.dp))

                Text(
                    text = "면접 방식",
                    fontFamily = PretendardSemiBold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = (-0.019).em,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SIDE)
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SIDE),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MethodButton(
                        label = "대면 면접",
                        selected = method == InterviewMethod.InPerson,
                        onClick = { method = InterviewMethod.InPerson },
                        modifier = Modifier
                            .weight(1f)
                            .height(70.dp)
                    )
                    MethodButton(
                        label = "전화 면접",
                        selected = method == InterviewMethod.Remote,
                        onClick = { method = InterviewMethod.Remote },
                        modifier = Modifier
                            .weight(1f)
                            .height(70.dp)
                    )
                }

                Spacer(Modifier.height(15.dp))

                InputBlock(
                    label = "면접 장소",
                    value = address,
                    onValueChange = { address = it },
                    placeholder = "주소를 검색해주세요"
                )
                Spacer(Modifier.height(10.dp))
                InputBlock(
                    label = "상세주소",
                    value = addressDetail,
                    onValueChange = { addressDetail = it },
                    placeholder = "상세주소를 입력해주세요"
                )
            }

            SectionDivider()

            // ===== 안내사항 =====
            SectionContainer {
                SectionHeader(
                    iconRes = R.drawable.more_about,
                    title = "안내사항"
                )
                Spacer(Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SIDE),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "면접 안내사항",
                        fontFamily = PretendardSemiBold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        letterSpacing = (-0.019).em,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .height(112.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, BrandBlue, RoundedCornerShape(10.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        TextField(
                            value = note,
                            onValueChange = { note = it },
                            modifier = Modifier.fillMaxSize(),
                            placeholder = {
                                Text(
                                    text =
                                        "예시 :\n" +
                                                "• 신분증을 지참해주세요\n" +
                                                "• 편안한 복장으로 오셔도 됩니다\n" +
                                                "• 건물 1층 로비에서 면접 안내를 받으실 수 있어요",
                                    color = TextGray,
                                    fontSize = 15.sp,
                                    fontFamily = PretendardMedium,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 22.sp,
                                    letterSpacing = (-0.019).em
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = BrandBlue
                            ),
                            singleLine = false,
                            minLines = 3,
                            maxLines = 6,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 15.sp,
                                fontFamily = PretendardSemiBold,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 22.sp,
                                letterSpacing = (-0.019).em
                            )
                        )
                    }
                }
            }

            // ===== CTA =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = SIDE, vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val form = SuggestInterviewFormState(
                            method = method,
                            date = date.trim(),
                            time = time.trim(),
                            address = address.trim(),
                            addressDetail = addressDetail.trim(),
                            note = note.trim()
                        )
                        // TODO: 서버 전송
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(47.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = White
                    )
                ) {
                    Text(
                        text = "면접 제안 보내기",
                        fontFamily = PretendardSemiBold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        lineHeight = 27.sp,
                        letterSpacing = (-0.019).em
                    )
                }
            }

            // 하단 네비게이션바 placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(43.dp)
                    .background(Color(0xFFF4F5F7))
            )
        }
    }
}

/* ======================================================================
 *  재사용 컴포넌트
 * ====================================================================== */

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .background(ScreenBg)
    )
}

@Composable
private fun SectionContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun SectionHeader(iconRes: Int, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SIDE)
            .height(27.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            fontFamily = PretendardSemiBold,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 27.sp,
            letterSpacing = (-0.019).em,
            color = Color.Black
        )
    }
}

/* 면접 방식 버튼 (modifier 지원) */
@Composable
private fun MethodButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) BrandBlue.copy(alpha = 0.06f) else White
    val stroke = if (selected) BrandBlue else TextGray
    val textColor = if (selected) BrandBlue else TextGray

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, stroke, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontFamily = PretendardBold,  // Bold
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.019).em
        )
    }
}

/* 라벨 + OutlinedTextField (placeholder 확실) */
@Composable
private fun InputBlock(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SIDE),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            fontFamily = PretendardSemiBold,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.019).em,
            color = Color.Black
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    color = TextGray,
                    fontSize = 15.sp,
                    fontFamily = PretendardMedium,   // placeholder: Medium
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.019).em
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                unfocusedBorderColor = TextGray,
                cursorColor = BrandBlue,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 15.sp,
                fontFamily = PretendardSemiBold,   // 입력 텍스트: SemiBold
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.019).em
            )
        )
    }
}

/* =========================
 *  Outlined 지원자 정보 박스
 * ========================= */
@Composable
fun ApplicantInfoBox(
    data: ApplicantUi,
    modifier: Modifier = Modifier,
    onViewPostingClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .padding(horizontal = SIDE)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(10.dp))
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(Color(0xFFEAEFFB)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = data.profileRes),
                    contentDescription = "프로필",
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = data.name,
                        fontFamily = PretendardSemiBold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "(${data.gender}, ${data.age}세)",
                        fontFamily = PretendardSemiBold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextGray
                    )
                    Image(
                        painter = painterResource(id = medalRes(data.activityLevel)),
                        contentDescription = "활동레벨 메달",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "“${data.headline}”",
                    fontFamily = PretendardSemiBold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF5C5C5C)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = data.address,
                        fontFamily = PretendardSemiBold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }

                Row {
                    MetaLabel("경력"); Spacer(Modifier.width(8.dp)); MetaValue("${data.careerYears}년")
                }
                Row {
                    MetaLabel("지원"); Spacer(Modifier.width(8.dp)); MetaValue(data.method)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, LineGray, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .border(1.dp, LineGray, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "지원공고",
                    fontFamily = PretendardSemiBold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
            Spacer(Modifier.width(8.dp))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onViewPostingClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = " ${data.postingTitle} ",
                    fontFamily = PretendardSemiBold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF111827),
                    maxLines = 1
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/* 텍스트 스타일 유틸 */
@Composable private fun MetaLabel(text: String) = Text(
    text = text,
    fontSize = 13.sp,
    fontFamily = PretendardSemiBold,
    fontWeight = FontWeight.SemiBold,
    color = TextGray
)
@Composable private fun MetaValue(text: String) = Text(
    text = text,
    fontSize = 13.sp,
    fontFamily = PretendardSemiBold,
    fontWeight = FontWeight.SemiBold,
    color = Color(0xFF111827)
)
