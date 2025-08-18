package com.example.dodojob.ui.feature.experience

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dodojob.navigation.Route

@Composable
fun ExperienceScreen(nav: NavController) {
    var description by remember { mutableStateOf("") }
    var showPhotoSheet by remember { mutableStateOf(false) } // ← 모달 표시 상태

    val Bg = Color(0xFFF1F5F7)
    val Primary = Color(0xFF005FFF)

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            // ✅ SignUp과 동일한 버튼 위치/크기/여백/스타일
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Bg)
                    .padding(horizontal = 18.dp, vertical = 50.dp)
            ) {
                Button(
                    onClick = { nav.navigate(Route.ExperienceComplete.path) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("완료", fontSize = 25.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    ) { inner ->
        // ✅ SignUp과 동일한 비율 기반 레이아웃
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            val W = maxWidth
            val H = maxHeight

            // SignUpIdPwScreen 과 동일한 규격(약간 조정만)
            val hPad = (W * 0.045f)
            val titleTop = (H * 0.04f)
            val titleSp = (W.value * 0.085f).sp
            val backSp = (W.value * 0.065f).sp
            val subTop = (H * 0.008f)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = hPad)
            ) {
                Spacer(Modifier.height(titleTop))

                // ← 뒤로가기
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "<",
                        fontSize = backSp,
                        color = Color.Black,
                        modifier = Modifier.clickable { nav.popBackStack() }
                    )
                }

                Spacer(Modifier.height(12.dp))

                // 제목
                Text(
                    text = "프로필을 완성해볼까요?",
                    fontSize = titleSp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Spacer(Modifier.height(subTop))

                Spacer(Modifier.height(20.dp))

                // 큰 원 + 카메라 아이콘
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(178.dp)
                            .background(Color(0xFFD9D9D9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = "사진 추가",
                            tint = Color(0xFF606060),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(Modifier.height(35.dp))

                Text(
                    text = "경력사항을 적어주세요",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                // 카드형 입력 영역
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(10.dp),
                            clip = true
                        )
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp),
                        placeholder = {
                            Text(
                                "담당업무와 근무했던 회사명을 적어주세요",
                                color = Color(0xFF999999),
                                fontSize = 16.sp
                            )
                        },
                        singleLine = false,
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            errorContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        )
                    )
                }

                Spacer(Modifier.height(14.dp))

                // 연한 파란색 pill +추가  → 모달 표시
                Button(
                    onClick = { showPhotoSheet = true },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(39.dp),
                    shape = RoundedCornerShape(31.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x2B005FFF),
                        contentColor = Primary
                    ),
                    contentPadding = PaddingValues(horizontal = 15.dp, vertical = 8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("추가", fontSize = 16.sp, textAlign = TextAlign.Center)
                }

                Spacer(Modifier.height(12.dp))
            }

            // ====== 사진 선택 모달 (스샷 느낌) ======
            if (showPhotoSheet) {
                PhotoOptionsDialog(
                    onDismiss = { showPhotoSheet = false },
                    onPickCamera = {
                        // TODO: 카메라 연결
                        showPhotoSheet = false
                    },
                    onPickGallery = {
                        // TODO: 갤러리 연결
                        showPhotoSheet = false
                    },
                    onUseDefault = {
                        // TODO: 기본 이미지 적용
                        showPhotoSheet = false
                    }
                )
            }
        }
    }
}

/** 스샷과 비슷한 중앙 모달: 반투명 오버레이 + 라운드 20dp 카드 + 3개 옵션 */
@Composable
private fun PhotoOptionsDialog(
    onDismiss: () -> Unit,
    onPickCamera: () -> Unit,
    onPickGallery: () -> Unit,
    onUseDefault: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // 오버레이
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x6A3E454B)) // rgba(62,69,75,0.42) 근사치
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            // 카드
            Box(
                modifier = Modifier
                    .padding(bottom = 48.dp) // 스샷처럼 살짝 위로
                    .width(340.dp)
                    .wrapContentHeight()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 18.dp, vertical = 9.dp)
                    .clickable(enabled = false) {}, // 바깥 클릭 dismiss만 허용
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "프로필 사진 설정",
                        color = Color(0xFF828282),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFCFCFCF))

// 카메라로 찍기
                    OptionRow(text = "카메라로 찍기", onClick = onPickCamera)
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFCFCFCF))

// 앨범에서 사진 선택
                    OptionRow(text = "앨범에서 사진 선택", onClick = onPickGallery)
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFCFCFCF))

                    // 기본 이미지 적용
                    OptionRow(text = "기본 이미지 적용", onClick = onUseDefault)

                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun OptionRow(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(51.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF005FFF),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
