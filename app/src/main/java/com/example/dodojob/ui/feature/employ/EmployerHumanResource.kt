@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dodojob.ui.feature.employ

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
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
import com.example.dodojob.session.CurrentUser
import com.example.dodojob.ui.feature.main.EmployerBottomNavBar
import kotlinx.parcelize.Parcelize
import com.example.dodojob.data.greatuser.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.em
import com.example.dodojob.dao.fetchDisplayNameByUsername
import java.util.Calendar
import kotlin.random.Random
import com.example.dodojob.session.JobBits
import com.example.dodojob.data.career.CareerRepositoryImpl
import com.example.dodojob.data.license.LicenseRepositoryImpl
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.session.GreatUserView


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
    val expYears: String,
    val location: String,
    val jobCategories: List<String>,
    val updatedMinutesAgo: String
) : Parcelable

/* =============== Fake Repo (list) =============== */
object FakeTalentRepo {
    data class Talent(
        val name: String?, val gender: String?, val age: Int?,
        val seniorLevel: Long?, val intro: String?, val expYears: Int?,
        val location: String?, val jobCategories: List<String>, val updatedMinutesAgo: String?
    )
}

data class GreatUserUiState(
    val isLoading: Boolean = false,
    val users: List<GreatUser> = emptyList(),
    val talents: List<TalentUi> = emptyList(),
    val error: String? = null
)

class GreatUserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GreatUserUiState())
    val uiState: StateFlow<GreatUserUiState> = _uiState

    fun loadUserData(username: String?, repo: CareerRepositoryImpl) {
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
                val users = fetchGreatUser() // 서버 호출

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

                    val jobtalent = JobBits.parse(JobBits.JobCategory.TALENT, user.job_talent)
                    val jobmanage = JobBits.parse(JobBits.JobCategory.MANAGE, user.job_manage)
                    val jobservice = JobBits.parse(JobBits.JobCategory.SERVICE, user.job_service)
                    val jobcare = JobBits.parse(JobBits.JobCategory.CARE, user.job_care)

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
                        seniorLevel = user.activityLevel!!.toInt(),
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

/* 경력 년수만 숫자로 추출 (예: "3년 2개월" -> 3) */
private fun parseYears(exp: String): Int {
    return exp.takeWhile { it.isDigit() }.toIntOrNull() ?: 0
}

/* 🔥 업데이트 문자열을 '분' 단위 숫자로 변환해서 정렬용으로 사용
   - "10분 전"   -> 10
   - "3시간 전"  -> 180
   - "오래전"    -> 아주 큰 값 (맨 뒤로 가도록)
 */
private fun parseUpdatedMinutes(label: String): Int {
    val text = label.trim()

    return when {
        text.endsWith("분 전") -> {
            text.removeSuffix("분 전").trim().toIntOrNull() ?: Int.MAX_VALUE
        }
        text.endsWith("시간 전") -> {
            val hour = text.removeSuffix("시간 전").trim().toIntOrNull()
            if (hour != null) hour * 60 else Int.MAX_VALUE
        }
        text == "오래전" -> Int.MAX_VALUE
        else -> Int.MAX_VALUE
    }
}

/* =============== Screen: List =============== */
@Composable
fun EmployerHumanResourceScreen(
    nav: NavController,
    viewModel: GreatUserViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    val client = LocalSupabase.current
    val repo = remember { CareerRepositoryImpl(client) }
    LaunchedEffect(Unit) {
        viewModel.loadUserData(CurrentUser.username, repo)
    }

    val uiState by viewModel.uiState.collectAsState()
    var sort by remember { mutableStateOf("업데이트순") }
    val sortOptions = listOf("업데이트순", "경력순")

    val licenseRepo = LicenseRepositoryImpl(client)
    val careerRepo = CareerRepositoryImpl(client)

    val scope = rememberCoroutineScope()

    val talents = uiState.talents

    // 🔧 정렬 로직 수정: "업데이트순"은 분 단위로 파싱해서 '가장 최근(작은 분)'이 먼저 오도록
    val talentsSorted = remember(talents, sort) {
        when (sort) {
            "업데이트순" -> talents.sortedBy { parseUpdatedMinutes(it.updatedMinutesAgo) }
            "경력순"   -> talents.sortedByDescending { parseYears(it.expYears) }
            else       -> talents
        }
    }

    val totalCountForHeader = talentsSorted.size

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
                    onStarClick = { nav.safeNavigate("scrapped_human_resource") }
                )
            }

            items(talentsSorted.size) { idx ->
                val t = talentsSorted[idx]
                TalentCard(
                    data = t,
                    onClick = {
                        scope.launch {
                            var greatuserone = fetchGreatUserone(t.name)
                            if (greatuserone == null) {
                                greatuserone = GreatUser(
                                    name = t.name,
                                    region = "-",
                                    phone = "-",
                                    email = "-",
                                    gender = null,
                                    username = null
                                )
                            }
                            val licenses = licenseRepo.list(t.name)
                            val careers = careerRepo.list(t.name)

                            GreatUserView.setLicenses(licenses)
                            GreatUserView.setCareers(careers)
                            GreatUserView.setGreatuser(greatuserone)

                            nav.currentBackStackEntry?.savedStateHandle?.set("talent", t)
                            nav.safeNavigate("view_resource_detail")
                        }
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
    onStarClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(start = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "인재",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Pretendard,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onStarClick,
                modifier = Modifier
                    .size(28.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.empty_star),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(22.dp)
                        .offset(y = 2.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ScreenBg)
            .padding(top = 12.dp)
            .padding(horizontal = 16.dp)
    ) {
        HumanListControls(
            totalLabel = "총 ${formatWithComma(totalCount)}개",
            sortOptions = sortOptions,
            selectedSort = sort,
            onSortChange = onSortChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HumanListControls(
    totalLabel: String,
    sortOptions: List<String>,
    selectedSort: String,
    onSortChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = totalLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = Pretendard,
            color = TextGray,
            letterSpacing = (-0.019).em
        )

        var expanded by remember { mutableStateOf(false) }

        val sortIconRes = if (expanded) {
            R.drawable.upper
        } else {
            R.drawable.down
        }

        Box {
            Row(
                modifier = Modifier
                    .clickable { expanded = true }
                    .height(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedSort,
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

/* =============== Talent Card (list item) =============== */
@Composable
private fun TalentCard(
    data: TalentUi,
    onClick: () -> Unit = {}
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // 메인 내용
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
                // 왼쪽 컬럼 (아바타 + 경력)
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

                    // 경력 표시 문구 결정
                    val isNewbie = data.expYears == "0개월" ||
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

            // 상단 우측 업데이트 텍스트 (예: "5분 전")
            Text(
                text = data.updatedMinutesAgo,
                fontFamily = Pretendard,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.019).em,
                color = TextGray,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 20.dp),
                textAlign = TextAlign.Right
            )
        }
    }
}

/* =============== Utils =============== */
private fun formatWithComma(n: Int?): String = "%,d".format(n)
private fun maskName(name: String) = if (name.isNotEmpty()) name.first() + "**" else "**"
@DrawableRes
private fun medalResForLevel(level: Int?): Int = when (level) {
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
