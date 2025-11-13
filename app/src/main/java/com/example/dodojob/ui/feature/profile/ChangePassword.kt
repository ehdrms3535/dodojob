package com.example.dodojob.ui.feature.account

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.data.user.UserRepository
import com.example.dodojob.data.user.UserRepositorySupabase
import com.example.dodojob.navigation.Route
import com.example.dodojob.ui.components.CheckState
import com.example.dodojob.ui.components.UnderlineFieldRow
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordScreen(nav: NavController) {
    val client = LocalSupabase.current
    val repo: UserRepository = remember(client) { UserRepositorySupabase(client) }

    var currPw by rememberSaveable { mutableStateOf("") }
    var newPw by rememberSaveable { mutableStateOf("") }
    var newPw2 by rememberSaveable { mutableStateOf("") }

    var currPwValid by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var loading by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var done by rememberSaveable { mutableStateOf(false) }

    var showComplete by rememberSaveable { mutableStateOf(false) }

    // 유효성
    val newPwOk = newPw.length >= 3 && newPw.any { it.isDigit() } && newPw.any { it.isLetter() }
    val newPw2Ok = newPw2.isNotEmpty() && newPw2 == newPw
    val notSameAsOld = currPw.isNotEmpty() && newPw.isNotEmpty() && currPw != newPw
    val canSubmit = currPwValid && newPwOk && newPw2Ok && notSameAsOld

    val Bg = Color(0xFFF1F5F7)
    val Primary = Color(0xFF005FFF)

    LaunchedEffect(currPw) {
        if (currPw.isBlank()) {
            currPwValid = false
            return@LaunchedEffect
        }

        runCatching {
            repo.verifyPassword(currPw)
        }.onSuccess { result ->
            currPwValid = result
        }.onFailure {
            currPwValid = false
        }
    }

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(Color(0xFFEFEFEF))
                )
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
                Spacer(Modifier.height(10.dp))
                Text(
                    "비밀번호 변경",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.019f).em,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
                )
            }
        },
        bottomBar = {
            if (!showComplete) {
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
                            done = false
                            loading = true
                            scope.launch {
                                runCatching {
                                    repo.changePassword(currPw, newPw)
                                }.onSuccess {
                                    currPw = ""; newPw = ""; newPw2 = ""
                                    done = true
                                    showComplete = true
                                }.onFailure { e ->
                                    error = e.message ?: "비밀번호 변경 실패"
                                }
                                loading = false
                            }
                        },
                        enabled = canSubmit && !loading,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
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
                        Text(
                            text = if (loading) "처리 중…" else "변경하기",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-0.019f).em
                        )
                    }
                }
            }
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // 🔹 기존 폼
            Column(
                modifier = Modifier
                    .matchParentSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    "새 비밀번호로 변경하시겠습니까?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF636363),
                    lineHeight = 30.sp,
                    letterSpacing = (-0.019f).em,
                    modifier = Modifier.padding(top = 0.dp, bottom = 20.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))

                SectionLabel20("현재 비밀번호")
                UnderlineFieldRowOverlayCheck(
                    value = currPw,
                    onValueChange = { currPw = it },
                    placeholder = "",
                    placeholderSp = 18.sp,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    checkState = if (currPwValid) CheckState.ValidBlue else CheckState.NeutralGrey
                )

                Spacer(Modifier.height(24.dp))
                SectionLabel20("새 비밀번호")
                UnderlineFieldRowOverlayCheck(
                    value = newPw,
                    onValueChange = { newPw = it },
                    placeholder = "숫자,영문 포함 3자리 이상",
                    placeholderSp = 18.sp,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    checkState = if (newPwOk && notSameAsOld) CheckState.ValidBlue else CheckState.NeutralGrey
                )

                Spacer(Modifier.height(24.dp))
                SectionLabel20("새 비밀번호 확인")
                UnderlineFieldRowOverlayCheck(
                    value = newPw2,
                    onValueChange = { newPw2 = it },
                    placeholder = "숫자, 영문 포함 3자리 이상",
                    placeholderSp = 18.sp,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    checkState = if (newPw2Ok) CheckState.ValidBlue else CheckState.NeutralGrey
                )

                Spacer(Modifier.height(12.dp))

                if (!notSameAsOld && currPw.isNotEmpty() && newPw.isNotEmpty()) {
                    Text(
                        "현재 비밀번호와 다른 값이어야 합니다.",
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp,
                        letterSpacing = (-0.019f).em
                    )
                    Spacer(Modifier.height(4.dp))
                }
                error?.let {
                    Text(
                        it,
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp,
                        letterSpacing = (-0.019f).em
                    )
                }
            }

            // 🔹 비밀번호 변경 완료 오버레이
            if (showComplete) {
                PasswordChangeCompleteOverlay(
                    onClickedAnywhere = {
                        nav.navigate(Route.My.path) {
                            popUpTo(Route.ChangePassword.path) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PasswordChangeCompleteOverlay(
    onClickedAnywhere: () -> Unit
) {
    val Bg = Color(0xFFF1F5F7)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .clickable(onClick = onClickedAnywhere),
        contentAlignment = Alignment.Center
    ) {
        // 가운데 체크 아이콘 + 텍스트
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.complete_image),
                contentDescription = "변경 완료",
                modifier = Modifier.size(69.dp)
            )
            Text(
                text = "변경 완료!",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.019f).em,
                color = Color.Black
            )
        }

        // 하단 홈 인디케이터(선)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
                .width(80.dp)
                .height(4.dp)
                .background(
                    color = Color(0xFF6A7685),
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}

/* =================== 보조 컴포넌트 =================== */

@Composable
private fun SectionLabel20(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,               // 라벨 20
        fontWeight = FontWeight.Medium, // 500
        letterSpacing = (-0.019f).em,
        color = Color.Black
    )
    Spacer(Modifier.height(2.dp))      // 라벨-필드 gap: 12
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
            placeholderSize = placeholderSp,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isPassword = isPassword,
            checkState = null, // 내부 체크는 끄고, 오버레이만 사용
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
