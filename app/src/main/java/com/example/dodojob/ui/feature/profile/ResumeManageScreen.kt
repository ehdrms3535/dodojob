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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import com.example.dodojob.data.career.CareerModels
import com.example.dodojob.data.career.CareerRepositoryImpl
import com.example.dodojob.data.license.LicenseModels
import com.example.dodojob.data.license.LicenseRepositoryImpl
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.session.CurrentUser
import kotlinx.coroutines.launch

/* ===== 컬러 ===== */
private val BrandBlue = Color(0xFF005FFF)
private val TextGray  = Color(0xFF828282)
private val LineGray  = Color(0xFFDDDDDD)
private val LabelGray = Color(0xFF9C9C9C)
private val BgGray    = Color(0xFFF1F5F7)
private val TagGray   = Color(0xFFE0E0E0)

/* ===== Filled Inputs (캡슐형) ===== */
private val InputBg         = Color(0xFFEFEFEF)
private val PlaceholderGray = Color(0xFF959595)

@Composable
private fun FilledInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    height: Dp = 56.dp,
    radius: Dp = 10.dp
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        placeholder = { Text(placeholder, fontSize = 16.sp, color = PlaceholderGray) },
        shape = RoundedCornerShape(radius),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = InputBg,
            unfocusedContainerColor = InputBg,
            disabledContainerColor = InputBg,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Color.Black,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    )
}

@Composable
private fun FilledMultilineInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 4,
    radius: Dp = 10.dp
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = false,
        minLines = minLines,
        placeholder = { Text(placeholder, fontSize = 16.sp, color = PlaceholderGray) },
        shape = RoundedCornerShape(radius),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = InputBg,
            unfocusedContainerColor = InputBg,
            disabledContainerColor = InputBg,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Color.Black,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 110.dp)
    )
}

@Serializable
private data class UserTmpRow(
    val name: String? = null,
    val gender: String? = null,
    val birthdate: String? = null,  // YYYY-MM-DD
    val region: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val username: String? = null
)

private class ProfileRepositoryImpl(
    private val client: SupabaseClient
) {
    suspend fun getUser(username: String): UserTmpRow? {
        val list = client.from("users_tmp")
            .select { filter { eq("username", username) } }
            .decodeList<UserTmpRow>()
        return list.firstOrNull()
    }
}

/* ===== 공통 컴포넌트 ===== */
@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .shadow(10.dp, shape = RoundedCornerShape(10.dp))
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(vertical = 20.dp)
    ) { content() }
}

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
            .padding(horizontal = 20.dp, vertical = 2.dp)
            .clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 3.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Image(
            painter = painterResource(
                id = if (expanded) R.drawable.upper else R.drawable.down
            ),
            contentDescription = if (expanded) "접기" else "펼치기",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun KeyValueRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    startPadding: Dp = 24.dp,
    endPadding: Dp = 24.dp
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
private fun BlueButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandBlue,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFBFD4FF),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        )
    ) {
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.White)
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

/* ===== 메인 ===== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeManageScreen(nav: NavController) {
    val username = remember { CurrentUser.username ?: "guest" }
    val client = LocalSupabase.current

    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()

    var personalExpanded by remember { mutableStateOf(true) }
    var careerExpanded by remember { mutableStateOf(true) }
    var licenseExpanded by remember { mutableStateOf(true) }
    var hopeExpanded by remember { mutableStateOf(true) }

    var selectedJob by remember { mutableStateOf("서비스업") }
    var showSheet by remember { mutableStateOf(false) }

    /* -------- 인적사항 상태/로드 -------- */
    val profileRepo = remember { ProfileRepositoryImpl(client) }
    var user by remember { mutableStateOf<UserTmpRow?>(null) }
    var userLoading by remember { mutableStateOf(false) }
    var userError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(username) {
        userLoading = true
        userError = null
        try {
            user = profileRepo.getUser(username)
        } catch (e: Exception) {
            userError = "인적사항을 불러오지 못했어요: ${e.message}"
        } finally {
            userLoading = false
        }
    }

    /* -------- 경력 -------- */
    var careerConsentChecked by remember { mutableStateOf(false) }
    val careerRepo = remember { CareerRepositoryImpl(client) }
    var careers by remember { mutableStateOf<List<CareerModels>>(emptyList()) }
    var careerLoading by remember { mutableStateOf(false) }
    var cTitle by remember { mutableStateOf("") }
    var cCompany by remember { mutableStateOf("") }
    var cStart by remember { mutableStateOf("") }
    var cEnd by remember { mutableStateOf("") }
    var cDesc by remember { mutableStateOf("") }
    var careerError by remember { mutableStateOf<String?>(null) }
    var careerAddedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(username) {
        careerLoading = true
        try {
            careers = careerRepo.list(username)
        } catch (e: Exception) {
            careerError = "경력 목록을 불러오지 못했어요: ${e.message}"
        } finally {
            careerLoading = false
        }
    }

    /* -------- 자격증  -------- */
    var licenseConsentChecked by remember { mutableStateOf(false) }
    val licenseRepo = remember { LicenseRepositoryImpl(client) }
    var licenses by remember { mutableStateOf<List<LicenseModels>>(emptyList()) }
    var licenseLoading by remember { mutableStateOf(false) }
    var lName by remember { mutableStateOf("") }
    var lLocation by remember { mutableStateOf("") }
    var lNumber by remember { mutableStateOf("") }
    var licenseError by remember { mutableStateOf<String?>(null) }
    var licenseAddedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(username) {
        licenseLoading = true
        try {
            licenses = licenseRepo.list(username)
        } catch (e: Exception) {
            licenseError = "자격증 목록을 불러오지 못했어요: ${e.message}"
        } finally {
            licenseLoading = false
        }
    }

    Scaffold(
        containerColor = BgGray,
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFEFEF))
            ) {
                // 상태바 영역
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                )
            }
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(scroll)
        ) {
            // 헤더 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgGray) // 🔹 전체 통일된 배경색
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp) // 살짝 여백만
                ) {
                    // 🔹 상단 뒤로가기 아이콘
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

                    // 🔹 타이틀
                    Text(
                        text = "이력서 관리",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        letterSpacing = (-0.019f).em,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, bottom = 16.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                /* ===== 인적사항 ===== */
                SectionCard {
                    SectionTitle(
                        title = "인적사항",
                        iconRes = R.drawable.resume_personal,
                        expanded = personalExpanded,
                        onToggle = { personalExpanded = !personalExpanded }
                    )

                    if (personalExpanded) {
                        Spacer(Modifier.height(24.dp))

                        if (userLoading) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else if (userError != null) {
                            Text(
                                userError!!,
                                color = Color(0xFFD32F2F),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        } else {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                ProfileImage()
                            }

                            Spacer(Modifier.height(20.dp))
                            KeyValueRow(
                                "이름",
                                user?.name ?: "-",
                                startPadding = 24.dp,
                                endPadding = 24.dp
                            )
                            KeyValueRow(
                                "생년월일",
                                user?.birthdate?.let { formatBirthdateKR(it) } ?: "-",
                                startPadding = 24.dp,
                                endPadding = 24.dp
                            )
                            KeyValueRow(
                                "전화번호",
                                user?.phone ?: "-",
                                startPadding = 24.dp,
                                endPadding = 24.dp
                            )
                            KeyValueRow(
                                "주소",
                                user?.region ?: "-",
                                startPadding = 24.dp,
                                endPadding = 24.dp
                            )
                            KeyValueRow(
                                "이메일",
                                user?.email ?: "-",
                                startPadding = 24.dp,
                                endPadding = 24.dp
                            )

                            Spacer(Modifier.height(24.dp))
                            BlueButton(
                                text = "수정",
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                /* ===== 경력 ===== */
                SectionCard {
                    SectionTitle(
                        title = "경력",
                        iconRes = R.drawable.resume_experience,
                        expanded = careerExpanded,
                        onToggle = { careerExpanded = !careerExpanded }
                    )

                    if (careerExpanded) {
                        if (careerLoading) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            if (!careers.isEmpty()) {
                                careers.forEach { c ->
                                    Spacer(Modifier.height(20.dp))
                                    CareerItem(
                                        title = c.title ?: (c.company ?: "경력"),
                                        start = c.startDate ?: "-",
                                        end   = c.endDate ?: "-"
                                    )
                                    if (!c.description.isNullOrBlank()) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            c.description!!,
                                            fontSize = 14.sp,
                                            color = Color(0xFF616161),
                                            modifier = Modifier.padding(horizontal = 20.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(20.dp))
                                    ThinDivider()
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            // ---- 새 경력 추가 (캡슐형 입력) ----
                            Column(Modifier.padding(horizontal = 20.dp)) {
                                Text("새 경력 추가", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(14.dp))

                                FilledInput(
                                    value = cTitle, onValueChange = { cTitle = it },
                                    placeholder = "직무/직책 (career_title)"
                                )
                                Spacer(Modifier.height(8.dp))

                                FilledInput(
                                    value = cCompany, onValueChange = { cCompany = it },
                                    placeholder = "회사/기관 (company)"
                                )
                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp), // 🔹 살짝 여유 줌
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledInput(
                                        value = cStart,
                                        onValueChange = { cStart = it },
                                        placeholder = "시작 (예: 2008.03)",
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 2.dp) // 🔹 오른쪽 약간 여백
                                    )
                                    FilledInput(
                                        value = cEnd,
                                        onValueChange = { cEnd = it },
                                        placeholder = "종료 (예: 2015.03)",
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 2.dp) // 🔹 왼쪽 약간 여백
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                FilledMultilineInput(
                                    value = cDesc, onValueChange = { cDesc = it },
                                    placeholder = "상세 업무"
                                )

                                if (careerError != null) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(careerError!!, color = Color(0xFFD32F2F), fontSize = 14.sp)
                                }
                                if (careerAddedOnce) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("경력이 추가되었습니다.", color = BrandBlue, fontSize = 14.sp)
                                }

                                Spacer(Modifier.height(8.dp))
                                ConsentRow(
                                    fontSize = 16.sp,
                                    checked = careerConsentChecked,
                                    onCheckedChange = { careerConsentChecked = it }
                                )

                                Spacer(Modifier.height(8.dp))
                                BlueButton(
                                    text = "추가하기",
                                    enabled = careerConsentChecked
                                ) {
                                    if (cTitle.isBlank() && cCompany.isBlank()) {
                                        careerError = "직무 또는 회사 중 하나는 입력해주세요."
                                        careerAddedOnce = false
                                        return@BlueButton
                                    }
                                    scope.launch {
                                        careerLoading = true
                                        careerError = null
                                        careerAddedOnce = false
                                        try {
                                            careerRepo.add(
                                                username = username,
                                                title = cTitle,
                                                company = cCompany,
                                                startDate = cStart,
                                                endDate = cEnd,
                                                description = cDesc.ifBlank { null }
                                            )
                                            careers = careerRepo.list(username)
                                            cTitle = ""; cCompany = ""; cStart = ""; cEnd = ""; cDesc = ""
                                            careerAddedOnce = true
                                        } catch (e: Exception) {
                                            careerError = "추가 실패: ${e.message}"
                                        } finally {
                                            careerLoading = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                /* ===== 자격증 ===== */
                SectionCard {
                    SectionTitle(
                        title = "자격증",
                        iconRes = R.drawable.resume_certi,
                        expanded = licenseExpanded,
                        onToggle = { licenseExpanded = !licenseExpanded }
                    )

                    if (licenseExpanded) {
                        if (licenseLoading) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            if (!licenses.isEmpty()) {
                                licenses.forEach { lic ->
                                    Spacer(Modifier.height(18.dp))
                                    LicenseItem(
                                        org = lic.location ?: "발급기관 미입력",
                                        title = lic.name ?: "자격증명 미입력",
                                        code = lic.number ?: "-"
                                    )
                                }
                            }

                            Spacer(Modifier.height(22.dp))

                            // ---- 새 자격증 추가 (캡슐형 입력) ----
                            Column(Modifier.padding(horizontal = 20.dp)) {
                                Text("새 자격증 추가", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(14.dp))

                                FilledInput(
                                    value = lName, onValueChange = { lName = it },
                                    placeholder = "자격증명 (license_name)"
                                )
                                Spacer(Modifier.height(8.dp))

                                FilledInput(
                                    value = lLocation, onValueChange = { lLocation = it },
                                    placeholder = "발급기관 (license_location)"
                                )
                                Spacer(Modifier.height(8.dp))

                                FilledInput(
                                    value = lNumber, onValueChange = { lNumber = it },
                                    placeholder = "자격번호 (license_number)"
                                )

                                if (licenseError != null) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(licenseError!!, color = Color(0xFFD32F2F), fontSize = 14.sp)
                                }
                                if (licenseAddedOnce) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("추가되었습니다.", color = BrandBlue, fontSize = 14.sp)
                                }

                                Spacer(Modifier.height(8.dp))
                                ConsentRow(
                                    fontSize = 16.sp,
                                    checked = licenseConsentChecked,
                                    onCheckedChange = { licenseConsentChecked = it }
                                )

                                Spacer(Modifier.height(8.dp))
                                BlueButton(
                                    text = "추가하기",
                                    enabled = licenseConsentChecked
                                ) {
                                    if (lName.isBlank() && lLocation.isBlank() && lNumber.isBlank()) {
                                        licenseError = "한 가지 이상 입력해주세요."
                                        licenseAddedOnce = false
                                        return@BlueButton
                                    }
                                    scope.launch {
                                        licenseLoading = true
                                        licenseError = null
                                        licenseAddedOnce = false
                                        try {
                                            licenseRepo.add(username, lName, lLocation, lNumber)
                                            licenses = licenseRepo.list(username)
                                            lName = ""; lLocation = ""; lNumber = ""
                                            licenseAddedOnce = true
                                        } catch (e: Exception) {
                                            licenseError = "추가 실패: ${e.message}"
                                        } finally {
                                            licenseLoading = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                /* ===== 희망직무 ===== */
                SectionCard {
                    SectionTitle(
                        title = "희망직무",
                        iconRes = R.drawable.resume_hope,
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
                                desc = "매장관리,고객 응대",
                                selected = selectedJob == "서비스업",
                                onClick = { selectedJob = "서비스업" },
                                modifier = Modifier.weight(1f)
                            )
                            JobChip(
                                title = "교육/강의",
                                desc = "전문지식 전수",
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
                                desc = "시설,인력관리",
                                selected = selectedJob == "관리/운영",
                                onClick = { selectedJob = "관리/운영" },
                                modifier = Modifier.weight(1f)
                            )
                            JobChip(
                                title = "돌봄서비스",
                                desc = "아동,시니어돌봄",
                                selected = selectedJob == "돌봄서비스",
                                onClick = { selectedJob = "돌봄서비스" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(30.dp))
                        BlueButton(
                            text = "자세히 보기",
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            showSheet = true
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))

                /* ===== 하단 이력서 저장 버튼 ===== */
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgGray)
                        .padding(vertical = 20.dp)
                ) {
                    Button(
                        onClick = { /* TODO: 저장 로직 */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("이력서 저장", fontSize = 24.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    if (showSheet) {
        ExperiencePickerSheet(
            preselected = emptySet(),
            onApply = { showSheet = false },
            onDismiss = { showSheet = false }
        )
    }
}

/* ===== 유틸: YYYY-MM-DD -> "YYYY년 M월 D일" ===== */
private fun formatBirthdateKR(iso: String): String {
    return runCatching {
        val y = iso.substring(0, 4)
        val m = iso.substring(5, 7).trimStart('0').ifBlank { "0" }
        val d = iso.substring(8, 10).trimStart('0').ifBlank { "0" }
        "${y}년 ${m}월 ${d}일"
    }.getOrElse { iso }
}

/* ===== 경력/자격증/동의/칩 ===== */
@Composable
private fun CareerItem(title: String, start: String, end: String) {
    Column(Modifier.padding(horizontal = 20.dp)) {
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
    Column(Modifier.padding(horizontal = 20.dp)) {
        Text(org, fontSize = 14.sp, color = Color(0xFF616161))
        Spacer(Modifier.height(6.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Text("자격번호 $code", fontSize = 16.sp, color = BrandBlue, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ConsentRow(
    fontSize: androidx.compose.ui.unit.TextUnit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 20.dp, top = 10.dp, bottom = 10.dp) // 🔹 왼쪽 여백 줄임
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(
                id = if (checked) R.drawable.autologin_checked
                else R.drawable.autologin_unchecked
            ),
            contentDescription = if (checked) "동의함" else "동의 안 함",
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "(필수) 개인정보 제 3자 제공 동의",
            color = Color(0xFFFF2F00),
            fontSize = fontSize
        )
    }
}

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
            .height(80.dp)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(start = 14.dp, top = 12.dp, end = 12.dp, bottom = 12.dp), // 🔹 왼쪽 패딩 증가
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
        Spacer(Modifier.height(2.dp))
        Text(desc, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = descColor)
    }
}

/* ===== PreferWorkSheet ===== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExperiencePickerSheet(
    preselected: Set<String>,
    onApply: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val maxSheetFraction = 0.88f
    var showSheet by remember { mutableStateOf(true) }
    if (!showSheet) return

    val Brand = BrandBlue
    val FieldBg = Color(0xFFEFEFEF)
    val DividerColor = Color(0xFFCFCFCF)
    val ChipBase = Color(0xFFF7F7F7)
    val ChipSelBg = Color(0xFFDEEBFF)
    val ChipUnselBorder = Color(0xFFE0E0E0)

    var query by remember { mutableStateOf("") }
    var healthy by remember { mutableStateOf(false) }

    // ✔️ 선택된 라벨들 (기존 preselected를 그대로 반영)
    val selected = remember { mutableStateListOf<String>().apply { addAll(preselected) } }

    // 기존 ExperiencePickerSheet에서 쓰던 카테고리/옵션 그대로 사용
    val categories: List<Pair<String, List<String>>> = listOf(
        "서비스업" to listOf("고객 응대","카운터/계산","상품 진열","청결 관리","안내 데스크","주차 관리"),
        "교육/강의" to listOf("영어 회화","악기 지도","요리 강사","역사 강의","공예 강의","예술 지도"),
        "관리/운영" to listOf("환경미화","인력 관리","사서 보조","사무 보조","경비/보안"),
        "돌봄" to listOf("등하원 도우미","가정 방문","보조 교사")
    )

    fun filtered(list: List<String>) =
        if (query.isBlank()) list else list.filter { it.contains(query.trim(), ignoreCase = true) }

    ModalBottomSheet(
        onDismissRequest = {
            showSheet = false
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color.White,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = { } // 상단 custom handle 그릴 거라 기본 핸들 제거
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(maxSheetFraction)
                .navigationBarsPadding()
                .imePadding()
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val W = maxWidth
                fun frac(h: Float) = (W * h)
                val handleW = frac(122.89f / 360f)
                val handleH = frac(4.16f / 360f)
                val radius10 = (W * (10f / 360f))
                val fieldH = frac(57f / 360f)
                val chipH = 64.dp
                val btnH = frac(54f / 360f)
                val gap12 = frac(12f / 360f)
                val gap16 = frac(16f / 360f)
                val gap20 = frac(20f / 360f)

                val tightLS = (-0.019f).em

                Column(
                    Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 18.dp)
                        .heightIn(min = 0.dp, max = 882.dp)
                ) {
                    // 상단 핸들 (PreferWorkSheetBottomSheet와 동일 스타일)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = gap12),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .size(width = handleW, height = handleH)
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color(0xFFB3B3B3))
                        )
                    }

                    // 타이틀
                    Text(
                        "경험을 살릴 일을 설정해주세요",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = tightLS,
                        lineHeight = 39.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Spacer(Modifier.height(8.dp))

                    // 검색창
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = {
                            Text(
                                "직종 키워드",
                                color = Color(0xFFA6A6A6),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = (-0.019f).em
                            )
                        },
                        trailingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = "검색",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = fieldH, max = fieldH),
                        shape = RoundedCornerShape(radius10),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = FieldBg,
                            unfocusedContainerColor = FieldBg,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Brand
                        )
                    )

                    Spacer(Modifier.height(22.dp))
                    Divider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = DividerColor
                    )
                    Spacer(Modifier.height(20.dp))

                    // 스크롤 영역
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(gap16)
                    ) {
                        categories.forEach { (title, list) ->
                            val opts = filtered(list)
                            if (opts.isNotEmpty()) {
                                SectionHeader(text = title)
                                TwoColumnChipsResponsive(
                                    options = opts,
                                    chipHeight = chipH,
                                    radius = radius10,
                                    baseBg = ChipBase,
                                    selectedBg = ChipSelBg,
                                    brand = Brand,
                                    unselectedBorder = ChipUnselBorder,
                                    isSelected = { it in selected },
                                    onToggle = { label ->
                                        if (label in selected) selected.remove(label)
                                        else selected.add(label)
                                    }
                                )
                                Spacer(Modifier.height(1.dp))
                                Divider(color = DividerColor)
                                Spacer(Modifier.height(0.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("＊ 필수", color = Color(0xFFF24822), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(gap12 / 2))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(
                                id = if (healthy) R.drawable.autologin_checked else R.drawable.autologin_unchecked
                            ),
                            contentDescription = "체크박스",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { healthy = !healthy }
                        )
                        Spacer(Modifier.width(gap12))
                        Text("건강해서 일하는 데 지장이 없어요.", fontSize = 22.sp)
                    }

                    Spacer(Modifier.height(gap20))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap12)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selected.clear()
                                selected.addAll(preselected) // 초기 상태로 되돌리기
                                healthy = false
                                query = ""
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = btnH, max = btnH),
                            shape = RoundedCornerShape(radius10),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp,
                                brush = SolidColor(Brand)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Brand
                            )
                        ) {
                            Text("초기화", fontSize = 24.sp, fontWeight = FontWeight.Medium)
                        }

                        val canApply = healthy
                        Button(
                            onClick = {
                                showSheet = false
                                onApply(selected.toSet())
                            },
                            enabled = canApply,
                            modifier = Modifier
                                .weight(2f)
                                .heightIn(min = btnH, max = btnH),
                            shape = RoundedCornerShape(radius10),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canApply) Brand else Color(0xFFBFC6D2),
                                disabledContainerColor = Color(0xFFBFC6D2)
                            )
                        ) {
                            Text(
                                "적용하기",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ----- 칩 2열 레이아웃 ----- */
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

/* ----- 칩 스타일 ----- */
@Composable
private fun SimpleChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    radius: Dp = 10.dp
) {
    val bg = if (selected) Color(0xFFC1D2ED) else Color(0xFFE0E0E0)
    val border = if (selected) Color(0xFF005FFF) else Color.Transparent
    val textColor = if (selected) Color(0xFF005FFF) else Color(0xFF111111)

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
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProfileImage() {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val imageSize = screenWidth * (104.53f / 360f)  // 🔹 화면 비율 기반 크기 계산

    Image(
        painter = painterResource(id = R.drawable.senior_id),
        contentDescription = "프로필",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(imageSize)
            .clip(RoundedCornerShape(10.dp))
    )
}

/* ----- 체크박스 ----- */
@Composable
private fun OptionCheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                id = if (checked) {
                    R.drawable.autologin_checked
                } else {
                    R.drawable.autologin_unchecked
                }
            ),
            contentDescription = if (checked) "선택됨" else "선택 안 됨",
            modifier = Modifier.fillMaxSize()
        )
    }
}

/* ----- 상단 드래그 핸들 ----- */
@Composable
private fun SheetDragHandle() {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .width(122.89.dp)
                .height(4.16.dp)
                .background(Color(0xFFB3B3B3), RoundedCornerShape(10395.dp))
        )
    }
}

/* ---------- 섹션 헤더 (모달용) ---------- */
@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        letterSpacing = (-0.019f).em,
        lineHeight = 33.sp
    )
}

/* ---------- 칩 2열 레이아웃 (모달용) ---------- */
@Composable
private fun TwoColumnChipsResponsive(
    options: List<String>,
    chipHeight: Dp,
    radius: Dp,
    baseBg: Color,
    selectedBg: Color,
    brand: Color,
    unselectedBorder: Color,
    isSelected: (String) -> Boolean,
    onToggle: (String) -> Unit
) {
    val rows = remember(options) { options.chunked(2) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { label ->
                    ChoiceChipResponsive(
                        text = label,
                        selected = isSelected(label),
                        onClick = { onToggle(label) },
                        height = chipHeight,
                        radius = radius,
                        baseBg = baseBg,
                        selectedBg = selectedBg,
                        brand = brand,
                        unselectedBorder = unselectedBorder,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/* ---------- 칩 하나 (모달용) ---------- */
@Composable
private fun ChoiceChipResponsive(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    height: Dp,
    radius: Dp,
    baseBg: Color,
    selectedBg: Color,
    brand: Color,
    unselectedBorder: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = height, max = height)
            .clip(RoundedCornerShape(radius))
            .background(if (selected) selectedBg else baseBg)
            .border(1.dp, if (selected) brand else unselectedBorder, RoundedCornerShape(radius))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) brand else Color.Black,
            letterSpacing = (-0.019f).em,
            maxLines = 1
        )
    }
}