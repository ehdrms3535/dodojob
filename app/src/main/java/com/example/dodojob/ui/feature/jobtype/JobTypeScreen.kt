package com.example.dodojob.ui.feature.jobtype

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import com.example.dodojob.session.CurrentUser

@Composable
fun JobTypeScreen(nav: NavController) {
    val Bg = Color(0xFFF1F5F7)
    val Primary = Color(0xFF005FFF)
    val TextBlack = Color(0xFF000000)

    val options = listOf("급여형", "단기알바", "원격", "봉사")
    var selected by remember { mutableStateOf(options.first()) }

    Scaffold(
        containerColor = Bg,
        topBar = {
            Column {
                // 상태바 (24dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(Color(0xFFEFEFEF))
                )
                Spacer(Modifier.height(60.dp))
                // 뒤로가기 없음: 회원가입 화면과 동일 톤/여백
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Bg)
                ) {
                    Text(
                        text = "원하는 일자리가\n어떻게 되시나요?",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.019f).em,
                        color = TextBlack,
                        lineHeight = 45.sp,
                        modifier = Modifier
                            .padding(start = 16.dp, top = 24.dp, bottom = 16.dp)
                    )
                }
                Spacer(Modifier.height(48.dp))
            }
        },
        bottomBar = {
            // 회원가입 화면 '완료' 버튼과 동일한 크기/위치
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .background(Bg)
            ) {
                Button(
                    onClick = {
                        CurrentUser.setJob(selected)
                        nav.navigate(Route.Hope.path)
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .height(54.dp), // 54.48≈54
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "다음",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.019).em
                    )
                }
            }
        }
    ) { inner ->
        // ========= 비율 기반 레이아웃 =========
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            val W = maxWidth               // Dp
            val scale = W / 360.dp         // Float  (피그마 360 기준 스케일)

// 좌우 패딩 (타이틀/컨텐츠 공통)
            val titleHorizontal = 16.dp * scale         // Dp

// 카드 세로는 비율 유지, 가로는 "화면폭 - 좌우 패딩*2"로 안전하게
            val cardW = W - (titleHorizontal * 2)       // Dp
            val cardH = W * (73.99f / 360f)             // Dp

// 내부 여백/간격(비율)
            val horizontalPad = 16.dp * scale           // Dp
            val verticalPad   = 10.dp * scale           // Dp
            val listTopPad    = 10.dp * scale           // Dp
            val itemGap       = 10.dp * scale           // Dp

// 타이틀 아래 간격(피그마 26)
            val titleBottomGap = 26.dp * scale          // Dp

// 텍스트/아이콘 사이즈(비율)
            val labelSp  = (22f * scale).sp             // Sp
            val iconBox  = (56f * scale).dp             // Dp
            val iconSize = (36f * scale).dp             // Dp


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = titleHorizontal)
                    .padding(top = titleBottomGap),
                verticalArrangement = Arrangement.spacedBy(itemGap)
            ) {
                options.forEach { title ->
                    OptionItemSelectableRatio(
                        title = title,
                        selected = selected == title,
                        onClick = { selected = title },
                        width = cardW,              // ← 변경된 폭 사용
                        height = cardH,
                        innerHPad = horizontalPad,
                        innerVPad = verticalPad,
                        labelSize = labelSp,
                        iconBox = iconBox,
                        iconSize = iconSize
                    )
                }
                Spacer(Modifier.height(listTopPad))
            }
        }
    }
}

@Composable
private fun OptionItemSelectableRatio(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    width: Dp,
    height: Dp,
    innerHPad: Dp,
    innerVPad: Dp,
    labelSize: TextUnit,
    iconBox: Dp,
    iconSize: Dp
) {
    Surface(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = Color.White,
        shadowElevation = 1.dp // 0 1 3 근사
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = innerHPad, vertical = innerVPad),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = labelSize,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF000000),
                letterSpacing = (-0.019).em,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier.size(iconBox),
                contentAlignment = Alignment.Center
            ) {
                val resId = if (selected)
                    R.drawable.jobtype_checked
                else
                    R.drawable.jobtype_unchecked

                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
