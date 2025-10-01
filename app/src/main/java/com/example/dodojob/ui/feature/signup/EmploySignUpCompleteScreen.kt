package com.example.dodojob.ui.feature.signup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route

private val ScreenBg = Color(0xFFF1F5F7)
private val StatusBg = Color(0xFFEFEFEF)
private val BrandBlue = Color(0xFF005FFF)
private val Gray500 = Color(0xFF828282)
private val CoolGray02 = Color(0xFFF4F5F7)

/**
 * 공고등록 완료 화면
 * - dp 기준
 * - 폰트는 전역 Typography 적용 가정
 */
@Composable
fun PostingRegisterCompleteScreen(
    nav: NavController,
    @DrawableRes checkImage: Int = R.drawable.complete_image
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        // 상단 StatusBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(StatusBg)
                .align(Alignment.TopCenter)
        )

        // 하단 NavigationBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp)
                .background(CoolGray02)
                .align(Alignment.BottomCenter)
        )

        // 메인 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 180.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = painterResource(id = checkImage),
                contentDescription = "완료 체크",
                modifier = Modifier
                    .size(69.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "회원가입 완료!",
                // 전역 폰트 기반, 크기/굵기만 지정
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "환영합니다! 두두잡과 함께해주셔서 감사해요.\n이제 경험 많은 시니어 인재들과 만날 준비가 되었어요.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                ),
                color = Gray500,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 버튼 영역 (홈 / 공고등록)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 홈(뒤로가기 대체)
                OutlinedButton(
                    onClick = {
                        nav.navigate(Route.EmployerHome.path)
                    },
                    border = ButtonDefaults.outlinedButtonBorder,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = BrandBlue
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(47.dp)
                ) {
                    Text(
                        text = "홈",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // 공고등록
                Button(
                    onClick = {
                        nav.navigate(Route.Announcement.path)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(2f)
                        .height(47.dp)
                ) {
                    Text(
                        text = "공고등록",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewPostingRegisterCompleteScreen() {
    val nav = rememberNavController()
    PostingRegisterCompleteScreen(nav = nav)
}
