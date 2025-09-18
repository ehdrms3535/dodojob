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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.dodojob.data.employ.EmployDto
import kotlinx.coroutines.launch
import com.example.dodojob.data.employ.EmployRepository
import com.example.dodojob.data.employ.EmployRepositorySupabase
@Composable
fun EmployerSignupScreen(nav: NavController) {
    val client = LocalSupabase.current
    val repo: UserRepository = remember(client) { UserRepositorySupabase(client) }
    val repo1: EmployRepository = remember(client) { EmployRepositorySupabase(client)}

    // Verify → SignUp에서 전달된 값
    val prefill = remember {
        nav.previousBackStackEntry?.savedStateHandle?.get<SignUpPrefill>("prefill")
    }

    // **** 데이터만 변경: 담당자명/연락처/이메일/사업자번호 ****
    var name by rememberSaveable { mutableStateOf(prefill?.name.orEmpty()) }
    var gender by rememberSaveable { mutableStateOf(prefill?.gender.orEmpty()) }
    var region by rememberSaveable { mutableStateOf(prefill?.region.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(prefill?.phone.orEmpty()) }
    var userId by rememberSaveable { mutableStateOf("") }

    var email by rememberSaveable { mutableStateOf("") }
    var bizNo by rememberSaveable { mutableStateOf("") } // 하이픈 없이 10자리

    // 기존 prefill 민감정보는 그대로 유지(사용 안 함)
    var rrnFront by remember { mutableStateOf(prefill?.rrnFront.orEmpty()) }
    var rrnBackFirst by remember { mutableStateOf(prefill?.rrnBackFirst.orEmpty()) }

    val scope = rememberCoroutineScope()
    var loading by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    // ===== 유효성 검사 (필드 교체) =====
    val nameOk = name.trim().length >= 2
    val phoneDigits = phone.filter { it.isDigit() }
    val phoneOk = phoneDigits.length in 10..11
    val emailOk = email.contains("@") && email.contains(".")
    val bizDigits = bizNo.filter { it.isDigit() }
    val bizOk = bizDigits.length == 10

    val Bg = Color(0xFFF1F5F7)
    val Primary = Color(0xFF005FFF)

    // 제출 가능 조건
    val canSubmit = nameOk && phoneOk && emailOk && bizOk && prefill != null

    // prefill 없이 접근 시 방어
    LaunchedEffect(prefill) {
        if (prefill == null) nav.popBackStack()
    }

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
                                // NOTE: 레포 스키마가 username/password를 요구해서
                                // 임시로 bizNo/임시비밀번호를 넣었습니다. 실제 스키마에 맞게 교체하세요.
                                val user = UserDto(
                                    id = UUID.randomUUID().toString(),
                                    name = name,
                                    gender = prefill?.gender.orEmpty(),
                                    rrnFront = rrnFront,
                                    rrnBackFirst = rrnBackFirst,
                                    region = prefill?.region.orEmpty(),
                                    phone = phone,
                                    email = email,
                                    username = "TempPass#1234",           // FIXME: 스키마에 맞게 변경
                                    password = "TempPass#1234",     // FIXME: 운영에서는 미사용/삭제
                                    job = "고용주",
                                )
                                repo.insertUser(user)

                                val employ = EmployDto(
                                    id = email,
                                    companyid = bizNo
                                )
                                repo1.insertEmploy(employ)

                            }.onSuccess {
                                // 민감정보 즉시 파기
                                rrnFront = ""; rrnBackFirst = ""
                                nav.currentBackStackEntry?.savedStateHandle?.set("prefill", prefill)
                                nav.navigate(Route.EmploySignupsec.path) {
                                   // launchSingleTop = true
                                }
                            }.onFailure { e ->
                                error = e.message ?: "등록 실패"
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

            // 비율 기반 치수(그대로 유지)
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

                // 뒤로가기
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "<",
                        fontSize = backSp,
                        color = Color.Black,
                        modifier = Modifier.clickable { nav.popBackStack() }
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text("회원가입", fontSize = titleSp, fontWeight = FontWeight.SemiBold, color = Color.Black)

                Spacer(Modifier.height(subTop))
                Text(
                    "회원가입에 필요한 정보를 정확히\n입력해 주세요",
                    fontSize = subSp, color = Color(0xFF636363)
                )

                // ====== 담당자명 ======
                Spacer(Modifier.height(gapAfterSubtitle))
                Text("담당자명", fontSize = labelSp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(Modifier.height(fieldTop))
                UnderlineFieldRow(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "담당자 성함",
                    placeholderSize = placeSp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isPassword = false,
                    checkState = if (nameOk) CheckState.ValidBlue else CheckState.NeutralGrey
                )
                Spacer(Modifier.height(lineGap))

                // ====== 담당자 연락처 ======
                Spacer(Modifier.height(labelTop))
                Text("담당자 연락처", fontSize = labelSp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(Modifier.height(fieldTop))
                UnderlineFieldRow(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "010-0000-0000",
                    placeholderSize = placeSp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isPassword = false,
                    checkState = if (phoneOk) CheckState.ValidBlue else CheckState.NeutralGrey
                )
                Spacer(Modifier.height(lineGap))

                // ====== 담당자 이메일 ======
                Spacer(Modifier.height(labelTop))
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

                // ====== 사업자 등록번호 ======
                Spacer(Modifier.height(labelTop))
                Text("사업자 등록번호", fontSize = labelSp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(Modifier.height(fieldTop))
                UnderlineFieldRow(
                    value = bizNo,
                    onValueChange = { input ->
                        // 하이픈 제거 + 숫자만 허용
                        bizNo = input.filter { it.isDigit() }.take(10)
                    },
                    placeholder = "사업자 등록번호(-제외 입력)",
                    placeholderSize = placeSp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isPassword = false,
                    checkState = if (bizOk) CheckState.ValidBlue else CheckState.NeutralGrey
                )

                Spacer(Modifier.height(8.dp))
                error?.let { Text(it, color = Color(0xFFD32F2F), fontSize = 14.sp) }

                Spacer(Modifier.height(lineGap))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "Employer Signup UI")
@Composable
fun EmployerSignupScreenPreview() {
    EmployerSignupScreen(nav = rememberNavController())
}