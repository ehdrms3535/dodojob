package com.example.dodojob.ui.feature.jobdetail

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ======== 모델 ======== */
data class JobDetailUiState(
    val title: String,
    val companyName: String,
    val chips: List<InfoChip>,
    val recruitment: List<LabelValue>,
    val workplaceMapHint: String,
    val working: List<LabelValue>,
    val duties: List<String>,
    val isLiked: Boolean = false
)

data class InfoChip(
    val small: String,
    val value: String,
    val style: ChipStyle = ChipStyle.Primary,
    val emoji: String? = null      // ← 아이콘(이모지) 옵션
)

enum class ChipStyle { Primary, Neutral, Danger }
data class LabelValue(val label: String, val value: String)

/* ======== 색/스타일 공통 ======== */
private val BrandBlue = Color(0xFF005FFF)
private val ScreenBg = Color(0xFFF1F5F7)
private val CardBg = Color.White
private val DividerGray = Color(0xFFF0F0F0)
private val TextDim = Color(0xFF9C9C9C)

/* ======== 진입 ======== */
@Composable
fun JobDetailRoute(
    ui: JobDetailUiState,
    onBack: () -> Unit,
    onToggleLike: (Boolean) -> Unit,
    onCall: () -> Unit,
    onApply: () -> Unit
) {
    JobDetailScreen(ui, onBack, onToggleLike, onCall, onApply)
}

/* ======== 화면 ======== */
@Composable
fun JobDetailScreen(
    ui: JobDetailUiState,
    onBack: () -> Unit,
    onToggleLike: (Boolean) -> Unit,
    onCall: () -> Unit,
    onApply: () -> Unit
) {
    var liked by remember(ui.isLiked) { mutableStateOf(ui.isLiked) }

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = { BottomActionBar(onCall, onApply) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            /* 상단 카드 */
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(CardBg),
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 상단 바: 뒤로/좋아요
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Outlined.ArrowBackIosNew, "뒤로가기", tint = Color.Black)
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = {
                            liked = !liked
                            onToggleLike(liked)
                        }) {
                            Icon(
                                if (liked) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "관심",
                                tint = if (liked) BrandBlue else Color(0xFFCDCDCD)
                            )
                        }
                    }

                    // 제목
                    Text(
                        text = "채용공고",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )

                    // 대표 이미지 자리(플레이스홀더)
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .height(193.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE9EDF2)),
                        contentAlignment = Alignment.Center
                    ) { Text("이미지 영역 (DB 연동 예정)", color = TextDim, fontSize = 13.sp) }

                    // 공고 제목
                    Text(
                        text = ui.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 36.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    // 회사(로고 자리 + 이름)
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

                    // ✅ 칩: 2열 그리드 + 아이콘배지 + 라벨/값 (두번째 이미지 스타일)
                    ChipsGrid(
                        chips = ui.chips,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            // 탭 헤더(시각만 동일)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabText("모집조건", selected = true)
                TabText("근무장소")
                TabText("근무조건")
                TabText("담당업무")
            }
            Box(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .width(60.dp)
                    .height(4.dp)
                    .background(BrandBlue)
            )

            SectionCard(title = "모집조건", rows = ui.recruitment)

            // 근무지 장소
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
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
                        Text(ui.workplaceMapHint, fontSize = 20.sp, lineHeight = 30.sp)
                    }
                }
            }

            SectionCard(title = "근무조건", rows = ui.working)

            // 담당업무
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
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
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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

            Spacer(Modifier.height(50.dp))
        }
    }
}

/* ======== 하단 액션바 ======== */
@Composable
private fun BottomActionBar(
    onCall: () -> Unit,
    onApply: () -> Unit
) {
    Surface(shadowElevation = 6.dp, color = ScreenBg) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onCall,
                modifier = Modifier
                    .height(54.dp)
                    .weight(0.9f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BrandBlue),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Text("전화", fontSize = 20.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.width(7.dp))
            Button(
                onClick = onApply,
                modifier = Modifier
                    .height(54.dp)
                    .weight(2f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("지원하기", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }
        }
    }
}

/* ======== 재사용 뷰 ======== */
@Composable
private fun SectionCard(title: String, rows: List<LabelValue>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        colors = CardDefaults.cardColors(CardBg),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().background(CardBg)) {
            SectionHeader(title)
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                rows.forEachIndexed { i, lv ->
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
                            modifier = Modifier.widthIn(min = 68.dp).weight(1f)
                        )
                        Text(lv.value, fontSize = 20.sp, modifier = Modifier.weight(2f))
                    }
                    if (i < rows.lastIndex) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = DividerGray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable private fun SectionHeader(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 10.dp, top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) { Text(text, fontSize = 24.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun TabText(text: String, selected: Boolean = false) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = if (selected) BrandBlue else Color.Black
    )
}

/* === 칩 영역 (2열 그리드) === */
@Composable
private fun ChipsGrid(chips: List<InfoChip>, modifier: Modifier = Modifier) {
    val hGap = 8.dp
    val vGap = 8.dp
    Column(modifier) {
        chips.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(hGap)
            ) {
                row.forEach { chip ->
                    ChipCard(
                        chip = chip,
                        modifier = Modifier
                            .height(64.dp)
                            .weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(vGap))
        }
    }
}

@Composable
private fun ChipCard(chip: InfoChip, modifier: Modifier = Modifier) {
    val (borderColor, valueColor, badgeBg) = when (chip.style) {
        ChipStyle.Primary -> Triple(BrandBlue, BrandBlue, Color(0xFFEAF2FF))
        ChipStyle.Neutral -> Triple(BrandBlue, Color.Black, Color(0xFFF6F8FA))
        ChipStyle.Danger -> Triple(Color(0xFFFF2F00), Color(0xFFFF2F00), Color(0xFFFFEFEA))
    }
    Row(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽 아이콘 배지
        Surface(color = badgeBg, shape = RoundedCornerShape(8.dp)) {
            Text(
                text = chip.emoji ?: "",
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = chip.small,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B7280)
            )
            Text(
                text = chip.value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}

/* ======== 미리보기 ======== */
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewJobDetail() {
    val sample = JobDetailUiState(
        title = "매장운영 및 고객관리 하는 일에\n적합한 분 구해요",
        companyName = "모던하우스",
        chips = listOf(
            InfoChip("월급", "월 240만원", ChipStyle.Primary, "💵"),
            InfoChip("시간", "시간협의", ChipStyle.Neutral, "⏰"),
            InfoChip("요일", "주 4일 근무", ChipStyle.Neutral, "📅"),
            InfoChip("기타", "경력자 우대", ChipStyle.Danger, "👔")
        ),
        recruitment = listOf(
            LabelValue("모집기간", "상시모집"),
            LabelValue("자격요건", "중졸 / 현재 경력자"),
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
            "매장 고객 피드 백 방문",
            "고객 전화 연결 처리",
            "매장 환경 점검 및 진열 관리"
        ),
        isLiked = false
    )
    JobDetailRoute(sample, onBack = {}, onToggleLike = {}, onCall = {}, onApply = {})
}
