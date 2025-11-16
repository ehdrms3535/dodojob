package com.example.dodojob.ui.feature.employ

import android.os.Parcelable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.data.suggestinterview.SuggestInterviewInsert
import com.example.dodojob.data.suggestinterview.SuggestInterviewRepository
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import com.example.dodojob.data.naver.NaverGeocoding

/* =========================
 *  Fonts
 * ========================= */
private val PretendardMedium   = FontFamily(Font(R.font.pretendard_medium))
private val PretendardSemiBold = FontFamily(Font(R.font.pretendard_semibold))
private val PretendardBold     = FontFamily(Font(R.font.pretendard_bold))

/* =========================
 *  Spacing / Colors
 * ========================= */
private val SIDE       = 4.dp   // 상단 앱바 정도에서만 사용
private val ScreenBg   = Color(0xFFF1F5F7)
private val White      = Color(0xFFFFFFFF)
private val BrandBlue  = Color(0xFF005FFF)
private val TextGray   = Color(0xFF828282)
private val LineGray   = Color(0xFFE5E7EB)

/* =========================
 *  모델
 * ========================= */
@Parcelize
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
    val profileRes: Int = R.drawable.basic_profile,
    val announcementId: Long? = null,
    val username: String? = null
) : Parcelable

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
    val applicant = navController
        .previousBackStackEntry
        ?.savedStateHandle
        ?.get<ApplicantUi>("applicant")

    if (applicant == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    // Supabase / Repository / CoroutineScope
    val supabase = LocalSupabase.current
    val suggestRepo = remember { SuggestInterviewRepository(supabase) }
    val scope = rememberCoroutineScope()

    var method by remember { mutableStateOf(InterviewMethod.InPerson) }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var addressDetail by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var screenLoading by remember { mutableStateOf(false) }   // 초기 사용자 정보
    var geocodeLoading by remember { mutableStateOf(false) }  // 주소찾기
    var nextLoading by remember { mutableStateOf(false) }     // 다음단계 저장
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

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

        // 앱바 (상단만 좌우 패딩 유지)
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
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "뒤로가기",
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "면접 제안",
                fontFamily = PretendardSemiBold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.019).em,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
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
                Spacer(Modifier.height(15.dp))
                ApplicantInfoBox(
                    data = applicant,
                    modifier = Modifier.fillMaxWidth(),
                    onViewPostingClick = { /* TODO: 공고 상세 */ }
                )
            }

            SectionDivider()

            // ===== 면접 일정 =====
            SectionContainer {
                SectionHeader(
                    iconRes = R.drawable.schedule_of_interview,
                    title = "면접 일정"
                )
                Spacer(Modifier.height(15.dp))
                InputBlock(
                    label = "면접 날짜",
                    value = date,
                    onValueChange = { date = it },
                    placeholder = "2025.01.01"
                )
                Spacer(Modifier.height(16.dp))
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
                Spacer(Modifier.height(15.dp))

                Text(
                    text = "면접 방식",
                    fontFamily = PretendardSemiBold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    letterSpacing = (-0.019).em,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                // 면접 장소 인풋
                InputBlock(
                    label = "면접 장소",
                    value = address,
                    onValueChange = { address = it },
                    placeholder = "주소를 검색해주세요"
                )

                Spacer(Modifier.height(8.dp))
                AddressSearchButton(
                    onClick = {
                        scope.launch {
                            geocodeLoading = true
                            try {
                                val q = address.trim()
                                if (q.isEmpty()) {
                                    Toast.makeText(context, "주소를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                val r = NaverGeocoding.geocode(context, q)
                                if (r != null) {
                                    fun stripHtml(s: String?) =
                                        s?.replace(Regex("<.*?>"), "")?.trim().orEmpty()

                                    val best = listOf(
                                        r.roadAddress,
                                        r.jibunAddress,
                                        r.display
                                    ).map(::stripHtml).firstOrNull { it.isNotEmpty() }.orEmpty()

                                    if (best.isNotEmpty()) {
                                        address = best
                                        focusManager.clearFocus()
                                        Toast.makeText(
                                            context,
                                            "찾음: $best (${r.lat}, ${r.lng})",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "주소 문자열이 비어 있습니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "주소를 찾을 수 없어요. 다른 표현으로 검색해 보세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "오류: ${e.message ?: "네트워크/권한/키 확인"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                geocodeLoading = false
                            }
                        }
                    }
                )


                Spacer(Modifier.height(16.dp))
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
                Spacer(Modifier.height(15.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "면접 안내사항",
                        fontFamily = PretendardSemiBold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        letterSpacing = (-0.019).em,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, BrandBlue, RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp)
                    ) {
                        TextField(
                            value = note,
                            onValueChange = { note = it },
                            modifier = Modifier.fillMaxSize(),
                            placeholder = {
                                Text(
                                    text =
                                        "예시 :\n" +
                                                "  • 신분증을 지참해주세요\n" +
                                                "  • 편안한 복장으로 오셔도 됩니다\n" +
                                                "  • 건물 1층 로비에서 면접 안내를 받으실 수 있어요",
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

            // 에러 메시지
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            SectionDivider()
            // ===== CTA =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (isLoading) return@Button

                        scope.launch {
                            try {
                                isLoading = true
                                errorMessage = null

                                val form = SuggestInterviewFormState(
                                    method = method,
                                    date = date.trim(),
                                    time = time.trim(),
                                    address = address.trim(),
                                    addressDetail = addressDetail.trim(),
                                    note = note.trim()
                                )

                                val methodCode = when (form.method) {
                                    InterviewMethod.InPerson -> "in_person"
                                    InterviewMethod.Remote   -> "phone"
                                }

                                val row = SuggestInterviewInsert(
                                    announcement_id = applicant.announcementId,
                                    username        = applicant.username,
                                    interview_date  = form.date.ifBlank { null },
                                    interview_time  = form.time.ifBlank { null },
                                    method          = methodCode,
                                    address         = form.address.ifBlank { null },
                                    address_detail  = form.addressDetail.ifBlank { null },
                                    note            = form.note.ifBlank { null }
                                )

                                suggestRepo.insert(row)

                                navController.popBackStack()
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "면접 제안 저장 중 오류가 발생했습니다."
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .height(47.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
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
            }
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
            .height(20.dp)
            .background(ScreenBg)
    )
}

/**
 * 섹션 컨테이너
 */
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            content = content
        )
        Spacer(Modifier.height(30.dp))
    }
}

/**
 * 섹션 헤더
 */
@Composable
private fun SectionHeader(iconRes: Int, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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

/* 면접 방식 버튼 */
@Composable
private fun MethodButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = White
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
            fontFamily = PretendardBold,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.019).em
        )
    }
}

/* 🔹 주소찾기 버튼 */
@Composable
private fun AddressSearchButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandBlue,
            contentColor = White
        )
    ) {
        Text(
            text = "주소찾기",
            fontFamily = PretendardSemiBold,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.019).em
        )
    }
}

/* 라벨 + OutlinedTextField */
@Composable
private fun InputBlock(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
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
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    color = TextGray,
                    fontSize = 15.sp,
                    fontFamily = PretendardMedium,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.019).em,
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
                fontFamily = PretendardSemiBold,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.019).em
            )
        )
    }
}

/* =========================
 *  지원자 정보 박스
 * ========================= */
@Composable
fun ApplicantInfoBox(
    data: ApplicantUi,
    modifier: Modifier = Modifier,
    onViewPostingClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
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
            Image(
                painter = painterResource(id = data.profileRes),
                contentDescription = "프로필",
                modifier = Modifier.size(50.dp)
            )


            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 이름 + (성별, 나이) + 메달
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = data.name,
                        fontFamily = PretendardSemiBold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
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
                        modifier = Modifier.size(width = 11.dp, height = 18.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))

                // 한줄 소개
                Text(
                    text = "“${data.headline}”",
                    fontFamily = PretendardSemiBold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF000000)
                )

                Spacer(Modifier.height(4.dp))

                // 위치
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.location),
                        contentDescription = "위치 아이콘",
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = data.address,
                        fontFamily = PretendardSemiBold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }

                Row {
                    MetaLabel("경력")
                    Spacer(Modifier.width(8.dp))
                    MetaValue("${data.careerYears}년")
                }

                Row {
                    MetaLabel("지원")
                    Spacer(Modifier.width(8.dp))
                    MetaValue(data.method)
                }
            }

        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White)
                .border(1.dp, LineGray, RoundedCornerShape(5.dp))
                .padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "지원공고",
                    fontFamily = PretendardSemiBold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF848484)
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
                    text = " [ ${data.postingTitle} ] ",
                    fontFamily = PretendardSemiBold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color(0xFF000000),
                    maxLines = 1
                )
                Spacer(Modifier.weight(1f))
                Image(
                    painter = painterResource(R.drawable.right_back),
                    contentDescription = "지원공고 열기",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun MetaLabel(text: String) = Text(
    text = text,
    fontSize = 12.sp,
    fontFamily = PretendardSemiBold,
    fontWeight = FontWeight.SemiBold,
    color = TextGray
)

@Composable
private fun MetaValue(text: String) = Text(
    text = text,
    fontSize = 12.sp,
    fontFamily = PretendardSemiBold,
    fontWeight = FontWeight.SemiBold,
    color = Color(0xFF111827)
)
