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
import kotlin.math.min

// Size 확장: 짧은 변 길이
private val androidx.compose.ui.geometry.Size.minSide: Float
    get() = min(width, height)

/** ✅ NavGraph에서 호출하는 엔트리 */
@Composable
fun Announcement1Route(nav: NavController /*, 콜백 필요시 추가 */) {
    Announcement1Screen(
        onSubmit = { /* TODO */ },
        onUploadPhoto = { /* TODO */ },
        onTabClick = { /* TODO */ }
    )
}

/** 🔒 실제 UI (NavController 의존 X) */
@Composable
fun Announcement1Screen(
    onSubmit: () -> Unit,
    onUploadPhoto: () -> Unit,
    onTabClick: (Int) -> Unit
) {
    val scroll = rememberScrollState()

    var companyName by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var saveContact by remember { mutableStateOf(false) }
    var placeName by remember { mutableStateOf("") }
    var placeAddress by remember { mutableStateOf("") }

    // 시스템바 인셋을 정확히 적용
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F5F7))
                .padding(inner)
        ) {
            Column(Modifier.fillMaxSize()) {

                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .height(76.dp),
                    contentAlignment = Alignment.Center
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
                    SectionCard {
                        TitleRow(text = "공고등록/설명")
                        Spacer(Modifier.height(4.dp))
                        LabelText(text = "근무회사명")
                        UnderlineField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            placeholder = "내용입력"
                        )
                    }

                    SectionCard(padding = 20.dp) {
                        LabelText(text = "담당자명")
                        OutlinedInput(value = contactName, onValueChange = { contactName = it }, placeholder = "입력")
                        Spacer(Modifier.height(13.dp))

                        LabelText(text = "담당자 연락처")
                        OutlinedInput(value = contactPhone, onValueChange = { contactPhone = it }, placeholder = "입력")
                        Spacer(Modifier.height(13.dp))

                        LabelText(text = "담당자 이메일")
                        OutlinedInput(value = contactEmail, onValueChange = { contactEmail = it }, placeholder = "입력")

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

                    SectionCard {
                        TitleRow(text = "공고등록")
                        Spacer(Modifier.height(10.dp))

                        LabelText(text = "사업장명")
                        OutlinedInput(value = placeName, onValueChange = { placeName = it }, placeholder = "입력")
                    }

                    SectionCard {
                        LabelText(text = "사업장 주소")
                        OutlinedInput(value = placeAddress, onValueChange = { placeAddress = it }, placeholder = "입력")
                    }

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

                    SectionCard {
                        PrimaryButton(text = "공고등록", onClick = onSubmit)
                    }

                    Spacer(Modifier.height(8.dp))
                }

                BottomNavPlaceholder()
            }

            // ✅ 새 APK 여부(설치 시간) 우상단 오버레이
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) { DebugBuildTag() }
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
    // Compose가 추적하는 상태 리스트(센터 x 좌표 px)
    val centersPx = remember(labels.size) {
        mutableStateListOf<Float>().apply { repeat(labels.size) { add(0f) } }
    }
    val indicatorWidth = 41.dp
    val rowPaddingStart = 24.dp // Row의 좌측 패딩과 동일해야 함

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

        // 선택된 탭의 센터가 계산되기 전이면(=0) 인디케이터를 잠시 숨김
        val centerPx = centersPx.getOrNull(selected) ?: 0f
        if (centerPx > 0f) {
            val startInRow = with(density) {
                (centerPx - indicatorWidth.toPx() / 2f).toDp()
            }
            // Row의 좌측 패딩을 Box 좌표계로 보정
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
