@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dodojob.ui.feature.employ

import androidx.compose.foundation.Image
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.DpOffset
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.example.dodojob.R
import com.example.dodojob.dao.getCompanyIdByUsername
import com.example.dodojob.data.career.CareerRepositoryImpl
import com.example.dodojob.data.greatuser.fetchGreatUser
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.session.CurrentUser
import com.example.dodojob.session.JobBits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random
import com.example.dodojob.data.greatuser.SrafetchGreatUser
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.style.TextAlign
import com.example.dodojob.dao.fetchDisplayNameByUsername
import com.example.dodojob.navigation.Route

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

private fun parseYears(exp: String): Int {
    return exp.takeWhile { it.isDigit() }.toIntOrNull() ?: 0
}

class SrcGreatUserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GreatUserUiState())
    val uiState: StateFlow<GreatUserUiState> = _uiState

    fun loadUserData(username: String?,repo: CareerRepositoryImpl) {
        if (username.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "username이 비어있습니다."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val users = SrafetchGreatUser(getCompanyIdByUsername(username).toString()) // 서버 호출

                val talents = users.map { user ->
                    val year = user.birthdate.toString().take(4).toIntOrNull() ?: 0
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val diff = currentYear - year
                    val n = (1..100).random()

                    val result = if (n <= 60) {
                        // 1~1440분 사이 랜덤
                        val minutes = (1..1440).random()

                        if (minutes < 60)
                            "${minutes}분 전"
                        else
                            "${minutes / 60}시간 전"
                    } else {
                        "오래전"
                    }
                    val jobtalent = JobBits.parse(JobBits.JobCategory.TALENT,user.job_talent)
                    val jobmanage = JobBits.parse(JobBits.JobCategory.MANAGE,user.job_manage)
                    val jobservice = JobBits.parse(JobBits.JobCategory.SERVICE,user.job_service)
                    val jobcare = JobBits.parse(JobBits.JobCategory.CARE,user.job_care)

                    val allJobs = sequenceOf(
                        jobtalent,
                        jobmanage,
                        jobservice,
                        jobcare
                    ).flatten()
                        .filter { it.isNotBlank() }
                        .distinct()
                        .toList()

                    val randomJobs = allJobs.shuffled(Random(System.currentTimeMillis()))
                        .take(minOf(4, allJobs.size))

                    val m = (0..6).random()
                    val introlist = listOf(
                        "열심히 일 할 수 있습니다.",
                        "성실합니다",
                        "고객 응대에 자신",
                        "빠른 적응, 꼼꼼함",
                        "책임감 있게 합니다",
                        "배우면서 성장할게요",
                        "정확하고 신속하게"
                    )
                    val t = introlist[m]

                    val (years, months) = repo.totalCareerPeriod(user.username ?: "")


                    TalentUi(
                        name = user.username.toString(),
                        gender = user.gender.toString(),
                        age = diff,
                        seniorLevel = user.activityLevel!!.toInt()?: 0,
                        intro = t,
                        expYears = repo.formatCareerPeriod(years, months),
                        location = user.region.toString(),
                        jobCategories = randomJobs,
                        updatedMinutesAgo = result
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    users = users,
                    talents = talents,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "데이터 로드 실패"
                )
            }
        }
    }
}


/* =============== Screen: List =============== */
@Composable
fun ScrappedHumanResourceScreen(
    nav: NavController,
    viewModel: SrcGreatUserViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val client = LocalSupabase.current
    val repo = remember { CareerRepositoryImpl(client) }

    LaunchedEffect(Unit) {
        viewModel.loadUserData(CurrentUser.username, repo)
    }

    val uiState by viewModel.uiState.collectAsState()
    val talents = uiState.talents

    var sort by remember { mutableStateOf("업데이트순") }
    val sortOptions = listOf("업데이트순", "경력순")

    val talentsSorted = remember(talents, sort) {
        when (sort) {
            "업데이트순" -> talents.sortedBy { it.updatedMinutesAgo }
            "경력순"    -> talents.sortedByDescending { parseYears(it.expYears) }
            else        -> talents
        }
    }

    val totalCountForHeader = talentsSorted.size   // 🔥 정렬된 개수 사용

    Scaffold(containerColor = ScreenBg) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 헤더 + 정렬
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
                        onSortChange = { sort = it }
                    )
                }
            }

            // 🔥 정렬된 리스트로 렌더링
            items(talentsSorted.size) { idx ->
                val t = talentsSorted[idx]

                TalentCard(
                    data = t,
                    onClick = {
                        nav.currentBackStackEntry?.savedStateHandle?.set("talent", t)
                        nav.safeNavigate("view_resource_detail")
                    },
                    onInterviewClick = { maskedName ->   // 🔥 마스킹된 이름 전달받음
                        val applicant = ApplicantUi(
                            id            = 0L,                 // 스크랩 인재이므로 임시 ID
                            name          = maskedName,         // 화면 표시용 이름 (마스킹)
                            gender        = t.gender,
                            age           = t.age,
                            headline      = t.intro,
                            address       = t.location,
                            careerYears   = parseYears(t.expYears),
                            method        = "직접 제안",
                            postingTitle  = "-",
                            status        = ApplicantStatus.SUGGESTING,
                            activityLevel = t.seniorLevel,
                            profileRes    = R.drawable.basic_profile,
                            announcementId = null,              // 특정 공고 없이 제안
                            username      = t.name              // 실제 식별용 username
                        )

                        nav.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("applicant", applicant)

                        nav.safeNavigate(Route.SuggestInterview.path)
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
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.019).em,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center),
                maxLines = 1
            )
        }

        HorizontalDivider(thickness = 1.dp, color = DividerGray)
    }
}

/* =============== Sort Bar (여백 축소 버전) =============== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortBar(
    totalCount: Int,
    sort: String,
    sortOptions: List<String>,
    onSortChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ScreenBg)
            .padding(
                top = 16.dp,
                bottom = 8.dp,
                start = 16.dp,
                end = 16.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "총 ${formatWithComma(totalCount)}개",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = Pretendard,
            color = TextGray,
            letterSpacing = (-0.019).em
        )

        var expanded by remember { mutableStateOf(false) }
        val sortIconRes = if (expanded) R.drawable.upper else R.drawable.down

        Box {
            Row(
                modifier = Modifier
                    .clickable { expanded = true }
                    .height(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sort,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Pretendard,
                    color = TextGray,
                    letterSpacing = (-0.019).em
                )
                Spacer(Modifier.width(4.dp))
                Image(
                    painter = painterResource(sortIconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    surface = Color.White,
                    surfaceVariant = Color.White,
                    surfaceTint = Color.Transparent
                ),
                typography = MaterialTheme.typography,
                shapes = MaterialTheme.shapes
            ) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    offset = DpOffset(x = (-40).dp, y = 0.dp),
                    modifier = Modifier
                        .width(113.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        sortOptions.forEach { option ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .clickable {
                                        onSortChange(option)
                                        expanded = false
                                    },
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = (-0.019).em,
                                    color = TextGray,
                                    fontFamily = Pretendard,
                                    modifier = Modifier.padding(start = 20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* =============== Talent Card (원래 모양 + 하단 3버튼) =============== */
@Composable
private fun TalentCard(
    data: TalentUi,
    onClick: () -> Unit = {},
    onInterviewClick: (String) -> Unit = {}   // 🔥 String(마스킹 이름) 전달
) {
    val client = LocalSupabase.current
    var displayName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(data.name) {
        try {
            val name = fetchDisplayNameByUsername(data.name)
            displayName = name ?: data.name
        } catch (e: Exception) {
            e.printStackTrace()
            displayName = data.name
        }
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            // ───────────── 상단 내용 영역 ─────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 20.dp,
                            bottom = 20.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // 왼쪽 컬럼 (프로필 + 경력)
                    Column(
                        modifier = Modifier.width(51.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.basic_profile),
                            contentDescription = "profile",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(Modifier.height(5.dp))

                        // 경력 표시 문구 결정 (기존 로직 유지)
                        val isNewbie =
                            data.expYears == "0개월" ||
                                    data.expYears == "0년 0개월" ||
                                    data.expYears == "0년"

                        if (isNewbie) {
                            Text(
                                text = "신입",
                                modifier = Modifier.width(51.dp),
                                fontFamily = Pretendard,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = BrandBlue,
                                textAlign = TextAlign.Center,
                                letterSpacing = (-0.019).em,
                                lineHeight = 16.sp
                            )
                        } else {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            color = BrandBlue,
                                            fontWeight = FontWeight.Medium
                                        )
                                    ) { append("경력") }
                                    append("\n")
                                    withStyle(
                                        SpanStyle(
                                            color = BrandBlue,
                                            fontWeight = FontWeight.Medium
                                        )
                                    ) { append(data.expYears) }
                                },
                                modifier = Modifier.width(51.dp),
                                fontFamily = Pretendard,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                letterSpacing = (-0.019).em,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // 오른쪽 컬럼 (이름/한줄소개/주소/직무)
                    Column(
                        modifier = Modifier
                            .width(0.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // 이름 + (성별, 나이) + 메달
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = maskName(displayName ?: data.name),
                                fontFamily = Pretendard,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.019).em,
                                color = Color.Black,
                                maxLines = 1
                            )
                            Text(
                                text = "(${data.gender}, ${data.age}세)",
                                fontFamily = Pretendard,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = (-0.019).em,
                                color = TextGray,
                                maxLines = 1
                            )
                            Icon(
                                painter = painterResource(id = medalResForLevel(data.seniorLevel)),
                                contentDescription = "medal",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // 한 줄 자기소개
                        Text(
                            text = "“${data.intro}”",
                            fontFamily = Pretendard,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.019).em,
                            lineHeight = 21.sp,
                            color = Color.Black,
                            maxLines = 1
                        )

                        // 주소 / 직무 2줄
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // 주소
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.location),
                                    contentDescription = null,
                                    tint = TextGray,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = data.location,
                                    fontFamily = Pretendard,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = (-0.019).em,
                                    lineHeight = 18.sp,
                                    color = TextGray,
                                    maxLines = 1
                                )
                            }

                            // 직무
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.cargo),
                                    contentDescription = null,
                                    tint = TextGray,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = data.jobCategories.joinToString(", "),
                                    fontFamily = Pretendard,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = (-0.019).em,
                                    lineHeight = 18.sp,
                                    color = TextGray,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // ───────────── 하단 액션 바 (면접제안 / 문자 / 전화) ─────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(47.dp)
                    .background(CardBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .align(Alignment.TopCenter)
                        .background(DividerGray.copy(alpha = 0.5f))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 🔥 여기
                    ActionButton("면접제안", Modifier.weight(1f)) {
                        val masked = maskName(displayName ?: data.name)
                        onInterviewClick(masked)
                    }

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
}


@Composable
private fun ActionCell(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(37.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = Pretendard,
            color = TextGray,
            letterSpacing = (-0.019).em
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(29.dp)
            .background(DividerGray.copy(alpha = 0.5f))
    )
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
