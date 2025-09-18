package com.example.dodojob.ui.feature.verify

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dodojob.navigation.Route
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.example.dodojob.session.SessionViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

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

    // ✅ 한글 조합 보존(TextFieldValue) – 이름/거주지역
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var gender by remember { mutableStateOf("남") }
    var rrnFront by remember { mutableStateOf("") }              // 주민번호 앞 6자리 (숫자)
    var rrnBackFirst by remember { mutableStateOf("") }          // 주민번호 뒷 첫 자리 (숫자 1)
    var region by remember { mutableStateOf(TextFieldValue("")) } // 거주지역
    var phone by remember { mutableStateOf("") }                  // 휴대전화 (숫자)
    val role = sessionVm.role.collectAsState().value
    Scaffold(
        containerColor = Color(0xFFF1F5F7),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column {
                // 상단 상태바 느낌의 회색 띠
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(Color(0xFFEFEFEF))
                )
                // 뒤로가기 줄
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F7))
                        .padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "<", // "<"로 바꿔도 됨
                        fontSize = 28.sp,
                        color = Color.Black,
                        modifier = Modifier.clickable { nav.navigate(Route.Onboarding.path) }
                    )
                }
                // 큰 제목
                Text(
                    text = "본인인증",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
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

                        if (!ok) {
                            // TODO: 스낵바/토스트로 안내
                            return@Button
                        }

                        val prefill = SignUpPrefill(
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
                            nav.navigate(Route.SignUp.path) {
                                //popUpTo(Route.Verify.path) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        else{
                            nav.navigate(Route.EmploySignup.path) {
                                //popUpTo(Route.Verify.path) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FFF))
                ) {
                    Text("인증완료", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.White)
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
            // 설명 문구
            Text(
                "본인인증을 위해 필요한 정보를\n입력해 주세요",
                fontSize = 20.sp,
                color = Color(0xFF636363),
                lineHeight = 30.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // 이름
            FieldLabel("이름")
            KoreanUnderlineField(
                value = name,
                onValueChange = { name = it },
                placeholder = "이름 입력"
            )

            Spacer(Modifier.height(24.dp))

            // 성별
            FieldLabel("성별")
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                GenderBullet("남", gender == "남") { gender = "남" }
                Spacer(Modifier.width(24.dp))
                GenderBullet("여", gender == "여") { gender = "여" }
            }

            Spacer(Modifier.height(28.dp))

            // 주민등록번호
            FieldLabel("주민등록번호")
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // 앞 6자리
                Column(Modifier.weight(1f)) {
                    UnderlineNumberField(
                        value = rrnFront,
                        onValueChange = { s -> rrnFront = s.filter { it.isDigit() }.take(6) },
                        placeholder = "주민등록번호 앞 6자리"
                    )
                }
                Spacer(Modifier.width(12.dp))
                // 뒷 첫 자리(입력 가능)
                OneDigitNumberBox(
                    value = rrnBackFirst,
                    onValueChange = { rrnBackFirst = it }
                )
                Spacer(Modifier.width(12.dp))
                // 나머지 6자리는 ●●●●●● 마스킹 표현
                MaskDots(count = 6, diameter = 10.dp, color = Color(0xFF757575), gap = 8.dp)
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
            UnderlineNumberField(
                value = phone,
                onValueChange = { s -> phone = s.filter { it.isDigit() }.take(11) },
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

/** 숫자 전용 밑줄 필드(TextField 사용) */
@Composable
private fun UnderlineNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFFA6A6A6)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedIndicatorColor = Color(0xFFC0C0C0),
            unfocusedIndicatorColor = Color(0xFFC0C0C0),
            cursorColor = Color.Black,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        shape = RoundedCornerShape(0.dp)
    )
}

/** ✅ 한글 조합/자유 입력 밑줄 필드(BasicTextField 사용) */
@Composable
private fun KoreanUnderlineField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String
) {
    val style: TextStyle = TextStyle.Default.copy(
        color = Color.Black,
        fontSize = 16.sp
    )
    Column {
        BasicTextField(
            value = value,
            onValueChange = onValueChange, // 조합 문자열 그대로 반영
            textStyle = style,
            singleLine = true,
            cursorBrush = SolidColor(Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth()) {
                    if (value.text.isEmpty()) {
                        Text(text = placeholder, color = Color(0xFFA6A6A6), fontSize = 16.sp)
                    }
                    inner()
                }
            }
        )
        Divider(color = Color(0xFFC0C0C0), thickness = 1.dp)
    }
}

/** ✅ 거주지역 박스(한글 조합 보존) */
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
                textStyle = TextStyle.Default.copy(
                    color = Color.Black,
                    fontSize = 16.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(Color.Black),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                decorationBox = { inner ->
                    Box(Modifier.fillMaxWidth()) {
                        if (value.text.isEmpty()) {
                            Text("거주지역", color = Color(0xFF9E9E9E), fontSize = 16.sp)
                        }
                        inner()
                    }
                }
            )
            Spacer(Modifier.width(8.dp))
            // 시안의 회색 동그라미 (돋보기 자리)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF696969), CircleShape)
            )
        }
    }
}

/** 주민번호 뒷 첫 자리 – 숫자 1글자만 입력 */
@Composable
private fun OneDigitNumberBox(
    value: String,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 36.dp, height = 48.dp)
            .border(1.dp, Color(0xFFD9D9D9))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = { s -> onValueChange(s.filter { it.isDigit() }.take(1)) },
            singleLine = true,
            textStyle = TextStyle.Default.copy(
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

/** 마스킹 점 ●●●●●● */
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
            if (i != count - 1) {
                Spacer(modifier = Modifier.width(gap))
            }
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
        Text(
            text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}
/* ---------- Preview ---------- */
//@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = "id:pixel_7", locale = "ko")
//@Composable
//private fun VerifyScreenPreview() {
//    val nav = rememberNavController()
//    VerifyScreen(nav = nav, sessionVm = sessionVm)
//}
