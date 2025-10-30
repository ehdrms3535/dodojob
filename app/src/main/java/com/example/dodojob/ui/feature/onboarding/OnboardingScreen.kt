package com.example.dodojob.ui.feature.onboarding

import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.dodojob.R
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
                            "boss"   -> nav.navigate(Route.PreLogin.path)     // 임시
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
            val groupTop = H * 0.1f
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

                OptionCard(
                    iconRes = R.drawable.intro_senior,
                    title = "일하고 싶은 시니어입니다",
                    selected = selected == "senior",
                    onClick = { selected = "senior" }
                )

                Spacer(Modifier.height(cardGap))

                OptionCard(
                    iconRes = R.drawable.intro_employer,
                    title = "사람을 구하는 사장님입니다",
                    selected = selected == "boss",
                    onClick = { selected = "boss" }
                )

                Spacer(Modifier.height(contentBottomSpacer))
            }
        }
    }
}

@Composable
private fun OptionCard(
    iconRes: Int,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val strokeColor = if (selected) Color(0xFF005FFF) else Color.Transparent
    val textColor = if (selected) Color(0xFF005FFF) else Color(0xFF000000)

    Row(
        modifier = Modifier
            .fillMaxWidth()               // 화면 패딩 내에서 꽉 차게
            .height(87.dp)                // Figma 높이 87
            .clip(RoundedCornerShape(10.dp))
            .then(if (!selected) Modifier.shadow(3.dp, RoundedCornerShape(10.dp), clip = false) else Modifier)
            .background(Color.White)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = strokeColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 28.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .width(36.dp)
                .height(54.dp)
        )
        Spacer(Modifier.width(15.dp))
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            lineHeight = 30.sp
        )
    }
}