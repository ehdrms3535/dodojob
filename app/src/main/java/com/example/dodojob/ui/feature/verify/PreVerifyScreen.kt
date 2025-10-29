package com.example.dodojob.ui.feature.verify

import android.os.Parcelable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import kotlinx.parcelize.Parcelize
import com.example.dodojob.session.SessionViewModel

@Parcelize
data class PreVerifyPrefill(
    val name : String,
    val gender : String,
    val rrnFront : String,
    val rrnBackFirst : String,
    val region : String,
    val phone: String,
    val verifiedAt: Long
) : Parcelable

@Composable
fun PreVerifyScreen(
    nav: NavController,
    sessionVm: SessionViewModel
) {
    val scroll = rememberScrollState()

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var gender by remember { mutableStateOf("남") }
    var rrnFront by remember { mutableStateOf("") }
    var rrnBackFirst by remember { mutableStateOf("") }
    var region by remember { mutableStateOf(TextFieldValue("")) }
    var phone by remember { mutableStateOf("") }
    val role = sessionVm.role.collectAsState().value

    Scaffold(
        containerColor = Color(0xFFF1F5F7),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column {
                // 상태바 영역
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(Color(0xFFEFEFEF))
                )

                // 뒤로가기 (LoginScreen과 동일한 위치/터치영역)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F7))
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 24.dp, start = 6.dp)
                            .size(48.dp)
                            .clickable { nav.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "뒤로가기",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 타이틀 — 24/600
                Text(
                    text = "본인인증",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .background(Color(0xFFF1F5F7))
            ) {
                Button(
                    onClick = {
                        val ok = name.text.isNotBlank()
                                && (gender == "남" || gender == "여")
                                && rrnFront.length == 6
                                && rrnBackFirst.length == 1
                                && phone.length in 10..11

                        if (!ok) return@Button

                        val prefill = PreVerifyPrefill(
                            name = name.text.trim(),
                            gender = gender,
                            rrnFront = rrnFront,
                            rrnBackFirst = rrnBackFirst,
                            region = region.text.trim(),
                            phone = phone,
                            verifiedAt = System.currentTimeMillis()
                        )
                        nav.currentBackStackEntry?.savedStateHandle?.set("prefill", prefill)

                        if (role == "시니어") {
                            nav.navigate(Route.SignUp.path) { launchSingleTop = true }
                        } else {
                            nav.navigate(Route.EmploySignup.path) { launchSingleTop = true }
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .height(47.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FFF))
                ) {
                    // 버튼 라벨 — 18/700
                    Text(
                        "인증완료",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .imePadding()
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 서브텍스트 — 20/400
            Text(
                "본인인증을 위해 필요한 정보를\n입력해 주세요",
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF636363),
                lineHeight = 30.sp,
                modifier = Modifier.padding(top = 0.dp, bottom = 20.dp)
            )

            // 이름
            FieldLabel18("이름")
            KoreanUnderlineField15(
                value = name,
                onValueChange = { name = it },
                placeholder = "이름 입력"
            )

            Spacer(Modifier.height(24.dp))

            // 성별
            FieldLabel18("성별")
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp)
            ) {
                GenderBullet18("남", gender == "남") { gender = "남" }
                Spacer(Modifier.width(120.dp))
                GenderBullet18("여", gender == "여") { gender = "여" }
            }

            Spacer(Modifier.height(28.dp))

            // 주민등록번호
            FieldLabel18("주민등록번호")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                val underlineHeight = 48.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.50f)
                        .height(underlineHeight)
                ) {
                    // 플레이스홀더 15
                    BasicTextField(
                        value = TextFieldValue(rrnFront),
                        onValueChange = { s ->
                            val filtered = s.text.filter { it.isDigit() }.take(6)
                            rrnFront = filtered
                        },
                        textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),
                        singleLine = true,
                        cursorBrush = SolidColor(Color.Black),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(bottom = 6.dp),
                        decorationBox = { inner ->
                            Box(Modifier.fillMaxWidth()) {
                                if (rrnFront.isEmpty()) {
                                    Text(
                                        "주민등록번호 앞 6자리",
                                        color = Color(0xFFA6A6A6),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                inner()
                            }
                        }
                    )
                    Divider(
                        color = Color(0xFFC0C0C0),
                        thickness = 1.dp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                    )
                }

                Spacer(Modifier.width(12.dp))

                OneDigitNumberBox(
                    value = rrnBackFirst,
                    onValueChange = { rrnBackFirst = it },
                    height = underlineHeight
                )

                Spacer(Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .height(underlineHeight),
                    contentAlignment = Alignment.CenterStart
                ) {
                    MaskDotsRow(count = 6, diameter = 10.dp, color = Color(0xFF757575), gap = 8.dp)
                }
            }

            Spacer(Modifier.height(28.dp))

            // 거주지역
            FieldLabel18("거주지역")
            Spacer(Modifier.height(6.dp))
            RegionFieldKorean(
                value = region,
                onValueChange = { region = it },
                height = 57.dp,
                radius = 10.dp
            )

            Spacer(Modifier.height(28.dp))

            // 휴대전화
            FieldLabel18("휴대전화")
            Spacer(Modifier.height(6.dp))
            KoreanUnderlineField15(
                value = TextFieldValue(phone),
                onValueChange = { s ->
                    val filtered = s.text.filter { it.isDigit() }.take(11)
                    phone = filtered
                },
                placeholder = "휴대전화 번호 입력"
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

/* ----------------- 재사용 컴포넌트 (폰트사이즈만 조정) ----------------- */

// 라벨 18/500
@Composable
private fun FieldLabel18(text: String) {
    Text(
        text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black
    )
    Spacer(Modifier.height(8.dp))
}

// 밑줄 입력 (한글 조합 보존) — 플레이스홀더 15
@Composable
private fun KoreanUnderlineField15(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String
) {
    val style = TextStyle(color = Color.Black, fontSize = 18.sp)
    Column {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = style,
            singleLine = true,
            cursorBrush = SolidColor(Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth()) {
                    if (value.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFFA6A6A6),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    inner()
                }
            }
        )
        Divider(color = Color(0xFFC0C0C0), thickness = 1.dp)
    }
}

// 거주지역 박스 내부 입력 — 텍스트 18, (placeholder 없음)
@Composable
private fun RegionFieldKorean(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    height: Dp,
    radius: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(radius))
            .background(Color.White)
            .padding(horizontal = 17.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),
                singleLine = true,
                cursorBrush = SolidColor(Color.Black),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            Spacer(Modifier.width(8.dp))
            Image(
                painter = painterResource(R.drawable.verify_search),
                contentDescription = "지역 검색",
                modifier = Modifier
                    .requiredSize(24.dp)
                    .clickable { /* TODO */ }
            )
        }
    }
}

// 주민등록번호 뒷 첫 자리 네모 (배경 통일)
@Composable
private fun OneDigitNumberBox(
    value: String,
    onValueChange: (String) -> Unit,
    height: Dp = 48.dp
) {
    Box(
        modifier = Modifier
            .width(40.dp)
            .height(height)
            .border(1.dp, Color(0xFFD9D9D9))
            .background(Color(0xFFF1F5F7)),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = { s -> onValueChange(s.filter { it.isDigit() }.take(1)) },
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(Color.Black),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        )
    }
}

// 마스킹점
@Composable
private fun MaskDotsRow(
    count: Int,
    diameter: Dp,
    color: Color,
    gap: Dp
) {
    Row {
        repeat(count) { i ->
            Box(
                modifier = Modifier
                    .size(diameter)
                    .background(color, CircleShape)
            )
            if (i != count - 1) Spacer(Modifier.width(gap))
        }
    }
}

// 성별 라디오 — 라벨 18/500
@Composable
private fun GenderBullet18(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color(0xFFD9D9D9), CircleShape)
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = if (selected) Color(0xFF005FFF) else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(
                        if (selected) Color(0xFF005FFF) else Color(0xFFB5B5B5),
                        CircleShape
                    )
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}
