package com.example.dodojob.ui.feature.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route

// ▼ 추가: 아이콘
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close

// ▼ 추가: Supabase + 코루틴 + 직렬화 + 쿼리 DSL
import com.example.dodojob.data.supabase.LocalSupabase
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.*   // eq, limit 등
import io.github.jan.supabase.postgrest.query.Columns

@Serializable
private data class PreLoginRow(
    val id: String,
    val username: String,
    val password: String? = null,
    val name: String? = null,
    val job: String? = null      // ★ 추가
)

@Composable
fun PreLoginScreen(nav: NavController) {
    var id by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var autoLogin by remember { mutableStateOf(false) }
    var pwVisible by remember { mutableStateOf(false) }

    // ▼ 추가: 상태/클라
    val pretendard = FontFamily(Font(R.font.pretendard_regular))
    val client = LocalSupabase.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 타이틀 + 뒤로가기
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "로그인",
                    fontFamily = pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 안내 텍스트
            Text(
                text = "당신의 경험이 빛날 곳,\n두두잡에서 만나보세요.",
                fontFamily = pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 30.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 아이디 입력
            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("아이디", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 비밀번호 입력
            OutlinedTextField(
                value = pw,
                onValueChange = { pw = it },
                label = { Text("비밀번호", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (pwVisible) Icons.Filled.Check else Icons.Filled.Close
                    IconButton(onClick = { pwVisible = !pwVisible }) {
                        Icon(icon, contentDescription = null)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 자동 로그인 체크박스
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = autoLogin,
                    onCheckedChange = { autoLogin = it }
                )
                Text("자동로그인", fontFamily = pretendard, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 로그인 버튼 (여기에 DB 조회 연결)
            Button(
                onClick = {
                    status = null
                    loading = true
                    scope.launch {
                        val q = id.trim()
                        val json = Json { ignoreUnknownKeys = true }

                        runCatching {
                            // 서버에서 username = q 인 1건만, 필요한 컬럼만 조회
                            val res = client.from("users_tmp").select {
                                filter { eq("username", q) }
                                limit(1)
                            }
                            val list = json.decodeFromJsonElement(
                                ListSerializer(PreLoginRow.serializer()),
                                Json.parseToJsonElement(res.data)
                            )
                            val user = list.firstOrNull() ?: error("존재하지 않는 아이디입니다.")
                            if (user.password != pw) error("비밀번호가 일치하지 않습니다.")
                            if (user.job?.trim() != "고용주") error("시니어 전용 탭입니다. (현재: ${user.job ?: "미지정"})")
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
                    .height(57.dp),
                enabled = !loading && id.isNotBlank() && pw.isNotBlank(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FFF))
            ) {
                Text(
                    text = if (loading) "로그인 중..." else "로그인",
                    fontFamily = pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            // 결과/에러 메시지
            Spacer(modifier = Modifier.height(12.dp))
            status?.let { Text(it, color = Color.Black, fontFamily = pretendard) }

            Spacer(modifier = Modifier.height(20.dp))

            // 아이디/비번 찾기
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "아이디 찾기",
                    fontFamily = pretendard,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable { }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(width = 1.dp, height = 16.dp)
                        .background(Color.Black)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "비밀번호 찾기",
                    fontFamily = pretendard,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 회원가입 버튼
            OutlinedButton(
                onClick = { nav.navigate(Route.SignUp.path) { launchSingleTop = true } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(57.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7E7D7D))
            ) {
                Text(
                    text = "휴대폰 번호로 회원가입",
                    fontFamily = pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }
        }
    }
}
