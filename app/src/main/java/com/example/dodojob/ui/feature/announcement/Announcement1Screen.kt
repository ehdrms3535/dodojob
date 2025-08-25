package com.example.dodojob.ui.feature.announcement

import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.navigation.Route
import kotlin.math.min
// Size 확장: 짧은 변 길이
private val androidx.compose.ui.geometry.Size.minSide: Float
    get() = min(width, height)



// 2) Route에서 nav 전달 + 클릭 핸들러에 navigate 연결
@Composable
fun Announcement1Route(
    nav: NavController,
    onNext: () -> Unit = {
        // 하단 "다음단계" → 02로 이동
        nav.navigate(Route.Announcement2.path) { launchSingleTop = true }
    },
    onBack: () -> Unit = { nav.popBackStack() },
    onTabClick: (Int) -> Unit = { idx ->
        val target = when (idx) {
            0 -> Route.Announcement.path
            1 -> Route.Announcement2.path
            2 -> Route.Announcement3.path
            else -> Route.Announcement4.path
        }
        val current = nav.currentBackStackEntry?.destination?.route
        if (current != target) {
            nav.navigate(target) { launchSingleTop = true }
        }
    }
) {
    Announcement1Screen(
        onSubmit = onNext,              // 버튼 콜백 연결
        onUploadPhoto = { /* TODO */ },
        onTabClick = onTabClick
    )
}

@Composable
fun Announcement1Screen(
    onSubmit: () -> Unit,
    onUploadPhoto: () -> Unit,
    onTabClick: (Int) -> Unit
) {
    val scroll = rememberScrollState()

    var companyName by remember { mutableStateOf("") }
    var bizNo by remember { mutableStateOf("") }          // 사업자번호
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var saveContact by remember { mutableStateOf(false) }

    var placeAddressSearch by remember { mutableStateOf("") }
    var placeAddressDetail by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F7))
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .height(76.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "공고등록",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    letterSpacing = (-0.46).sp
                )
            }

            // Tabs 01~04
            TabBar(
                selected = 0,
                labels = listOf("01", "02", "03", "04"),
                onClick = onTabClick
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scroll)
            ) {
                // 01. 기본정보
                SectionCard {
                    TitleRow(text = "01. 기본정보를 입력해주세요!")
                    Spacer(Modifier.height(6.dp))

                    // 근무회사명 (언더라인 인풋)
                    LabelText(text = "근무회사명")
                    UnderlineField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        placeholder = "내용입력"
                    )
                }

                // 사업자 등록번호 + 인증하기
                SectionCard(padding = 20.dp) {
                    LabelText(text = "사업자 등록번호")
                    OutlinedInput(
                        value = bizNo,
                        onValueChange = { bizNo = it },
                        placeholder = "000-00-00000"
                    )
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton(
                        text = "인증하기",
                        onClick = { /* TODO: 사업자번호 인증 로직 */ }
                    )
                }

                // 담당자 정보
                SectionCard(padding = 20.dp) {
                    LabelText(text = "담당자명")
                    OutlinedInput(
                        value = contactName,
                        onValueChange = { contactName = it },
                        placeholder = "담당자 성함"
                    )
                    Spacer(Modifier.height(13.dp))

                    LabelText(text = "담당자 연락처")
                    OutlinedInput(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        placeholder = "010-0000-0000"
                    )
                    Spacer(Modifier.height(13.dp))

                    LabelText(text = "담당자 이메일")
                    OutlinedInput(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        placeholder = "이메일을 입력해주세요"
                    )

                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = saveContact, onCheckedChange = { saveContact = it })
                        Text(
                            text = "입력한 담당자 정보 저장",
                            fontSize = 15.sp,
                            color = Color(0xFF828282),
                            letterSpacing = (-0.29).sp
                        )
                    }
                }

                // 주소 블록 (요청 레이아웃)
                SectionCard(padding = 20.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 328.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            LabelText(text = "회사주소")
                            OutlinedInput(
                                value = placeAddressSearch,
                                onValueChange = { placeAddressSearch = it },
                                placeholder = "주소를 검색해주세요"
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 328.dp)
                        ) {
                            PrimaryButton(text = "주소찾기", onClick = { /* TODO */ })
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 328.dp)
                        ) {
                            LabelText(text = "상세주소")
                            OutlinedInput(
                                value = placeAddressDetail,
                                onValueChange = { placeAddressDetail = it },
                                placeholder = "상세주소를 입력해주세요"
                            )
                        }
                    }
                }

                // 근무지 사진 업로드
                SectionCard {
                    Text(
                        text = "근무지 사진",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        letterSpacing = (-0.34).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) {
                            DashedAddBox(size = 74.5.dp, onClick = onUploadPhoto)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // 다음단계 버튼
                SectionCard {
                    PrimaryButton(text = "다음단계", onClick = onSubmit)
                }

                Spacer(Modifier.height(8.dp))
            }

            BottomNavPlaceholder()
        }
    }
}

@Composable
private fun TabBar(
    selected: Int,
    labels: List<String>,
    onClick: (Int) -> Unit
) {
    val density = LocalDensity.current
    val centersPx = remember(labels.size) {
        mutableStateListOf<Float>().apply { repeat(labels.size) { add(0f) } }
    }
    val indicatorWidth = 41.dp
    val rowPaddingStart = 24.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = rowPaddingStart),
            horizontalArrangement = Arrangement.spacedBy(61.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            labels.forEachIndexed { idx, text ->
                Box(
                    modifier = Modifier
                        .onGloballyPositioned { coords ->
                            val center = coords.positionInParent().x + coords.size.width / 2f
                            if (centersPx[idx] != center) centersPx[idx] = center
                        }
                        .clickable { onClick(idx) }
                ) {
                    val color = if (idx == selected) Color(0xFF005FFF) else Color.Black
                    Text(
                        text = text,
                        color = color,
                        fontSize = 16.sp,
                        fontWeight = if (idx == selected) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(24.dp)
                    )
                }
            }
        }

        val centerPx = centersPx.getOrNull(selected) ?: 0f
        if (centerPx > 0f) {
            val startInRow = with(density) { (centerPx - indicatorWidth.toPx() / 2f).toDp() }
            val targetX = rowPaddingStart + startInRow
            val animatedX by animateDpAsState(targetValue = targetX, label = "tab-indicator")

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = animatedX)
                    .width(indicatorWidth)
                    .height(4.dp)
                    .background(Color(0xFF005FFF))
            )
        }
    }
}

@Composable
private fun SectionCard(
    padding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { content() }
    }
}

@Composable
private fun TitleRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            letterSpacing = (-0.38).sp
        )
    }
}

@Composable
private fun LabelText(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        letterSpacing = (-0.34).sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 6.dp)
    )
}

@Composable
private fun UnderlineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val textColor = if (value.isEmpty()) Color(0xFF828282) else Color.Black
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 15.sp, color = textColor),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 23.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 15.sp,
                            color = Color(0xFF828282),
                            letterSpacing = (-0.29).sp
                        )
                    }
                    inner()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFC0C0C0))
        )
    }
}

@Composable
private fun OutlinedInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .border(width = 1.dp, color = Color(0xFF005FFF), shape = shape)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 15.sp, color = if (value.isEmpty()) Color(0xFF828282) else Color.Black),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 15.sp,
                        color = Color(0xFF828282),
                        letterSpacing = (-0.29).sp
                    )
                }
                inner()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DashedAddBox(
    size: Dp,
    onClick: () -> Unit
) {
    val dashColor = Color(0xFF68A0FE)
    val plusColor = Color(0xFF7EAAF3)

    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                val stroke = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
                drawRoundRect(
                    color = dashColor,
                    style = stroke,
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
            }
            .background(Color.White, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.55f)) {
            val strokeWidth = 5f
            val side = this.size.minSide
            val cx = this.size.width * 0.5f
            val cy = this.size.height * 0.5f
            val half = side * 0.275f

            drawLine(color = plusColor, start = Offset(cx, cy - half), end = Offset(cx, cy + half), strokeWidth = strokeWidth)
            drawLine(color = plusColor, start = Offset(cx - half, cy), end = Offset(cx + half, cy), strokeWidth = strokeWidth)
        }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(47.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF005FFF),
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.34).sp
        )
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

/** ▶ 설치/업데이트 시각(새 APK 여부) 표시 */
@Composable
fun DebugBuildTag() {
    val ctx = LocalContext.current
    val pm = ctx.packageManager
    val pkg = ctx.packageName

    val installText = remember {
        runCatching {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(pkg, 0)
            }
            DateFormat.format("MM-dd HH:mm:ss", info.lastUpdateTime).toString()
        }.getOrElse { "preview" }
    }
    Text(
        text = "Installed: $installText",
        color = Color.White,
        fontSize = 10.sp,
        modifier = Modifier
            .background(Color(0xAA000000), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Preview(
    showSystemUi = true,
    device = Devices.PIXEL_7,
    locale = "ko"
)
@Composable
private fun PreviewAnnouncement1() {
    Announcement1Screen(
        onSubmit = {},
        onUploadPhoto = {},
        onTabClick = {}
    )
}
