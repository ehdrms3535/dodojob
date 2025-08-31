package com.example.dodojob.ui.feature.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

private val Primary = Color(0xFF005FFF)
private val BgGray  = Color(0xFFF1F5F7)
private val TextGray = Color(0xFF636363)

/** 공통 중앙 모달 (조금 더 넓게 + 메세지 2단 분리) */
@Composable
fun ConfirmDialog(
    title: String,
    message1: String,                   // 1단: 검정, 조금 큰 폰트
    message2: String,                   // 2단: 회색, 더 작은 폰트
    positiveText: String,
    negativeText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    positiveColor: Color = Primary,
) {
    Dialog(onDismissRequest = { onCancel() }) {
        Box(
            modifier = Modifier
                .width(400.dp) // ⬅ 기존 328dp → 360dp (수평으로 꽉 채우진 않음)
                .wrapContentHeight()
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 타이틀 (파란 포인트 유지)
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(14.dp))

                // 메시지 1 (검정, 조금 더 큼)
                Text(
                    text = message1,
                    fontSize = 18.sp, // ← 메인 문장
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                Spacer(Modifier.height(8.dp))

                // 메시지 2 (회색, 더 작게)
                if (message2.isNotBlank()) {
                    Text(
                        text = message2,
                        fontSize = 14.sp, // ← 보조 문장
                        fontWeight = FontWeight.Medium,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                } else {
                    Spacer(Modifier.height(8.dp))
                }

                // 버튼 영역
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 취소
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .background(BgGray, RoundedCornerShape(10.dp))
                            .border(1.dp, Primary, RoundedCornerShape(10.dp))
                            .clickable { onCancel() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = negativeText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                    // 확인
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .background(positiveColor, RoundedCornerShape(10.dp))
                            .clickable { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = positiveText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/** 회원탈퇴 */
@Composable
fun LeaveDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    ConfirmDialog(
        title = "회원탈퇴",
        message1 = "두두잡과 함께해주셔서 감사합니다.",
        message2 = "회원탈퇴 시 모든 정보가 삭제되며,\n다시 되돌릴 수 없습니다.\n언제든지 다시 만나뵐 수 있기를 바랍니다.",
        positiveText = "탈퇴하기",
        negativeText = "취소",
        onConfirm = onConfirm,
        onCancel = onCancel,
        positiveColor = Primary
    )
}

/** 로그아웃 */
@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    ConfirmDialog(
        title = "로그아웃",
        message1 = "두두잡에서 로그아웃하시겠어요?",
        message2 = "다시 아이디와 비밀번호로 로그인해야 합니다.",
        positiveText = "로그아웃",
        negativeText = "취소",
        onConfirm = onConfirm,
        onCancel = onCancel,
        positiveColor = Primary
    )
}
