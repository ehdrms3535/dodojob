package com.example.dodojob.ui.feature.announcement

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController

/* -------- Colors -------- */
private val Blue = Color(0xFF005FFF)
private val TextGray = Color(0xFF828282)
private val BgGray = Color(0xFFF1F5F7)
private val CardBg = Color.White

/* -------- Plan -------- */
enum class PostPlan { Free, BasicInstant, StandardPromo, PremiumPromo }

/* ====== Route Entrypoint ====== */
@Composable
fun Announcement5Route(
    nav: NavController,
    defaultPlan: PostPlan = PostPlan.Free,
    onPost: (PostPlan) -> Unit = {},
    onBack: () -> Unit = { nav.popBackStack() }
) {
    Announcement5Screen(
        initial = defaultPlan,
        onPost = onPost,
        onBack = onBack
    )
}

/* ====== Screen ====== */
@Composable
fun Announcement5Screen(
    initial: PostPlan = PostPlan.Free,
    onPost: (PostPlan) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var selected by remember { mutableStateOf(initial) }
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
    ) {
        /* Header */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .background(CardBg)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) { Text("공고등록", fontSize = 24.sp, fontWeight = FontWeight.SemiBold) }

        /* Body */
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
        ) {
            /* 안내 */
            SectionCard { TitleRow("공고에 적용할 상품을 선택해주세요!") }

            /* ✅ 구분선: 안내 ↔ 무료 등록 옵션 */
            SectionDivider()

            /* ===== 무료 등록 옵션 ===== */
            SectionCard {
                LabelText("무료 등록 옵션")
                Spacer(Modifier.height(15.dp))

                PlanCard(
                    title = "기본 등록",
                    price = "0원",
                    selected = selected == PostPlan.Free,
                    onClick = { selected = PostPlan.Free },
                    highlightBorder = true,
                    premiumBackground = false,
                    features = listOf(
                        F("7일간 게시", highlight = "7일"),
                        F("기본 검색 노출"),
                        F("일반 공고 표시"),
                        F("기본 지원자 관리"),
                        F("이메일 지원 접수")
                    ),
                    // 두 번째 이미지 스타일: 첫 줄의 '24시간 검수'만 파랑
                    footerTitle = "24시간 검수 후 게시",
                    footerCaption = "검수 과정에서 수정이 요청될 수 있습니다"
                )
            }

            /* ✅ 구분선: 무료 등록 옵션 ↔ 유료 등록 옵션 */
            SectionDivider()

            /* ===== 유료 등록 옵션 ===== */
            SectionCard {
                LabelText("유료 등록 옵션")
                Spacer(Modifier.height(15.dp))

                PlanCard(
                    title = "기본 즉시 게시",
                    price = "9,900원",
                    selected = selected == PostPlan.BasicInstant,
                    onClick = { selected = PostPlan.BasicInstant },
                    highlightBorder = true,
                    premiumBackground = false,
                    features = listOf(
                        F("즉시 게시", highlight = "즉시"),
                        F("2주간 게시", highlight = "2주"),
                        F("상위 노출 3일", highlight = "3일"),
                        F("지원자 알림"),
                        F("이메일 지원 접수")
                    )
                )

                PlanCard(
                    title = "스탠다드 홍보",
                    price = "19,900원",
                    selected = selected == PostPlan.StandardPromo,
                    onClick = { selected = PostPlan.StandardPromo },
                    highlightBorder = false,
                    premiumBackground = true,
                    popularBadge = true,
                    features = listOf(
                        F("즉시 게시", highlight = "즉시"),
                        F("3주간 게시", highlight = "3주"),
                        F("상위 노출 7일", highlight = "7일"),
                        F("강조 표시"),
                        F("지원자 알림"),
                        F("이메일 지원 접수"),
                        F("우선 매칭")
                    )
                )

                PlanCard(
                    title = "프리미엄 홍보",
                    price = "29,900원",
                    selected = selected == PostPlan.PremiumPromo,
                    onClick = { selected = PostPlan.PremiumPromo },
                    highlightBorder = true,
                    premiumBackground = false,
                    features = listOf(
                        F("즉시 게시", highlight = "즉시"),
                        F("한달간 게시", highlight = "한달"),
                        F("상위 노출 14일", highlight = "14일"),
                        F("프리미엄 강조 표시"),
                        F("지원자 알림"),
                        F("이메일 지원 접수"),
                        F("우선 매칭")
                    )
                )
            }

            /* ✅ 구분선: 프리미엄 홍보(마지막 카드) ↔ CTA 버튼 */
            SectionDivider()
        }

        /* 하단 CTA */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg)
                .padding(vertical = 20.dp, horizontal = 16.dp)
        ) {
            Button(
                onClick = { onPost(selected) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(47.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White)
            ) {
                Text("공고 게시하기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        BottomNavPlaceholder()
    }
}

/* --------- Reusable --------- */

private data class F(
    val text: String,
    val emph: Boolean = false,      // 전체 파랑(굵게) 적용 여부(옵션)
    val highlight: String? = null   // 텍스트 일부만 파랗게 표시할 단어
)

/** 회색 구분선 */
@Composable
private fun SectionDivider(
    height: Dp = 20.dp,
    color: Color = Color(0xFFF1F5F7),
    verticalPadding: Dp = 0.dp
) {
    Column(Modifier.fillMaxWidth()) {
        if (verticalPadding > 0.dp) Spacer(Modifier.height(verticalPadding))
        HorizontalDivider(
            thickness = height,
            color = color,
            modifier = Modifier.fillMaxWidth()
        )
        if (verticalPadding > 0.dp) Spacer(Modifier.height(verticalPadding))
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    selected: Boolean,
    onClick: () -> Unit,
    highlightBorder: Boolean,
    premiumBackground: Boolean,
    features: List<F>,
    popularBadge: Boolean = false,

    // 하단 안내 박스 (두 줄)
    footerTitle: String? = null,      // 예: "24시간 검수 후 게시" → '24시간 검수'만 파랑
    footerCaption: String? = null,    // 예: "검수 과정에서 …"
    // (옵션) 기존 한 줄 안내를 쓰고 싶으면 사용
    footerNote: String? = null,
    footerGreyOnWhite: Color = Color.Unspecified,
    footerGreyOnSelected: Color = Color.Unspecified
) {
    val shape = RoundedCornerShape(10.dp)

    val bgColor =
        if (selected) Color(0xFFDEEBFF)
        else if (premiumBackground) Color(0xFFFFFEFA) else Color.White

    val borderColor =
        if (premiumBackground && !selected) Color(0xFFFFE600) else Color(0xFF005FFF)
    val borderWidth = if (selected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (highlightBorder || premiumBackground) borderWidth else 0.dp,
                color = if (highlightBorder || premiumBackground) borderColor else Color.Transparent,
                shape = shape
            )
            .background(bgColor, shape)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Text(price, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Blue)
            }
            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                features.forEach { f ->
                    FeatureRow(text = f.text, blue = f.emph, highlight = f.highlight)
                }
            }

            // ===== 하단 안내 박스 =====
            when {
                !footerTitle.isNullOrBlank() || !footerCaption.isNullOrBlank() -> {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEFEFEF), RoundedCornerShape(10.dp))
                            .padding(vertical = 14.dp, horizontal = 16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            if (!footerTitle.isNullOrBlank()) {
                                // "24시간 검수"만 파란색, 나머지는 검은색
                                val keyword = "24시간 검수"
                                val annotated = buildAnnotatedString {
                                    if (footerTitle.contains(keyword)) {
                                        val s = footerTitle.indexOf(keyword)
                                        val e = s + keyword.length
                                        append(footerTitle.substring(0, s))
                                        pushStyle(SpanStyle(color = Blue, fontWeight = FontWeight.Bold))
                                        append(keyword)
                                        pop()
                                        append(footerTitle.substring(e))
                                    } else {
                                        append(footerTitle)
                                    }
                                }
                                Text(
                                    annotated,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    lineHeight = 20.sp
                                )
                            }
                            if (!footerCaption.isNullOrBlank()) {
                                Text(
                                    footerCaption,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextGray,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
                !footerNote.isNullOrBlank() -> {
                    Spacer(Modifier.height(10.dp))
                    val footerBg = if (selected && footerGreyOnSelected != Color.Unspecified)
                        footerGreyOnSelected else footerGreyOnWhite
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (footerBg == Color.Unspecified) Color(0xFFF0F0F0) else footerBg,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(vertical = 15.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            footerNote,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blue,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        if (popularBadge) {
            val badgeBg = if (selected) Color(0xFF0064FF) else Color(0xFFFFE600)
            val badgeTextColor = if (selected) Color.White else Color.Black
            val badgeBorderWidth = if (selected) 2.dp else 0.dp
            val badgeBorderColor = if (selected) Color(0xFF005FFF) else Color.Transparent

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 14.dp, y = (-13).dp)
                    .zIndex(1f)
                    .background(badgeBg, RoundedCornerShape(20.dp))
                    .border(badgeBorderWidth, badgeBorderColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text("인기", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = badgeTextColor)
            }
        }
    }

    Spacer(Modifier.height(20.dp)) // 카드 간 간격
}

@Composable
private fun FeatureRow(text: String, blue: Boolean, highlight: String? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("✓", fontSize = 12.sp, color = Blue)
        Spacer(Modifier.width(5.dp))

        if (!highlight.isNullOrBlank() && text.contains(highlight)) {
            Text(
                buildAnnotatedString {
                    val start = text.indexOf(highlight)
                    val end = start + highlight.length
                    append(text.substring(0, start))
                    pushStyle(SpanStyle(color = Blue, fontWeight = FontWeight.Bold))
                    append(highlight)
                    pop()
                    append(text.substring(end))
                },
                fontSize = 12.sp,
                fontWeight = if (blue) FontWeight.Bold else FontWeight.Medium,
                color = if (blue) Blue else TextGray
            )
        } else {
            Text(
                text,
                fontSize = 12.sp,
                fontWeight = if (blue) FontWeight.Bold else FontWeight.Medium,
                color = if (blue) Blue else TextGray
            )
        }
    }
}

@Composable
private fun SectionCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(horizontal = 16.dp),
            content = content
        )
    }
}

@Composable private fun TitleRow(text: String) {
    Text(text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.38).sp)
}

@Composable private fun LabelText(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        letterSpacing = (-0.34).sp
    )
}

@Composable private fun BottomNavPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .background(Color(0xFFF4F5F7))
    )
}

/* -------- Preview -------- */
@Preview(showSystemUi = true, device = Devices.PIXEL_7, locale = "ko")
@Composable
private fun PreviewAnnouncement5() {
    Announcement5Screen(onPost = {})
}
