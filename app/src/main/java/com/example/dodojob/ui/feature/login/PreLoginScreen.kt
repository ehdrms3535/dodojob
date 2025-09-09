// package 생략 없이 프로젝트 경로에 맞춰 두세요.
package com.example.dodojob.ui.feature.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.navigation.Route
import com.example.dodojob.data.supabase.LocalSupabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.*
import com.example.dodojob.session.CurrentUser
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Serializable
private data class PreLoginRow(
    val id: String,
    val username: String,
    val password: String? = null,
    val name: String? = null,
    val job: String? = null
)

@Composable
fun PreLoginScreen(nav: NavController) {
    var id by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var autoLogin by remember { mutableStateOf(false) }
    var pwVisible by remember { mutableStateOf(false) }

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
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(text = "로그인", fontSize = 24.sp)
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "당신의 경험이 빛날 곳,\n두두잡에서 만나보세요.",
                fontSize = 20.sp,
                lineHeight = 30.sp,
                color = Color.Black
            )

            Spacer(Modifier.height(32.dp))
            OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("아이디", color = Color.Gray) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = pw,
                onValueChange = { pw = it },
                label = { Text("비밀번호", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (pwVisible) Icons.Filled.Check else Icons.Filled.Close
                    IconButton(onClick = { pwVisible = !pwVisible }) { Icon(icon, contentDescription = null) }
                }
            )

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = autoLogin, onCheckedChange = { autoLogin = it })
                Text("자동로그인", fontSize = 15.sp)
            }

            Spacer(Modifier.height(24.dp))
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
                                ListSerializer(PreLoginRow.serializer()),
                                Json.parseToJsonElement(res.data)
                            )
                            val user = list.firstOrNull() ?: error("존재하지 않는 아이디입니다.")
                            if (user.password != pw) error("비밀번호가 일치하지 않습니다.")

                            val currentJob = user.job?.trim().takeUnless { it.isNullOrEmpty() } ?: "미지정"
                            if (currentJob != "시니어") error("시니어 전용 탭입니다. (현재: $currentJob)")
                            user
                        }.onSuccess { user ->
                            // set 또는 setLogin 사용 가능
                            CurrentUser.setLogin(user.id, user.username)
                            // CurrentUser.set(user.id)

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
                modifier = Modifier.fillMaxWidth().height(57.dp),
                enabled = !loading && id.isNotBlank() && pw.isNotBlank(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FFF))
            ) {
                Text(if (loading) "로그인 중..." else "로그인", fontSize = 18.sp, color = Color.White)
            }

            Spacer(Modifier.height(12.dp))
            status?.let { Text(it, color = Color.Black) }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "아이디 찾기", modifier = Modifier.clickable { })
                Spacer(Modifier.width(16.dp))
                Box(modifier = Modifier.size(width = 1.dp, height = 16.dp).background(Color.Black))
                Spacer(Modifier.width(16.dp))
                Text(text = "비밀번호 찾기", modifier = Modifier.clickable { })
            }

            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = { nav.navigate(Route.SignUp.path) { launchSingleTop = true } },
                modifier = Modifier.fillMaxWidth().height(57.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7E7D7D))
            ) {
                Text("휴대폰 번호로 회원가입", fontSize = 18.sp)
            }
        }
    }
}
