@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dodojob.ui.feature.hope

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import com.example.dodojob.session.CurrentUser
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.data.jobtype.JobTypeRepository
import com.example.dodojob.data.jobtype.JobTypeRepositorySupabase
import com.example.dodojob.data.jobtype.JobtypeDto
import com.example.dodojob.data.senior.SeniorRepository
import com.example.dodojob.data.senior.SeniorRepositorySupabase
import com.example.dodojob.ui.feature.prefer.PreferWorkSheetBottomSheet
import com.example.dodojob.ui.feature.prefer.RegionPickerBottomSheet
import kotlinx.coroutines.launch

/* --- 요일/칩 동기화 유틸 --- */
private fun bitOf(opt: String): Int = when (opt) {
    "월" -> 0; "화" -> 1; "수" -> 2; "목" -> 3; "금" -> 4; "토" -> 5; "일" -> 6
    else -> -1
}
private fun setWeekdays(on: Boolean, currentMask: Int): Int {
    val bits = (1 shl 0) or (1 shl 1) or (1 shl 2) or (1 shl 3) or (1 shl 4)
    return if (on) currentMask or bits else currentMask and bits.inv()
}
private fun setWeekend(on: Boolean, currentMask: Int): Int {
    val bits = (1 shl 5) or (1 shl 6)
    return if (on) currentMask or bits else currentMask and bits.inv()
}
private fun recomputeChipsFromMask(mask: Int): Pair<Boolean, Boolean> {
    val wdBits = 0b00011111; val weBits = 0b01100000
    return ((mask and wdBits) == wdBits) to ((mask and weBits) == weBits)
}
private fun maskToString(mask: Int): String =
    (0..6).joinToString("") { if (((mask shr it) and 1) == 1) "1" else "0" }

/* ------------------------ 화면 ------------------------- */
@Composable
fun HopeWorkFilterScreen(nav: NavController) {
    val client = LocalSupabase.current
    val repo: JobTypeRepository = remember(client) { JobTypeRepositorySupabase(client) }
    val seniorrepo = remember(client) {SeniorRepositorySupabase(client)}

    val scope = rememberCoroutineScope()

    val prev = nav.previousBackStackEntry?.savedStateHandle
    val talentBits  = prev?.get<String>("sheet_talent_bits")  ?: ""
    val serviceBits = prev?.get<String>("sheet_service_bits") ?: ""
    val manageBits  = prev?.get<String>("sheet_manage_bits")  ?: ""
    val careBits    = prev?.get<String>("sheet_care_bits")    ?: ""

    var region by rememberSaveable { mutableStateOf<String?>(CurrentUser.locate) }
    var jobSelections by rememberSaveable { mutableStateOf<Set<String>>(emptySet()) }
    var showJobSheet by remember { mutableStateOf(false) }
    var period by remember { mutableStateOf<String?>(null) }
    var weekdaysChip by remember { mutableStateOf(false) }
    var weekendChip  by remember { mutableStateOf(false) }
    var dayMask      by remember { mutableStateOf(0) }
    var timeOfDay    by remember { mutableStateOf<String?>(null) }

    // 바텀시트(지역 선택) 온/오프
    var showRegionSheet by remember { mutableStateOf(false) }

    // 서브화면 복귀값 반영 (기존 Prefer 화면을 아직 쓰는 곳이 있으면 유지)
    LaunchedEffect(Unit) {
        val handle = nav.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.get<String>("pickedRegion")?.let {
            region = it
            handle.remove<String>("pickedRegion")
        }
        handle.get<Any>("pickedJobs")?.let { raw ->
            val list = when (raw) {
                is ArrayList<*> -> raw.filterIsInstance<String>()
                is List<*>      -> raw.filterIsInstance<String>()
                is Set<*>       -> raw.filterIsInstance<String>()
                else -> emptyList()
            }
            jobSelections = list.toSet()
            handle.remove<Any>("pickedJobs")
        }
    }

    val periodOptions = listOf("1일", "1주일 이하", "1주일~1개월", "6개월~1년", "1년 이상")
    val dayOptions = listOf("평일", "주말", "월", "화", "수", "목", "금", "토", "일")
    val scroll = rememberScrollState()

    val Bg = Color(0xFFF1F5F7)
    val BrandBlue = Color(0xFF005FFF)
    val letter = (-0.019f).em




    Scaffold(
        containerColor = Bg,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color(0xFFEFEFEF))
            )
        },
        bottomBar = {
            val canApply = region != null ||
                    jobSelections.isNotEmpty() ||
                    period != null ||
                    dayMask != 0 || weekdaysChip || weekendChip ||
                    timeOfDay != null

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 초기화
                Button(
                    onClick = {
                        region = null
                        jobSelections = emptySet()
                        period = null
                        weekdaysChip = false
                        weekendChip = false
                        dayMask = 0
                        timeOfDay = null
                        nav.currentBackStackEntry?.savedStateHandle?.apply {
                            set("pickedRegion", "")
                            set("pickedJobs", arrayListOf<String>())
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color(0xFF005FFF))
                ) {
                    Text(
                        "초기화",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.019f).em,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }

                // 적용하기
                Button(
                    onClick = {
                        val username = CurrentUser.username
                        val timeFlag = timeOfDay != null
                        val weekString = maskToString(dayMask)

                        scope.launch {
                            runCatching {
                                val dto = JobtypeDto(
                                    id          = username,
                                    jobtype     = CurrentUser.jobtype,
                                    locate      = CurrentUser.locate,
                                    job_talent  = talentBits,
                                    job_manage  = manageBits,
                                    job_service = serviceBits,
                                    job_care    = careBits,
                                    term        = period,
                                    days        = weekdaysChip,
                                    weekend     = weekendChip,
                                    week        = weekString,
                                    time        = timeFlag
                                )
                                repo.insertJobtype(dto)
                                dto
                                seniorrepo.upsertSenior(username.toString())

                            }.onSuccess {
                                nav.navigate(Route.Experience.path)
                            }.onFailure {
                                android.util.Log.e("HopeWorkFilter", "insert failed", it)
                            }
                        }
                    },
                    enabled = canApply,
                    modifier = Modifier
                        .weight(2f)
                        .height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canApply) BrandBlue else Color(0xFFBFC6D2),
                        disabledContainerColor = Color(0xFFBFC6D2)
                    )
                ) {
                    Text("적용하기", fontSize = 24.sp, fontWeight = FontWeight.Medium, letterSpacing = letter, color = Color.White)
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg)
                .verticalScroll(scroll)
                .padding(inner)
        ) {
            // 상단 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Bg)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 24.dp, start = 6.dp)
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
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "나에게 맞게\n입력해주세요 :)",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = letter,
                color = Color.Black,
                lineHeight = 45.sp,
                modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
            )
            Spacer(Modifier.height(40.dp))

            // ───────── 근무할 지역 ─────────
            SectionLabel("근무할 지역", 26.sp, modifier = Modifier.padding(start = 16.dp))
            Spacer(Modifier.height(20.dp))
            FieldBox(
                text = region ?: "지역, 동네를 선택해주세요",
                hintColor = if (region == null) Color(0xFF727272) else Color(0xFF111111),
                height = 57.dp,
                radius = 10.dp,
                onClick = { showRegionSheet = true },   // ← 모달 오픈
                trailingIconRes = R.drawable.right_back,
                contentStartPadding = 12.dp
            )

            Spacer(Modifier.height(26.dp))

            // ───────── 직종 선택 ─────────
            SectionLabel("직종 선택", 26.sp, modifier = Modifier.padding(start = 16.dp))
            Spacer(Modifier.height(20.dp))
            val jobSummary = when {
                jobSelections.isEmpty() -> "직종을 선택해주세요"
                jobSelections.size <= 2 -> jobSelections.joinToString(" · ")
                else -> jobSelections.first() + " 외 ${jobSelections.size - 1}개"
            }
            FieldBox(
                text = jobSummary,
                hintColor = if (jobSelections.isEmpty()) Color(0xFF727272) else Color(0xFF111111),
                height = 57.dp,
                radius = 10.dp,
                onClick = { showJobSheet = true }, // 기존 직종 선택 화면 사용
                trailingIconRes = R.drawable.right_back,
                contentStartPadding = 12.dp
            )

            Spacer(Modifier.height(34.dp))

            // 구분선
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = Color(0xFFCFCFCF)
            )

            Spacer(Modifier.height(24.dp))

            // ───────── 기간 ─────────
            SectionLabel("얼마나 일하실 건가요?", 26.sp, modifier = Modifier.padding(start = 16.dp))
            Spacer(Modifier.height(18.dp))
            TwoColumnChips(
                options = listOf("1일", "1주일 이하", "1주일~1개월", "6개월~1년", "1년 이상"),
                isSelected = { it == period },
                onClick = { opt -> period = if (period == opt) null else opt },
                itemHeight = 63.96.dp,
                radius = 10.dp,
                textSize = 24.sp,
                startPadding = 16.dp,
                endPadding = 16.dp
            )
            Spacer(Modifier.height(40.dp))

            // ───────── 요일 ─────────
            SectionLabel("일할 수 있는 요일을\n선택해주세요", 26.sp, modifier = Modifier.padding(start = 16.dp))
            Spacer(Modifier.height(18.dp))
            TwoColumnChips(
                options = listOf("평일", "주말", "월", "화", "수", "목", "금", "토", "일"),
                isSelected = { opt ->
                    when (opt) {
                        "평일" -> weekdaysChip
                        "주말" -> weekendChip
                        else -> {
                            val bit = bitOf(opt)
                            bit >= 0 && (dayMask and (1 shl bit)) != 0
                        }
                    }
                },
                onClick = { opt ->
                    when (opt) {
                        "평일" -> { weekdaysChip = !weekdaysChip; dayMask = setWeekdays(weekdaysChip, dayMask) }
                        "주말" -> { weekendChip = !weekendChip; dayMask = setWeekend(weekendChip, dayMask) }
                        else -> {
                            val bit = bitOf(opt)
                            if (bit >= 0) {
                                dayMask = dayMask xor (1 shl bit)
                                val (wd, we) = recomputeChipsFromMask(dayMask)
                                weekdaysChip = wd; weekendChip = we
                            }
                        }
                    }
                },
                itemHeight = 63.96.dp,
                radius = 10.dp,
                textSize = 24.sp,
                startPadding = 16.dp,
                endPadding = 16.dp
            )
            Spacer(Modifier.height(40.dp))

            // ───────── 시간 ─────────
            SectionLabel("일하실 시간을 선택해주세요", 26.sp, modifier = Modifier.padding(start = 16.dp))
            Spacer(Modifier.height(18.dp))
            TwoColumnChips(
                options = listOf("오전", "오후"),
                isSelected = { it == timeOfDay },
                onClick = { opt -> timeOfDay = if (timeOfDay == opt) null else opt },
                itemHeight = 63.96.dp,
                radius = 10.dp,
                textSize = 24.sp,
                startPadding = 16.dp,
                endPadding = 16.dp
            )
            Spacer(Modifier.height(40.dp))
        }
    }

    // ===== Hope 화면 위에 뜨는 지역선택 바텀시트 =====
    if (showRegionSheet) {
        RegionPickerBottomSheet(
            onApply = { picked, radiusM ->
                region = picked
                CurrentUser.setLocate(locate = picked, radius = radiusM)
                nav.currentBackStackEntry?.savedStateHandle?.apply {
                    set("pickedRegion", picked)
                    set("searchRadiusM", radiusM)
                }
                showRegionSheet = false
            },
            onDismiss = { showRegionSheet = false }
        )
    }

    if (showJobSheet) {
        PreferWorkSheetBottomSheet(
            // ✅ 선택한 라벨 목록 + 각 비트 문자열 + healthy 콜백으로 받음
            onApply = { selectedLabels, talentBits, serviceBits, manageBits, careBits, healthy ->
                jobSelections = selectedLabels.toSet()

                // (선택) 다음 화면/서버 저장 시 재사용하려면 savedStateHandle에도 싣기
                nav.currentBackStackEntry?.savedStateHandle?.apply {
                    set("pickedJobs", ArrayList(selectedLabels))
                    set("sheet_talent_bits",  talentBits)
                    set("sheet_service_bits", serviceBits)
                    set("sheet_manage_bits",  manageBits)
                    set("sheet_care_bits",    careBits)
                    set("sheet_healthy", healthy)
                }

                showJobSheet = false
            },
            onDismiss = { showJobSheet = false }
        )
    }
}

/* ───────── 공통 UI ───────── */
@Composable
private fun SectionLabel(text: String, size: TextUnit, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize = size,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        lineHeight = size * 1.4f,
        letterSpacing = (-0.019f).em,
        modifier = modifier
    )
}

@Composable
private fun FieldBox(
    text: String,
    hintColor: Color,
    height: Dp,
    radius: Dp,
    onClick: () -> Unit,
    trailingIconRes: Int? = null,
    contentStartPadding: Dp = 0.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(height)
            .clip(RoundedCornerShape(radius))
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(radius))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text,
                color = hintColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.019f).em,
                modifier = Modifier.padding(start = contentStartPadding)
            )
            if (trailingIconRes != null) {
                Image(
                    painter = painterResource(trailingIconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun TwoColumnChips(
    options: List<String>,
    isSelected: (String) -> Boolean,
    onClick: (String) -> Unit,
    itemHeight: Dp,
    radius: Dp,
    textSize: TextUnit,
    startPadding: Dp = 0.dp,
    endPadding: Dp = 0.dp
) {
    val rows = options.chunked(2)
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(start = startPadding, end = endPadding)
    ) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
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
    val Brand = Color(0xFF005FFF)
    val SelectedBg = Color(0xFFDEEBFF)
    val UnselectedBorder = Color(0xFFE0E0E0)

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(radius))
            .background(if (selected) SelectedBg else Color.White)
            .border(
                width = 1.dp,
                color = if (selected) Brand else UnselectedBorder,
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
            color = if (selected) Brand else Color.Black,
            letterSpacing = (-0.019f).em
        )
    }
}
