package com.example.dodojob.ui.feature.support

import android.graphics.Color.BLACK
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/* ===================== 색상 ===================== */
private val PrimaryBlue = Color(0xFF005FFF)
private val DangerRed   = Color(0xFFF24822)
private val Bg          = Color(0xFFF1F5F7)

/* ===================== 상태 Enum ===================== */
enum class ReadState { Read, Unread }
enum class ResultState { Pass, Fail }

/* ===================== 데이터 모델 ===================== */
// 지원완료 탭 (열람유무 포함)
data class AppliedItem(
    val id: String,
    val readState: ReadState,  // 열람/미열람
    val appliedAt: String,     // e.g., 2025.08.25
    val company: String,
    val title: String,
)

// 면접예정 탭
data class InterviewItem(
    val id: String,
    val date: LocalDate,    // 면접 날짜
    val company: String,
    val title: String,
    val address: String
)

// 합격유무 탭 (지원날짜 포함)
data class ResultItem(
    val id: String,
    val appliedAt: String,     // 지원날짜
    val company: String,
    val title: String,
    val result: ResultState
)

/* ===================== Fake DB ===================== */
private object SupportFakeDb {
    fun applied(): List<AppliedItem> = listOf(
        AppliedItem("a1", ReadState.Read,   "2025.08.25", "모던하우스", "매장운영 및 고객관리 하는 일에 적합한 분 구해요"),
        AppliedItem("a2", ReadState.Unread, "2025.08.20", "대구동구 어린이도서관", "아이들 책 읽어주기, 독서 습관 형성 프로그램 지원"),
        AppliedItem("a3", ReadState.Unread, "2025.08.14", "수성구 체육센터", "회원 운동 지도 보조, 센터 관리 가능하신 분 지원 요망"),
        AppliedItem("a4", ReadState.Read,   "2025.08.10", "대구도시철도공사", "지하철 역사 안전 순찰, 이용객 안내, 분실물 관리"),
    )

    fun interviews(): List<InterviewItem> {
        val ws = weekStart(LocalDate.now()) // 이번 주 일요일
        return listOf(
            InterviewItem("i1", ws.plusDays(1), "모던하우스",      "고객관리/매장운영 보조", "대구 수성구 용학로 118 1,2층(두산동) 모던하우스"),
            InterviewItem("i2", ws.plusDays(3), "수성구 체육센터", "회원운동 지도 보조",     "대구 수성구 체육센터로 12"),
            InterviewItem("i3", ws.plusDays(3), "대구도시철도공사","역사 안전/안내",         "대구 도시철도 2호선 ○○역"),
            InterviewItem("i4", ws.plusDays(5), "대구동구 어린이도서관", "독서 프로그램 도우미", "대구 동구 ○○로 123"),
        )
    }

    fun results(): List<ResultItem> = listOf(
        ResultItem("r1", "2025.08.22", "모던하우스",        "매장운영 및 고객관리", ResultState.Pass),
        ResultItem("r2", "2025.08.18", "수성구 체육센터",  "회원 운동 지도 보조",   ResultState.Fail),
        ResultItem("r3", "2025.08.12", "대구동구 어린이도서관", "독서 프로그램 지원", ResultState.Pass),
    )
}

/* ===================== Route + Screen ===================== */
@Composable
fun SupportRoute(nav: NavController) {
    val appliedAll   = remember { SupportFakeDb.applied() }
    val interviewAll = remember { SupportFakeDb.interviews() }
    val resultAll    = remember { SupportFakeDb.results() }

    var keyword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단
        SupportTopSection(
            nav = nav,
            countText = countText(appliedAll.size),
            keyword = keyword,
            onKeywordChange = { keyword = it }
        )

        // 간단 필터
        val appliedFiltered = remember(keyword, appliedAll) {
            if (keyword.isBlank()) appliedAll
            else appliedAll.filter { it.company.contains(keyword, true) || it.title.contains(keyword, true) }
        }
        val interviewFiltered = remember(keyword, interviewAll) {
            if (keyword.isBlank()) interviewAll
            else interviewAll.filter { it.company.contains(keyword, true) || it.title.contains(keyword, true) }
        }
        val resultFiltered = remember(keyword, resultAll) {
            if (keyword.isBlank()) resultAll
            else resultAll.filter { it.company.contains(keyword, true) || it.title.contains(keyword, true) }
        }

        SupportBodySection(
            appliedItems = appliedFiltered,
            interviewItems = interviewFiltered,
            resultItems = resultFiltered
        )
    }
}

/* ===================== 상단 섹션 ===================== */
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
            .heightIn(min = 247.dp)
            .background(Color.White)
            .padding(bottom = 20.dp)
    ) {
        // 상태바 대체
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color(0xFFEFEFEF))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(start = 16.dp, top = 20.dp)
        ) {
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

        // 검색 박스
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

/* ===================== 본문 섹션 ===================== */
@Composable
private fun SupportBodySection(
    appliedItems: List<AppliedItem>,
    interviewItems: List<InterviewItem>,
    resultItems: List<ResultItem>
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: 지원완료, 1: 면접예정, 2: 합격유무

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)   // ✅ 세 탭 공통: 회색 배경
    ) {
        // 탭바
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
                TabLabel("합격유무", isSelected = selectedTab == 2) { selectedTab = 2 }
            }
        }

        when (selectedTab) {
            0 -> AppliedTab(appliedItems)
            1 -> InterviewWeeklyTab(interviewItems) // ✅ 내부는 흰 컨테이너 + 카드, 전체는 회색
            2 -> ResultTab(resultItems)             // ✅ 카드 UI 적용
        }
    }
}

/* 탭 라벨 + 밑줄 */
@Composable
private fun TabLabel(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
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

/* ===================== 지원완료 탭: 열람유무 표시 ===================== */
@Composable
private fun AppliedTab(items: List<AppliedItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { item ->
            AppliedCard(item)
        }
    }
}

@Composable
private fun AppliedCard(item: AppliedItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .heightIn(min = 120.dp)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 열람/미열람
            Text(
                text = if (item.readState == ReadState.Read) "열람" else "미열람",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.019).em,
                color = if (item.readState == ReadState.Read) PrimaryBlue else DangerRed
            )
            Spacer(Modifier.width(8.dp))
            Text(text = "${item.appliedAt} 지원", fontSize = 12.sp, color = Color(0xFF848484))
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Outlined.MoreHoriz, contentDescription = "더보기", tint = Color(0xFF343330))
            }
        }

        Text(text = item.company, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF848484))
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

/* ===================== 면접예정 탭: 주간 달력(선택일만 리스트) ===================== */

private val CAL_VERTICAL_GAP = 12.dp
private val CAL_HORIZ_GAP = 16.dp
private val CAL_SELECTED_SIZE = 40.dp
@Composable
private fun InterviewWeeklyTab(items: List<InterviewItem>) {
    var anchorDate by remember { mutableStateOf(LocalDate.now()) } // 주 기준
    var selectedDate by remember { mutableStateOf(weekStart(anchorDate).plusDays(1)) } // 기본 월요일 등

    val weekStart = remember(anchorDate) { weekStart(anchorDate) } // 일요일
    val weekDates = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }

    val itemsForSelectedDay = remember(selectedDate, items) {
        items.filter { it.date == selectedDate }.sortedBy { it.date }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg) // ✅ 전체 회색 (지원완료와 동일)
    ) {
        // 상단: 흰색 컨테이너로 헤더+요일줄
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(CAL_VERTICAL_GAP)
        ) {
            WeeklyHeader(
                weekStart = weekStart,
                onPrevWeek = {
                    anchorDate = anchorDate.minusWeeks(1)
                    selectedDate = weekStart(anchorDate).plusDays(1)
                },
                onNextWeek = {
                    anchorDate = anchorDate.plusWeeks(1)
                    selectedDate = weekStart(anchorDate).plusDays(1)
                }
            )

            // 요일/날짜 줄 (선택 날짜는 파란 "정사각형" 라운드 사각형)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(CAL_HORIZ_GAP, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val weekdays = listOf("일", "월", "화", "수", "목", "금", "토")
                weekDates.forEachIndexed { idx, date ->
                    val isSel = date == selectedDate
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(weekdays[idx], fontSize = 14.sp, color = Color.Black)
                        Spacer(Modifier.height(CAL_VERTICAL_GAP))
                        Box(
                            modifier = Modifier
                                .size(CAL_SELECTED_SIZE) // ✅ 정사각형
                                .background(
                                    if (isSel) PrimaryBlue else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedDate = date },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                fontSize = 16.sp,
                                color = if (isSel) Color.White else Color.Black
                            )
                        }
                        Spacer(Modifier.height(CAL_VERTICAL_GAP))
                    }
                }
            }
        }

        // 선택된 날짜의 면접만 카드로
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(itemsForSelectedDay, key = { it.id }) { item ->
                InterviewCard(item)
            }
        }
    }
}

@Composable
private fun WeeklyHeader(
    weekStart: LocalDate,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val locale = Locale.KOREAN
    val monthName = weekStart.month.getDisplayName(TextStyle.FULL, locale) // 예: "9월"
    val label = "$monthName"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("‹", color = Color(0xFFBDBDBD), fontSize = 32.sp,
            modifier = Modifier
                .padding(end = 12.dp)
                .clickable { onPrevWeek() })
        Text(label, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        Text("›", color = Color(0xFF000000), fontSize = 32.sp,
            modifier = Modifier
                .padding(start = 12.dp)
                .clickable { onNextWeek() })
    }

}

/* 면접 카드: 면접 날짜 + 주소 + 지도 자리 + 버튼 */
@Composable
private fun InterviewCard(item: InterviewItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 면접 날짜
            Text(
                text = "${item.date}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.019).em,
                color = PrimaryBlue
            )
            Text(
                text = " 면접",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.019).em,
                color = Color(BLACK)
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Outlined.MoreHoriz, contentDescription = "더보기", tint = Color(0xFF343330))
            }
        }

        Text(text = item.company, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF848484))
        Text(
            text = item.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF000000),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // 주소
        Text(
            text = item.address,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A1A)
        )

        // 지도 API 영역 (플레이스홀더)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEDEFF3))
        ) {
            // TODO: 지도 API(WebView/지도 컴포넌트) 삽입 예정
        }

        // 버튼 예시
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .border(1.dp, PrimaryBlue, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) { Text("지도", fontSize = 16.sp, fontWeight = FontWeight.Medium) }

            Box(
                modifier = Modifier
                    .weight(2f)
                    .height(44.dp)
                    .background(PrimaryBlue, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) { Text("면접 준비", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White) }
        }
    }
}

/* ===================== 합격유무 탭: 카드 UI + 지원날짜 표기 ===================== */
@Composable
private fun ResultTab(items: List<ResultItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg), // ✅ 지원완료와 동일한 회색 배경
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { item ->
            ResultCard(item)
        }
    }
}

@Composable
private fun ResultCard(item: ResultItem) {
    val (label, color) = when (item.result) {
        ResultState.Pass -> "합격" to PrimaryBlue
        ResultState.Fail -> "불합격" to DangerRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White) // ✅ 흰 카드
            .heightIn(min = 120.dp)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = color)
            Spacer(Modifier.width(7.dp))
            // ✅ 지원날짜 표기
            Text(text = "${item.appliedAt} 지원", fontSize = 13.sp, color = Color(0xFF848484))
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Outlined.MoreHoriz, contentDescription = "더보기", tint = Color(0xFF343330))
            }
        }

        Text(text = item.company, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF848484))
        Text(
            text = item.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF000000),
            maxLines = 2,
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

/* 주 시작(일요일) 계산 */
private fun weekStart(date: LocalDate): LocalDate {
    val dow = date.dayOfWeek
    val shift = (dow.value % 7) // Mon(1)…Sun(7)-> 0..6로 보정 → 일요일로 이동
    return date.minusDays(shift.toLong())
}
