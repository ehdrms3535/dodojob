package com.example.dodojob.ui.feature.education

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.dodojob.R

private val ScreenBg = Color(0xFFF1F5F7)
private val NavBarBg = Color(0xFFF4F5F7)

@Composable
fun PaymentCompleteScreen(
    onDone: () -> Unit = {}   // ← 어디든 탭하면 호출
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .clickable { onDone() },            // ← 화면 어디든 탭 -> onDone
        color = ScreenBg
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.complete_image),
                    contentDescription = "결제 완료",
                    modifier = Modifier.size(69.dp)
                )
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "결제 완료!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.019).em,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) { BottomNavBarStub() }
    }
}


@Composable
private fun BottomNavBarStub() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .background(NavBarBg)
    )
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewPaymentCompleteScreen() {
    PaymentCompleteScreen()
}
