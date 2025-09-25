// com/example/dodojob/ui/feature/hope/HopeWorkFilterScreen.kt
package com.example.dodojob.ui.feature.hope

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dodojob.navigation.Route
import com.example.dodojob.util.Bits
import com.example.dodojob.data.jobtype.JobTypeRow
import com.example.dodojob.data.jobtype.JobTypeRepository
import com.example.dodojob.session.CurrentUser
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.data.jobtype.JobTypeRepositorySupabase
import com.example.dodojob.data.jobtype.JobtypeDto
import com.example.dodojob.data.user.UserDto
import kotlinx.coroutines.launch
import java.util.UUID

/* --- 요일/칩 동기화 유틸 --- */

// "월,화,..." → bit index
private fun bitOf(opt: String): Int = when (opt) {
    "월" -> 0; "화" -> 1; "수" -> 2; "목" -> 3; "금" -> 4; "토" -> 5; "일" -> 6
    else -> -1
}

// 월~금 일괄 on/off
private fun setWeekdays(on: Boolean, currentMask: Int): Int {
    val weekdaysBits = (1 shl 0) or (1 shl 1) or (1 shl 2) or (1 shl 3) or (1 shl 4)
    return if (on) (currentMask or weekdaysBits) else (currentMask and weekdaysBits.inv())
}

// 토/일 일괄 on/off
private fun setWeekend(on: Boolean, currentMask: Int): Int {
    val weekendBits = (1 shl 5) or (1 shl 6)
    return if (on) (currentMask or weekendBits) else (currentMask and weekendBits.inv())
}

// dayMask에서 평일/주말 칩 상태를 재계산
private fun recomputeChipsFromMask(mask: Int): Pair<Boolean, Boolean> {
    val weekdaysBits = 0b00011111 // 월~금
    val weekendBits  = 0b01100000 // 토~일
    val weekdaysOn = (mask and weekdaysBits) == weekdaysBits
    val weekendOn  = (mask and weekendBits)  == weekendBits
    return weekdaysOn to weekendOn
}

// 저장용 "1110000" 문자열
private fun maskToString(mask: Int): String =
    (0..6).joinToString("") { if (((mask shr it) and 1) == 1) "1" else "0" }


/* ------------------------ 화면 컴포저블 ------------------------- */
@Composable
fun HopeWorkFilterScreen(nav: NavController) {

    val client = LocalSupabase.current
    val repo: JobTypeRepository = remember(client) { JobTypeRepositorySupabase(client) }

    val scope = rememberCoroutineScope()

    val prev = nav.previousBackStackEntry?.savedStateHandle

    val talentBits  = prev?.get<String>("sheet_talent_bits")  ?: ""
    val serviceBits = prev?.get<String>("sheet_service_bits") ?: ""
    val manageBits  = prev?.get<String>("sheet_manage_bits")  ?: ""
    val careBits    = prev?.get<String>("sheet_care_bits")    ?: ""
    val healthy     = prev?.get<Boolean>("sheet_healthy") ?: false
    // ----- 내부 상태 -----
    var region by rememberSaveable { mutableStateOf(CurrentUser.locate) }
    var jobSelections by rememberSaveable { mutableStateOf<Set<String>>(emptySet()) } // 별도 설계 전 임시 유지
    var period by remember { mutableStateOf<String?>(null) }

    var weekdaysChip by remember { mutableStateOf(false) } // 평일 버튼(dyas)
    var weekendChip  by remember { mutableStateOf(false) } // 주말 버튼(weekend)
    var dayMask      by remember { mutableStateOf(0) }     // 개별 요일 비트(week)
    var timeOfDay    by remember { mutableStateOf<String?>(null) }

    // 🔁 서브화면에서 돌아온 값 수신 (savedStateHandle)

    LaunchedEffect(Unit) {
        val handle = nav.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect

        // 지역
        handle.get<String>("pickedRegion")?.let { picked ->
            region = picked
            handle.remove<String>("pickedRegion")
        }

        // 직종 - 어떤 타입으로 들어있든 방어적으로 읽기
        handle.get<Any>("pickedJobs")?.let { raw ->
            val pickedList: List<String> = when (raw) {
                is ArrayList<*> -> raw.filterIsInstance<String>()
                is List<*>      -> raw.filterIsInstance<String>()
                is Set<*>       -> raw.filterIsInstance<String>()
                else            -> emptyList()
            }
            jobSelections = pickedList.toSet()
            // ✅ 읽었으면 타입 혼선 방지를 위해 키를 지워둡니다
            handle.remove<Any>("pickedJobs")
        }
    }

    val periodOptions = listOf("1일", "1주일 이하", "1주일~1개월", "6개월~1년", "1년 이상")
    val dayOptions = listOf("평일", "주말", "월", "화", "수", "목", "금", "토", "일")

    val scroll = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        region = null
                        jobSelections = emptySet()
                        period = null
                        weekdaysChip = false
                        weekendChip = false
                        dayMask = 0
                        timeOfDay = null
                        // savedStateHandle 초기화(선택)
                        nav.currentBackStackEntry?.savedStateHandle?.apply {
                            set("pickedRegion", "")
                            set("pickedJobs", arrayListOf<String>())
                        }
                    },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDADADA))
                ) { Text("초기화", fontSize = 20.sp, color = Color.Black) }

                Button(
                    onClick = {
                        // if (!canApply) return@Button

                        val username = CurrentUser.username
                        val timeFlag = timeOfDay != null

                        // TODO: 직종/기간 비트 문자열 설계 후 교체
                        val jobTalentBin  = talentBits
                        val jobManageBin  = manageBits
                        val jobServiceBin = serviceBits
                        val jobCareBin    = careBits
                        val termBin       = true

                        val weekString = maskToString(dayMask)

                        scope.launch {
                            runCatching {
                                val toSave = JobtypeDto(
                                    id      = username,
                                    jobtype = "0", // job 이동하는거 생성시 수정
                                    locate        = "태구", // ← 개별 요일 비트
                                    job_talent = jobTalentBin,
                                    job_manage = jobManageBin,
                                    job_service = jobServiceBin,
                                    job_care =  jobCareBin,
                                    term = period,
                                    days = weekdaysChip,
                                    weekend    = weekendChip,
                                    week = weekString, // 요일 비트 변환
                                    time = timeFlag //
                                )
                                repo.insertJobtype(toSave)
                                toSave
                            }.onSuccess {
                                nav.navigate(Route.Experience.path)
                            }.onFailure {
                                android.util.Log.e("HopeWorkFilter", "insert failed", it)
                                // TODO: 에러 UI 처리(it.message)
                            }

                        }
                    },
                    enabled = canApply,
                    modifier = Modifier.weight(2f).height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canApply) Color(0xFF005FFF) else Color(0xFFBFC6D2),
                        disabledContainerColor = Color(0xFFBFC6D2)
                    )
                ) { Text("적용하기", fontSize = 20.sp, color = Color.White) }
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

            fun clamp(dp: Dp, min: Dp, max: Dp) = dp.coerceIn(min, max)
            val hPad = clamp(W * 0.045f, 12.dp, 20.dp)
            val vPad = clamp(H * 0.02f, 8.dp, 20.dp)

            val titleSp = (W.value * 0.09f).sp
            val titleLH = (W.value * 0.12f).sp
            val labelSp = (W.value * 0.07f).sp
            val chipSp = (W.value * 0.055f).sp

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
                Text(
                    "<",
                    fontSize = (W.value * 0.07f).sp,
                    color = Color.Black,
                    modifier = Modifier
                        .clickable { nav.popBackStack() }
                        .padding(bottom = 16.dp)
                )

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
                    hintColor = if (region == null) Color(0xFF727272) else Color(0xFF111111),
                    height = fieldH,
                    radius = fieldRadius,
                    onClick = { nav.navigate(Route.PreferMap.path) } // ✅ 지역 선택 화면 이동
                )

                Spacer(Modifier.height(blockGap))

                // ───────── 직종 선택 ─────────
                SectionLabel("직종 선택", labelSp)
                val jobSummary = when {
                    jobSelections.isEmpty() -> "직종을 선택해주세요                                  >"
                    jobSelections.size <= 2 -> jobSelections.joinToString(" · ")
                    else -> jobSelections.first() + " 외 ${jobSelections.size - 1}개"
                }
                FieldBox(
                    text = jobSummary,
                    hintColor = if (jobSelections.isEmpty()) Color(0xFF727272) else Color(0xFF111111),
                    height = fieldH,
                    radius = fieldRadius,
                    onClick = { nav.navigate(Route.Prefer.path) } // ✅ 직종 선택 화면 이동
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
                    isSelected = { opt ->
                        when (opt) {
                            "평일" -> weekdaysChip
                            "주말" -> weekendChip
                            else   -> {
                                val bit = bitOf(opt)
                                bit >= 0 && (dayMask and (1 shl bit)) != 0
                            }
                        }
                    },
                    onClick = { opt ->
                        when (opt) {
                            "평일" -> {
                                weekdaysChip = !weekdaysChip
                                dayMask = setWeekdays(weekdaysChip, dayMask)   // 비트 반영
                            }
                            "주말" -> {
                                weekendChip = !weekendChip
                                dayMask = setWeekend(weekendChip, dayMask)     // 비트 반영
                            }
                            else -> {
                                val bit = bitOf(opt)
                                if (bit >= 0) {
                                    dayMask = dayMask xor (1 shl bit)          // 개별 비트 토글
                                    val (wd, we) = recomputeChipsFromMask(dayMask)
                                    weekdaysChip = wd
                                    weekendChip  = we
                                }
                            }
                        }
                    },
                    itemHeight = chipH,
                    radius = chipRadius,
                    textSize = chipSp
                )

                Spacer(Modifier.height(blockGap))

                // ───────── 시간 ─────────
                SectionLabel("일하실 시간을 선택해주세요", labelSp)
                Spacer(Modifier.height(rowGap))
                TwoColumnChips(
                    options = listOf("오전", "오후"),
                    isSelected = { it == timeOfDay },
                    onClick = { opt -> timeOfDay = if (timeOfDay == opt) null else opt },
                    itemHeight = chipH,
                    radius = chipRadius,
                    textSize = chipSp
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

/* ───────── 공통 UI ───────── */
@Composable private fun SectionLabel(text: String, size: TextUnit) {
    Text(text, fontSize = size, fontWeight = FontWeight.SemiBold, color = Color.Black, lineHeight = size * 1.4f)
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

/** 외부 라이브러리 없이 2열 칩 */
@Composable
private fun TwoColumnChips(
    options: List<String>,
    isSelected: (String) -> Boolean,
    onClick: (String) -> Unit,
    itemHeight: Dp,
    radius: Dp,
    textSize: TextUnit
) {
    val rows = options.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        Text(text = text, fontSize = textSize, fontWeight = FontWeight.Medium, color = if (selected) Color(0xFF005FFF) else Color.Black)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewHopeWorkFilterScreen() {
    MaterialTheme { HopeWorkFilterScreen(rememberNavController()) }
}
