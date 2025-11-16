package com.example.dodojob.ui.feature.support


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.ui.feature.support.MapCardData

import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dodojob.session.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.dodojob.dao.fetchSupportDataMerged
import com.example.dodojob.dao.SupportData
import com.example.dodojob.session.CurrentCompany
import com.example.dodojob.dao.fetchInterDataMerged
import com.example.dodojob.data.naver.rememberGeocodedLatLng
import com.naver.maps.geometry.LatLng
import com.example.dodojob.ui.components.DodoNaverMap
import com.example.dodojob.data.naver.rememberGeocodedLatLng
import com.example.dodojob.ui.components.DodoNaverMap
import com.naver.maps.map.CameraPosition
import java.time.format.DateTimeFormatter


/* ===================== 색상/타이포 공통 ===================== */
private val PrimaryBlue = Color(0xFF005FFF)
private val DangerRed   = Color(0xFFF24822)
private val Bg          = Color(0xFFF1F5F7)
private val Letter      = (-0.019f).em

/* ===================== 상태 Enum ===================== */
enum class ReadState { Read, Unread }
enum class ResultState { Pass, Fail }

/* ===================== 데이터 모델 ===================== */
data class AppliedItem(
    val id: String,
    val readState: ReadState,
    val appliedAt: String,
    val company: String,
    val title: String,

    val company_locate: String

)

data class InterviewItem(
    val id: String,
    val date: LocalDate,
    val company: String,
    val title: String,
    val address: String
)

data class ResultItem(
    val id: String,
    val appliedAt: String,
    val company: String,
    val title: String,
    val result: ResultState
)



private val DOT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd")
/* ===================== Fake DB ===================== */
private object SupportFakeDb {


/* ===================== Fake DB ===================== */
private object SupportFakeDb {
    fun applied(): List<AppliedItem> = listOf(
        AppliedItem("a1", ReadState.Read,   "2025.08.25", "모던하우스", "매장운영 및 고객관리 하는 일에 적합한 분 구해요"),
        AppliedItem("a2", ReadState.Unread, "2025.08.20", "대구동구 어린이도서관", "아이들 책 읽어주기, 독서 습관 형성 프로그램 지원"),
        AppliedItem("a3", ReadState.Unread, "2025.08.14", "수성구 체육센터", "회원 운동 지도 보조, 센터 관리 가능하신 분 지원 요망"),
        AppliedItem("a4", ReadState.Read,   "2025.08.10", "대구도시철도공사", "지하철 역사 안전 순찰, 이용객 안내, 분실물 관리"),
    )


    fun interviews(): List<InterviewItem> {
        val ws = weekStart(LocalDate.now())
        return listOf(
            InterviewItem("i1", ws.plusDays(1), "모던하우스",           "고객관리/매장운영 보조", "대구 수성구 용학로 118 1,2층(두산동) 모던하우스"),
            InterviewItem("i2", ws.plusDays(3), "수성구 체육센터",      "회원운동 지도 보조",      "대구 수성구 체육센터로 12"),
            InterviewItem("i3", ws.plusDays(3), "대구도시철도공사",     "역사 안전/안내",          "대구 도시철도 2호선 ○○역"),
            InterviewItem("i4", ws.plusDays(5), "대구동구 어린이도서관", "독서 프로그램 도우미",     "대구 동구 ○○로 123"),
        )
    }

    fun results(): List<ResultItem> = listOf(
        ResultItem("r1", "2025.08.22", "모던하우스",        "매장운영 및 고객관리", ResultState.Pass),
        ResultItem("r2", "2025.08.18", "수성구 체육센터",  "회원 운동 지도 보조",   ResultState.Fail),
        ResultItem("r3", "2025.08.12", "대구동구 어린이도서관", "독서 프로그램 지원", ResultState.Pass),
    )
}


data class SupportUiState(
    val applied: List<AppliedItem> = emptyList(),
    val interviews: List<InterviewItem> = emptyList(),
    val results: List<ResultItem> = emptyList(),
    val keyword: String = "",
    val selectedTab: Int = 0,
    val loading: Boolean = false,
    val error: String? = null
)

class SupportViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState

    fun SupportData.majorToJobSentence(): String {
        val m = major?.trim()?.replace("/", "·") ?: return "직무 정보 없음"

        return when {
            // 헬스/운동 계열
            m.contains("운동") || m.contains("체육") -> "$m 보조 및 센터 운영에 함께하실 분을 찾고 있어요"

            // 돌봄/케어 계열
            m.contains("돌봄") || m.contains("요양") || m.contains("케어") ->
                "$m 관련 업무를 성실히 도와주실 분을 모집합니다"

            // 매장/판매 계열
            m.contains("매장") || m.contains("고객") || m.contains("상품") ->
                "$m 업무에 함께하실 분을 구하고 있어요"

            // 사무 계열
            m.contains("사무") || m.contains("행정") ->
                "$m 관련 업무를 도와주실 분을 찾습니다"

            // 도서관/교육 계열
            m.contains("도서") || m.contains("교육") || m.contains("독서") ->
                "$m 활동에 관심 있는 분 환영합니다"

            // 예비 처리 (기본)
            else -> "$m 업무에 적합한 분을 모집합니다"
        }
    }

    fun load(username: String) {
        viewModelScope.launch {
            Log.d("SupportVM", "▶ load() called username=$username")
            _uiState.update { it.copy(loading = true, error = null) }

            runCatching {
                val supportList = fetchSupportDataMerged(username)
                val interviewList  = fetchInterDataMerged(username)

                Log.d("SupportVM", "✅ supportList size=${supportList.size}")
                supportList.forEachIndexed { idx, sd ->
                    Log.d("SupportVM", "item[$idx] = $sd")
                }


                val applied = supportList.map { sd ->
                    val title = sd.majorToJobSentence()
                    Log.d("SupportVM", "mapped item: id=${sd.announcement_id}, company=${sd.company_name}, title=$title, status=${sd.user_status}")

                    AppliedItem(
                        id = sd.announcement_id.toString(),
                        readState = if (sd.user_status.equals("unread", true)) {
                            ReadState.Unread
                        } else {
                            ReadState.Read
                        },
                        appliedAt = sd.applied_at.take(10).replace("-", "."),
                        company = sd.company_name.orEmpty(),
                        title = title,
                        company_locate = sd.company_locate.toString()
                    )
                }
                val interviews = interviewList.map{ iv->
                    val parsedDate = LocalDate.parse(iv.interview_date, DOT_DATE_FORMAT)
                    InterviewItem(
                        id = iv.announcement_id.toString(),
                        date = parsedDate,
                        company = iv.company_name,
                        title = iv.major,
                        address = iv.address
                    )

                }


                Triple(applied, interviews, emptyList<ResultItem>())
            }.onSuccess { (applied, interviews, results) ->
                Log.d("SupportVM", "✅ onSuccess applied.size=${applied.size}")
                _uiState.update {
                    it.copy(
                        applied = applied,
                        interviews = interviews,
                        results = results,
                        loading = false
                    )
                }
            }.onFailure { e ->
                Log.e("SupportVM", "❌ load() failed", e)
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun onKeywordChange(v: String) {
        _uiState.update { it.copy(keyword = v) }
    }

    fun onTabChange(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}


/* ===================== Route + Screen ===================== */
@Composable
fun SupportRoute(nav: NavController,
                 viewModel: SupportViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {

    val state by viewModel.uiState.collectAsState()

    // 화면 진입 시 한 번만 로드
    LaunchedEffect(Unit) {
        viewModel.load(username = CurrentUser.username.toString())  // TODO 실제 username 넣기
    }
    val appliedAll   = state.applied
    val interviewAll = state.interviews
    val resultAll    = state.results
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
            .background(Bg)
    ) {
        // 상단
        SupportTopSection(
            nav = nav,
            countText = countText(appliedAll.size),
            keyword = keyword,
            onKeywordChange = { keyword = it }
        )

        // 필터
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

        // 본문
        SupportBodySection(
            appliedItems   = appliedFiltered,
            interviewItems = interviewFiltered,
            resultItems    = resultFiltered,
            onShowMap      = { card ->
                nav.currentBackStackEntry?.savedStateHandle?.set("mapCard", card)
                nav.navigate("map")
            }
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
        // 상단 상태바 (24dp, 회색)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color(0xFFEFEFEF))
        )

        // 헤더 (ApplicationScreen 과 동일한 구조)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { nav.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "뒤로가기",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            Text(
                text = countText,
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = Letter,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 검색 박스 (Figma 스타일)
        val shape = RoundedCornerShape(10.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(57.dp)
                .clip(shape)
                .background(Bg)
                .border(1.dp, Color(0xFFC1D2ED), shape)
        ) {
            OutlinedTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                singleLine = true,
                trailingIcon = {
                    Image(
                        painter = painterResource(R.drawable.search),
                        contentDescription = "검색",
                        modifier = Modifier.size(24.dp)
                    )
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
    resultItems: List<ResultItem>,
    onShowMap: (MapCardData) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        // 탭바
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .padding(horizontal = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabLabel("지원완료", isSelected = selectedTab == 0) { selectedTab = 0 }
                TabLabel("면접예정", isSelected = selectedTab == 1) { selectedTab = 1 }
                TabLabel("합격결과", isSelected = selectedTab == 2) { selectedTab = 2 }
            }
        }

        when (selectedTab) {
            0 -> AppliedTab(appliedItems, onShowMap)
            1 -> InterviewWeeklyTab(interviewItems, onShowMap)
            2 -> ResultTab(resultItems, onShowMap)
        }
    }
}

/* 탭 라벨 + 밑줄 (Figma 스타일) */
@Composable
private fun TabLabel(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            lineHeight = 20.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = (-0.5f).sp,
            color = if (isSelected) PrimaryBlue else Color(0xFF000000)
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(70 .dp)
                .height(2.5.dp)
                .background(if (isSelected) PrimaryBlue else Color.Transparent)
        )
    }
}

/* ===================== 지원완료 탭 ===================== */
@Composable
private fun AppliedTab(items: List<AppliedItem>, onShowMap: (MapCardData) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg),
        contentPadding = PaddingValues(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(19.dp)
    ) {
        items(items, key = { it.id }) { item ->
            AppliedCard(item) { onShowMap(item.toMapCardData()) }
        }
    }
}

@Composable
private fun AppliedCard(item: AppliedItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { CurrentCompany.setCompanylocate(item.company_locate)   // ← 여기서 id 저장
                onClick()  }
            .padding(horizontal = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 27.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (item.readState == ReadState.Read) "열람" else "미열람",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = Letter,
                    color = if (item.readState == ReadState.Read) PrimaryBlue else DangerRed
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "${item.appliedAt} 지원",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = Letter,
                    color = Color(0xFF848484)
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Outlined.MoreHoriz,
                        contentDescription = "더보기",
                        tint = Color(0xFF343330)
                    )
                }
            }

            Text(
                text = item.company,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = Letter,
                color = Color(0xFF848484)
            )
            Text(
                text = item.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = Letter,
                color = Color(0xFF000000),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* ===================== 면접예정 탭 ===================== */
private val CAL_VERTICAL_GAP = 12.dp
private val CAL_HORIZ_GAP = 16.dp
private val CAL_SELECTED_SIZE = 40.dp

@Composable
private fun InterviewWeeklyTab(
    items: List<InterviewItem>,
    onShowMap: (MapCardData) -> Unit
) {
    var anchorDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedDate by remember { mutableStateOf(weekStart(anchorDate).plusDays(1)) }

    val weekStart = remember(anchorDate) { weekStart(anchorDate) }
    val weekDates = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }

    val itemsForSelectedDay = remember(selectedDate, items) {
        items.filter { it.date == selectedDate }.sortedBy { it.date }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
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
                        Text(
                            weekdays[idx],
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(CAL_VERTICAL_GAP))
                        Box(
                            modifier = Modifier
                                .size(CAL_SELECTED_SIZE)
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(Color(0xFFF1F5F7))
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg),
            contentPadding = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(19.dp)
        ) {
            items(itemsForSelectedDay, key = { it.id }) { item ->
                InterviewCard(item) { onShowMap(item.toMapCardData()) }
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
    val monthName = weekStart.month.getDisplayName(TextStyle.FULL, locale)
    val label = "$monthName"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.back),
            contentDescription = "이전 주",
            modifier = Modifier
                .size(24.dp)
                .clickable { onPrevWeek() }
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Spacer(Modifier.width(8.dp))

        Image(
            painter = painterResource(R.drawable.black_right),
            contentDescription = "다음 주",
            modifier = Modifier
                .size(24.dp)
                .clickable { onNextWeek() }
        )
    }
}


@Composable
private fun InterviewCard(item: InterviewItem, onClick: () -> Unit) {


    val mapCenter = rememberGeocodedLatLng(item.address)   // ← 주소 기반 좌표

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
    ) {
        /* ───── 상단 정보 영역 (Frame 1707480138) ───── */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 27.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Figma 에서 '열람' 자리 → 면접 날짜 + 텍스트
                Text(
                    text = "${item.date} 면접",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = Letter,
                    color = PrimaryBlue
                )

                Spacer(Modifier.weight(1f))

                // DotsThree 32 x 32
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { /* TODO: 메뉴 */ },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.three_dot),
                        contentDescription = "더보기",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = item.company,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = Letter,
                color = Color(0xFF8E8E8E)   // Figma: #8E8E8E
            )

            Text(
                text = item.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = Letter,
                color = Color(0xFF000000),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        /* ───── 하단 지도/주소/버튼 영역 (Frame 3469136) ───── */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            val mapHeight = 187.54.dp

            // 🔵 지도 영역
            if (mapCenter != null) {
                DodoNaverMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mapHeight)
                        .clip(RoundedCornerShape(10.dp)),
                    initialCameraPosition = CameraPosition(mapCenter!!, 16.0),
                    enableMyLocation = false,
                    markerPosition = mapCenter,
                    markerCaption = item.company   // or item.title
                )
            } else {
                // 지오코딩 전 / 실패시 플레이스홀더
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mapHeight)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEDEFF3)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("지도 영역 (주소 지오코딩 중)", color = Color(0xFF9C9C9C), fontSize = 13.sp)
                }
            }

            // 주소 텍스트

            // 스크린샷 자리 (지도 썸네일)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(187.54.dp) // Figma height
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEDEFF3))
            )

            // 주소 텍스트 (20sp, 30px line-height)

            Text(
                text = item.address,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = Letter,
                lineHeight = 30.sp,
                color = Color(0xFF000000),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
            )


            // 지도 보기 버튼

            // 지도 보기 버튼 (327.47 x 54.48 근사)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryBlue)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "지도 보기",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = Letter,
                    color = Color.White
                )
            }
        }
    }
}

/* ===================== 합격결과 탭 ===================== */
@Composable
private fun ResultTab(items: List<ResultItem>, onShowMap: (MapCardData) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg),
        contentPadding = PaddingValues(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(19.dp)
    ) {
        items(items, key = { it.id }) { item ->
            ResultCard(item) { onShowMap(item.toMapCardData()) }
        }
    }
}

@Composable
private fun ResultCard(item: ResultItem, onClick: () -> Unit) {
    val (label, color) = when (item.result) {
        ResultState.Pass -> "합격" to PrimaryBlue
        ResultState.Fail -> "불합격" to DangerRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 27.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = Letter,
                    color = color
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "${item.appliedAt} 지원",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = Letter,
                    color = Color(0xFF848484)
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { /* TODO */ },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.three_dot),
                        contentDescription = "더보기",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                item.company,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = Letter,
                color = Color(0xFF848484)
            )
            Text(
                text = item.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = Letter,
                color = Color(0xFF000000),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
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

private fun weekStart(date: LocalDate): LocalDate {
    val dow = date.dayOfWeek
    val shift = (dow.value % 7)
    return date.minusDays(shift.toLong())
}

/* ===================== MapCardData 매퍼 ===================== */

private fun AppliedItem.toMapCardData(): MapCardData {
    val badge = "지원"
    val highlight = if (readState == ReadState.Unread) "미열람" else "열람"
    return MapCardData(
        badgeText = badge,
        company = company,
        highlight = highlight,
        title = title,
        distanceText = "내 위치에서 214m",
        imageUrl = "https://your-image-url"   // 나중에 실제 이미지 주소
        distanceText = "내 위치에서 214m"

    )
}

private fun InterviewItem.toMapCardData(): MapCardData {
    val today = LocalDate.now()
    val d = ChronoUnit.DAYS.between(today, date).toInt()
    val badge = if (d >= 0) "D-$d" else "D+${-d}"
    return MapCardData(
        badgeText = badge,
        company = company,
        highlight = "면접예정",
        title = title,
        distanceText = "내 위치에서 214m",
        imageUrl = "https://your-image-url"   // 나중에 실제 이미지 주소
        distanceText = "내 위치에서 214m"

    )
}

private fun ResultItem.toMapCardData(): MapCardData {
    val badge = when (result) {
        ResultState.Pass -> "합격"
        ResultState.Fail -> "불합격"
    }
    val highlight = "${appliedAt} 지원"
    return MapCardData(
        badgeText = badge,
        company = company,
        highlight = highlight,
        title = title,
        distanceText = "내 위치에서 214m"
    )
}
