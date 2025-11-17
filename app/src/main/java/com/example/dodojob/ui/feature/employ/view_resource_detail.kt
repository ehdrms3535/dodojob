@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.dodojob.ui.feature.employ

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.dao.fetchDisplayNameByUsername
import com.example.dodojob.data.greatuser.ScrappedGreatUserDto
import com.example.dodojob.data.greatuser.ScrappedGreatUserSupabase
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.session.CurrentUser
import com.example.dodojob.session.GreatUserView
import com.example.dodojob.session.JobBits
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.dodojob.data.greatuser.existSGU
import com.example.dodojob.navigation.Route

/* ===== Colors / Fonts ===== */
private val BrandBlue = Color(0xFF005FFF)
private val TextGray  = Color(0xFF828282)
private val LineGray  = Color(0xFFDDDDDD)
private val LabelGray = Color(0xFF9C9C9C)
private val BgGray    = Color(0xFFF1F5F7)

private val PretendardSemi = FontFamily(Font(R.font.pretendard_semibold, FontWeight.SemiBold))
private val PretendardMed  = FontFamily(Font(R.font.pretendard_medium,  FontWeight.Medium))

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

    val triplescareer = GreatUserView.careers.map { career ->
        Triple(career.title, career.startDate, career.endDate)
    }
    val triplelicense = GreatUserView.licenses.map { lisense ->
        Triple(lisense.location, lisense.name, lisense.number)
    }

    val gu = GreatUserView.greatuser

    LaunchedEffect(gu?.username) {
        val companyId = CurrentUser.companyid.toString()
        val seniorId = gu?.username ?: return@LaunchedEffect
        runCatching {
            isFavorite = existSGU(companyId, seniorId.toString())
        }.onFailure {
            Log.e("Scrap", "초기 조회 실패: ${it.message}")
        }
    }

    val jobtalent  = JobBits.parse(JobBits.JobCategory.TALENT,  gu?.job_talent)
    val jobmanage  = JobBits.parse(JobBits.JobCategory.MANAGE,  gu?.job_manage)
    val jobservice = JobBits.parse(JobBits.JobCategory.SERVICE, gu?.job_service)
    val jobcare    = JobBits.parse(JobBits.JobCategory.CARE,    gu?.job_care)

    val allJobs = sequenceOf(jobtalent, jobmanage, jobservice, jobcare)
        .flatten()
        .filter { it.isNotBlank() }
        .distinct()
        .toList()

    val randomJobs = allJobs.shuffled(Random(System.currentTimeMillis()))
        .take(minOf(4, allJobs.size))

    val client = LocalSupabase.current
    val scope = rememberCoroutineScope()
    val repo = ScrappedGreatUserSupabase(client)

    Scaffold(containerColor = BgGray, topBar = { }) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(scroll)
        ) {
            // 👉 헤더: ApplicantInformationScreen 스타일 적용
            ScrollHeader(
                title = "인재 상세보기",
                onBack = { navController.popBackStack() }
            )
            Spacer(Modifier.height(12.dp))

            val context = LocalContext.current
            var displayName by remember { mutableStateOf<String?>(null) }

            // 이름 비동기로 가져오기
            LaunchedEffect(safeTalent.name) {
                try {
                    val name = fetchDisplayNameByUsername(safeTalent.name.toString())
                    displayName = name ?: safeTalent.name
                } catch (e: Exception) {
                    e.printStackTrace()
                    displayName = safeTalent.name
                }
            }

            /* 헤더 카드 */
            TalentHeaderCard(
                name = displayName.toString(),
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
                /* 인적사항 섹션 */
                SectionCard(expanded = personalExpanded) {
                    SectionTitle(
                        title = " 인적사항",
                        iconRes = R.drawable.identification,
                        expanded = personalExpanded,
                        onToggle = { personalExpanded = !personalExpanded }
                    )
                    if (personalExpanded) {
                        Spacer(Modifier.height(16.dp))
                        KeyValueRow("이름",     gu?.name ?: safeTalent.name)
                        KeyValueRow("생년월일", gu?.birthdate ?: "-")
                        KeyValueRow("전화번호", gu?.phone ?: "-")
                        KeyValueRow("주소",     gu?.region ?: safeTalent.location)
                        KeyValueRow("이메일",   gu?.email ?: "-")
                    }
                }

                Spacer(Modifier.height(28.dp))

                /* 경력 섹션 */
                SectionCard(expanded = careerExpanded) {
                    SectionTitle(
                        title = " 경력",
                        iconRes = R.drawable.career,
                        expanded = careerExpanded,
                        onToggle = { careerExpanded = !careerExpanded }
                    )
                    if (careerExpanded) {
                        if (triplescareer.isEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "등록된 경력이 없습니다.",
                                fontSize = 14.sp,
                                fontFamily = PretendardMed,
                                color = TextGray,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                        } else {
                            // 🔹 제목 아래에 첫 구분선
                            Spacer(Modifier.height(12.dp))
                            ThinDivider()
                            Spacer(Modifier.height(12.dp))

                            triplescareer.forEachIndexed { index, (title, start, end) ->
                                CareerItem(
                                    title = title?.trim().orEmpty(),
                                    start = start.orEmpty(),
                                    end   = end.orEmpty()
                                )

                                // 마지막 아이템이 아니면 그 다음 구분선
                                if (index != triplescareer.lastIndex) {
                                    Spacer(Modifier.height(12.dp))
                                    ThinDivider()
                                    Spacer(Modifier.height(12.dp))
                                }
                            }

                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                /* 자격증 섹션 */
                SectionCard(expanded = licenseExpanded) {
                    SectionTitle(
                        title = " 자격증",
                        iconRes = R.drawable.license,
                        expanded = licenseExpanded,
                        onToggle = { licenseExpanded = !licenseExpanded }
                    )
                    if (licenseExpanded) {
                        if (triplelicense.isEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "등록된 자격증이 없습니다.",
                                fontSize = 14.sp,
                                fontFamily = PretendardMed,
                                color = TextGray,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                        } else {
                            // 🔹 제목 아래 첫 구분선
                            Spacer(Modifier.height(12.dp))
                            ThinDivider()
                            Spacer(Modifier.height(12.dp))

                            triplelicense.forEachIndexed { index, (org, title, code) ->
                                LicenseItem(
                                    org   = org.orEmpty(),
                                    title = title.orEmpty(),
                                    code  = code.orEmpty()
                                )

                                if (index != triplelicense.lastIndex) {
                                    Spacer(Modifier.height(12.dp))
                                    ThinDivider()
                                    Spacer(Modifier.height(12.dp))
                                }
                            }

                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                /* 희망직무 카드 (기존 로직 그대로, UI도 그대로 둠) */
                HopeJobCard(
                    jobs = safeTalent.jobCategories,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))
            }

            // ===== 하단 액션바 =====
            BottomActionBar(
                isFavorite = isFavorite,
                onFavoriteToggle = { next ->
                    scope.launch {
                        val dto = ScrappedGreatUserDto(
                            employ = CurrentUser.companyid.toString(),
                            senior = safeTalent.name,
                            isreal = true
                        )

                        val prev = isFavorite
                        isFavorite = next

                        val result = runCatching {
                            if (next) repo.insertSGU(dto) else repo.deleteSGU(dto)
                        }

                        if (result.isSuccess) {
                            Log.d("Scrap", if (next) "insert success ✅" else "delete success ✅")
                        } else {
                            isFavorite = prev
                            Log.e(
                                "Scrap",
                                (if (next) "insert" else "delete") +
                                        " failed ❌: ${result.exceptionOrNull()?.message}"
                            )
                        }
                    }
                },
                onInviteClick = {
                    // 🔥 면접 제의하기 눌렀을 때 전달할 ApplicantUi 구성
                    val maskedName = maskName(displayName ?: safeTalent.name)

                    val applicant = ApplicantUi(
                        id            = 0L,                         // 상세에서 바로 제안이라 임시 ID
                        name          = maskedName,                 // 화면에 보일 이름 (마스킹)
                        gender        = gu?.gender ?: safeTalent.gender,
                        age           = safeTalent.age,
                        headline      = safeTalent.intro,
                        address       = gu?.region ?: safeTalent.location,
                        careerYears   = parseYears(safeTalent.expYears),
                        method        = "직접 제안",
                        postingTitle  = "-",
                        status        = ApplicantStatus.SUGGESTING,
                        activityLevel = safeTalent.seniorLevel,
                        profileRes    = R.drawable.basic_profile,
                        announcementId = null,                      // 공고 없이 직접 제안
                        username      = gu?.username ?: safeTalent.name  // 실제 식별용 username
                    )

                    // 면접제안 화면에서 꺼내 쓸 데이터 넣어주기
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("applicant", applicant)

                    // 면접제안 화면으로 이동
                    navController.navigate(Route.SuggestInterview.path)
                },
                modifier = Modifier.fillMaxWidth()
            )
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
            .padding(horizontal = 16.dp)
            .height(96.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(R.drawable.basic_profile),
                contentDescription = "profile",
                tint = Color.Unspecified,
                modifier = Modifier.size(76.dp)
            )

            Box(
                modifier = Modifier
                    .height(76.dp)
                    .weight(1f)
            ) {
                // 위쪽: 이름/나이/메달 + 한 줄 소개
                Column(
                    modifier = Modifier.align(Alignment.TopStart),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = maskName(name),
                            fontSize = 15.sp,
                            fontFamily = PretendardSemi,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.019).em,
                            color = Color.Black
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "(${gender}, ${age}세)",
                            fontSize = 13.sp,
                            fontFamily = PretendardMed,
                            letterSpacing = (-0.019).em,
                            color = TextGray
                        )
                        Spacer(Modifier.width(5.dp))
                        Icon(
                            painter = painterResource(id = medalResForLevel(seniorLevel)),
                            contentDescription = "medal",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(width = 11.dp, height = 18.dp) // 11 x 18
                        )
                    }

                    Text(
                        text = "“$intro”",
                        fontSize = 14.sp,
                        fontFamily = PretendardSemi,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 21.sp,
                        letterSpacing = (-0.019).em,
                        color = Color.Black,
                        maxLines = 1
                    )
                }

                val displayExp = when {
                    expYears.contains("0개월") || expYears.contains("0년") -> "신입"
                    expYears.trim().isEmpty() -> "신입"
                    else -> "경력 $expYears"
                }

                // 아래쪽 오른쪽 정렬: 경력 텍스트
                Text(
                    text = displayExp,
                    fontSize = 11.sp,
                    fontFamily = PretendardMed,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.5.sp,
                    letterSpacing = (-0.019).em,
                    color = BrandBlue,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
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
            .padding(horizontal = 20.dp, vertical = 20.dp)
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

        Spacer(Modifier.height(32.dp))

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

/* ===== 하단 액션바 ===== */
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
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onFavoriteToggle(!isFavorite) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (isFavorite) R.drawable.full_star else R.drawable.grey_star
                    ),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp)
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
                Text("면접 제의하기", fontSize = 18.sp, fontFamily = PretendardSemi, color = Color.White)
            }
        }
    }
}

/* ===== 공통 섹션 (ApplicantInformationScreen 스타일) ===== */
@Composable
private fun SectionCard(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val vPadding = if (expanded) 20.dp else 8.dp

    Column(
        modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = vPadding)
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
            .padding(
                start = 4.dp,
                end = 4.dp,
                top = 8.dp,
                bottom = 2.dp
            )
            .clickable { onToggle() }
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
        Spacer(Modifier.height(6.dp))
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

/* ===== 스크롤 헤더 (ApplicantInformationScreen 스타일) ===== */
@Composable
private fun ScrollHeader(
    title: String,
    onBack: () -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(Color.White)
                .padding(horizontal = 4.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "뒤로가기",
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = title,
                fontFamily = PretendardSemi,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.019).em,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center),
                maxLines = 1
            )
        }
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
                withStyle(SpanStyle(color = LabelGray, fontFamily = PretendardMed, fontSize = 14.sp)) {
                    append("자격번호 ")
                }
                withStyle(SpanStyle(color = BrandBlue, fontFamily = PretendardMed, fontSize = 14.sp)) {
                    append(code)
                }
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
private fun parseYears(exp: String): Int {
    // "3년 2개월", "8년", "0개월" 같은 문자열에서 앞 숫자만 파싱
    return exp.takeWhile { it.isDigit() }.toIntOrNull() ?: 0
}