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

private fun parseYears(exp: String): Int {
    return exp.takeWhile { it.isDigit() }.toIntOrNull() ?: 0
}

/* =============== Screen: List =============== */
@Composable
fun EmployerHumanResourceScreen(nav: NavController,viewModel: GreatUserViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {

    val client = LocalSupabase.current
    val repo = remember { CareerRepositoryImpl(client) }
    LaunchedEffect(Unit) {
        viewModel.loadUserData(CurrentUser.username,repo)
    }

    val uiState by viewModel.uiState.collectAsState()
    var sort by remember { mutableStateOf("업데이트순") }
    val sortOptions = listOf("업데이트순", "이름순", "경력순")

    val licenseRepo = LicenseRepositoryImpl(client)
    val careerRepo = CareerRepositoryImpl(client)

    val scope = rememberCoroutineScope()   // ← 추가

    val talents = uiState.talents
    val talentsSorted = remember(talents, sort) {
        when (sort) {
            "업데이트순" -> talents.sortedBy { it.updatedMinutesAgo }     // 최근 업데이트가 상단이면 오름차순이 자연스럽습니다
            "이름순"   -> talents.sortedBy { it.name }
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
                    onStarClick = { nav.safeNavigate("scrapped_human_resource") },
                    onFilterClick = { /* TODO */ }
                )
            }

            items(talentsSorted.size) { idx ->
                val t = talentsSorted[idx]
                TalentCard(
                    data = t,
                    onClick = {
                        scope.launch {  // ✅ suspend 함수 안전하게 호출
                            var greatuserone = fetchGreatUserone(t.name)
                            if (greatuserone == null) {
                                greatuserone = GreatUser(        // ✅ 기본값 or 빈 객체 대입
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
    val context = LocalContext.current
    val client = LocalSupabase.current  // ⚙️ 이미 CompositionLocal로 주입된 SupabaseClient라면
    var displayName by remember { mutableStateOf<String?>(null) }

    // 이름 비동기로 가져오기
    LaunchedEffect(data.name) {
        try {
            val name = fetchDisplayNameByUsername(data.name.toString())
            displayName = name ?: data.name   // 없으면 원래 username 그대로
        } catch (e: Exception) {
            e.printStackTrace()
            displayName = data.name
        }
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        onClick = onClick
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
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
                            withStyle(SpanStyle(color = BrandBlue, fontWeight = FontWeight.Medium)) { append("${data.expYears}") }
                        },
                        fontFamily = Pretendard,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }

                Spacer(Modifier.width(20.dp))

                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // ✅ 가져온 displayName 사용
                        Text(
                            text = maskName(displayName ?: data.name),
                            fontFamily = Pretendard,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "(${data.gender}, ${data.age}세)",
                            fontSize = 15.sp,
                            fontFamily = Pretendard,
                            color = TextGray
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            painterResource(medalResForLevel(data.seniorLevel)),
                            contentDescription = "medal",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
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
            Text(
                "${data.updatedMinutesAgo}",
                fontFamily = Pretendard,
                fontSize = 11.sp,
                color = TextGray,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 20.dp)
            )
        }
    }
}


/* =============== Utils =============== */
private fun formatWithComma(n: Int?): String = "%,d".format(n)
private fun maskName(name: String) = if (name.isNotEmpty()) name.first() + "**" else "**"
@DrawableRes private fun medalResForLevel(level: Int?): Int = when (level) {
    1 -> R.drawable.red_medal
    2 -> R.drawable.yellow_medal
    else -> R.drawable.blue_medal
}
private fun NavController.safeNavigate(
    route: String,
    builder: (NavOptionsBuilder.() -> Unit)? = { launchSingleTop = true; restoreState = true }
) { navigate(route) { builder?.invoke(this) } }
