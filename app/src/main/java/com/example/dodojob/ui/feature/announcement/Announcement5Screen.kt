package com.example.dodojob.ui.feature.announcement

import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.dodojob.R
import com.example.dodojob.dao.getCompanyIdByUsername
import com.example.dodojob.dao.getannouncebycom
import com.example.dodojob.data.announcement.price4.PriceDto
import com.example.dodojob.data.announcement.price4.PriceRepo
import com.example.dodojob.data.announcement.price4.PriceRepoSupabase
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.navigation.Route
import com.example.dodojob.session.CurrentUser
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas

/* ---------- Colors ---------- */
private val BrandBlue = Color(0xFF005FFF)
private val TextBlack = Color(0xFF000000)
private val ScreenBg = Color(0xFFF1F5F7)
private val SectionBg = Color(0xFFFFFFFF)
private val StatusBg = Color(0xFFEFEFEF)

/* ---------- Plans ---------- */
enum class PostPlan { Free, Premium }

/* ===== Route Entrypoint ===== */
@Composable
fun Announcement5Route(
    nav: NavHostController,
    defaultPlan: PostPlan = PostPlan.Free,
) {
    Announcement5Screen(
        initial = defaultPlan,
        onPost = { _ ->
            nav.navigate(Route.Announcement6.path) {
                launchSingleTop = true
            }
        },
        onBack = { nav.popBackStack() }
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
    val client = LocalSupabase.current
    val repo: PriceRepo = PriceRepoSupabase(client)
    val scope = rememberCoroutineScope()

    var premiumDays by rememberSaveable { mutableStateOf(2) }
    var premiumPrice by rememberSaveable { mutableStateOf(19000) }
    var loading by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        /* Status bar */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(StatusBg)
        )

        /* TopAppBar */
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
                color = TextBlack
            )
        }

        /* Header */
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
                color = TextBlack
            )
        }

        SectionDivider()

        /* ===== Body ===== */
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
        ) {
            // ===== 무료 등록 옵션 =====
            SectionCard {
                LabelRow("무료 등록 옵션")
                Spacer(Modifier.height(10.dp))

                val freeRes = if (selected == PostPlan.Free)
                    R.drawable.selected_basic_register
                else
                    R.drawable.unselected_basic_register

                ResourceCard(
                    drawableRes = freeRes,
                    dynamicWidth = LocalConfiguration.current.screenWidthDp.dp * (328f / 360f),
                    onClick = { selected = PostPlan.Free },
                    contentDesc = "무료 등록 옵션"
                )
            }

            SectionDivider()

            // ===== 유료 등록 옵션 (프리미엄 카드 1회만) =====
            SectionCard {
                LabelRow("유료 등록 옵션")
                Spacer(Modifier.height(20.dp))

                // ✅ 이 Box가 “카드 밖 좌우 여백”을 담당함
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)  // ← 🔹 여기가 ‘카드 밖 좌우 여백’ 조정 포인트!
                ) {
                    PremiumOptionCard(
                        modifier = Modifier.clickable { selected = PostPlan.Premium },
                        isSelected = (selected == PostPlan.Premium),
                        selectedDays = premiumDays,
                        onPlanChange = { d, p ->
                            premiumDays = d
                            premiumPrice = p
                            selected = PostPlan.Premium
                        },
                        horizontalPadding = 24.dp // ← 카드 안쪽 좌우 여백 (다른 조정 포인트)
                    )
                }
            }

        }

        /* ===== Primary Button ===== */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SectionBg)
                .navigationBarsPadding()
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
                    onClick = {
                        scope.launch {
                            loading = true
                            runCatching {
                                val companyid = getCompanyIdByUsername(CurrentUser.username)
                                val announceid = getannouncebycom(companyid)
                                    ?: error("해당 회사의 공고가 없습니다.")
                                val announce = announceid.id
                                val pricing = selected == PostPlan.Premium
                                val days = if (pricing) premiumDays else 0
                                val price = PriceDto(
                                    id = announce,
                                    price = pricing,
                                    date = days.toLong()
                                )
                                repo.insertPrice(price)
                            }.onSuccess { onPost(selected) }
                            loading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(47.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("공고 게시하기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/* ====== Resource Card ====== */
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
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
    }
}

/* ===== Reusable Components ===== */
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
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
    }
}

@Composable
private fun SectionDivider(height: Dp = 20.dp, color: Color = ScreenBg) {
    HorizontalDivider(thickness = height, color = color, modifier = Modifier.fillMaxWidth())
}

/* ----------------------------- */
/* ====== 프리미엄 카드 ===== */
/* ----------------------------- */

private data class PremiumPlan(val days: Int, val price: Int)

private val PREMIUM_PLANS = listOf(
    PremiumPlan(2, 19000),
    PremiumPlan(3, 27000),
    PremiumPlan(5, 45000),
    PremiumPlan(7, 62000),
    PremiumPlan(14, 119000),
    PremiumPlan(30, 199000),
)

private fun Int.won(): String = "%,d원".format(this)

/* 드롭다운 */
@Composable
private fun TinyDropdown(
    selectedLabel: String,
    items: List<String>,
    onSelect: (index: Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .width(67.dp)
                .height(30.dp)
                .border(0.5.dp, Color(0xFF828282), RoundedCornerShape(5.dp))
                .clip(RoundedCornerShape(5.dp))
                .clickable { expanded = true }
                .padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selectedLabel, fontSize = 12.sp, color = Color(0xFF828282), modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = Color(0xFF828282),
                modifier = Modifier.size(20.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEachIndexed { idx, label ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        expanded = false
                        onSelect(idx)
                    }
                )
            }
        }
    }
}

/* 체크 라인 + 설명 */
@Composable
private fun FeatureRow(
    text: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.announce5_check),
            contentDescription = "체크 표시",
            modifier = Modifier
                .width(14.dp)
                .height(8.dp)
        )

        Spacer(Modifier.width(5.dp))

        Text(
            text = text,
            fontSize = 13.sp,
            lineHeight = 12.sp,
            color = Color(0xFF828282),
            letterSpacing = (-0.019f * 12).sp,
            maxLines = 1
        )
    }
}

@Composable
private fun PremiumOptionCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    selectedDays: Int,
    onPlanChange: (days: Int, price: Int) -> Unit,
    horizontalPadding: Dp = 20.dp
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWidth * (352f / 360f)  // 카드 밖 여백을 줄인 폭(네가 쓰던 값 유지)

    val selIndex = PREMIUM_PLANS.indexOfFirst { it.days == selectedDays }.coerceAtLeast(0)
    val plan = PREMIUM_PLANS[selIndex]

    val cardBg  = if (isSelected) Color(0xFFF7FAFF) else Color(0xFFFFFEFA)
    val cardBd  = if (isSelected) Color(0xFF005FFF) else Color(0xFFFFE600)
    val badgeBg = if (isSelected) Color(0xFF005FFF) else Color(0xFFFFE600)
    val badgeTxt = if (isSelected) Color.White else Color.Black

    Box(modifier = modifier.width(cardWidth)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 185.dp) // 콘텐츠 늘어나면 자동 확장
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, cardBd, RoundedCornerShape(10.dp))
                .background(cardBg)
                .padding(vertical = 20.dp, horizontal = horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(23.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "프리미엄 공고",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack,
                    letterSpacing = (-0.019f * 15).sp
                )
                Text(
                    plan.price.won(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    letterSpacing = (-0.019f * 16).sp
                )
            }

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
                .offset(x = (-3).dp),
                verticalArrangement = Arrangement.spacedBy(3.dp) // ✅ 줄 간 간격 줄임
            ) {
                FeatureRow("공고목록 상단 노출")
                FeatureRow("시니어 홈화면 노출")
                FeatureRow("실시간 공고 추천 알림톡 발송")
                FeatureRow("맞춤 인재 추천")
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "시작일(무료)  +",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF828282),
                    letterSpacing = (-0.019f * 12).sp
                )
                Spacer(Modifier.width(8.dp))
                TinyDropdown(
                    selectedLabel = "${plan.days}일",
                    items = PREMIUM_PLANS.map { "${it.days}일" },
                    onSelect = { idx ->
                        val p = PREMIUM_PLANS[idx]
                        onPlanChange(p.days, p.price)
                    }
                )
            }
        }

        // 인기 배지
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 14.dp, y = (-13).dp)
                .size(width = 59.dp, height = 26.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "인기",
                color = badgeTxt,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.019f * 12).sp
            )
        }
    }
}


@Composable
private fun BottomNavPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .background(Color(0xFFF4F5F7))
    )
}