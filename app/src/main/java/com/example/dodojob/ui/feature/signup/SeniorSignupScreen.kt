package com.example.dodojob.ui.feature.signup

import com.example.dodojob.ui.components.CheckState
import com.example.dodojob.ui.components.UnderlineFieldRow
import androidx.compose.foundation.background
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.BoxWithConstraints
import com.example.dodojob.navigation.Route
import com.example.dodojob.data.user.UserRepository
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.data.user.UserRepositorySupabase
import com.example.dodojob.ui.feature.verify.SignUpPrefill
import com.example.dodojob.data.user.UserDto
import java.util.UUID
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.dodojob.session.CurrentUser

@Composable
fun SignUpIdPwScreen(nav: NavController) {
    val client = LocalSupabase.current
    val repo: UserRepository = remember(client) { UserRepositorySupabase(client) }

    val prefill = remember {
        nav.previousBackStackEntry?.savedStateHandle?.get<SignUpPrefill>("prefill")
    }

    var name by rememberSaveable { mutableStateOf(prefill?.name.orEmpty()) }
    var gender by rememberSaveable { mutableStateOf(prefill?.gender.orEmpty()) }
    var region by rememberSaveable { mutableStateOf(prefill?.region.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(prefill?.phone.orEmpty()) }
    var email by rememberSaveable { mutableStateOf("") }
    var userId by rememberSaveable { mutableStateOf("") }

    var rrnFront by remember { mutableStateOf(prefill?.rrnFront.orEmpty()) }
    var rrnBackFirst by remember { mutableStateOf(prefill?.rrnBackFirst.orEmpty()) }
    var pw by remember { mutableStateOf("") }
    var pw2 by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var loading by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val emailOk = email.contains("@") && email.contains(".")
    val idOk = userId.length >= 3 && userId.any { it.isDigit() } && userId.any { it.isLetter() }
    val pwOk = pw.length >= 8 && pw.any { it.isDigit() } && pw.any { it.isLetter() }
    val pw2Ok = pw2.isNotEmpty() && pw2 == pw

    val Bg = Color(0xFFF1F5F7)
    val Primary = Color(0xFF005FFF)

    val canSubmit = emailOk && idOk && pwOk && pw2Ok && prefill != null

    LaunchedEffect(prefill) { if (prefill == null) nav.popBackStack() }

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Bg)
                    .padding(horizontal = 18.dp, vertical = 50.dp)
            ) {
                Button(
                    onClick = {
                        if (!canSubmit || loading) return@Button
                        error = null
                        loading = true

                        scope.launch {
                            runCatching {
                                val toSave = UserDto(
                                    id = UUID.randomUUID().toString(),
                                    name = name,
                                    gender = gender,
                                    rrnFront = rrnFront,
                                    rrnBackFirst = rrnBackFirst,
                                    region = region,
                                    phone = phone,
                                    email = email,
                                    username = userId,
                                    password = pw,
                                    job = "시니어"
                                )
                                repo.insertUser(toSave)
                                toSave // onSuccess로 전달
                            }.onSuccess { created ->
                                // ✅ 람다 파라미터로 받은 created 사용 (user 참조 에러 해결)
                                CurrentUser.setLogin(created.id, created.username)

                                // 민감정보 파기
                                rrnFront = ""; rrnBackFirst = ""; pw = ""; pw2 = ""

                                nav.navigate(Route.SignUpComplete.path) { launchSingleTop = true }
                            }.onFailure { e ->
                                error = e.message ?: "회원가입 실패"
                            }
                            loading = false
                        }
                    },
                    enabled = canSubmit && !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canSubmit) Primary else Color(0xFFBFC6D2),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFBFC6D2),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text(if (loading) "처리 중…" else "완료", fontSize = 25.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    ) { inner ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            val W = maxWidth
            val H = maxHeight

            val hPad = (W * 0.045f)
            val titleTop = (H * 0.03f)
            val titleSp = (W.value * 0.09f).sp
            val backSp = (W.value * 0.065f).sp
            val subTop = (H * 0.008f)
            val subSp = (W.value * 0.055f).sp
            val labelTop = (H * 0.019f)
            val labelSp = (W.value * 0.055f).sp
            val fieldTop = 2.dp
            val placeSp = (W.value * 0.042f).sp
            val lineGap = 8.dp
            val gapAfterSubtitle = (H * 0.035f)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = hPad)
            ) {
                Spacer(Modifier.height(titleTop))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("<", fontSize = backSp, color = Color.Black, modifier = Modifier.clickable { nav.popBackStack() })
                }
                Spacer(Modifier.height(8.dp))
                Text("회원가입", fontSize = titleSp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Spacer(Modifier.height(subTop))
                Text("회원가입에 필요한 정보를 정확히\n입력해 주세요", fontSize = subSp, color = Color(0xFF636363))

                Spacer(Modifier.height(gapAfterSubtitle))
                Text("이메일", fontSize = labelSp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(Modifier.height(fieldTop))
                UnderlineFieldRow(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "수신 가능한 본인 이메일을 입력해주세요",
                    placeholderSize = placeSp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isPassword = false,
                    checkState = if (emailOk) CheckState.ValidBlue else CheckState.NeutralGrey
                )
                Spacer(Modifier.height(lineGap))

                Spacer(Modifier.height(labelTop))
                Text("아이디", fontSize = labelSp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(Modifier.height(fieldTop))
                UnderlineFieldRow(
                    value = userId,
                    onValueChange = { userId = it },
                    placeholder = "숫자,영문 포함 3자리 이상",
                    placeholderSize = placeSp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isPassword = false,
                    checkState = if (idOk) CheckState.ValidBlue else CheckState.NeutralGrey
                )
                Spacer(Modifier.height(lineGap))

                Spacer(Modifier.height(labelTop))
                Text("비밀번호", fontSize = labelSp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(Modifier.height(fieldTop))
                UnderlineFieldRow(
                    value = pw,
                    onValueChange = { pw = it },
                    placeholder = "숫자, 영문 포함 8자리 이상",
                    placeholderSize = placeSp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isPassword = true,
                    checkState = if (pwOk) CheckState.ValidBlue else CheckState.NeutralGrey
                )
                Spacer(Modifier.height(lineGap))

                Spacer(Modifier.height(labelTop))
                Text("비밀번호 재입력", fontSize = labelSp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(Modifier.height(fieldTop))
                UnderlineFieldRow(
                    value = pw2,
                    onValueChange = { pw2 = it },
                    placeholder = "",
                    placeholderSize = placeSp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isPassword = true,
                    checkState = null
                )

                Spacer(Modifier.height(8.dp))
                error?.let { Text(it, color = Color(0xFFD32F2F), fontSize = 14.sp) }

                Spacer(Modifier.height(lineGap))
            }
        }
    }
}

<<<<<<< HEAD:app/src/main/java/com/example/dodojob/ui/feature/signup/SignupScreen.kt
/* 입력 라인 + 체크 아이콘 */
@Composable
private fun UnderlineFieldRow(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    placeholderSize: androidx.compose.ui.unit.TextUnit,
    keyboardOptions: KeyboardOptions,
    isPassword: Boolean,
    checkState: CheckState?
) {
    val hint = Color(0xFFA6A6A6)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = placeholderSize, color = Color.Black),
            placeholder = { Text(placeholder, color = hint, fontSize = placeholderSize) },
            modifier = Modifier.weight(1f).heightIn(min = 40.dp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color(0xFFA2A2A2),
                disabledIndicatorColor = Color(0xFFC0C0C0),
                cursorColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedPlaceholderColor = hint,
                unfocusedPlaceholderColor = hint
            )
        )
        if (checkState != null) {
            Spacer(Modifier.width(10.dp))
            CheckDot(state = checkState)
        }
    }
}

private enum class CheckState { NeutralGrey, ValidBlue }

@Composable
private fun CheckDot(state: CheckState) {
    when (state) {
        CheckState.NeutralGrey -> {
            Box(
                modifier = Modifier.size(22.dp).clip(CircleShape).background(Color(0xFFE6E6E6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(14.dp))
            }
        }
        CheckState.ValidBlue -> {
            Box(
                modifier = Modifier.size(22.dp).clip(CircleShape).background(Color(0xFF2A77FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}
=======
>>>>>>> 5b5d2e97ec1fafdd8967cf0961e74c7b38f3cce3:app/src/main/java/com/example/dodojob/ui/feature/signup/SeniorSignupScreen.kt
