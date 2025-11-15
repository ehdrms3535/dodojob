package com.example.dodojob.ui.feature.employ

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import com.example.dodojob.ui.feature.main.EmployerBottomNavBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.data.announcement.AnnouncementRepositorySupabase

/* =========================
 *  Fonts
 * ========================= */
private val PretendardMedium = FontFamily(Font(R.font.pretendard_medium))
private val PretendardBold = FontFamily(Font(R.font.pretendard_bold))
private val PretendardSemiBold = FontFamily(Font(R.font.pretendard_semibold))

/* =========================
 *  Colors
 * ========================= */
private val ScreenBg = Color(0xFFF1F5F7)
private val White = Color(0xFFFFFFFF)
private val BrandBlue = Color(0xFF005FFF)
private val TextGray = Color(0xFF828282)
private val LineGray = Color(0xFFD7D7D7) // 공고관리 스타일
private val TileBlueBg = Color(0xFFF5F9FF)
private val IconBoxBg = Color(0xFFDEEAFF)

/* =========================
 *  Provider
 * ========================= */
fun interface ApplicantsProvider {
    suspend fun fetchApplicants(): List<ApplicantUi>
}

/* =======================================================================
 *  State / ViewModel (백엔드 로직 그대로)
 * ======================================================================= */
data class ApplicantsState(
    val items: List<ApplicantUi> = emptyList(),
    val selectedSort: String = "지원일순",
    val loading: Boolean = false,
    val error: String? = null,
    val totalCount: Int = 0,
    val unreadCount: Int = 0,
    val suggestingCount: Int = 0,
    val hiredCount: Int = 0
)

class ApplicantsViewModel(
    private val provider: ApplicantsProvider,
    private val repo: AnnouncementRepositorySupabase
) : ViewModel() {

    private val _state = MutableStateFlow(ApplicantsState())
    val state: StateFlow<ApplicantsState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { provider.fetchApplicants() }
                .onSuccess { list ->
                    val base = when (_state.value.selectedSort) {
                        "이름 A-Z" -> list.sortedBy { it.name }
                        "최근열람순" -> list.sortedByDescending { it.status == ApplicantStatus.READ }
                        else -> list // 지원일순(서버에서 최신순 주는 것으로 가정)
                    }

                    _state.update {
                        it.copy(
                            items = base,
                            loading = false,
                            totalCount = base.size,
                            unreadCount = base.count { x -> x.status == ApplicantStatus.UNREAD },
                            suggestingCount = base.count { x -> x.status == ApplicantStatus.SUGGESTING },
                            hiredCount = 0
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, error = e.message ?: "알 수 없는 오류") }
                }
        }
    }

    fun onSortChange(label: String) {
        _state.update { it.copy(selectedSort = label) }
        val list = _state.value.items
        val resorted = when (label) {
            "경력순" -> list.sortedByDescending { it.careerYears } // 예시
            else      -> list                                      // 지원일순(서버 최신순)
        }
        _state.update { it.copy(items = resorted) }
    }
    fun markAsRead(announcementId: Long?, username: String?) {
        if (announcementId == null || username.isNullOrBlank()) return

        val target = _state.value.items.firstOrNull {
            it.announcementId == announcementId && it.username == username
        }
        if (target?.status != ApplicantStatus.UNREAD) {
            return
        }

        viewModelScope.launch {
            runCatching {
                repo.markApplicantRead(announcementId, username)
            }.onSuccess {
                _state.update { old ->
                    val newList = old.items.map { ap ->
                        if (ap.announcementId == announcementId && ap.username == username) {
                            ap.copy(status = ApplicantStatus.READ)
                        } else ap
                    }

                    old.copy(
                        items = newList,
                        unreadCount = newList.count { it.status == ApplicantStatus.UNREAD }
                    )
                }
            }
        }
    }
}

/* =======================================================================
 *  Route) 지원자 관리
 *  - 스타일만 공고관리 화면 스타일로 변경
 * ======================================================================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantManagementRoute(
    nav: NavController,
    vm: ApplicantsViewModel = run {
        // 1) Supabase 클라이언트/레포 준비
        val client = LocalSupabase.current
        val repo = remember { AnnouncementRepositorySupabase(client) }

        // 2) Provider 구성
        val provider = remember {
            ApplicantsProvider {
                val rows = repo.getannounceRows(companyId = null) // 필요 시 회사ID 전달
                rows.map { r ->
                    ApplicantUi(
                        id = r.id,
                        name = r.name ?: "(이름없음)",
                        gender = r.gender ?: "-",
                        age = r.age ?: 0,
                        headline = r.headline ?: "열정 넘치는 인재입니다!",
                        address = r.address ?: "-",
                        careerYears = r.careerYears ?: 0,
                        method = r.method ?: "온라인지원",
                        postingTitle = r.postingTitle ?: "-",
                        status = when (r.status?.lowercase()) {
                            "unread" -> ApplicantStatus.UNREAD
                            "read" -> ApplicantStatus.READ
                            "suggesting",
                            "interview" -> ApplicantStatus.SUGGESTING
                            else -> ApplicantStatus.UNREAD
                        },
                        activityLevel = r.activityLevel ?: 1,
                        announcementId = r.announcementId,
                        username = r.seniorUserName
                    )
                }
            }
        }

        // 3) ViewModel 팩토리 생성 (Hilt 미사용)
        viewModel(
            factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ApplicantsViewModel(provider, repo) as T
                }
            }
        )
    }
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    val sortOptions = listOf("지원일순", "경력순")

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            EmployerBottomNavBar(
                current = "applicant",
                onClick = { key ->
                    when (key) {
                        "home" -> nav.safeNavigate(Route.EmployerHome.path)
                        "notice" -> nav.safeNavigate(Route.EmployerNotice.path)
                        "applicant" -> nav.safeNavigate(Route.EmployerApplicant.path)
                        "human_resource" -> nav.safeNavigate(Route.EmployerHumanResource.path)
                        "my" -> nav.safeNavigate(Route.EmployerMy.path)
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
            // 타이틀 + 통계 (공고관리 스타일)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                ) {
                    TopNavigationBar(title = "지원자 관리", useOwnBackground = false)
                    Spacer(Modifier.height(8.dp))

                    val stats = listOf(
                        StatItem("전체 지원자", state.totalCount, R.drawable.total_applicants),
                        StatItem("미열람", state.unreadCount, R.drawable.unread_applicants),
                        StatItem("면접예정", state.suggestingCount, R.drawable.going_to_interview),
                        StatItem("채용완료", state.hiredCount, R.drawable.check_mark),
                    )
                    StatGrid(
                        items = stats,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }

            // 상단 컨트롤 (총 N개 / 정렬) - 공고관리 스타일
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ScreenBg)
                        .padding(horizontal = 16.dp)
                ) {
                    ListControls(
                        totalLabel = "총 ${state.items.size}개",
                        sortOptions = sortOptions,
                        selectedSort = state.selectedSort,
                        onSortChange = vm::onSortChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 상태별 UI
            when {
                state.loading -> {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                }

                state.error != null -> {
                    item {
                        Text(
                            text = "로드 실패: ${state.error}",
                            color = Color.Red,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }

                else -> {
                    items(state.items) { ap ->
                        ApplicantCard(
                            data = ap,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            onMenuClick = { /* TODO */ },
                            onViewPostingClick = { /* TODO: 공고 상세 이동 */ },
                            onAction = { key ->
                                if (key == "suggest_interview") {
                                    vm.markAsRead(ap.announcementId!!, ap.username!!)
                                    nav.currentBackStackEntry?.savedStateHandle?.set("applicant", ap)
                                    nav.safeNavigate(Route.SuggestInterview.path)
                                }
                            },
                            onApplicantCardClick = {
                                vm.markAsRead(ap.announcementId!!, ap.username!!)
                                nav.safeNavigate(Route.InformationOfApplicants.of(ap.username!!))
                            }
                        )
                    }
                }
            }
        }
    }
}

/* =========================
 *  공통 컴포넌트 (공고관리 스타일로 수정)
 * ========================= */
@Composable
private fun TopNavigationBar(
    title: String,
    useOwnBackground: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .then(if (useOwnBackground) Modifier.background(Color(0xFFF4F5F7)) else Modifier)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

/* ===== Stats (공고관리 스타일 재사용) ===== */
// StatItem 은 다른 파일(공고관리)에서 이미 선언되어 있다고 가정하고 사용만 함.

@Composable
private fun StatGrid(items: List<StatItem>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.take(2).forEach { item ->
                StatTile(item = item, isLast = false, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.drop(2).forEachIndexed { i, item ->
                StatTile(
                    item = item,
                    isLast = (i == items.drop(2).lastIndex),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatTile(item: StatItem, isLast: Boolean, modifier: Modifier = Modifier) {
    val bgColor = TileBlueBg

    Row(
        modifier = modifier
            .height(73.dp)
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 22.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(IconBoxBg),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                item.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                "${item.number}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlue
            )
        }
    }
}


/* ===== Sort bar (공고관리 스타일을 지원자 정렬에 맞게 수정) ===== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListControls(
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
            fontFamily = PretendardMedium,
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
                    fontFamily = PretendardMedium,
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

            // 드롭다운: 스타일만 공고관리처럼, 실제 항목은 sortOptions 사용
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
                        modifier = Modifier
                            .fillMaxWidth()
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

/* =========================
 *  칩: 리소스 이미지
 * ========================= */
private val CHIP_HEIGHT = 20.dp

@Composable
private fun StatusChip(status: ApplicantStatus) {
    val resId = when (status) {
        ApplicantStatus.UNREAD     -> R.drawable.unread_korean
        ApplicantStatus.READ       -> R.drawable.read_korean
        ApplicantStatus.SUGGESTING -> R.drawable.suggest_interview_korean
    }

    val painter = painterResource(id = resId)
    val density = LocalDensity.current

    val intrinsic: Size = painter.intrinsicSize
    val aspect = if (intrinsic.width.isFinite() && intrinsic.height.isFinite() && intrinsic.height != 0f) {
        intrinsic.width / intrinsic.height
    } else 3f

    val chipWidth: Dp = with(density) { (CHIP_HEIGHT.toPx() * aspect).toDp() }

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .height(CHIP_HEIGHT)
            .width(chipWidth)
    )
}

@Composable
private fun ApplicantCard(
    data: ApplicantUi,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    onViewPostingClick: () -> Unit = {},
    onAction: (String) -> Unit = {},
    onApplicantCardClick: (String?) -> Unit = {}
) {
    Card(
        modifier = modifier
            .clickable { onApplicantCardClick(data.username) },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // 안쪽에서 흰 배경
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // ───────────────── 상단 컨텐츠 영역 (230px) ─────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .padding(horizontal = 20.dp, vertical = 20.dp) // Figma: 20px 0 상하, 안쪽 288 폭
            ) {
                // 1) 상단: 상태 칩 + more 버튼
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (data.status == ApplicantStatus.SUGGESTING) {
                            StatusChip(ApplicantStatus.READ)
                            StatusChip(ApplicantStatus.SUGGESTING)
                        } else {
                            StatusChip(data.status)
                        }
                    }

                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "더 보기",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // 2) 본문: 프로필 + 정보
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 118.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Image(
                        painter = painterResource(id = data.profileRes),
                        contentDescription = "프로필",
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Crop
                    )

                    // 🔹 오른쪽 내용 전체
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 2-1) 이름 + (성별, 나이) + 메달
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = data.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = PretendardMedium,
                                lineHeight = 22.sp,
                                letterSpacing = (-0.019).em,
                                color = Color.Black
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "(${data.gender}, ${data.age}세)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = PretendardMedium,
                                lineHeight = 18.sp,
                                letterSpacing = (-0.019).em,
                                color = TextGray
                            )
                            Spacer(Modifier.width(2.dp))
                            Image(
                                painter = painterResource(id = medalRes(data.activityLevel)),
                                contentDescription = "활동레벨 메달",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // 2-2) 한 줄 자기소개 (따옴표 포함, Bold 느낌)
                        Text(
                            text = buildAnnotatedString {
                                append("“")
                                withStyle(SpanStyle(color = Color.Black)) { append(data.headline) }
                                append("”")
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = PretendardMedium,
                            lineHeight = 21.sp,
                            letterSpacing = (-0.019).em,
                            color = Color.Black,
                            maxLines = 1
                        )

                        // 2-3) 지역 / 경력 / 지원  세로 배치
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // 지역 (아이콘 + 주소)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.location),
                                    contentDescription = "위치",
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = data.address,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = PretendardMedium,
                                    lineHeight = 18.sp,
                                    letterSpacing = (-0.019).em,
                                    color = TextGray,
                                    maxLines = 1
                                )
                            }

                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = PretendardMedium,
                                            color = TextGray
                                        )
                                    ) { append("경력 ") }

                                    withStyle(
                                        SpanStyle(
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = PretendardMedium,
                                            color = Color.Black
                                        )
                                    ) { append("${data.careerYears}년") }
                                },
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                letterSpacing = (-0.019).em
                            )

                            // 지원 (label = Medium, value = Bold)
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = PretendardMedium,
                                            color = TextGray
                                        )
                                    ) { append("지원 ") }

                                    withStyle(
                                        SpanStyle(
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = PretendardMedium,
                                            color = Color.Black
                                        )
                                    ) { append(data.method) }
                                },
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                letterSpacing = (-0.019).em
                            )
                        }
                    }
                }


                Spacer(Modifier.height(10.dp))

                // 3) 지원공고 박스
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(5.dp)) // SuggestInterview 스타일
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "지원공고" 라벨 박스
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "지원공고",
                            fontFamily = PretendardSemiBold,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            letterSpacing = (-0.019).em,
                            color = Color(0xFF848484)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // 공고 제목 + 오른쪽 화살표
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
                            letterSpacing = (-0.019).em,
                            color = Color(0xFF000000),
                            maxLines = 1
                        )

                        Spacer(Modifier.weight(1f))

                        Image(
                            painter = painterResource(R.drawable.right_back), // 🔥 리소스도 동일
                            contentDescription = "지원공고 열기",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ───────────────── 하단 액션 바 (47px) ─────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(47.dp)
                    .background(White)
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 10.dp,
                            bottomEnd = 10.dp
                        )
                    )
            ) {
                // 상단 라인
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .align(Alignment.TopCenter)
                        .background(LineGray.copy(alpha = 0.5f))
                )

                // 액션 3개
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionCell(text = "면접제안") { onAction("suggest_interview") }

                    Box(
                        modifier = Modifier
                            .width(29.dp)
                            .height(47.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(29.dp)
                                .background(LineGray.copy(alpha = 0.5f))
                        )
                    }

                    ActionCell(text = "문자") { /* TODO */ }

                    Box(
                        modifier = Modifier
                            .width(29.dp)
                            .height(47.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(29.dp)
                                .background(LineGray.copy(alpha = 0.5f))
                        )
                    }

                    ActionCell(text = "전화") { /* TODO */ }
                }
            }
        }
    }
}

/* =========================
 *  서브 컴포넌트
 * ========================= */
@Composable
private fun MetaLabel(text: String) = Text(
    text = text,
    fontSize = 13.sp,
    fontWeight = FontWeight.Medium,
    fontFamily = PretendardMedium,
    color = TextGray
)

@Composable
private fun MetaValue(text: String) = Text(
    text = text,
    fontSize = 13.sp,
    fontWeight = FontWeight.Medium,
    fontFamily = PretendardMedium,
    color = Color(0xFF111827)
)

@Composable
private fun ActionCell(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(96.dp)
            .height(37.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PretendardMedium,
            color = Color(0xFF6B7280),
            letterSpacing = (-0.019).em
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(0.6.dp)
            .height(29.dp)
            .background(LineGray)
    )
}

/* =========================
 *  Nav 확장
 * ========================= */
private fun NavController.safeNavigate(
    route: String,
    builder: (NavOptionsBuilder.() -> Unit)? = { launchSingleTop = true; restoreState = true }
) {
    navigate(route) { builder?.invoke(this) }
}
