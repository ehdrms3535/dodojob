package com.example.dodojob.ui.feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dodojob.R

/* ===== 컬러 ===== */
private val BrandBlue = Color(0xFF005FFF)
private val TextGray  = Color(0xFF828282)
private val LineGray  = Color(0xFFDDDDDD)
private val LabelGray = Color(0xFF9C9C9C)
private val BgGray    = Color(0xFFF1F5F7)
private val TagGray   = Color(0xFFE0E0E0)

/* ===== 공통 컴포넌트 ===== */
@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .shadow(6.dp, shape = RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp)
    ) { content() }
}

/* 접기/펼치기 타이틀 (아이콘 리소스 복구) */
@Composable
private fun SectionTitle(
    title: String,
    iconRes: Int,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFDEEAFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = Color(0xFF9C9C9C),
            modifier = Modifier
                .size(18.dp)
                .rotate(if (expanded) 0f else -90f)
        )
    }
}

@Composable
private fun KeyValueRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    startPadding: Dp = 16.dp,
    endPadding: Dp =  16.dp
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = endPadding, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = LabelGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.Right
        )
    }
}

@Composable
private fun BlueButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
    ) {
        Text(text, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

@Composable
private fun ThinDivider(insetStart: Dp = 16.dp, insetEnd: Dp = 16.dp) {
    Divider(
        modifier = Modifier.padding(start = insetStart, end = insetEnd),
        color = LineGray,
        thickness = 1.dp
    )
}

@Composable
private fun GrayInputHint(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(TagGray, RoundedCornerShape(10.dp))
            .height(38.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text,
            fontSize = 16.sp,
            color = Color(0xFFA6A6A6),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

/* ===== 메인 ===== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeManageScreen() {
    val scroll = rememberScrollState()

    var personalExpanded by remember { mutableStateOf(true) }
    var careerExpanded by remember { mutableStateOf(true) }
    var licenseExpanded by remember { mutableStateOf(true) }
    var hopeExpanded by remember { mutableStateOf(true) }

    var selectedJob by remember { mutableStateOf("서비스업") }

    // 바텀시트 표시
    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgGray,
        topBar = {
            Column(Modifier.fillMaxWidth().background(Color(0xFFEFEFEF))) {
                Spacer(Modifier.fillMaxWidth().height(24.dp))
            }
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(scroll)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBackIosNew,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(" 이력서 관리", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            }

            Spacer(Modifier.height(16.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                /* ===== 인적사항 ===== */
                SectionCard {
                    SectionTitle(
                        title = " 인적사항",
                        iconRes = R.drawable.app_manage_personal,  // ✅ 복구
                        expanded = personalExpanded,
                        onToggle = { personalExpanded = !personalExpanded }
                    )

                    if (personalExpanded) {
                        Spacer(Modifier.height(20.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Image(                               // ✅ 복구
                                painter = painterResource(id = R.drawable.senior_id),
                                contentDescription = "프로필",
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            )
                            Spacer(Modifier.width(20.dp))
                        }

                        Spacer(Modifier.height(12.dp))
                        KeyValueRow("이름", "홍길동", startPadding = 24.dp, endPadding = 24.dp)
                        KeyValueRow("생년월일", "1964년 3월 15일", startPadding = 24.dp, endPadding = 24.dp)
                        KeyValueRow("전화번호", "010-1234-1234", startPadding = 24.dp, endPadding = 24.dp)
                        KeyValueRow("주소", "경북 경산시", startPadding = 24.dp, endPadding = 24.dp)
                        KeyValueRow("이메일", "Hong_11@naver.com", startPadding = 24.dp, endPadding = 24.dp)

                        Spacer(Modifier.height(16.dp))
                        BlueButton("수정")
                    }
                }

                Spacer(Modifier.height(16.dp))

                /* ===== 경력 ===== */
                SectionCard {
                    SectionTitle(
                        title = " 경력",
                        iconRes = R.drawable.app_manage_career,   // ✅ 복구
                        expanded = careerExpanded,
                        onToggle = { careerExpanded = !careerExpanded }
                    )

                    if (careerExpanded) {
                        Spacer(Modifier.height(20.dp))
                        ThinDivider()
                        Spacer(Modifier.height(20.dp))

                        CareerItem("프랜차이즈 카페 점장", "2008.03", "2010.01")

                        Spacer(Modifier.height(20.dp))
                        ThinDivider()
                        Spacer(Modifier.height(20.dp))

                        CareerItem("기업체 인사/총무 담당 과장", "2010.01", "2020.06")

                        Spacer(Modifier.height(30.dp))
                        GrayInputHint("추가 경력을 적어주세요")

                        Spacer(Modifier.height(12.dp))
                        ConsentRow(fontSize = 16.sp)

                        Spacer(Modifier.height(16.dp))
                        BlueButton("추가하기")
                    }
                }

                Spacer(Modifier.height(16.dp))

                /* ===== 자격증 ===== */
                SectionCard {
                    SectionTitle(
                        title = " 자격증",
                        iconRes = R.drawable.app_manage_certi,    // ✅ 복구
                        expanded = licenseExpanded,
                        onToggle = { licenseExpanded = !licenseExpanded }
                    )

                    if (licenseExpanded) {
                        Spacer(Modifier.height(22.dp))
                        ThinDivider()
                        Spacer(Modifier.height(22.dp))
                        LicenseItem("한국서비스산업진흥원", "고객 서비스 매니저 1급 자격증", "CSM-2018-1103-1023")
                        Spacer(Modifier.height(22.dp))
                        ThinDivider()
                        Spacer(Modifier.height(22.dp))
                        LicenseItem("대구문화센터", "문화·여가 프로그램 지도사 자격증", "CPC-2021-0420-0789")
                        Spacer(Modifier.height(22.dp))
                        ThinDivider()
                        Spacer(Modifier.height(22.dp))
                        LicenseItem("대한시니어평생교육원", "시니어 케어 코디네이터 2급 자격증", "SCC-2020-0612-0457")

                        Spacer(Modifier.height(22.dp))
                        GrayInputHint("추가 자격증을 적어주세요")

                        Spacer(Modifier.height(12.dp))
                        ConsentRow(fontSize = 16.sp)

                        Spacer(Modifier.height(16.dp))
                        BlueButton("추가하기")
                    }
                }

                Spacer(Modifier.height(16.dp))

                /* ===== 희망직무 ===== */
                SectionCard {
                    SectionTitle(
                        title = " 희망직무",
                        iconRes = R.drawable.app_manage_hope,     // ✅ 복구
                        expanded = hopeExpanded,
                        onToggle = { hopeExpanded = !hopeExpanded }
                    )

                    if (hopeExpanded) {
                        Spacer(Modifier.height(30.dp))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            JobChip(
                                title = "서비스업",
                                desc = "매장관리, 고객 응대",
                                selected = selectedJob == "서비스업",
                                onClick = { selectedJob = "서비스업" },
                                modifier = Modifier.weight(1f)
                            )
                            JobChip(
                                title = "교육/강의",
                                desc = "강사, 지도사",
                                selected = selectedJob == "교육/강의",
                                onClick = { selectedJob = "교육/강의" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            JobChip(
                                title = "관리/운영",
                                desc = "시설, 인력관리",
                                selected = selectedJob == "관리/운영",
                                onClick = { selectedJob = "관리/운영" },
                                modifier = Modifier.weight(1f)
                            )
                            JobChip(
                                title = "돌봄서비스",
                                desc = "방문, 요양, 돌봄",
                                selected = selectedJob == "돌봄서비스",
                                onClick = { selectedJob = "돌봄서비스" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(30.dp))
                        BlueButton("자세히 보기") { showSheet = true }
                    }
                }

                Spacer(Modifier.height(40.dp))

                /* ===== 하단 이력서 저장 버튼 ===== */
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgGray)
                        .padding(horizontal = 4.dp, vertical = 20.dp)
                ) {
                    Button(
                        onClick = { /* TODO: 저장 로직 */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("이력서 저장", fontSize = 25.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    /* ===== PreferWorkScreen 스타일을 그대로 쓴 모달 시트 ===== */
    if (showSheet) {
        ExperiencePickerSheet(
            preselected = emptySet(),
            onApply = { /* 선택 결과 사용 */ showSheet = false },
            onDismiss = { showSheet = false }
        )
    }
}

/* ===== 경력/자격증/동의 ===== */
@Composable
private fun CareerItem(title: String, start: String, end: String) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(start, color = BrandBlue, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(" ~ ", color = TextGray, fontSize = 16.sp)
            Text(end, color = BrandBlue, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun LicenseItem(org: String, title: String, code: String) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(org, fontSize = 14.sp, color = Color(0xFF616161))
        Spacer(Modifier.height(6.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Text("자격번호 $code", fontSize = 16.sp, color = BrandBlue, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ConsentRow(fontSize: androidx.compose.ui.unit.TextUnit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Color(0xFFDEEAFF)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(BrandBlue)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text("(필수) 개인정보 제 3자 제공 동의", color = Color(0xFFFF2F00), fontSize = fontSize)
    }
}

/* ===== 희망직무 요약 칩 ===== */
@Composable
private fun JobChip(
    title: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = BrandBlue
    val bg = if (selected) BrandBlue else Color.White
    val titleColor = if (selected) Color.White else BrandBlue
    val descColor = if (selected) Color.White else BrandBlue

    Column(
        modifier = modifier
            .height(70.dp)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
        Spacer(Modifier.height(2.dp))
        Text(desc, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = descColor)
    }
}

/* ===== PreferWorkScreen 스타일 모달 (요청사항 반영) ===== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperiencePickerSheet(
    preselected: Set<String>,
    onApply: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val Primary = BrandBlue
    var query by remember { mutableStateOf("") }
    val selected = remember { mutableStateListOf<String>().apply { addAll(preselected) } }
    var healthy by remember { mutableStateOf(false) }

    val categories: List<Pair<String, List<String>>> = listOf(
        "서비스업" to listOf("고객 응대","카운터/계산","상품 진열","청결 관리","안내 데스크","주차 관리"),
        "교육/강의" to listOf("영어 회화","악기 지도","요리 강사","역사 강의","공예 강의","예술 지도"),
        "관리/운영" to listOf("환경미화","인력 관리","사서 보조","사무 보조","경비/보안"),
        "돌봄" to listOf("등하원 도우미","가정 방문","보조 교사")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp),
        dragHandle = null // ✅ 위 바 하나만 (커스텀) 쓰기 위해 제거
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(18.dp))
            SheetDragHandle() // ✅ 아래꺼 하나만
            Spacer(Modifier.height(28.dp))

            Text(
                "경험을 살릴 일을 설정해주세요",
                fontSize = 26.sp,                 // ✅ 26sp
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(Modifier.height(18.dp))

            // ✅ 배경 #EFEFEF, radius 10, 오른쪽(트레일링) 아이콘, placeholder #959595
            TextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                placeholder = { Text("직종 키워드", fontSize = 18.sp, color = Color(0xFF959595)) },
                trailingIcon = {                  // ← 오른쪽 아이콘
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = Color(0xFF959595),
                        modifier = Modifier.size(20.dp)
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TagGray,     // #EFEFEF
                    unfocusedContainerColor = TagGray,
                    disabledContainerColor = TagGray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(57.dp)
            )

            Spacer(Modifier.height(20.dp))
            Divider(color = Color(0xFFCFCFCF))
            Spacer(Modifier.height(14.dp))

            // 본문 리스트
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                categories.forEach { (title, list) ->
                    val filtered = if (query.isBlank()) list
                    else list.filter { it.contains(query.trim(), ignoreCase = true) }

                    item {
                        Text(
                            title,
                            fontSize = 26.sp,                  // ✅ 섹션 타이틀 26sp
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                        Spacer(Modifier.height(16 .dp))
                        TwoColumnChipsEqualWidth(
                            options = filtered,
                            isSelected = { it in selected },
                            onToggle = { label ->
                                if (label in selected) selected.remove(label) else selected.add(label)
                            }
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            Divider(color = Color(0xFFCFCFCF))
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("＊ 필수", color = Color(0xFFF24822), fontSize = 18.sp)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OptionCheckBox(checked = healthy, onCheckedChange = { healthy = it })
                Spacer(Modifier.width(10.dp))
                Text("건강해서 일하는 데 지장이 없어요.", fontSize = 22.sp, color = Color.Black)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { selected.clear(); healthy = false; query = "" },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Primary
                    ),
                    border = BorderStroke(1.dp, Primary)
                ) { Text("초기화", color = Color.Black, fontSize = 24.sp) }

                Button(
                    onClick = { onApply(selected.toSet()) },
                    enabled = healthy,
                    modifier = Modifier.weight(2f).height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (healthy) Primary else Color(0xFFBFC6D2),
                        disabledContainerColor = Color(0xFFBFC6D2)
                    )
                ) { Text("적용하기", color = Color.White, fontSize = 24.sp) }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

/* ----- 칩 2열 레이아웃 (PreferWorkScreen과 동일) ----- */
@Composable
private fun TwoColumnChipsEqualWidth(
    options: List<String>,
    isSelected: (String) -> Boolean,
    onToggle: (String) -> Unit,
    itemHeight: Dp = 56.dp,
    radius: Dp = 12.dp
) {
    val rows = options.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { label ->
                    val selected = isSelected(label)
                    SimpleChoiceChip(
                        text = label,
                        selected = selected,
                        onClick = { onToggle(label) },
                        modifier = Modifier
                            .weight(1f)
                            .height(itemHeight),
                        radius = radius
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/* ----- 칩 스타일 (요청 스펙 반영) ----- */
@Composable
private fun SimpleChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    radius: Dp = 10.dp // 디자인 radius 10
) {
    val bg = if (selected) Color(0xFFC1D2ED) else Color(0xFFE0E0E0)
    val border = if (selected) Color(0xFF005FFF) else Color.Transparent
    val textColor = if (selected) Color(0xFF005FFF) else Color(0xFF111111) // 미선택 연회색

    Box(
        modifier = modifier
            .border(1.dp, border, RoundedCornerShape(radius))
            .background(bg, RoundedCornerShape(radius))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp,          // 요청: 24sp
            textAlign = TextAlign.Center
        )
    }
}

/* ----- 체크박스 (PreferWorkScreen 동일) ----- */
@Composable
private fun OptionCheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val badgeSize = 28.dp
    val iconSize = 18.dp

    Box(
        modifier = Modifier
            .size(badgeSize)
            .clip(CircleShape)
            .background(if (checked) Color(0xFF2A77FF) else Color(0xFFE6E6E6))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = if (checked) Color.White else Color(0xFFBDBDBD),
            modifier = Modifier.size(iconSize)
        )
    }
}

/* ----- 상단 드래그 핸들 (하나만 사용) ----- */
@Composable
private fun SheetDragHandle() {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .width(122.89.dp)       // 프레임 값과 비슷하게
                .height(4.16.dp)
                .background(Color(0xFFB3B3B3), RoundedCornerShape(10395.dp))
        )
    }
}

/* ----- 미리보기 ----- */
@Preview(
    device = Devices.PHONE,
    showBackground = true,
    backgroundColor = 0xFFF1F5F7,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun PreviewResumeManageScreen() {
    ResumeManageScreen()
}
