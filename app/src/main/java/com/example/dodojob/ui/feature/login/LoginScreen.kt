package com.example.dodojob.ui.feature.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.BoxWithConstraints

@Composable
fun LoginScreen(nav: NavController) {
    var id by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var autoLogin by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val W = maxWidth      // 화면 가로(Dp)
        val H = maxHeight     // 화면 세로(Dp)

        // ===== 비율 기반 스케일 =====
        val screenHPad   = (W * 0.045f).coerceIn(12.dp, 24.dp)     // 좌우 패딩
        val topVPad      = (H * 0.03f).coerceIn(8.dp, 28.dp)       // 상단 여백

        val backSizeSp   = (W.value * 0.065f).sp                   // "<" 크기
        val titleSp      = (W.value * 0.09f).sp                    // "로그인" 타이틀
        val subtitleSp   = (W.value * 0.065f).sp                   // 서브 타이틀
        val subtitleLH   = (W.value * 0.095f).sp                   // 서브 타이틀 lineHeight

        val fieldGap     = (H * 0.015f).coerceIn(8.dp, 18.dp)      // 입력칸 간격
        val sectionGap   = (H * 0.02f).coerceIn(12.dp, 24.dp)      // 섹션 간격

        val circleSize   = (W * 0.065f).coerceIn(20.dp, 28.dp)     // 자동로그인 원 버튼 크기
        val checkSize    = (circleSize * 0.65f)                    // 내부 체크 크기
        val loginBtnH    = (H * 0.07f).coerceIn(48.dp, 60.dp)      // 로그인 버튼 높이
        val signBtnH     = (H * 0.07f).coerceIn(48.dp, 60.dp)      // 회원가입 버튼 높이
        val betweenBtns  = (H * 0.02f).coerceIn(12.dp, 20.dp)      // 버튼들 사이 간격

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = screenHPad)
                .padding(top = topVPad),
            horizontalAlignment = Alignment.Start
        ) {
            // 🔙 상단 "<" (얇게)
            TextButton(onClick = { nav.popBackStack() }) {
                Text(
                    "<",
                    fontSize = backSizeSp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
            }

            // 제목 (왼쪽 여백 살짝)
            Spacer(Modifier.height((H * 0.005f).coerceAtLeast(2.dp)))
            Text(
                "로그인",
                fontSize = titleSp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(start = (W * 0.02f).coerceIn(6.dp, 14.dp))
            )

            Spacer(Modifier.height((H * 0.01f).coerceIn(8.dp, 16.dp)))

            // ✨ 서브타이틀 (왼쪽 여백)
            Text(
                "당신의 경험이 빛날 곳,\n두두잡에서 만나보세요.",
                fontSize = subtitleSp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                lineHeight = subtitleLH,
                modifier = Modifier.padding(start = (W * 0.02f).coerceIn(6.dp, 14.dp))
            )

            Spacer(Modifier.height((H * 0.03f).coerceIn(16.dp, 32.dp)))

            // 🔑 아이디 입력 (밑줄 스타일)
            UnderlineTextField(
                value = id,
                onValueChange = { id = it },
                placeholder = "아이디"
            )

            Spacer(Modifier.height(fieldGap))

            // 🔒 비밀번호 입력 (밑줄 스타일)
            UnderlineTextField(
                value = pw,
                onValueChange = { pw = it },
                placeholder = "비밀번호",
                isPassword = true
            )

            // ✅ 자동로그인 (배경 회색 원, 테두리 없음, 체크 검은/흰색 선택 가능)
            Spacer(Modifier.height(sectionGap))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { autoLogin = !autoLogin },
                    modifier = Modifier.size(circleSize),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (autoLogin) Color(0xFF555555) else Color(0xFFDDDDDD),
                        contentColor = Color.White
                    )
                ) {
                    if (autoLogin) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "자동 로그인 체크됨",
                            tint = Color.White,
                            modifier = Modifier.size(checkSize)
                        )
                    }
                }
                Spacer(Modifier.width((W * 0.02f).coerceIn(6.dp, 14.dp)))
                Text("자동로그인", fontSize = (W.value * 0.045f).sp, color = Color.Black)
            }

            // 🔵 로그인 버튼
            Spacer(Modifier.height(sectionGap))
            Button(
                onClick = { /* TODO: 로그인 로직 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(loginBtnH),
                shape = RoundedCornerShape((W * 0.08f).coerceIn(16.dp, 28.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FFF))
            ) {
                Text(
                    "로그인",
                    fontSize = (W.value * 0.055f).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // 📌 아이디 찾기 | 비밀번호 찾기
            Spacer(Modifier.height(betweenBtns))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { /* TODO */ }) {
                    Text("아이디 찾기", fontSize = (W.value * 0.043f).sp, color = Color.Black)
                }
                Text(" | ", fontSize = (W.value * 0.043f).sp, color = Color.Black)
                TextButton(onClick = { /* TODO */ }) {
                    Text("비밀번호 찾기", fontSize = (W.value * 0.043f).sp, color = Color.Black)
                }
            }

            // ⚪ 회원가입 버튼
            Spacer(Modifier.height(betweenBtns))
            OutlinedButton(
                onClick = { /* TODO: 회원가입 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(signBtnH),
                shape = RoundedCornerShape((W * 0.08f).coerceIn(16.dp, 28.dp)),
                border = ButtonDefaults.outlinedButtonBorder,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF7E7D7D)
                )
            ) {
                Text(
                    "휴대폰 번호로 회원가입",
                    fontSize = (W.value * 0.055f).sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/* -------------------------------
   밑줄만 보이는 입력창 (Material3 TextField)
-------------------------------- */
@Composable
private fun UnderlineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = { Text(placeholder, color = Color(0xFFA6A6A6)) },
        modifier = modifier.fillMaxWidth(),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(0.dp), // 컨테이너 모양은 의미 없음(투명)
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,

            focusedIndicatorColor = Color.Black,        // 포커스 밑줄
            unfocusedIndicatorColor = Color(0xFFA2A2A2),// 기본 밑줄
            disabledIndicatorColor = Color(0xFFE0E0E0),

            cursorColor = Color.Black,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            errorTextColor = Color.Black,
            focusedPlaceholderColor = Color(0xFFA6A6A6),
            unfocusedPlaceholderColor = Color(0xFFA6A6A6)
        )
    )
}
