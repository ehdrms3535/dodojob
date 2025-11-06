package com.example.dodojob.ui.feature.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.data.user.UserDto
import com.example.dodojob.data.user.UserRepository
import com.example.dodojob.data.user.UserRepositorySupabase
import com.example.dodojob.navigation.Route
import com.example.dodojob.session.CurrentUser
import com.example.dodojob.ui.components.CheckState
import com.example.dodojob.ui.components.UnderlineFieldRow
import com.example.dodojob.ui.feature.verify.SignUpPrefill
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun SignUpIdPwScreen(nav: NavController) {
    // ===== 원본 로직 유지 =====
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

    // ===== 검증 =====
    val emailOk = email.contains("@") && email.contains(".")
    val idOk = userId.length >= 3 && userId.any { it.isDigit() } && userId.any { it.isLetter() }
    val pwOk = pw.length >= 8 && pw.any { it.isDigit() } && pw.any { it.isLetter() }
    val pw2Ok = pw2.isNotEmpty() && pw2 == pw
    val canSubmit = emailOk && idOk && pwOk && pw2Ok && prefill != null

    LaunchedEffect(prefill) { if (prefill == null) nav.popBackStack() }

    val Bg = Color(0xFFF1F5F7)
    val Primary = Color(0xFF005FFF)

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column {
                // Status bar (24px)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(Color(0xFFEFEFEF))
                )
                // 뒤로가기 (VerifyScreen과 동일 위치)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Bg)
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
                // 타이틀 32 / 600
                Spacer(Modifier.height(10.dp))
                Text(
                    "회원가입",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.019f).em,
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
                    .background(Bg)
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
                                toSave
                            }.onSuccess { created ->
                                CurrentUser.setAuthUserId(created.id)
                                CurrentUser.setUsername(created.username)
                                CurrentUser.setLogin(created.username, pw)
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
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .height(54.dp), // 스펙: 54.48 ≈ 54dp
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canSubmit) Primary else Color(0xFFBFC6D2),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFBFC6D2),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (loading) "처리 중…" else "완료",
                        fontSize = 24.sp,              // 스펙: 24
                        fontWeight = FontWeight.Medium, // 스펙: 500
                        letterSpacing = (-0.019f).em
                    )
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 서브텍스트 20 / lineHeight 30 / −0.019em
            Text(
                "회원가입에 필요한 정보를\n정확히 입력해 주세요",
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF636363),
                lineHeight = 30.sp,
                letterSpacing = (-0.019f).em,
                modifier = Modifier.padding(top = 0.dp, bottom = 20.dp)
            )

            // ===== 이메일 =====
            SectionLabel20("이메일")
            UnderlineFieldRowOverlayCheck(
                value = email,
                onValueChange = { email = it },
                placeholder = "선택사항",           // 스펙의 placeholder 샘플을 반영(18sp)
                placeholderSp = 18.sp,
                keyboardType = KeyboardType.Email,
                isPassword = false,
                checkState = if (emailOk) CheckState.ValidBlue else CheckState.NeutralGrey
            )

            // ===== 아이디 =====
            Spacer(Modifier.height(20.dp))
            SectionLabel20("아이디")
            UnderlineFieldRowOverlayCheck(
                value = userId,
                onValueChange = { userId = it },
                placeholder = "선택사항",
                placeholderSp = 18.sp,
                keyboardType = KeyboardType.Text,
                isPassword = false,
                checkState = if (idOk) CheckState.ValidBlue else CheckState.NeutralGrey
            )

            // ===== 비밀번호 =====
            Spacer(Modifier.height(20.dp))
            SectionLabel20("비밀번호")
            UnderlineFieldRowOverlayCheck(
                value = pw,
                onValueChange = { pw = it },
                placeholder = "숫자, 영문 포함 8자리 이상", // 스펙 본문 복원
                placeholderSp = 18.sp,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                checkState = if (pwOk) CheckState.ValidBlue else CheckState.NeutralGrey
            )

            // ===== 비밀번호 재입력 =====
            Spacer(Modifier.height(20.dp))
            SectionLabel20("비밀번호 재입력")
            UnderlineFieldRow(
                value = pw2,
                onValueChange = { pw2 = it },
                placeholder = "",
                placeholderSize = 18.sp, // placeholder도 18sp로 통일
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isPassword = true,
                checkState = null
            )

            Spacer(Modifier.height(12.dp))
            error?.let {
                Text(
                    it,
                    color = Color(0xFFD32F2F),
                    fontSize = 14.sp,
                    letterSpacing = (-0.019f).em
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/* =================== 보조 컴포넌트 =================== */

@Composable
private fun SectionLabel20(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,                    // 스펙: 라벨 20
        fontWeight = FontWeight.Medium,      // 500
        letterSpacing = (-0.019f).em,
        color = Color.Black
    )
    Spacer(Modifier.height(12.dp))           // 라벨-필드 gap: 12
}

@Composable
private fun UnderlineFieldRowOverlayCheck(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    placeholderSp: androidx.compose.ui.unit.TextUnit,
    keyboardType: KeyboardType,
    isPassword: Boolean,
    checkState: CheckState?
) {
    val iconRes = when (checkState) {
        CheckState.ValidBlue -> R.drawable.autologin_checked
        CheckState.NeutralGrey, null -> R.drawable.autologin_unchecked
        else -> R.drawable.autologin_unchecked
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp) // 필드 컨테이너 높이
    ) {
        UnderlineFieldRow(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            placeholderSize = placeholderSp, // 18sp
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isPassword = isPassword,
            checkState = null, // 내부 체크는 끄고, 오버레이만
        )

        Image(
            painter = painterResource(iconRes),
            contentDescription = "체크 상태",
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterEnd)
                .offset(y = (-2).dp)
                .padding(end = 2.dp)
        )
    }
}
