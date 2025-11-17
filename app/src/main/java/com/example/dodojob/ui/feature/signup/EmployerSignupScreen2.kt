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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.data.user.UserRepository
import com.example.dodojob.data.user.UserRepositorySupabase
import com.example.dodojob.navigation.Route
import com.example.dodojob.ui.feature.verify.SignUpPrefill
import com.example.dodojob.session.CurrentUser
import kotlinx.coroutines.launch

@Composable
fun EmploySignUpIdPwScreen(nav: NavController) {
    val client = LocalSupabase.current
    val repo: UserRepository = remember(client) { UserRepositorySupabase(client) }

    val userRowId = CurrentUser.id

    var userId by rememberSaveable { mutableStateOf("") }
    var pw by rememberSaveable { mutableStateOf("") }
    var pw2 by rememberSaveable { mutableStateOf("") }

    fun isValidId(s: String) = s.length >= 3 && s.any(Char::isDigit) && s.any(Char::isLetter)
    fun isValidPw(s: String) = s.length >= 8 && s.any(Char::isDigit) && s.any(Char::isLetter)
    fun isValidPw2(a: String, b: String) = b.isNotEmpty() && a == b

    val idOk by remember(userId) { mutableStateOf(isValidId(userId)) }
    val pwOk by remember(pw) { mutableStateOf(isValidPw(pw)) }
    val pw2Ok by remember(pw, pw2) { mutableStateOf(isValidPw2(pw, pw2)) }

    val Bg = Color(0xFFF1F5F7)
    val Primary = Color(0xFF005FFF)
    val canSubmit = idOk && pwOk && pw2Ok != null

    val scope = rememberCoroutineScope()
    var loading by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

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
                    "회원가입",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.019f).em,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "회원가입에 필요한 정보를 정확히\n입력해 주세요",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-0.019f).em,
                    color = Color(0xFF636363),
                    lineHeight = 30.sp,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
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

                                val idForUpsert = userRowId ?: error("userRowId 누락")
                                repo.upsertIdPw(id = idForUpsert, username = userId, rawPassword = pw)
                            }.onSuccess {
                                CurrentUser.setLogin(username = userId, password = pw)
                                nav.navigate(Route.PostingRegisterCompleteScreen.path) {
                                    launchSingleTop = true
                                }
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
                        .height(47.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        text = if (loading) "처리 중…" else "완료",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
            Spacer(Modifier.height(16.dp))

            FieldRowOverlayCheck(
                label = "아이디",
                value = userId,
                onValueChange = { userId = it },
                placeholder = "숫자, 영문 포함 3자리 이상",
                valid = idOk,
                keyboardType = KeyboardType.Text,
                topGap = 0.dp,
                labelFieldGap = 8.dp
            )
            FieldRowOverlayCheck(
                label = "비밀번호",
                value = pw,
                onValueChange = { pw = it },
                placeholder = "숫자, 영문 포함 8자리 이상",
                valid = pwOk,
                keyboardType = KeyboardType.Password,
                topGap = 24.dp,
                labelFieldGap = 8.dp
            )
            FieldRowOverlayCheck(
                label = "비밀번호 재입력",
                value = pw2,
                onValueChange = { pw2 = it },
                placeholder = "",
                valid = pw2Ok,
                keyboardType = KeyboardType.Password,
                topGap = 28.dp,
                labelFieldGap = 8.dp
            )

            Spacer(Modifier.height(8.dp))
            error?.let { Text(it, color = Color(0xFFD32F2F), fontSize = 14.sp) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/* 공통 컴포넌트 */
private val FieldHeight = 56.dp
private val UnderlineGap = 6.dp
private val IconWidth = 24.dp
private val IconHeight = 25.dp

@Composable
private fun FieldRowOverlayCheck(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    valid: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    topGap: Dp,
    labelFieldGap: Dp = 8.dp
) {
    if (topGap.value > 0f) Spacer(Modifier.height(topGap))

    Text(
        label,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.019f).em,
        color = Color.Black
    )
    Spacer(Modifier.height(labelFieldGap))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(FieldHeight)
    ) {
        Divider(
            color = Color(0xFFC0C0C0),
            thickness = 1.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )

        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, color = Color.Black),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.Black),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(end = IconWidth + 8.dp)
                .padding(bottom = UnderlineGap),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth()) {
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            color = Color(0xFFA6A6A6),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-0.019f).em
                        )
                    }
                    inner()
                }
            }
        )

        Image(
            painter = painterResource(
                if (valid) R.drawable.autologin_checked else R.drawable.autologin_unchecked
            ),
            contentDescription = "체크 상태",
            modifier = Modifier
                .width(IconWidth)
                .height(IconHeight)
                .align(Alignment.CenterEnd)
                .offset(y = (-2).dp)
        )
    }
}
