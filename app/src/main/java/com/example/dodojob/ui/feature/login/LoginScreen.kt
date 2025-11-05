package com.example.dodojob.ui.feature.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import com.example.dodojob.data.supabase.LocalSupabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import com.example.dodojob.session.CurrentUser
import com.example.dodojob.session.SessionViewModel
import androidx.compose.ui.unit.em

@Serializable
private data class LoginRow(
    val id: String,
    val username: String,
    val password: String,
    val name: String? = null,
    val job: String? = null
)

@Composable
fun LoginScreen(nav: NavController, sessionVm: SessionViewModel) {
    var id by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var autoLogin by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    val client = LocalSupabase.current
    val scope = rememberCoroutineScope()

    val BrandBlue = Color(0xFF005FFF)
    val Bg = Color(0xFFF1F5F7)
    val StatusGrey = Color(0xFFEFEFEF)
    val LineGrey = Color(0xFFC0C0C0)
    val SubBtnBorder = Color(0xFFE1E1E1)
    val SubBtnText = Color(0xFF7E7D7D)
    val letter = (-0.019f).em

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        // 상태바
        Box(
            Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(StatusGrey)
                .align(Alignment.TopCenter)
        )

        // 본문
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 43.dp), // 하단 네비 영역 고려
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .padding(top = 24.dp, start = 6.dp)  // 위치 여백
                    .size(48.dp)                          // 🔹 터치 영역 (표준)
                    .clickable { nav.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "뒤로가기",
                    modifier = Modifier.size(24.dp)       // 🔹 실제 아이콘 크기 (Figma 기준)
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "로그인",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = letter,
                color = Color.Black,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = "당신의 경험이 빛날 곳,\n두두잡에서 만나보세요.",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = letter,
                color = Color.Black,
                lineHeight = 36.sp,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(Modifier.height(20.dp))
            UnderlineTextField(
                value = id,
                onValueChange = { id = it },
                placeholder = "아이디 혹은 전화번호",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
            UnderlineTextField(
                value = pw,
                onValueChange = { pw = it },
                placeholder = "비밀번호",
                isPassword = true,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(22.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, bottom = 30.dp)
            ) {
                // 상태에 따라 이미지 리소스 선택
                val iconRes = if (autoLogin) {
                    R.drawable.autologin_checked
                } else {
                    R.drawable.autologin_unchecked
                }

                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "자동로그인",
                    modifier = Modifier
                        .size(width = 24.dp, height = 25.dp)   // Figma 기준
                        .padding(top = 1.dp)
                        .clickable { autoLogin = !autoLogin }
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    "자동로그인",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = letter,
                    color = Color.Black
                )
            }

            // 로그인 버튼
            Button(
                onClick = {
                    status = null
                    loading = true
                    scope.launch {
                        runCatching {
                            val res = client.from("users_tmp").select {
                                filter { eq("username", id.trim()) }
                                limit(1)
                            }
                            val json = Json { ignoreUnknownKeys = true }
                            val list = json.decodeFromJsonElement(
                                ListSerializer(LoginRow.serializer()),
                                Json.parseToJsonElement(res.data)
                            )
                            val user = list.firstOrNull() ?: error("존재하지 않는 아이디입니다.")
                            if (user.password != pw) error("비밀번호가 일치하지 않습니다.")
                            val currentJob = user.job?.trim().takeUnless { it.isNullOrEmpty() } ?: "미지정"
                            if (currentJob != "시니어") error("시니어 전용 탭입니다. (현재: $currentJob)")
                            user
                        }.onSuccess { user ->
                            CurrentUser.setLogin(user.username, user.password)
                            sessionVm.setLogin(
                                id = user.id,
                                name = user.username,
                                role = "시니어"
                            )
                            nav.navigate(Route.Main.path) {
                                popUpTo(Route.Login.path) { inclusive = true }
                                launchSingleTop = true
                            }
                        }.onFailure { e ->
                            status = "로그인 실패: ${e.message}"
                        }
                        loading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(57.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                enabled = !loading && id.isNotBlank() && pw.isNotBlank(),
                contentPadding = PaddingValues(vertical = 13.dp)
            ) {
                Text(
                    text = if (loading) "로그인 중..." else "로그인",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = letter,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(8.dp))
            status?.let { Text(it, color = Color.Black, modifier = Modifier.padding(start = 16.dp)) }

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽 텍스트
                TextButton(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                ) {
                    Text(
                        "아이디 찾기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.019f).em,
                        color = Color.Black
                    )
                }

                // 세로 구분선 (|) + 양옆 여백
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)   // 🔹 구분선 좌우 간격 추가 (기존 핵심)
                        .height(18.dp)
                        .width(1.dp)
                        .background(Color.Black)
                )

                // 오른쪽 텍스트
                TextButton(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                ) {
                    Text(
                        "비밀번호 찾기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.019f).em,
                        color = Color.Black
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    sessionVm.setrole(role = "시니어")
                    nav.navigate(Route.Verify.path) { launchSingleTop = true }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(57.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SubBtnBorder)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = SubBtnText
                ),
                contentPadding = PaddingValues(vertical = 13.dp)
            ) {
                Text(
                    "간편 회원가입",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = letter
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnderlineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier
) {
    val letter = (-0.019f).em
    val LineGrey = Color(0xFFC0C0C0)

    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = {
            Text(
                placeholder,
                color = Color(0xFFA6A6A6),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = letter
            )
        },
        textStyle = LocalTextStyle.current.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = letter,
            color = Color.Black
        ),
        modifier = modifier.fillMaxWidth(),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(0.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = LineGrey,
            unfocusedIndicatorColor = LineGrey,
            cursorColor = Color.Black
        )
    )
}
