package com.example.dodojob.ui.feature.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route

@Composable
fun SignUpCompleteScreen(nav: NavController) {
    val Bg = Color(0xFFF1F5F7)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        // 상단 StatusBar 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color(0xFFEFEFEF))
                .align(Alignment.TopCenter)
        )

        // 하단 Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp)
                .background(Color(0xFFF4F5F7))
                .align(Alignment.BottomCenter)
        )

        // 메인 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 250.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.complete_image),
                contentDescription = "가입 완료",
                modifier = Modifier.size(69.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "회원가입 완료!",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "환영합니다! 두두잡과 함께해주셔서 감사해요.\n소중한 경험을 살릴 시간이 왔습니다.",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF828282),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ✅ 화면 어디나 탭하면 MainScreen으로 이동
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    indication = null, // 물결 효과 제거 (원하면 빼세요)
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    nav.navigate(Route.Login.path)
                    // 필요 시, 현재 화면을 백스택에서 제거하고 싶다면:
                    // nav.navigate(Route.Main.path) {
                    //     popUpTo(Route.SignUpComplete.path) { inclusive = true }
                    //     launchSingleTop = true
                    // }
                }
        )
    }
}
