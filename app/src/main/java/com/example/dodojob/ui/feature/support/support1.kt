package com.example.dodojob.ui.feature.support

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/* ===================== 색상 ===================== */
private val PrimaryBlue = Color(0xFF005FFF)
private val Bg = Color(0xFFF1F5F7)

/* ===================== 더미데이터 ===================== */
enum class ReadState { Read, Unread }

data class SupportItem(
    val id: String,
    val readState: ReadState,     // 열람 / 미열람
    val appliedAt: String,        // 2025.08.25
    val company: String,
    val title: String
)

private object SupportFakeDb {
    fun items(): List<SupportItem> = listOf(
        SupportItem("1", ReadState.Read,   "2025.08.25", "모던하우스", "매장운영 및 고객관리 하는 일에 적합한 분 구해요"),
        SupportItem("2", ReadState.Unread, "2025.08.20", "대구동구 어린이도서관", "아이들 책 읽어주기, 독서 습관 형성 프로그램 지원"),
        SupportItem("3", ReadState.Unread, "2025.08.14", "수성구 체육센터", "회원 운동 지도 보조, 센터 관리 가능하신 분 지원 요망"),
        SupportItem("4", ReadState.Read,   "2025.08.10", "대구도시철도공사", "지하철 역사 안전 순찰, 이용객 안내, 분실물 관리"),
    )
}

/* ===================== Route + Screen(단일) ===================== */
@Composable
fun SupportRoute(nav: NavController) {
    val all = remember { SupportFakeDb.items() }
    var keyword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 (상태바 + 뒤로가기 + "총 n건 지원" + 검색)
        SupportTopSection(
            nav = nav,
            countText = countText(all.size),     // 숫자만 파란색 강조
            keyword = keyword,
            onKeywordChange = { keyword = it }
        )

        // 본문 (탭바 + 카드 리스트)
        val filtered = remember(keyword, all) {
            if (keyword.isBlank()) all
            else all.filter {
                it.company.contains(keyword, true) || it.title.contains(keyword, true)
            }
        }
        SupportBodySection(items = filtered)
    }
}

/* ===================== 상단 섹션 (Figma 반영) ===================== */
@Composable
private fun SupportTopSection(
    nav: NavController,
    countText: AnnotatedString,
    keyword: String,
    onKeywordChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 247.dp)     // Frame 1707480142 height
            .background(Color.White)
            .padding(bottom = 20.dp)    // gap: 20px
    ) {
        // MO/Status (24px, #EFEFEF)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color(0xFFEFEFEF))
        )

        // Frame 1707480140 (150px)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(start = 16.dp, top = 20.dp) // 20px 0 0 16px
        ) {
            // Back 아이콘 라인 (24px)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { nav.popBackStack() }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.ArrowBackIosNew, contentDescription = "뒤로", tint = Color.Black)
                }
            }

            // "총 n건 지원" (32sp, letterSpacing -0.019em)
            Row(
                modifier = Modifier
                    .height(68.dp)
                    .padding(start = 4.dp, end = 10.dp, top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = countText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.019).em,
                    color = Color(0xFF000000)
                )
            }
        }

        // 검색 박스 (Frame 3469017)
        val shape = RoundedCornerShape(10.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(57.dp)
                .clip(shape)
                .background(Bg)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                singleLine = true,
                trailingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = "검색", tint = Color(0xFF62626D))
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    cursorColor = PrimaryBlue
                ),
                shape = shape
            )
        }
    }
}

/* ===================== 본문 섹션 (탭바 + 카드 리스트) ===================== */
@Composable
private fun SupportBodySection(items: List<SupportItem>) {
    var selectedTab by remember { mutableStateOf(0) } // 0: 지원완료, 1: 면접예정, 2: 채용완료

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg) // ⬅️ 회색 배경
    ) {
        // ---------- 탭바 ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabLabel("지원완료", isSelected = selectedTab == 0) { selectedTab = 0 }
                TabLabel("면접예정", isSelected = selectedTab == 1) { selectedTab = 1 }
                TabLabel("채용완료", isSelected = selectedTab == 2) { selectedTab = 2 }
            }
            // 밑줄 indicator (선택 탭 중앙 60x4)
            Row(Modifier.fillMaxWidth()) {
                repeat(3) { idx ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedTab == idx) {
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(4.dp)
                                    .background(PrimaryBlue)
                            )
                        }
                    }
                }
            }
        }

        // ---------- 카드 리스트 (카드 사이 회색 노출) ----------
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // ⬅️ 카드 사이 간격
        ) {
            items(items, key = { it.id }) { item ->
                SupportCard(item = item)
            }
        }
    }
}

/* ===================== 탭 라벨 ===================== */
@Composable
private fun TabLabel(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp) // ⬆ 상하 여유
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = (-0.5).sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PrimaryBlue else Color(0xFF000000)
        )
        Spacer(Modifier.height(6.dp))
        // 선택된 탭만 파란 밑줄 (라벨과 동일 정렬)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .background(PrimaryBlue)
            )
        } else {
            Spacer(Modifier.height(4.dp))
        }
    }
}

/* ===================== 카드 (상하 패딩 축소 + 띄워진 흰 카드) ===================== */

@Composable
private fun SupportCard(item: SupportItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .heightIn(min = 120.dp)                  // ⬆ 최소 높이 부여
            .padding(horizontal = 16.dp, vertical = 20.dp), // ⬆ 상하 패딩 증가
        verticalArrangement = Arrangement.spacedBy(12.dp)   // ⬆ 내부 간격 조금 늘림
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (item.readState == ReadState.Read) "열람" else "미열람",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.019).em,
                color = if (item.readState == ReadState.Read) PrimaryBlue else Color(0xFFF24822)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "${item.appliedAt} 지원",
                fontSize = 12.sp,
                color = Color(0xFF848484)
            )
            Spacer(Modifier.weight(1f))
            // ⬇ 가로 점 3개 아이콘으로 변경
            IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Outlined.MoreHoriz,
                    contentDescription = "더보기",
                    tint = Color(0xFF343330)
                )
            }
        }

        Text(
            text = item.company,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF848484)
        )

        Text(
            text = item.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF000000),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* ===================== Util ===================== */
private fun countText(count: Int): AnnotatedString = buildAnnotatedString {
    append("총 ")
    pushStyle(SpanStyle(color = PrimaryBlue))
    append("${count}건")
    pop()
    append(" 지원")
}
