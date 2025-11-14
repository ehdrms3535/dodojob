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
import androidx.compose.material.icons.filled.ExpandMore
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
private val PretendardBold   = FontFamily(Font(R.font.pretendard_bold))
private val PretendardSemiBold = FontFamily(Font(R.font.pretendard_semibold))

/* =========================
 *  Colors
 * ========================= */
private val ScreenBg   = Color(0xFFF1F5F7)
private val White      = Color(0xFFFFFFFF)
private val BrandBlue  = Color(0xFF005FFF)
private val TextGray   = Color(0xFF828282)
private val LineGray   = Color(0xFFBDBDBD)
private val TileBlueBg = Color(0xFFF5F9FF)
private val IconBoxBg  = Color(0xFFDEEAFF)


fun interface ApplicantsProvider {
    suspend fun fetchApplicants(): List<ApplicantUi>
}

/* =======================================================================
 *  State / ViewModel
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
    private val provider: ApplicantsProvider
) : ViewModel() {

    private val _state = MutableStateFlow(ApplicantsState())
    val state: StateFlow<ApplicantsState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { provider.fetchApplicants() }
                .onSuccess { list ->
                    val base = when (_state.value.selectedSort) {
                        "이름 A-Z"   -> list.sortedBy { it.name }
                        "최근열람순" -> list.sortedByDescending { it.status == ApplicantStatus.READ }
                        else         -> list // 지원일순(서버에서 최신순 주는 것으로 가정)
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
            "이름 A-Z"   -> list.sortedBy { it.name }
            "최근열람순" -> list.sortedByDescending { it.status == ApplicantStatus.READ }
            else         -> list
        }
        _state.update { it.copy(items = resorted) }
    }
}

/* =======================================================================
 *  Route) 지원자 관리
 *  - NavGraph 수정 없이 Route 내부에서 repo/provider 준비
 * ======================================================================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantManagementRoute(
    nav: NavController,
    vm: ApplicantsViewModel = run {
        //  1) Supabase 클라이언트/레포 준비
        val client = LocalSupabase.current
        val repo = remember { AnnouncementRepositorySupabase(client) }

        // 2) Provider 구성 (회사ID 필터 필요시 여기에 주입)
        val provider = remember {
            ApplicantsProvider {
                val rows = repo.getannounceRows(companyId = null) // 필요 시 회사ID 전달
                // DB rows → UI 매핑
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
                            "unread"      -> ApplicantStatus.UNREAD
                            "read"        -> ApplicantStatus.READ
                            "suggesting",
                            "interview"   -> ApplicantStatus.SUGGESTING
                            else          -> ApplicantStatus.UNREAD
                        },
                        activityLevel = r.activityLevel ?: 1,
                        announcementId = r.announcementId,
                        username       = r.seniorUserName
                    )
                }
            }
        }

        // 3) ViewModel 팩토리 생성 (Hilt 미사용)
        viewModel(
            factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ApplicantsViewModel(provider) as T
                }
            }
        )
    }
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    val sortOptions = listOf("지원일순", "이름 A-Z", "최근열람순")

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            EmployerBottomNavBar(
                current = "applicant",
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
            // 타이틀 + 통계
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
                    Spacer(Modifier.height(8.dp))
                }
            }

            // 상단 컨트롤 (총 N개 / 정렬)
            item {
                ListControls(
                    totalLabel = "총 ${state.items.size}개",
                    sortOptions = sortOptions,
                    selectedSort = state.selectedSort,
                    onSortChange = vm::onSortChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
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
                                when (key) {
                                    "suggest_interview" -> {
                                        nav.currentBackStackEntry
                                            ?.savedStateHandle
                                            ?.set("applicant", ap)

                                        nav.safeNavigate(Route.SuggestInterview.path)
                                    }
                                }
                            },
                            onApplicantCardClick = { username ->
                                nav.safeNavigate(
                                    Route.InformationOfApplicants.of("1234")
                                )
                            }
                        )
                    }

                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

/* =========================
 *  공통 컴포넌트
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
            title,
            fontSize = 30.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PretendardSemiBold,
            color = Color.Black
        )
    }
}

@Composable
private fun StatGrid(items: List<StatItem>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.take(2).forEach { item ->
                StatTile(item = item, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.drop(2).forEach { item ->
                StatTile(item = item, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(item: StatItem, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(73.dp)
            .background(TileBlueBg, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
                fontFamily = PretendardMedium,
                color = Color.Black
            )
            Text(
                "${item.number}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PretendardBold,
                color = BrandBlue
            )
        }
    }
}

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

        ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
            Row(
                modifier = Modifier
                    .menuAnchor()
                    .height(24.dp)
                    .clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selectedSort,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = PretendardMedium,
                    color = TextGray,
                    letterSpacing = (-0.019).em
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = "정렬 선택",
                    modifier = Modifier.size(18.dp),
                    tint = TextGray
                )
            }
            ExposedDropdownMenu(expanded, { expanded = false }) {
                sortOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontFamily = PretendardMedium) },
                        onClick = {
                            onSortChange(option)
                            expanded = false
                        }
                    )
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
    } else {
        3f
    }

    val chipWidth: Dp = with(density) { (CHIP_HEIGHT.toPx() * aspect).toDp() }

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .heightIn(min = CHIP_HEIGHT)
            .size(width = chipWidth, height = CHIP_HEIGHT)
    )
}

/* =========================
 *  지원자 카드
 * ========================= */
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
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 상단: 칩(필요 시 2개) + 메뉴
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (data.status == ApplicantStatus.SUGGESTING) {
                        StatusChip(ApplicantStatus.READ)       // 열람 칩
                        StatusChip(ApplicantStatus.SUGGESTING) // 면접 제안 중 칩
                    } else {
                        StatusChip(data.status)                // 미열람 or 열람
                    }
                }
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "더 보기", tint = Color.Black)
                }
            }

            Spacer(Modifier.height(12.dp))

            // 본문: 프로필 + 내용
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // 프로필
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
                    // 이름(검정) + (성별, 나이)(회색) + 메달
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = data.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = PretendardMedium,
                            color = Color.Black
                        )
                        Text(
                            text = "(${data.gender}, ${data.age}세)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = PretendardMedium,
                            color = TextGray
                        )
                        Image(
                            painter = painterResource(id = medalRes(data.activityLevel)),
                            contentDescription = "활동레벨 메달",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 자기소개 한 줄 (따옴표 포함)
                    Text(
                        text = buildAnnotatedString {
                            append("“")
                            withStyle(SpanStyle(color = Color.Black)) { append(data.headline) }
                            append("”")
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = PretendardMedium,
                        color = Color(0xFF5C5C5C)
                    )

                    // 위치
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
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = PretendardMedium,
                            color = TextGray
                        )
                    }

                    // 경력 행
                    Row {
                        MetaLabel("경력")
                        Spacer(Modifier.width(8.dp))
                        MetaValue("${data.careerYears}년")
                    }

                    // 지원 행
                    Row {
                        MetaLabel("지원")
                        Spacer(Modifier.width(8.dp))
                        MetaValue(data.method)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 지원공고 pill (왼쪽 라벨 + 오른쪽 버튼형)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(White)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 좌측 작은 라벨
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "지원공고",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = PretendardMedium,
                        color = Color(0xFF6B7280)
                    )
                }
                Spacer(Modifier.width(8.dp))
                // 제목 + >
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onViewPostingClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = " ${data.postingTitle} ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = PretendardMedium,
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

            Spacer(Modifier.height(12.dp))

            // 하단 액션 바
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.6.dp)
                        .background(LineGray)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(47.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionCell(text = "면접제안") { onAction("suggest_interview") }
                    VerticalDivider()
                    ActionCell(text = "문자") {}
                    VerticalDivider()
                    ActionCell(text = "전화") { }
                }
            }
        }
    }
}

/* =========================
 *  서브 컴포넌트
 * ========================= */
@Composable private fun MetaLabel(text: String) = Text(
    text = text,
    fontSize = 13.sp,
    fontWeight = FontWeight.Medium,
    fontFamily = PretendardMedium,
    color = TextGray
)
@Composable private fun MetaValue(text: String) = Text(
    text = text,
    fontSize = 13.sp,
    fontWeight = FontWeight.Medium,
    fontFamily = PretendardMedium,
    color = Color(0xFF111827)
)
@Composable private fun ActionCell(text: String, onClick: () -> Unit) {
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
@Composable private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(0.6.dp)
            .height(29.dp)
            .background(LineGray)
    )
}

private fun NavController.safeNavigate(
    route: String,
    builder: (NavOptionsBuilder.() -> Unit)? = { launchSingleTop = true; restoreState = true }
) { navigate(route) { builder?.invoke(this) } }
