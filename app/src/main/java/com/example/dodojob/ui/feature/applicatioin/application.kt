package com.example.dodojob.ui.feature.application

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/* ================= 공통 색 ================ */
private val ScreenBg = Color(0xFFF1F5F7)

/* =============== 가라 DB =============== */
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

object ApplicantFakeDb {
    fun getApplicant(): ApplicantProfile = ApplicantProfile(
        name = "홍길동",
        tel = "010-1234-5678",
        experienceSummary = "고객대응 업무 경험",
        healthFlags = listOf(
            HealthFlag("오래 서 있기 어려움", false),
            HealthFlag("무거운 물건 들기 어려움", false),
            HealthFlag("시력 문제 있음", false),
        ),
        healthEtcPlaceholder = "기타"
    )
}

data class CompanyPosting(
    val orgName: String,
    val task: String
)

object PostingFakeDb {
    fun getPosting(): CompanyPosting = CompanyPosting(
        orgName = "모던하우스",
        task = "매장운영 및 관리"
    )
}

/* =============== Route =============== */
object ApplyRoute { const val path = "application" }

/* ============ Entry ============ */
@Composable
fun ApplicationRoute(nav: NavController) {
    val applicant = remember { ApplicantFakeDb.getApplicant() }
    val posting   = remember { PostingFakeDb.getPosting() }

    ApplicationScreen(
        applicant = applicant,
        posting = posting,
        onBackClick = { nav.popBackStack() },
        onSubmit = { /* TODO: 제출 처리 */ }
    )
}

/* ============ Screen ============ */
@Composable
fun ApplicationScreen(
    applicant: ApplicantProfile,
    posting: CompanyPosting,
    onBackClick: () -> Unit,
    onSubmit: () -> Unit = {}
) {
    // 건강사항 상태
    val healthFlags = remember { mutableStateListOf<HealthFlag>().apply { addAll(applicant.healthFlags) } }
    var etcChecked by remember { mutableStateOf(false) }
    var etcText by remember { mutableStateOf("") }

    // 경력 추가 상태
    var showExpInput by remember { mutableStateOf(false) }
    var extraExperience by remember { mutableStateOf("") }

    // 동의 체크
    var consentChecked by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onSubmit,
                    enabled = consentChecked,
                    modifier = Modifier
                        .width(326.15.dp)
                        .height(54.48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF005FFF),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFBFD4FF),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    contentPadding = PaddingValues(horizontal = 109.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = "지원서 제출",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.019).em
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
            /* 상단 */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 8.dp, top = 12.dp, bottom = 8.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = "뒤로가기", tint = Color.Black)
                    }
                }
                Text(
                    text = "지원서 작성",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.019).em,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            /* 지원내용 카드 */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                ) {
                    Text("지원내용", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.019).em, color = Color.Black)
                    Spacer(Modifier.height(16.dp))
                    InfoRow("회사명", posting.orgName, Color(0xFF848484), Color.Black)
                    Spacer(Modifier.height(12.dp))
                    InfoRow("업무내용", posting.task, Color(0xFF848484), Color.Black)
                }
            }

            Spacer(Modifier.height(20.dp))

            /* 기본정보 */
            SectionCard(title = "기본정보", outerPadding = PaddingValues(start = 16.dp, end = 16.dp)) {
                InfoRow("이름", applicant.name, Color(0xFF848484), Color.Black, gapDp = 70.dp)
                InfoRow("연락처", applicant.tel, Color(0xFF848484), Color.Black, gapDp = 53.dp)
                InfoRow("경력", applicant.experienceSummary, Color(0xFF848484), Color.Black, gapDp = 70.dp)
            }

            Spacer(Modifier.height(20.dp))

            /* 건강사항 */
            SectionCard(title = "건강사항", outerPadding = PaddingValues(start = 16.dp, end = 16.dp)) {
                healthFlags.forEachIndexed { idx, flag ->
                    CheckItem(
                        label = flag.label,
                        checked = flag.checked,
                        onToggle = { healthFlags[idx] = flag.copy(checked = !flag.checked) }
                    )
                    if (idx < healthFlags.lastIndex) Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(10.dp))
                CheckItemWithText(
                    label = applicant.healthEtcPlaceholder, // placeholder만 사용
                    checked = etcChecked,
                    text = etcText,
                    onToggle = { etcChecked = !etcChecked },
                    onTextChange = { etcText = it }
                )
            }

            Spacer(Modifier.height(20.dp))

            /* 경력사항 */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 16.dp, top = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("경력사항", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.Black, modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .background(Color(0x2B005FFF), RoundedCornerShape(31.dp))
                            .clickable { showExpInput = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("＋ 추가", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF005FFF))
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 16.dp, bottom = 20.dp, top = 12.dp)
                ) {
                    if (showExpInput) {
                        OutlinedTextField(
                            value = extraExperience,
                            onValueChange = { extraExperience = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 64.dp),
                            placeholder = { Text("추가 경력을 적어주세요") },
                            singleLine = false,
                            minLines = 2,
                            shape = RoundedCornerShape(10.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = ScreenBg,
                                unfocusedContainerColor = ScreenBg,
                                disabledContainerColor = ScreenBg,
                                errorContainerColor = ScreenBg,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    // 동의 라인 (커스텀 점 + 텍스트, 전체 클릭 토글)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { consentChecked = !consentChecked },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CheckDot(if (consentChecked) CheckState.ValidBlue else CheckState.NeutralGrey)
                        Spacer(Modifier.width(8.dp))
                        Text("(필수) 개인정보 제 3자 제공 동의", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ============ 재사용 컴포넌트 (최상위) ============ */

@Composable
fun SectionCard(
    title: String,
    outerPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(outerPadding)
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
        ) {
            Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    gapDp: Dp = 24.dp
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = labelColor)
        Spacer(Modifier.width(gapDp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = valueColor)
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
                .size(28.dp)
                .background(Color(0xFFD9D9D9), CircleShape)
                .border(1.dp, if (checked) Color(0xFF005FFF) else Color(0xFFB5B5B5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Box(modifier = Modifier.size(14.dp).background(Color(0xFF005FFF), CircleShape))
            }
        }
        Text(text = label, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}

/* 체크 점 */
private enum class CheckState { NeutralGrey, ValidBlue }

@Composable
private fun CheckDot(state: CheckState) {
    when (state) {
        CheckState.NeutralGrey -> {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color(0xFFE6E6E6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(14.dp))
            }
        }
        CheckState.ValidBlue -> {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color(0xFF2A77FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

/** 기타: 체크 오른쪽 즉시 입력창(라벨 텍스트 없음) */
@Composable
fun CheckItemWithText(
    label: String, // placeholder 용
    checked: Boolean,
    text: String,
    onToggle: () -> Unit,
    onTextChange: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // 체크
        Row(
            modifier = Modifier
                .clickable(onClick = onToggle)
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFFD9D9D9), CircleShape)
                    .border(1.dp, if (checked) Color(0xFF005FFF) else Color(0xFFB5B5B5), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Box(modifier = Modifier.size(14.dp).background(Color(0xFF005FFF), CircleShape))
                }
            }
        }

        Spacer(Modifier.width(10.dp))

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 44.dp),
            enabled = checked,
            singleLine = true,
            placeholder = { Text("기타 입력") },
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ScreenBg,
                unfocusedContainerColor = ScreenBg,
                disabledContainerColor = ScreenBg,
                errorContainerColor = ScreenBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            )
        )
    }
}