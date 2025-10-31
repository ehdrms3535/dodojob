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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.example.dodojob.BuildConfig

// delegated properties
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

private val Primary  = Color(0xFF005FFF)
private val BgGray   = Color(0xFFF1F5F7)
private val TextGray = Color(0xFF636363)

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
    Dialog(onDismissRequest = { if (enabled) onCancel() }) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .wrapContentHeight()
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = message1,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (message2.isNotBlank()) {
                    Text(
                        text = message2,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 버튼 영역
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 취소 버튼
                    var cancelModifier: Modifier = Modifier
                    if (enabled) cancelModifier = cancelModifier.clickable { onCancel() }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .background(BgGray, RoundedCornerShape(10.dp))
                            .border(1.dp, Primary, RoundedCornerShape(10.dp))
                            .then(cancelModifier),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = negativeText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }

                    // 확인 버튼
                    var confirmModifier: Modifier = Modifier
                    if (enabled) confirmModifier = confirmModifier.clickable { onConfirm() }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .background(
                                if (enabled) positiveColor else positiveColor.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .then(confirmModifier),
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
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/**
 * 회원탈퇴 (nullable username 지원)
 * - username이 null/blank면: 확인 버튼 비활성화 + 안내 문구 표시 (크래시 없음)
 * - username이 유효하면: RPC 실행 → 성공 시 백스택 초기화 후 로그인 화면으로 이동
 */
@Composable
fun LeaveDialog(
    currentUsername: String?,   // ← nullable 그대로 받음
    nav: NavController,
    onClosed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }

    // 모달 내부에서 SupabaseClient 직접 생성 (전역/세션 없음)
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
            "회원탈퇴 시 모든 정보가 삭제되며,\n되돌릴 수 없습니다."
        else
            "현재 사용자 정보를 확인할 수 없어 탈퇴를 진행할 수 없습니다.\n다시 로그인한 뒤 시도해 주세요.",
        positiveText = if (busy) "처리중..." else "탈퇴하기",
        negativeText = "닫기",
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
                } else {
                    // TODO: 에러 메시지 표시 (result.exceptionOrNull()?.message)
                }
            }
        },
        onCancel = { if (!busy) onClosed() },
        positiveColor = Primary,
        enabled = !busy && isUsernameValid,   // ← username 없으면 비활성화
        showProgress = busy
    )
}

/** 로그아웃 (세션/전역 없이 네비만 초기화) */
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
