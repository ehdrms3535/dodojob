package com.example.dodojob.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.example.dodojob.BuildConfig

private val Primary  = Color(0xFF005FFF)
private val BgGray   = Color(0xFFF1F5F7)
private val TextGray = Color(0xFF9C9C9C)
private val CancelBg = Color(0xFFDEEBFF)

/** 공통 확인 모달 — 이름있는 파라미터 + 로딩/비활성 지원 */
@Composable
fun ConfirmDialog(
    title: String,
    message1: String,
    message2: String,
    positiveText: String,
    negativeText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    positiveColor: Color = Primary,
    enabled: Boolean = true,
    showProgress: Boolean = false
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWidth * 0.90f

    Dialog(
        onDismissRequest = { if (enabled) onCancel() },
        properties = DialogProperties(usePlatformDefaultWidth = false) // ✅ 여백 제거
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 12.dp) // 살짝만 좌우 여백
        ) {
            Box(
                modifier = Modifier
                    .width(cardWidth)
                    .wrapContentHeight()
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(top = 30.dp, bottom = 10.dp) // 위는 더 늘리고 아래는 조금 줄임
                    .align(Alignment.Center)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 타이틀
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 첫 번째 문장
                    Text(
                        text = message1,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 두 번째 문장
                    if (message2.isNotBlank()) {
                        Text(
                            text = message2,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 버튼 영역
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 취소 버튼
                        var cancelModifier: Modifier = Modifier
                        if (enabled) cancelModifier = cancelModifier.clickable { onCancel() }

                        Box(
                            modifier = cancelModifier
                                .weight(1f)
                                .height(54.dp)
                                .background(CancelBg, RoundedCornerShape(10.dp))
                                .border(1.dp, Primary, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = negativeText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }

                        // 확인 버튼
                        var confirmModifier: Modifier = Modifier
                        if (enabled) confirmModifier = confirmModifier.clickable { onConfirm() }

                        Box(
                            modifier = confirmModifier
                                .weight(1f)
                                .height(54.dp)
                                .background(
                                    if (enabled) positiveColor else positiveColor.copy(alpha = 0.5f),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (showProgress) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = positiveText,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

/** 회원탈퇴 다이얼로그 */
@Composable
fun LeaveDialog(
    currentUsername: String?,
    nav: NavController,
    onClosed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }

    val supabase = remember {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) { install(Postgrest) }
    }

    val isUsernameValid = !currentUsername.isNullOrBlank()

    ConfirmDialog(
        title = "회원탈퇴",
        message1 = if (isUsernameValid)
            "두두잡과 함께해주셔서 감사합니다."
        else
            "로그인이 필요합니다.",
        message2 = if (isUsernameValid)
            "회원탈퇴 시 모든 정보가 삭제되며,\n다시 되돌릴 수 없습니다.\n언제든지 다시 만나뵐 수 있기를 바랍니다."
        else
            "현재 사용자 정보를 확인할 수 없어 탈퇴를 진행할 수 없습니다.\n다시 로그인한 뒤 시도해 주세요.",
        positiveText = if (busy) "처리중..." else "탈퇴하기",
        negativeText = "취소",
        onConfirm = {
            if (busy || !isUsernameValid) return@ConfirmDialog
            busy = true
            scope.launch {
                val result = runCatching {
                    supabase.postgrest.rpc(
                        function = "hard_delete_user",
                        parameters = buildJsonObject { put("p_username", currentUsername!!) }
                    )
                }
                busy = false
                onClosed()

                if (result.isSuccess) {
                    nav.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        },
        onCancel = { if (!busy) onClosed() },
        positiveColor = Primary,
        enabled = !busy && isUsernameValid,
        showProgress = busy
    )
}

/** 로그아웃 다이얼로그 */
@Composable
fun LogoutDialog(
    nav: NavController,
    onClosed: () -> Unit
) {
    var busy by remember { mutableStateOf(false) }

    ConfirmDialog(
        title = "로그아웃",
        message1 = "두두잡에서 로그아웃하시겠어요?",
        message2 = "다시 아이디와 비밀번호로 로그인해야 합니다.",
        positiveText = if (busy) "처리중..." else "로그아웃",
        negativeText = "취소",
        onConfirm = {
            if (busy) return@ConfirmDialog
            busy = true
            onClosed()
            nav.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            busy = false
        },
        onCancel = { if (!busy) onClosed() },
        positiveColor = Primary,
        enabled = !busy,
        showProgress = busy
    )
}
