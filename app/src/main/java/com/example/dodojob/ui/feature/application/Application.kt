package com.example.dodojob.ui.feature.application

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.data.application.ApplicationData
import com.example.dodojob.data.application.ApplyRepository
import com.example.dodojob.navigation.Route
import io.github.jan.supabase.SupabaseClient
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/* ================= 공통 색/타이포 ================= */
private val ScreenBg = Color(0xFFF1F5F7)
private val BrandBlue = Color(0xFF005FFF)
private val Letter = (-0.019f).em

/* =============== Route 정의 =============== */
object ApplyRoute {
    const val path = "application"
    const val withId = "application/{announcementId}"

    fun createRoute(announcementId: Long): String = "$path/$announcementId"
}

/* =============== UI용 모델 =============== */

data class ApplicantProfile(
    val name: String,
    val tel: String,
    val experienceSummary: String,
    val healthFlags: List<HealthFlag>,
    val healthEtcPlaceholder: String = "기타"
)

data class HealthFlag(
    val label: String,
    val checked: Boolean
)

data class CompanyPosting(
    val orgName: String,
    val task: String
)

data class ApplicationUiState(
    val applicant: ApplicantProfile,
    val posting: CompanyPosting
)

/* ============ Route ============ */

@Composable
fun ApplicationRoute(
    nav: NavController,
    announcementId: Long,
    username: String?,
    client: SupabaseClient
) {
    val repo = remember(client) { ApplyRepository(client) }

    var uiState by remember { mutableStateOf<ApplicationUiState?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(announcementId, username) {
        isLoading = true
        error = null
        try {
            val data: ApplicationData = repo.loadApplicationData(
                announcementId = announcementId,
                username = username
            )

            val posting = CompanyPosting(
                orgName = data.orgName ?: "회사명 없음",
                task = data.taskMajor ?: "업무내용 없음"
            )

            val applicant = ApplicantProfile(
                name = data.userName ?: "이름 없음",
                tel = formatPhoneNumber(data.userPhone),
                experienceSummary = data.careerTitle?.trim() ?: "경력 없음",
                healthFlags = listOf(
                    HealthFlag("오래 서 있기 어려움", false),
                    HealthFlag("무거운 짐 들기 어려움", false),
                    HealthFlag("시력 보조 필요", false),
                ),
                healthEtcPlaceholder = "기타"
            )

            uiState = ApplicationUiState(
                applicant = applicant,
                posting = posting
            )
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScreenBg),
                contentAlignment = Alignment.Center
            ) {
                Text("불러오는 중…")
            }
        }

        error != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScreenBg),
                contentAlignment = Alignment.Center
            ) {
                Text("데이터를 불러오지 못했습니다.\n$error")
            }
        }

        uiState != null -> {
            ApplicationScreen(
                applicant = uiState!!.applicant,
                posting = uiState!!.posting,
                onBackClick = { nav.popBackStack() },
                onSubmit = { healthCondition ->
                    scope.launch {
                        try {
                            repo.submitApplication(
                                announcementId = announcementId,
                                username = username,
                                healthCondition = healthCondition
                            )
                            nav.navigate(Route.ApplicationCompleted.path)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            nav.navigate(Route.ApplicationCompleted.path)
                        }
                    }
                }
            )
        }
    }
}

/* ============ Screen ============ */

@Composable
fun ApplicationScreen(
    applicant: ApplicantProfile,
    posting: CompanyPosting,
    onBackClick: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val healthFlags = remember {
        mutableStateListOf<HealthFlag>().apply { addAll(applicant.healthFlags) }
    }
    var etcChecked by remember { mutableStateOf(false) }
    var etcText by remember { mutableStateOf("") }

    var extraExperience by remember { mutableStateOf("") }

    var consentChecked by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScreenBg)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        val healthCondition = buildHealthCondition(
                            flags = healthFlags,
                            etcChecked = etcChecked,
                            etcText = etcText
                        )
                        onSubmit(healthCondition)
                    },
                    enabled = consentChecked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.48.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFBFD4FF),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    contentPadding = PaddingValues(vertical = 9.dp)
                ) {
                    Text(
                        text = "지원서 제출",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = Letter
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            /* 상단 헤더 */
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(0.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
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
                                .clickable { onBackClick() },
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
                        text = "지원서 작성",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = Letter,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, bottom = 16.dp)
                    )
                }
            }

            /* 지원내용 섹션 */
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
                ) {
                    Text(
                        "지원내용",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = Letter,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(20.dp))
                    InfoRow("회사명", posting.orgName, Color(0xFF848484), Color.Black)
                    Spacer(Modifier.height(12.dp))
                    InfoRow("업무내용", posting.task, Color(0xFF848484), Color.Black)
                }
            }

            Spacer(Modifier.height(20.dp))

            /* 기본정보 */
            SectionCard(
                title = "기본정보",
                titleBottomSpacing = 20.dp
            ) {
                InfoRow("이름", applicant.name, Color(0xFF848484), Color.Black)
                InfoRow("연락처", applicant.tel, Color(0xFF848484), Color.Black)
                InfoRow("경력", applicant.experienceSummary, Color(0xFF848484), Color.Black)
            }

            Spacer(Modifier.height(20.dp))

            /* 건강사항 */
            SectionCard(
                title = "건강사항",
                titleBottomSpacing = 20.dp
            ) {
                healthFlags.forEachIndexed { idx, flag ->
                    CheckItem(
                        label = flag.label,
                        checked = flag.checked,
                        onToggle = { healthFlags[idx] = flag.copy(checked = !flag.checked) }
                    )
                }
                CheckItemWithText(
                    label = applicant.healthEtcPlaceholder,
                    checked = etcChecked,
                    text = etcText,
                    onToggle = { etcChecked = !etcChecked },
                    onTextChange = { etcText = it }
                )
            }

            Spacer(Modifier.height(20.dp))

            /* 경력사항 */
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(0.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "경력사항",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = Letter,
                            color = Color.Black
                        )

                        Spacer(Modifier.weight(1f))

                        Row(
                            modifier = Modifier
                                .background(Color(0x2B005FFF), RoundedCornerShape(31.dp))
                                .clickable { /* +추가 눌렀을 때 확장 로직 필요하면 추가 */ }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "+ 추가",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = Letter,
                                color = BrandBlue
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        GraySingleLineInput(
                            value = extraExperience,
                            onValueChange = { extraExperience = it },
                            placeholder = "추가 경력을 적어주세요",
                            enabled = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { consentChecked = !consentChecked },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                CheckDot(consentChecked)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "(필수) 개인정보 제 3자 제공 동의",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = Letter,
                                lineHeight = 24.sp,
                                color = Color(0xFFFF2F00)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ============ 공통 컴포넌트 ============ */

@Composable
fun SectionCard(
    title: String,
    outerPadding: PaddingValues = PaddingValues(0.dp),
    titleBottomSpacing: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(outerPadding),
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = Letter,
                color = Color.Black
            )
            Spacer(Modifier.height(titleBottomSpacing))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(120.dp)) {
            Text(
                text = label,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = Letter,
                color = labelColor
            )
        }
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = Letter,
            color = valueColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CheckItem(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.87.dp)
                .background(Color(0xFFD9D9D9), CircleShape)
                .then(
                    if (checked) Modifier.border(2.dp, BrandBlue, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(14.44.dp)
                    .background(
                        if (checked) BrandBlue else Color(0xFFB5B5B5),
                        CircleShape
                    )
            )
        }
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = Letter,
            color = Color.Black
        )
    }
}

@Composable
fun CheckDot(checked: Boolean) {
    Image(
        painter = painterResource(
            if (checked) R.drawable.autologin_checked
            else R.drawable.autologin_unchecked
        ),
        contentDescription = null,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
fun CheckItemWithText(
    label: String,
    checked: Boolean,
    text: String,
    onToggle: () -> Unit,
    onTextChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onToggle)
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.87.dp)
                    .background(Color(0xFFD9D9D9), CircleShape)
                    .then(
                        if (checked) Modifier.border(2.dp, BrandBlue, CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.44.dp)
                        .background(
                            if (checked) BrandBlue else Color(0xFFB5B5B5),
                            CircleShape
                        )
                )
            }
        }

        Spacer(Modifier.width(11.dp))

        GraySingleLineInput(
            value = text,
            onValueChange = onTextChange,
            placeholder = label,
            enabled = checked,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun GraySingleLineInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(41.dp)
            .background(Color(0xFFEFEFEF), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = Letter,
                color = Color(0xFFA6A6A6)
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = Letter,
                color = if (enabled) Color.Black else Color(0xFFA6A6A6)
            ),
            cursorBrush = SolidColor(BrandBlue),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun formatPhoneNumber(raw: String?): String {
    val digits = raw
        ?.filter { it.isDigit() }
        .orEmpty()
    return if (digits.length == 11 && digits.startsWith("010")) {
        "010-${digits.substring(3, 7)}-${digits.substring(7, 11)}"
    } else {
        raw.orEmpty()
    }
}

private fun buildHealthCondition(
    flags: List<HealthFlag>,
    etcChecked: Boolean,
    etcText: String
): String {
    val selected = flags
        .filter { it.checked }
        .map { it.label.trim() }

    val etc = if (etcChecked && etcText.isNotBlank()) {
        etcText.trim()
    } else {
        null
    }

    return (selected + listOfNotNull(etc))
        .joinToString(separator = ", ")
        .ifBlank { "특이사항 없음" }
}
