package com.example.dodojob.ui.feature.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.navigation.Route

// ▼ 추가: Supabase + 코루틴 + 직렬화 + 쿼리 DSL 임포트
import com.example.dodojob.data.supabase.LocalSupabase
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.*   // eq, limit 등

@Serializable
private data class LoginRow(
    val id: String,
    val username: String,
    val password: String? = null,
    val name: String? = null,
    val job: String? = null      // ★ 추가
)

@Composable
fun LoginScreen(nav: NavController) {
    var id by remember { mutableStateOf("") }          // username 입력
    var pw by remember { mutableStateOf("") }
    var autoLogin by remember { mutableStateOf(false) }

    // ▼ 추가: Supabase 클라/코루틴/상태
    val client = LocalSupabase.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF1F5F7))
    ) {
        val W = maxWidth
        val H = maxHeight

        val screenHPad   = (W * 0.045f)
        val topVPad      = (H * 0.03f)
        val backSizeSp   = (W.value * 0.065f).sp
        val titleSp      = (W.value * 0.09f).sp
        val subtitleSp   = (W.value * 0.065f).sp
        val subtitleLH   = (W.value * 0.095f).sp
        val fieldGap     = (H * 0.015f).coerceIn(8.dp, 18.dp)
        val sectionGap   = (H * 0.02f).coerceIn(12.dp, 24.dp)
        val circleSize   = (W * 0.065f).coerceIn(20.dp, 28.dp)
        val checkSize    = (circleSize * 0.65f)
        val loginBtnH    = (H * 0.07f).coerceIn(48.dp, 60.dp)
        val signBtnH     = (H * 0.07f).coerceIn(48.dp, 60.dp)
        val betweenBtns  = (H * 0.02f).coerceIn(12.dp, 20.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = screenHPad)
        ) {
            Spacer(Modifier.height(topVPad))
            Spacer(Modifier.height(topVPad))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "<",
                    fontSize = backSizeSp,
                    color = Color.Black,
                    modifier = Modifier.clickable { nav.popBackStack() }
                )
            }

            Spacer(Modifier.height((H * 0.005f).coerceAtLeast(2.dp)))
            Text(
                "로그인",
                fontSize = titleSp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(start = (W * 0.02f).coerceIn(6.dp, 14.dp))
            )

            Spacer(Modifier.height((H * 0.01f).coerceIn(8.dp, 16.dp)))
            Text(
                "당신의 경험이 빛날 곳,\n두두잡에서 만나보세요.",
                fontSize = subtitleSp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                lineHeight = subtitleLH,
                modifier = Modifier.padding(start = (W * 0.02f).coerceIn(6.dp, 14.dp))
            )

            Spacer(Modifier.height((H * 0.03f).coerceIn(16.dp, 32.dp)))

            // 🔑 아이디(=username)
            UnderlineTextField(
                value = id,
                onValueChange = { id = it },
                placeholder = "아이디"
            )

            Spacer(Modifier.height(fieldGap))

            // 🔒 비밀번호
            UnderlineTextField(
                value = pw,
                onValueChange = { pw = it },
                placeholder = "비밀번호",
                isPassword = true
            )

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

            Spacer(Modifier.height(sectionGap))
            Button(
                onClick = {
                    status = null
                    loading = true
                    scope.launch {
                        val q = id.trim()
                        val json = Json { ignoreUnknownKeys = true }

                        runCatching {
                            // username 일치하는 1건만 서버에서 조회
                            val res = client.from("users_tmp").select {
                                filter { eq("username", q) }
                                limit(1)
                            }
                            val list = json.decodeFromJsonElement(
                                ListSerializer(LoginRow.serializer()),
                                Json.parseToJsonElement(res.data)
                            )
                            val user = list.firstOrNull() ?: error("존재하지 않는 아이디입니다.")
                            if (user.password != pw) error("비밀번호가 일치하지 않습니다.")
                            if (user.job?.trim() != "시니어") error("시니어 전용 탭입니다. (현재: ${user.job ?: "미지정"})")
                            user
                        }.onSuccess {
                            // 성공 → 메인으로 이동
                            nav.navigate(Route.Main.path) {
                                popUpTo(Route.Login.path) { inclusive = true }
                                launchSingleTop = true
                            }
                        }.onFailure {
                            status = "로그인 실패: ${it.message}"
                        }
                        loading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(loginBtnH),
                shape = RoundedCornerShape((W * 0.08f).coerceIn(16.dp, 28.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FFF)),
                enabled = !loading && id.isNotBlank() && pw.isNotBlank()
            ) {
                Text(
                    if (loading) "로그인 중..." else "로그인",
                    fontSize = (W.value * 0.055f).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // 결과/오류 메시지
            Spacer(Modifier.height(8.dp))
            status?.let { Text(it, color = Color.Black) }

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

            Spacer(Modifier.height(betweenBtns))
            OutlinedButton(
                onClick = {
                    nav.navigate(Route.Verify.path) { launchSingleTop = true }
                },
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

/* -------------------------------- */
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
        shape = RoundedCornerShape(0.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Black,
            unfocusedIndicatorColor = Color(0xFFA2A2A2),
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
