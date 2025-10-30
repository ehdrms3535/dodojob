package com.example.dodojob.ui.feature.signup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route

private val ScreenBg = Color(0xFFF1F5F7)
private val BrandBlue = Color(0xFF005FFF)
private val Gray500 = Color(0xFF828282)

/**
 * 공고등록/06 – 완료 화면
 * (중앙 정렬 + 버튼 하단 고정 + 좌우 16dp 여백)
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
        // 중앙 컨텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 87.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = checkImage),
                contentDescription = "완료 체크",
                modifier = Modifier.size(69.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "회원가입 완료!",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.019).em,
                    lineHeight = 27.sp
                ),
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "환영합니다! 두두잡과 함께해주셔서 감사해요.\n이제 경험 많은 시니어 인재들과 만날 준비가 되었어요.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.019).em,
                    lineHeight = 20.sp
                ),
                color = Gray500,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )
        }

        // 하단 버튼 영역 (고정 + 좌우 16dp 패딩)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(87.dp)
                .align(Alignment.BottomCenter)
                .background(ScreenBg)
                .padding(horizontal = 16.dp), // ✅ 좌우 패딩 추가
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좌측: 홈 버튼
            OutlinedButton(
                onClick = { nav.navigate(Route.Announcement.path) },
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(BrandBlue)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = BrandBlue
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(47.dp)
            ) {
                Text(
                    text = "홈",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.019).em,
                        lineHeight = 27.sp
                    )
                )
            }

            // 우측: 채용공고 등록하기 버튼
            Button(
                onClick = { nav.navigate(Route.Announcement.path) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(2f)
                    .height(47.dp)
            ) {
                Text(
                    text = "채용공고 등록하기",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.019).em,
                        lineHeight = 27.sp
                    )
                )
            }
        }
    }
}
