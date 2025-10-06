package com.example.dodojob.ui.feature.announcement

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R

/* ---------- Colors (최종 스펙) ---------- */
private val BrandBlue = Color(0xFF005FFF)
private val TextBlack = Color(0xFF000000)
private val ScreenBg  = Color(0xFFF1F5F7)
private val SectionBg = Color(0xFFFFFFFF)
private val StatusBg  = Color(0xFFEFEFEF)

/* ---------- Plans ---------- */
enum class PostPlan { Free, Premium }

/* ===== Route Entrypoint ===== */
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

/* ===== Screen ===== */
@Composable
fun Announcement5Screen(
    initial: PostPlan = PostPlan.Free,
    onPost: (PostPlan) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var selected by remember { mutableStateOf(initial) }
    val scroll = rememberScrollState()
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val dynamicWidth = screenWidthDp * (328f / 360f) // ✅ 328/360 비율로 자동 조정

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        /* ✅ Status bar (24dp) */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(StatusBg)
        )

        /* ✅ TopAppBar */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .background(SectionBg)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                "공고등록",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextBlack,
                letterSpacing = (-0.019f * 24).sp
            )
        }

        /* ✅ 설명 헤더 */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(SectionBg)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                "공고에 적용할 상품을 선택해주세요!",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextBlack,
                letterSpacing = (-0.019f * 20).sp
            )
        }

        SectionDivider()

        /* ===== Body ===== */
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
        ) {
            /* ===== 무료 등록 옵션 ===== */
            SectionCard {
                LabelRow("무료 등록 옵션")
                Spacer(Modifier.height(10.dp))

                val freeRes = if (selected == PostPlan.Free)
                    R.drawable.selected_basic_register
                else
                    R.drawable.unselected_basic_register

                ResourceCard(
                    drawableRes = freeRes,
                    dynamicWidth = dynamicWidth,
                    onClick = { selected = PostPlan.Free },
                    contentDesc = "무료 등록 옵션"
                )
            }

            SectionDivider()

            /* ===== 유료 등록 옵션 ===== */
            SectionCard {
                LabelRow("유료 등록 옵션")
                Spacer(Modifier.height(10.dp))

                val premiumRes = if (selected == PostPlan.Premium)
                    R.drawable.selected_premium_register
                else
                    R.drawable.unselected_premium_register

                ResourceCard(
                    drawableRes = premiumRes,
                    dynamicWidth = dynamicWidth,
                    onClick = { selected = PostPlan.Premium },
                    contentDesc = "유료 등록 옵션"
                )
            }
        }

        /* ===== Primary Button ===== */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SectionBg)
                .padding(vertical = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 360.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { onPost(selected) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(47.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "공고 게시하기",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.019f * 18).sp
                    )
                }
            }
        }

        /* Bottom navigation placeholder */
        BottomNavPlaceholder()
    }
}

/* ====== Resource Card (리소스 비율 그대로 표시) ====== */
@Composable
private fun ResourceCard(
    drawableRes: Int,
    dynamicWidth: Dp,
    onClick: () -> Unit,
    contentDesc: String
) {
    Box(
        modifier = Modifier
            .width(dynamicWidth)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDesc,
            modifier = Modifier.fillMaxWidth(),  // 가로만 채움
            contentScale = ContentScale.FillWidth // ✅ 리소스 자체 비율 그대로
        )
    }
}

/* ===== Reusable UI Components ===== */
@Composable
private fun SectionCard(
    padding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SectionBg)
            .padding(vertical = padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
private fun LabelRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(27.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextBlack,
            letterSpacing = (-0.019f * 18).sp
        )
    }
}

/* 구분 영역 */
@Composable
private fun SectionDivider(
    height: Dp = 20.dp,
    color: Color = ScreenBg
) {
    HorizontalDivider(
        thickness = height,
        color = color,
        modifier = Modifier.fillMaxWidth()
    )
}

/* 하단 네비게이션 자리 */
@Composable
private fun BottomNavPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .background(Color(0xFFF4F5F7))
    )
}
