package com.example.dodojob.ui.feature.hope

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*

@Composable
fun HopeWorkFilterScreenStandalone() {
    // ----- 내부 상태 (나중에 네비/VM으로 대체 가능) -----
    var region by remember { mutableStateOf<String?>(null) }
    var job by remember { mutableStateOf<String?>(null) }
    var period by remember { mutableStateOf<String?>(null) }
    var days by remember { mutableStateOf(setOf<String>()) }
    var timeOfDay by remember { mutableStateOf<String?>(null) }

    val periodOptions = listOf("1일", "1주일 이하", "1주일~1개월", "6개월~1년", "1년 이상")
    val dayOptions = listOf("평일", "주말", "월", "화", "수", "목", "금", "토", "일")
    val timeOptions = listOf("오전", "오후")

    val scroll = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        val W = maxWidth
        val H = maxHeight

        // 비율 기반 치수
        fun clamp(dp: Dp, min: Dp, max: Dp) = dp.coerceIn(min, max)
        val hPad = clamp(W * 0.045f, 12.dp, 20.dp)
        val vPad = clamp(H * 0.02f, 8.dp, 20.dp)

        val titleSp = (W.value * 0.09f).sp
        val titleLH = (W.value * 0.12f).sp
        val labelSp = (W.value * 0.07f).sp
        val chipSp = (W.value * 0.055f).sp
        val btnSp = (W.value * 0.06f).sp

        val blockGap = clamp(H * 0.06f, 16.dp, 36.dp)
        val rowGap = clamp(H * 0.02f, 10.dp, 20.dp)
        val chipH = clamp(H * 0.08f, 48.dp, 68.dp)
        val chipRadius = clamp(W * 0.03f, 8.dp, 12.dp)
        val fieldH = 57.dp
        val fieldRadius = 10.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = hPad, vertical = vPad)
        ) {
            // 상단 "<" (동작은 추후 연결)
            Text(
                "<",
                fontSize = (W.value * 0.07f).sp,
                color = Color.Black,
                modifier = Modifier
                    .clickable { /* TODO: nav.popBackStack() */ }
                    .padding(bottom = 16.dp)
            )

            // 타이틀
            Text(
                "나에게 맞게 \n입력해주세요 :)",
                fontSize = titleSp,
                lineHeight = titleLH,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(Modifier.height(blockGap))

            // ───────── 근무할 지역 ─────────
            SectionLabel("근무할 지역", labelSp)
            FieldBox(
                text = region ?: "지역, 동네를 선택해주세요                         >",
                hintColor = Color(0xFF727272),
                height = fieldH,
                radius = fieldRadius,
                onClick = { /* TODO: 지역 선택 열기 */ }
            )

            Spacer(Modifier.height(blockGap))

            // ───────── 직종 선택 ─────────
            SectionLabel("직종 선택", labelSp)
            FieldBox(
                text = job ?: "직종을 선택해주세요                                  >",
                hintColor = Color(0xFF727272),
                height = fieldH,
                radius = fieldRadius,
                onClick = { /* TODO: 직종 선택 열기 */ }
            )

            Spacer(Modifier.height(blockGap))

            // ───────── 기간 ─────────
            SectionLabel("얼마나 일하실 건가요?", labelSp)
            Spacer(Modifier.height(rowGap))
            TwoColumnChips(
                options = periodOptions,
                isSelected = { it == period },
                onClick = { opt -> period = if (period == opt) null else opt },
                itemHeight = chipH,
                radius = chipRadius,
                textSize = chipSp
            )

            Spacer(Modifier.height(blockGap))

            // ───────── 요일 ─────────
            SectionLabel("일할 수 있는 요일을 \n선택해주세요", labelSp)
            Spacer(Modifier.height(rowGap))
            TwoColumnChips(
                options = dayOptions,
                isSelected = { it in days },
                onClick = { opt -> days = if (opt in days) days - opt else days + opt },
                itemHeight = chipH,
                radius = chipRadius,
                textSize = chipSp
            )

            Spacer(Modifier.height(blockGap))

            // ───────── 시간 ─────────
            SectionLabel("일하실 시간을 선택해주세요", labelSp)
            Spacer(Modifier.height(rowGap))
            TwoColumnChips(
                options = timeOptions,
                isSelected = { it == timeOfDay },
                onClick = { opt -> timeOfDay = if (timeOfDay == opt) null else opt },
                itemHeight = chipH,
                radius = chipRadius,
                textSize = chipSp
            )

            Spacer(Modifier.height(clamp(H * 0.045f, 20.dp, 36.dp)))

            // 하단 버튼 (초기화 / 적용하기)
            val btnH = clamp(H * 0.07f, 48.dp, 64.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        region = null
                        job = null
                        period = null
                        days = emptySet()
                        timeOfDay = null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(btnH),
                    shape = RoundedCornerShape(chipRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDADADA))
                ) {
                    Text("초기화", fontSize = btnSp, color = Color.Black)
                }

                Spacer(Modifier.width(clamp(W * 0.05f, 12.dp, 24.dp)))

                val canApply = region != null || job != null || period != null || days.isNotEmpty() || timeOfDay != null
                Button(
                    onClick = {
                        // TODO: 다음 단계로 이동 or 결과 전달
                        // println("region=$region, job=$job, period=$period, days=$days, time=$timeOfDay")
                    },
                    enabled = canApply,
                    modifier = Modifier
                        .weight(2f)
                        .height(btnH),
                    shape = RoundedCornerShape(chipRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canApply) Color(0xFF005FFF) else Color(0xFFBFC6D2),
                        disabledContainerColor = Color(0xFFBFC6D2)
                    )
                ) {
                    Text("적용하기", fontSize = btnSp, color = Color.White)
                }
            }

            Spacer(Modifier.height(40.dp)) // 맨 아래 여백
        }
    }
}

@Composable
private fun SectionLabel(text: String, size: TextUnit) {
    Text(text,
        fontSize = size,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        lineHeight = size*1.4f)
}

@Composable
private fun FieldBox(
    text: String,
    hintColor: Color,
    height: Dp,
    radius: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(radius))
            .background(Color.White)
            .border(1.dp, Color(0xFFE1E1E1), RoundedCornerShape(radius))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = CenterStart
    ) {
        Text(text, color = hintColor, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}

/** 외부 라이브러리 없이 2열 칩 레이아웃 */
@Composable
private fun TwoColumnChips(
    options: List<String>,
    isSelected: (String) -> Boolean,
    onClick: (String) -> Unit,
    itemHeight: Dp,
    radius: Dp,
    textSize: TextUnit
) {
    // options를 2개씩 끊어서 행으로 배치
    val rows = options.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { opt ->
                    ChipItem(
                        text = opt,
                        selected = isSelected(opt),
                        onClick = { onClick(opt) },
                        height = itemHeight,
                        radius = radius,
                        textSize = textSize,
                        modifier = Modifier.weight(1f)
                    )
                }
                // 홀수개일 때 오른쪽 비워서 정렬 맞춤
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ChipItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    height: Dp,
    radius: Dp,
    textSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(radius))
            .background(if (selected) Color(0xFFC1D2ED) else Color.White)
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFF005FFF) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(radius)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = textSize,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color(0xFF005FFF) else Color.Black
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewHopeWorkFilterScreenStandalone() {
    MaterialTheme {
        HopeWorkFilterScreenStandalone()
    }
}
