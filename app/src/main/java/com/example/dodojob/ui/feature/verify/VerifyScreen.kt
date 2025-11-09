package com.example.dodojob.ui.feature.verify

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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.example.dodojob.session.SessionViewModel

@Parcelize
data class SignUpPrefill(
    val name : String,
    val gender : String,
    val rrnFront : String,
    val rrnBackFirst : String,
    val region : String,
    val phone: String,
    val verifiedAt: Long
) : Parcelable

@Composable
fun VerifyScreen(
    nav: NavController,
    sessionVm: SessionViewModel
) {
    val scroll = rememberScrollState()

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var gender by remember { mutableStateOf("남") }

    // ⭐ 바뀐 부분: rrnFront, phone 을 TextFieldValue 로 관리
    var rrnFront by remember { mutableStateOf(TextFieldValue("")) }
    var rrnBackFirst by remember { mutableStateOf("") }
    var region by remember { mutableStateOf(TextFieldValue("")) }
    var phone by remember { mutableStateOf(TextFieldValue("")) }

    val role = sessionVm.role.collectAsState().value

    Scaffold(
        containerColor = Color(0xFFF1F5F7),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column {
                // 상태바
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(Color(0xFFEFEFEF))
                )

                // 뒤로가기
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

                // 타이틀
                Text(
                    text = "본인인증",
                    fontSize = 32.sp,
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
                                && rrnFront.text.length == 6
                                && rrnBackFirst.length == 1
                                && phone.text.length in 10..11

                        if (!ok) return@Button

                        val prefill = SignUpPrefill(
                            name = name.text.trim(),
                            gender = gender,
                            rrnFront = rrnFront.text.trim(),
                            rrnBackFirst = rrnBackFirst,
                            region = region.text.trim(),
                            phone = phone.text.trim(),
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
                        .height(56.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FFF))
                ) {
                    Text("인증완료", fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.White)
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
            Text(
                "본인인증을 위해 필요한 정보를\n입력해 주세요",
                fontSize = 20.sp,
                color = Color(0xFF636363),
                lineHeight = 30.sp,
                modifier = Modifier.padding(top = 0.dp, bottom = 20.dp)
            )

            Spacer(Modifier.height(6.dp))
            // 이름
            FieldLabel("이름")
            Spacer(Modifier.height(6.dp))
            KoreanUnderlineField(
                value = name,
                onValueChange = { name = it },
                placeholder = "이름 입력"
            )

            Spacer(Modifier.height(24.dp))

            // 성별
            FieldLabel("성별")
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                GenderBullet("남", gender == "남") { gender = "남" }
                Spacer(Modifier.width(120.dp))
                GenderBullet("여", gender == "여") { gender = "여" }
            }

            Spacer(Modifier.height(28.dp))

            // 주민등록번호
            FieldLabel("주민등록번호")
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
                    BasicTextField(
                        value = rrnFront,
                        onValueChange = { s ->
                            val filtered = s.text.filter { it.isDigit() }.take(6)
                            rrnFront = TextFieldValue(
                                text = filtered,
                                selection = TextRange(filtered.length) // ⭐ 커서 맨 뒤로
                            )
                        },
                        textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                        singleLine = true,
                        cursorBrush = SolidColor(Color.Black),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(bottom = 6.dp),
                        decorationBox = { inner ->
                            Box(Modifier.fillMaxWidth()) {
                                if (rrnFront.text.isEmpty()) {
                                    Text("주민등록번호 앞 6자리", color = Color(0xFFA6A6A6), fontSize = 18.sp)
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
                        .height(underlineHeight)
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    MaskDots(count = 6, diameter = 10.dp, color = Color(0xFF757575), gap = 8.dp)
                }
            }

            Spacer(Modifier.height(28.dp))

            // 거주지역
            FieldLabel("거주지역")
            Spacer(Modifier.height(6.dp))
            RegionFieldKorean(
                value = region,
                onValueChange = { region = it },
                height = 57.dp,
                radius = 10.dp
            )

            Spacer(Modifier.height(28.dp))

            // 휴대전화
            FieldLabel("휴대전화")
            Spacer(Modifier.height(6.dp))
            KoreanUnderlineField(
                value = phone,
                onValueChange = { s ->
                    val filtered = s.text.filter { it.isDigit() }.take(11)
                    phone = TextFieldValue(
                        text = filtered,
                        selection = TextRange(filtered.length) // ⭐ 커서 맨 뒤로
                    )
                },
                placeholder = "휴대전화 번호 입력"
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

/* ----------------- 재사용 컴포넌트 ----------------- */

@Composable
private fun FieldLabel(text: String) {
    Text(text, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Black)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun KoreanUnderlineField(
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
                        Text(text = placeholder, color = Color(0xFFA6A6A6), fontSize = 18.sp)
                    }
                    inner()
                }
            }
        )
        Divider(color = Color(0xFFC0C0C0), thickness = 1.dp)
    }
}

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
                textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
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
                    .clickable { /* TODO: 검색 */ }
            )
        }
    }
}

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
            .background(Color(0xFFF1F5F7)), // 배경 통일 (#F1F5F7)
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

@Composable
fun MaskDots(
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

@Composable
private fun GenderBullet(
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
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}
