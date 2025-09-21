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
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import com.example.dodojob.ui.feature.main.EmployerBottomNavBar


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
private val SoftGray   = Color(0xFFF5F6F7)
private val LineGray   = Color(0xFFBDBDBD)
private val TileBlueBg = Color(0xFFF5F9FF)
private val IconBoxBg  = Color(0xFFDEEAFF)



/* =======================================================================
 *  Route) 지원자 관리
 * ======================================================================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantManagementRoute(nav: NavController) {
    val sortOptions = listOf("지원일순", "이름 A-Z", "최근열람순")
    var selectedSort by remember { mutableStateOf(sortOptions.first()) }

    // 더미 데이터 (시안 구조와 값 예시)
    val applicants = remember {
        listOf(
            ApplicantUi(
                id = 1,
                name = "김말바",
                gender = "여",
                age = 62,
                headline = "열정 넘치는 인재입니다!",
                address = "대구광역시 서구",
                careerYears = 5,
                method = "온라인지원",
                postingTitle = "[현대백화점 대구점] 주간 미화원 모집",
                status = ApplicantStatus.SUGGESTING,  // 상단 칩 2개 예시
                activityLevel = 3
            ),
            ApplicantUi(
                id = 2,
                name = "김지원",
                gender = "여",
                age = 27,
                headline = "고객과 동료에게 힘이 되는 지원자",
                address = "대구 중구",
                careerYears = 5,
                method = "온라인지원",
                postingTitle = "[현대백화점 대구점] 주간 미화원 모집",
                status = ApplicantStatus.UNREAD,
                activityLevel = 1
            )
        )
    }

    val displayed = remember(applicants, selectedSort) {
        when (selectedSort) {
            "이름 A-Z"   -> applicants.sortedBy { it.name }
            "최근열람순" -> applicants.sortedByDescending { it.status == ApplicantStatus.READ }
            else         -> applicants
        }
    }

    val stats = remember(applicants) {
        listOf(
            StatItem("전체 지원자", applicants.size, R.drawable.total_applicants),
            StatItem("미열람", applicants.count { it.status == ApplicantStatus.UNREAD }, R.drawable.unread_applicants),
            StatItem("면접예정", applicants.count { it.status == ApplicantStatus.SUGGESTING }, R.drawable.going_to_interview),
            StatItem("채용완료", 1, R.drawable.check_mark), // 시안 맞춤
        )
    }

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            EmployerBottomNavBar(
                current = "applicant",
                onClick = { key ->
                    when (key) {
                        "home"      -> nav.navigate(Route.EmployerHome.path)
                        "notice"    -> nav.navigate(Route.EmployerApplicant.path)
                        "applicant" -> Unit
                        "my"        -> nav.navigate(Route.EmployerMy.path)
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
            item { TopStatusBar() }

            // 타이틀 + 통계 2x2
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                ) {
                    TopNavigationBar(title = "지원자 관리", useOwnBackground = false)
                    Spacer(Modifier.height(8.dp))
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
                    totalLabel = "총 ${displayed.size}개",
                    sortOptions = sortOptions,
                    selectedSort = selectedSort,
                    onSortChange = { selectedSort = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // 카드 리스트
            items(displayed, key = { it.id }) { ap ->
                ApplicantCard(
                    data = ap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onMenuClick = { /* TODO */ },
                    onViewPostingClick = { nav.navigate(Route.SuggestInterview.path) },
                    onAction = { /* TODO */ }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

/* =========================
 *  공통 컴포넌트
 * ========================= */
@Composable
private fun TopStatusBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Color(0xFFEFEFEF))
    )
}

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
                fontFamily = PretendardBold,  // 숫자만 Bold
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
                        text = {
                            Text(option, fontFamily = PretendardMedium)
                        },
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

    // 1) painter 가져오고, 2) 원본 비율로 width 계산
    val painter = painterResource(id = resId)
    val density = LocalDensity.current

    // painter.intrinsicSize: 원본 크기(px). PNG/Vector 모두 제공됨
    val intrinsic: Size = painter.intrinsicSize
    val aspect = if (intrinsic.width.isFinite() && intrinsic.height.isFinite() && intrinsic.height != 0f) {
        intrinsic.width / intrinsic.height
    } else {
        3f // 안전한 기본 비율 (가로로 긴 칩 가정)
    }

    val chipWidth: Dp = with(density) { (CHIP_HEIGHT.toPx() * aspect).toDp() }

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.FillBounds,          // 지정한 박스를 꽉 채움
        modifier = Modifier
            .heightIn(min = CHIP_HEIGHT)                 // 부모가 눌러도 최소 높이 보장
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
    onAction: (String) -> Unit = {}
) {
    Card(
        modifier = modifier,
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
                modifier = Modifier
                    .fillMaxWidth(),
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
                    // 이름(검정) + (성별, 나이)(회색) + 메달(파랑)
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
                    ActionCell(text = "면접제안") {}
                    VerticalDivider()
                    ActionCell(text = "문자") {}
                    VerticalDivider()
                    ActionCell(text = "전화") {  }
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
