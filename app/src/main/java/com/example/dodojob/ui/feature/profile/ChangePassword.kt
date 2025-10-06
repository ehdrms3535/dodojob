package com.example.dodojob.ui.feature.account

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
import androidx.compose.material.icons.filled.ChevronLeft
import com.example.dodojob.data.user.UserRepository
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.data.user.UserRepositorySupabase

import kotlinx.coroutines.launch

@Composable
fun ChangePasswordScreen(nav: NavController) {
    val client = LocalSupabase.current
    val repo: UserRepository = remember(client) { UserRepositorySupabase(client) }

    var currPw by remember { mutableStateOf("") }
    var newPw by remember { mutableStateOf("") }
    var newPw2 by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var loading by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var done by rememberSaveable { mutableStateOf(false) }

    // 유효성
    val currPwOk = currPw.isNotEmpty()
    val newPwOk = newPw.length >= 3 && newPw.any { it.isDigit() } && newPw.any { it.isLetter() }
    val newPw2Ok = newPw2.isNotEmpty() && newPw2 == newPw
    val notSameAsOld = currPw.isNotEmpty() && newPw.isNotEmpty() && currPw != newPw
    val canSubmit = currPwOk && newPwOk && newPw2Ok && notSameAsOld

    val Bg = Color(0xFFF1F5F7)
    val Primary = Color(0xFF005FFF)

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
                                // 현재 로그인 사용자 기준으로 비밀번호 변경
                                repo.changePassword(currPw, newPw)
                            }.onSuccess {
                                currPw = ""; newPw = ""; newPw2 = ""
                                done = true
                                nav.popBackStack() // 성공 후 뒤로가기
                            }.onFailure { e ->
                                error = e.message ?: "비밀번호 변경 실패"
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
                    Text(if (loading) "처리 중…" else "변경하기", fontSize = 25.sp, fontWeight = FontWeight.Medium)
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
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = "뒤로가기",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(28.dp) // 원하는 크기로 조정
                            .clickable { nav.popBackStack() }
                    )
                }


                Spacer(Modifier.height(8.dp))
                Text("비밀번호 변경", fontSize = titleSp, fontWeight = FontWeight.SemiBold, color = Color.Black)

                Spacer(Modifier.height(subTop))
                Text("새 비밀번호로 변경하시겠습니까?", fontSize = subSp, color = Color(0xFF636363))

                // 현재 비밀번호
                Spacer(Modifier.height(gapAfterSubtitle))
                Text("현재 비밀번호", fontSize = labelSp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(Modifier.height(fieldTop))
                UnderlineFieldRow(
                    value = currPw,
                    onValueChange = { currPw = it },
                    placeholder = "",
                    placeholderSize = placeSp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isPassword = true,
                    checkState = if (currPwOk) CheckState.ValidBlue else CheckState.NeutralGrey
                )
                Spacer(Modifier.height(lineGap))

                // 새 비밀번호
                Spacer(Modifier.height(labelTop))
                Text("새 비밀번호", fontSize = labelSp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(Modifier.height(fieldTop))
                UnderlineFieldRow(
                    value = newPw,
                    onValueChange = { newPw = it },
                    placeholder = "숫자, 영문 포함 3자리 이상",
                    placeholderSize = placeSp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isPassword = true,
                    checkState = if (newPwOk && notSameAsOld) CheckState.ValidBlue else CheckState.NeutralGrey
                )
                Spacer(Modifier.height(lineGap))

                // 새 비밀번호 재입력
                Spacer(Modifier.height(labelTop))
                Text("새 비밀번호 확인", fontSize = labelSp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(Modifier.height(fieldTop))
                UnderlineFieldRow(
                    value = newPw2,
                    onValueChange = { newPw2 = it },
                    placeholder = "숫자, 영문 포함 3자리 이상",
                    placeholderSize = placeSp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isPassword = true,
                    checkState = if (newPw2Ok) CheckState.ValidBlue else CheckState.NeutralGrey
                )

                Spacer(Modifier.height(8.dp))

                // 안내/에러
                if (done) {
                    Text("비밀번호가 변경되었습니다.", color = Color(0xFF2E7D32), fontSize = 14.sp)
                } else {
                    if (!notSameAsOld && currPw.isNotEmpty() && newPw.isNotEmpty()) {
                        Text("현재 비밀번호와 다른 값이어야 합니다.", color = Color(0xFFD32F2F), fontSize = 14.sp)
                    }
                    error?.let { Text(it, color = Color(0xFFD32F2F), fontSize = 14.sp) }
                }

                Spacer(Modifier.height(lineGap))
            }
        }
    }
}

/* -------------------------------
   밑줄형 입력 + 오른쪽 체크 (회원가입 화면 동일)
-------------------------------- */
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
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 40.dp),
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
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE6E6E6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFFBDBDBD),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        CheckState.ValidBlue -> {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A77FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
