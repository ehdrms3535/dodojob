package com.example.dodojob.ui.feature.jobdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.dodojob.R
import com.example.dodojob.ui.feature.application.ApplyRoute
import kotlinx.coroutines.launch

/* ======== 모델 ======== */
data class JobDetailUiState(
    val title: String,
    val companyName: String,
    val chips: List<InfoChip>,
    val recruitment: List<LabelValue>,
    val workplaceMapHint: String,
    val working: List<LabelValue>,
    val duties: List<String>,
    val isLiked: Boolean = false,
    val imageUrl: String? = null,
    val careerYears: Int? = null,
    val benefits: List<String> = emptyList()
)

data class InfoChip(
    val small: String,
    val value: String,
    val style: ChipStyle = ChipStyle.Primary,
    val iconRes: Int? = null
)

enum class ChipStyle { Primary, Neutral, Danger }
data class LabelValue(val label: String, val value: String)

/* ======== 색/스타일 공통 ======== */
private val BrandBlue = Color(0xFF005FFF)
private val ScreenBg = Color(0xFFF1F5F7)

private val BgGray = ScreenBg
private val CardBg = Color.White
private val DividerGray = Color(0xFFF0F0F0)
private val TextDim = Color(0xFF9C9C9C)
private val Letter = (-0.019f).em

/* ======== 진입 ======== */
@Composable
fun JobDetailRoute(
    nav: NavController,
    ui: JobDetailUiState,
    onBack: () -> Unit,
    onToggleLike: (Boolean) -> Unit,
    onCall: () -> Unit,
    onApply: () -> Unit
) {
    JobDetailScreen(
        ui = ui,
        onBack = onBack,
        onToggleLike = onToggleLike,
        onCall = onCall,
        onApply = { /* bottom sheet 열기만 담당 (상위에 굳이 알릴 필요 없으면 비워둬도 됨) */ },
        onSimpleApply = { nav.navigate(ApplyRoute.path) }
    )
}

/* ======== 화면 ======== */
@Composable
fun JobDetailScreen(
    ui: JobDetailUiState,
    onBack: () -> Unit,
    onToggleLike: (Boolean) -> Unit,
    onCall: () -> Unit,
    onApply: () -> Unit,
    onSimpleApply: () -> Unit
) {
    var liked by remember(ui.isLiked) { mutableStateOf(ui.isLiked) }
    var selectedTab by remember { mutableStateOf(0) }
    var showApplySheet by remember { mutableStateOf(false) }

    // 스크롤 + 스크롤 이동용
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // 섹션별 스크롤 타깃
    val recruitRequester = remember { BringIntoViewRequester() }
    val workplaceRequester = remember { BringIntoViewRequester() }
    val workingRequester = remember { BringIntoViewRequester() }
    val dutiesRequester = remember { BringIntoViewRequester() }

    // 🔹 비율: 버튼/칩 "크기"에만 적용, 나머지는 dp 고정
    val config = LocalConfiguration.current
    val scale = (config.screenWidthDp / 360f).coerceIn(0.85f, 1.35f)

    Box {
        Scaffold(
            containerColor = ScreenBg,
            bottomBar = {
                BottomActionBar(
                    onCall = onCall,
                    onApply = {
                        showApplySheet = true
                        onApply()
                    },
                    scale = scale
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                // 상단 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(CardBg),
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { onBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.back),
                                    contentDescription = "뒤로가기",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(
                                        if (liked) R.drawable.heart else R.drawable.empty_heart
                                    ),
                                    contentDescription = "좋아요",
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(25.dp)
                                        .clickable {
                                            liked = !liked
                                            onToggleLike(liked)
                                        }
                                )
                            }
                        }

                        Text(
                            text = "채용공고",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = Letter,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp)
                        )
                        Spacer(Modifier.height(16.dp))

                        HeaderImage(ui.imageUrl, scale)

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = ui.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 36.sp,
                            letterSpacing = Letter,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(55.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE9EDF2)),
                                contentAlignment = Alignment.Center
                            ) { Text("로고", color = TextDim, fontSize = 12.sp) }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = ui.companyName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        ChipsGrid(
                            chips = ui.chips,
                            scale = scale,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        )
                    }
                }

                // 탭 위 회색 띠
                SectionSpacer()

                // 탭 (파란줄 짧게 + 클릭 시 스크롤)
                JobDetailTabs(
                    selectedIndex = selectedTab,
                    onTabSelected = { index ->
                        selectedTab = index
                        scope.launch {
                            when (index) {
                                0 -> recruitRequester.bringIntoView()
                                1 -> workplaceRequester.bringIntoView()
                                2 -> workingRequester.bringIntoView()
                                3 -> dutiesRequester.bringIntoView()
                            }
                        }
                    }
                )

                // 모집조건
                Box(
                    modifier = Modifier.bringIntoViewRequester(recruitRequester)
                ) {
                    SectionCard(title = "모집조건", rows = ui.recruitment)
                }

                SectionSpacer()

                // 근무지 장소
                Box(
                    modifier = Modifier.bringIntoViewRequester(workplaceRequester)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(CardBg),
                        shape = RoundedCornerShape(0.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardBg)
                                .padding(bottom = 20.dp)
                        ) {
                            SectionHeader("근무지 장소")
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(188.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFE9EDF2)),
                                    contentAlignment = Alignment.Center
                                ) { Text("지도 영역 (API 연동 예정)", color = TextDim, fontSize = 13.sp) }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = ui.workplaceMapHint,
                                    fontSize = 20.sp,
                                    lineHeight = 30.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp)
                                )
                            }
                        }
                    }
                }

                SectionSpacer()

                // 근무조건
                Box(
                    modifier = Modifier.bringIntoViewRequester(workingRequester)
                ) {
                    SectionCard(title = "근무조건", rows = ui.working)
                }

                SectionSpacer()

                // 담당업무
                Box(
                    modifier = Modifier.bringIntoViewRequester(dutiesRequester)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(CardBg),
                        shape = RoundedCornerShape(0.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardBg)
                                .padding(bottom = 12.dp)
                        ) {
                            SectionHeader("담당업무")
                            Column(modifier = Modifier.padding(horizontal = 32.dp)) {
                                ui.duties.forEach { duty ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(duty, fontSize = 20.sp, lineHeight = 30.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(50.dp))
            }
        }

        // ✅ 하단에 딱 붙는 지원하기 바텀시트
        ApplyBottomSheet(
            visible = showApplySheet,
            onClose = { showApplySheet = false },
            onMessageApply = { /* 문자지원 로직 */ },
            onSimpleApply = {
                showApplySheet = false   // ✅ 변수 이름 수정 (showBottomSheet -> showApplySheet)
                onSimpleApply()          // Application 화면으로 이동
            }
        )
    }
}

/* ======== 탭 컴포저블 ======== */
@Composable
private fun JobDetailTabs(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val titles = listOf("모집조건", "근무장소", "근무조건", "담당업무")

    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = CardBg,
        divider = {},
        indicator = { tabPositions ->
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[selectedIndex])
                    .padding(horizontal = 16.dp)
                    .height(2.dp)
                    .background(BrandBlue)
            )
        },
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        titles.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = { TabText(title, selected = selectedIndex == index) }
            )
        }
    }
}

/* ======== 이미지 (높이만 비율) ======== */
@Composable
private fun HeaderImage(url: String?, scale: Float) {
    val modifier = Modifier
        .padding(top = 10.dp, start = 16.dp, end = 16.dp)
        .fillMaxWidth()
        .height((193f * scale).dp)
        .clip(RoundedCornerShape(10.dp))

    if (url.isNullOrBlank()) {
        Box(
            modifier = modifier.background(Color(0xFFE9EDF2)),
            contentAlignment = Alignment.Center
        ) {
            Text("이미지 영역", color = TextDim, fontSize = 13.sp)
        }
    } else {
        Image(
            painter = rememberAsyncImagePainter(url),
            contentDescription = "대표 이미지",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

/* ======== 하단바 (버튼 높이만 비율) ======== */
@Composable
private fun BottomActionBar(
    onCall: () -> Unit,
    onApply: () -> Unit,
    scale: Float
) {
    val btnHeight = (54.48f * scale).dp
    Surface(shadowElevation = 6.dp, color = ScreenBg) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .height(btnHeight),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onCall,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BrandBlue),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(vertical = 9.dp, horizontal = 18.dp)
            ) {
                Text(
                    text = "전화",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.019).em,
                    color = Color.Black
                )
            }

            Button(
                onClick = onApply,
                modifier = Modifier
                    .weight(2f)
                    .height(btnHeight),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                contentPadding = PaddingValues(vertical = 9.dp, horizontal = 41.dp)
            ) {
                Text(
                    text = "지원하기",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.019).em,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ApplyBottomSheet(
    visible: Boolean,
    onClose: () -> Unit,
    onMessageApply: () -> Unit,
    onSimpleApply: () -> Unit
) {
    val config = LocalConfiguration.current
    val scale = (config.screenWidthDp / 360f).coerceIn(0.85f, 1.35f)

    AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
        exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it })
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // ✅ 딤드 배경 레이어
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x80000000)) // 반투명 검정
                    .clickable { onClose() }       // 바깥 클릭하면 닫힘
            )

            // ✅ 하단 시트
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height((160f * scale).dp)
                    .shadow(20.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = (32f * scale).dp,
                            end = (27f * scale).dp,
                            top = (18f * scale).dp,
                            bottom = (20f * scale).dp
                        ),
                    verticalArrangement = Arrangement.spacedBy((18f * scale).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 상단: 지원하기 + 닫기
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((30f * scale).dp)
                    ) {
                        Text(
                            text = "지원하기",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandBlue,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size((24f * scale).dp)
                                .clickable { onClose() },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.exit),
                                contentDescription = "닫기",
                                modifier = Modifier.size((24f * scale).dp)
                            )
                        }
                    }

                    // 하단 버튼 2개 (중앙 정렬 + 비율 크기)
                    val buttonWidth = (146f * scale).dp
                    val buttonHeight = (50f * scale).dp
                    val buttonSpacing = (8f * scale).dp

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.message_apply),
                            contentDescription = "문자지원",
                            modifier = Modifier
                                .width(buttonWidth)
                                .height(buttonHeight)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onMessageApply() }
                        )

                        Image(
                            painter = painterResource(R.drawable.simple_apply),
                            contentDescription = "간편지원",
                            modifier = Modifier
                                .width(buttonWidth)
                                .height(buttonHeight)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSimpleApply() }
                        )
                    }
                }
            }
        }
    }
}

/* ======== 섹션 ======== */
@Composable
private fun SectionCard(title: String, rows: List<LabelValue>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(CardBg),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg)
        ) {
            SectionHeader(title)
            Spacer(Modifier.height(6.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                rows.forEach { lv ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            lv.label,
                            color = TextDim,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .widthIn(min = 68.dp)
                                .weight(1f)
                        )
                        Text(lv.value, fontSize = 20.sp, modifier = Modifier.weight(2f))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 10.dp, top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, letterSpacing = Letter)
    }
}

@Composable
private fun TabText(text: String, selected: Boolean = false) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = if (selected) BrandBlue else Color.Black,
        letterSpacing = Letter
    )
}

/* === 칩: 크기(폭/높이)만 비율, 내부는 dp 고정 === */
@Composable
private fun ChipsGrid(
    chips: List<InfoChip>,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val gap = 6.dp
    val totalWidth = LocalConfiguration.current.screenWidthDp.dp - 32.dp
    val chipWidth = ((totalWidth - gap) / 2)
    val chipHeight = (67f * scale).dp

    Column(modifier) {
        chips.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                row.forEach { chip ->
                    ChipCard(
                        chip = chip,
                        width = chipWidth,
                        height = chipHeight
                    )
                }
                if (row.size == 1) {
                    Spacer(Modifier.width(chipWidth))
                }
            }
            Spacer(Modifier.height(5.dp))
        }
    }
}

@Composable
private fun ChipCard(
    chip: InfoChip,
    width: Dp,
    height: Dp
) {
    val borderColor: Color
    val valueColor: Color
    when (chip.style) {
        ChipStyle.Primary -> { borderColor = BrandBlue; valueColor = BrandBlue }
        ChipStyle.Neutral -> { borderColor = BrandBlue; valueColor = Color.Black }
        ChipStyle.Danger -> { borderColor = Color(0xFFFF2F00); valueColor = Color(0xFFFF2F00) }
    }

    Row(
        modifier = Modifier
            .width(width)
            .height(height)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(start = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        chip.iconRes?.let {
            Image(
                painter = painterResource(it),
                contentDescription = chip.small,
                modifier = Modifier
                    .width(20.dp)
                    .height(30.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = chip.value,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun SectionSpacer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(BgGray)
    )
}

/* ======== 프리뷰 ======== */
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewJobDetail() {
    val sample = JobDetailUiState(
        title = "매장운영 및 고객관리 하는 일에 적합한 분 구해요",
        companyName = "모던하우스",
        chips = listOf(
            InfoChip("급여", "월 240만원", ChipStyle.Primary, R.drawable.dollar),
            InfoChip("시간", "시간협의", ChipStyle.Neutral, R.drawable.time),
            InfoChip("요일", "주 4일 근무", ChipStyle.Neutral, R.drawable.calendar2),
            InfoChip("우대사항", "경력자 우대", ChipStyle.Danger, R.drawable.suit)
        ),
        recruitment = listOf(
            LabelValue("모집기간", "상시모집"),
            LabelValue("자격요건", "중졸 / 경력자"),
            LabelValue("모집인원", "4명"),
            LabelValue("우대조건", "동종업계 경력자"),
            LabelValue("기타조건", "주부 가능")
        ),
        workplaceMapHint = "대구 수성구 용학로 118 1,2층(두산동) 모던하우스",
        working = listOf(
            LabelValue("급여", "월 240만원"),
            LabelValue("근무기간", "1년 이상"),
            LabelValue("근무일", "주 4일 근무"),
            LabelValue("근무시간", "시간협의")
        ),
        duties = listOf(
            "매장 고객 피드백",
            "전화 응대",
            "진열 및 환경 관리"
        ),
        isLiked = false
    )

    // ✅ 프리뷰에서는 NavController 없으니까 Screen만 직접 호출
    JobDetailScreen(
        ui = sample,
        onBack = {},
        onToggleLike = {},
        onCall = {},
        onApply = {},
        onSimpleApply = {}
    )
}
