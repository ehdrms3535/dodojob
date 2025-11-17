package com.example.dodojob.ui.feature.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.data.employ.EmployDto
import com.example.dodojob.data.employ.EmployRepository
import com.example.dodojob.data.employ.EmployRepositorySupabase
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.data.user.UserDto
import com.example.dodojob.data.user.UserRepository
import com.example.dodojob.data.user.UserRepositorySupabase
import com.example.dodojob.navigation.Route
import com.example.dodojob.ui.feature.verify.PreVerifyPrefill
import kotlinx.coroutines.launch
import java.util.*
import android.util.Log
import com.example.dodojob.session.CurrentUser

@Composable
fun EmployerSignupScreen(nav: NavController) {
    val client = LocalSupabase.current
    val repo: UserRepository = remember(client) { UserRepositorySupabase(client) }
    val repo1: EmployRepository = remember(client) { EmployRepositorySupabase(client) }

    val prefill: PreVerifyPrefill? = remember {
        runCatching {
            nav.getBackStackEntry(Route.PreVerify.path)
                .savedStateHandle
                .get<PreVerifyPrefill>("prefill")
        }.getOrNull()
    }.also {
        Log.d("EmployerSignup", "🔍 prefill 로드 결과 = $it")
    }

    val generatedId = remember { UUID.randomUUID().toString() }

    var name by rememberSaveable { mutableStateOf(prefill?.name.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(prefill?.phone.orEmpty()) }
    var email by rememberSaveable { mutableStateOf("") }
    var bizNo by rememberSaveable { mutableStateOf("") }

    var rrnFront by remember { mutableStateOf(prefill?.rrnFront.orEmpty()) }
    var rrnBackFirst by remember { mutableStateOf(prefill?.rrnBackFirst.orEmpty()) }

    val scope = rememberCoroutineScope()
    var loading by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val nameOk = name.trim().length >= 2
    val phoneOk = phone.filter { it.isDigit() }.length in 10..11
    val emailOk = email.contains("@") && email.contains(".")
    val bizOk = bizNo.filter { it.isDigit() }.length == 10

    val Bg = Color(0xFFF1F5F7)
    val canSubmit = nameOk && phoneOk && emailOk && bizOk && prefill != null

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
                            .clickable {
                                val ok = nav.popBackStack(Route.PreVerify.path, inclusive = false)
                                if (!ok) {
                                    nav.navigate(Route.PreVerify.path) { launchSingleTop = true }
                                }
                            },
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
                    .background(Color(0xFFF1F5F7))
            ) {
                Button(
                    onClick = {
                        if (!canSubmit || loading) return@Button
                        error = null
                        loading = true
                        scope.launch {
                            Log.d(
                                "EmployerSignup",
                                "nameOk=$nameOk, phoneOk=$phoneOk, emailOk=$emailOk, bizOk=$bizOk, prefill=${prefill != null}"
                            )

                            val safePrefill = prefill
                            if (safePrefill == null) {
                                error = "인증 정보가 없습니다. 다시 인증을 진행해 주세요."
                                loading = false
                                return@launch
                            }

                            runCatching {
                                val user = UserDto(
                                    id = generatedId,
                                    name = name,
                                    gender = prefill!!.gender,
                                    rrnFront = rrnFront,
                                    rrnBackFirst = rrnBackFirst,
                                    region = prefill.region,
                                    phone = phone,
                                    email = email,
                                    username = null,
                                    password = null,
                                    job = "고용주",
                                )
                                repo.insertUser(user)
                                CurrentUser.setId(generatedId)
                                val employ = EmployDto(email, bizNo)
                                repo1.insertEmploy(employ)
                            }.onSuccess {
                                Log.d("EmployerSignup", "✅ onSuccess 진입, 이제 navigate 호출")
                                nav.navigate("employsignupsec")
                            }.onFailure { e ->
                                Log.d("EmployerSignup", "✅ onfail 진입, 이제 navigate 호출")
                                error = e.message ?: "등록 실패"
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FFF))
                ) {
                    Text(
                        text = if (loading) "처리 중…" else "다음",
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
                label = "담당자명",
                value = name,
                onValueChange = { name = it },
                placeholder = "담당자 성함",
                valid = nameOk,
                keyboardType = KeyboardType.Text,
                topGap = 0.dp,
                labelFieldGap = 8.dp
            )
            FieldRowOverlayCheck(
                label = "담당자 연락처",
                value = phone,
                onValueChange = { phone = it },
                placeholder = "010-0000-0000",
                valid = phoneOk,
                keyboardType = KeyboardType.Phone,
                topGap = 24.dp,
                labelFieldGap = 8.dp
            )
            FieldRowOverlayCheck(
                label = "담당자 이메일",
                value = email,
                onValueChange = { email = it },
                placeholder = "이메일을 입력해주세요",
                valid = emailOk,
                keyboardType = KeyboardType.Email,
                topGap = 28.dp,
                labelFieldGap = 8.dp
            )
            FieldRowOverlayCheck(
                label = "사업자 등록번호",
                value = bizNo,
                onValueChange = { bizNo = it.filter { c -> c.isDigit() }.take(10) },
                placeholder = "사업자 등록번호(-제외 입력)",
                valid = bizOk,
                keyboardType = KeyboardType.Number,
                topGap = 28.dp,
                labelFieldGap = 8.dp
            )

            error?.let {
                Spacer(Modifier.height(13.dp))
                Text(it, color = Color(0xFFD32F2F), fontSize = 14.sp, letterSpacing = (-0.019f).em)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ===== 스타일 상수 & 컴포넌트 =====
private val FieldHeight = 56.dp
private val UnderlineGap = 6.dp
private val IconSize = 24.dp

@Composable
private fun FieldRowOverlayCheck(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    valid: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    topGap: Dp,
    labelFieldGap: Dp = 0.dp
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

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
            cursorBrush = SolidColor(Color.Black),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(end = IconSize + 8.dp)
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
                .size(IconSize)
                .align(Alignment.CenterEnd)
                .offset(y = (-2).dp)
        )
    }
}
