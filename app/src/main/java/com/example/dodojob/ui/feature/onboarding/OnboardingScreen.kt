package com.example.dodojob.ui.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.BoxWithConstraints
import com.example.dodojob.navigation.Route

@Composable
fun OnboardingScreen(nav: NavController) {
    var selected by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color(0xFFF1F5F7),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F7))
                    .padding(horizontal = 18.dp, vertical = 50.dp)
            ) {
                Button(
                    onClick = {
                        // 선택값에 따라 다음 화면 라우팅
                        when (selected) {
                            "senior" -> nav.navigate(Route.Login.path)
                            "boss"   -> nav.navigate(Route.Login.path)     // 임시
                            "center" -> nav.navigate(Route.Login.path)     // 임시
                        }
                    },
                    enabled = selected != null,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected != null) Color(0xFF005FFF) else Color(0xFFBFC6D2),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFBFC6D2),
                        disabledContentColor = Color.White
                    )
                ) { Text("다음", fontSize = 25.sp, fontWeight = FontWeight.Medium) }
            }
        }
    ) { inner ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            val W = maxWidth
            val H = maxHeight
            val hPad = W * 0.045f
            val titleTop = H * 0.12f
            val titleSp = (W.value * 0.09f).sp
            val titleLH = (W.value * 0.125f).sp
            val subTop = H * 0.02f
            val subSp = (W.value * 0.055f).sp
            val subLH = (W.value * 0.083f).sp
            val groupTop = H * 0.075f
            val cardGap = H * 0.015f
            val contentBottomSpacer = H * 0.12f

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF1F5F7))
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = hPad)
            ) {
                Spacer(Modifier.height(titleTop))
                Text(
                    text = "당신의 목적에 맞게\n시작해보세요!",
                    fontSize = titleSp, fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF000000), lineHeight = titleLH
                )
                Spacer(Modifier.height(subTop))
                Text(
                    text = "경험을 살려 일할 수도, 좋은 인재를\n찾을 수도 있습니다.",
                    fontSize = subSp, color = Color(0xFF636363), lineHeight = subLH
                )
                Spacer(Modifier.height(groupTop))

                OptionCardRatio("🤝", "일하고 싶은 시니어입니다", selected == "senior") { selected = "senior" }
                Spacer(Modifier.height(cardGap))
                OptionCardRatio("👔", "사람을 구하는 사장님입니다", selected == "boss") { selected = "boss" }
                Spacer(Modifier.height(cardGap))
                OptionCardRatio("🫂", "복지센터 / 기관 담당자", selected == "center") { selected = "center" }

                Spacer(Modifier.height(contentBottomSpacer))
            }
        }
    }
}

@Composable
private fun OptionCardRatio(
    icon: String, title: String, selected: Boolean, onClick: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3.77f)
            .clip(RoundedCornerShape(10.dp))      // 살짝 둥근 사각형
            .background(if (selected) Color(0xFFE9F2FF) else Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        val W = maxWidth
        val H = maxHeight
        val hPad = W * 0.085f     // ≈ 28/328
        val vPad = H * 0.18f      // ≈ 16/87
        val gap  = W * 0.045f     // ≈ 15/328
        val emojiSp = (H.value * 0.4f).sp
        val textSp  = (H.value * 0.20f).sp

        Row(
            Modifier.fillMaxSize().padding(horizontal = hPad, vertical = vPad),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = emojiSp)
            Spacer(Modifier.width(gap))
            Text(title, fontSize = textSp, fontWeight = FontWeight.Medium, color = Color(0xFF111315))
        }
    }
}
