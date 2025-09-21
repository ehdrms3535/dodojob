// app/src/main/java/com/example/dodojob/ui/components/AppBottomBar.kt
package com.example.dodojob.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.dodojob.R

private object BarSpec {
    val Container = Color.White
    val Active = Color(0xFF005FFF)
    val Inactive = Color(0xFF828282)
    val BarHeight = 72.dp
    val TopPadding = 9.dp
    val Gap = 24.dp
    val ItemW = 48.dp
    val ItemH = 69.dp
    val IconSize = 30.dp
}

enum class AppTab(val key: String, val label: String) {
    Home("home", "홈"),
    Edu("edu", "교육"),
    Welfare("welfare", "복지"),
    My("my", "마이")
}

/** 타입 세이프 버전 */
@Composable
fun AppBottomBar(
    current: AppTab,
    onClick: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BarSpec.BarHeight)
            .background(BarSpec.Container)
            .padding(top = BarSpec.TopPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.spacedBy(
                BarSpec.Gap,
                Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(
                label = AppTab.Home.label,
                selected = current == AppTab.Home,
                selectedIcon = R.drawable.selected_home,
                unselectedIcon = R.drawable.unselected_home
            ) { onClick(AppTab.Home) }

            BottomItem(
                label = AppTab.Edu.label,
                selected = current == AppTab.Edu,
                selectedIcon = R.drawable.selected_education,
                unselectedIcon = R.drawable.unselected_education
            ) { onClick(AppTab.Edu) }

            BottomItem(
                label = AppTab.Welfare.label,
                selected = current == AppTab.Welfare,
                selectedIcon = R.drawable.selected_welfare,
                unselectedIcon = R.drawable.unselected_welfare
            ) { onClick(AppTab.Welfare) }

            BottomItem(
                label = AppTab.My.label,
                selected = current == AppTab.My,
                selectedIcon = R.drawable.selected_my,
                unselectedIcon = R.drawable.unselected_my
            ) { onClick(AppTab.My) }
        }
    }
}

/** 문자열 호환 버전 (기존 코드 drop-in) */
@Composable
fun AppBottomBar(
    current: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cur = when (current) {
        "home" -> AppTab.Home
        "edu" -> AppTab.Edu
        "welfare" -> AppTab.Welfare
        "my" -> AppTab.My
        else -> AppTab.Home
    }
    AppBottomBar(
        current = cur,
        onClick = { onClick(it.key) },
        modifier = modifier
    )
}

/* ---------- 내부 공통 아이템 ---------- */
@Composable
private fun BottomItem(
    label: String,
    selected: Boolean,
    selectedIcon: Int,
    unselectedIcon: Int,
    onClick: () -> Unit
) {
    val color = if (selected) BarSpec.Active else BarSpec.Inactive
    Column(
        modifier = Modifier
            .width(BarSpec.ItemW)
            .height(BarSpec.ItemH)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = if (selected) selectedIcon else unselectedIcon),
            contentDescription = label,
            modifier = Modifier.size(BarSpec.IconSize),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = label,
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 24.sp, // 디자인의 150% 느낌
        )
    }
}
