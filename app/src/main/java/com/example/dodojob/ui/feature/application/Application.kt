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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.dodojob.navigation.Route

/* ================= 공통 색 ================ */
private val ScreenBg = Color(0xFFF1F5F7)
private val BrandBlue = Color(0xFF005FFF)
private val Letter = (-0.019f).em

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
            HealthFlag("무거운 짐 들기 어려움", false),
            HealthFlag("시력 보조 필요", false),
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
    val posting = remember { PostingFakeDb.getPosting() }

    ApplicationScreen(
        applicant = applicant,
        posting = posting,
        onBackClick = { nav.popBackStack() },
        onSubmit = {
            nav.navigate(Route.ApplicationCompleted.path)
        }
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
    var showExpInput by remember { mutableStateOf(false) } // 지금은 minLines용이었지만 경력 추가 버튼 클릭 상태로만 사용
    var extraExperience by remember { mutableStateOf("") }

    // 동의 체크
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
                    onClick = onSubmit,
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
            /* 상단 헤더 (JobDetail 과 동일한 구조) */
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

            /* 지원내용 섹션 (상단 정보 카드) */
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

            /* 기본정보 – 제목 아래 여백만 늘리고 싶으면 여기 titleBottomSpacing 조절 */
            SectionCard(
                title = "기본정보",
                titleBottomSpacing = 20.dp    // 🔹 기본정보 ↔ 이름 사이만 20dp
            ) {
                InfoRow("이름", applicant.name, Color(0xFF848484), Color.Black)
                InfoRow("연락처", applicant.tel, Color(0xFF848484), Color.Black)
                InfoRow("경력", applicant.experienceSummary, Color(0xFF848484), Color.Black)
            }

            Spacer(Modifier.height(20.dp))

            /* 건강사항 – 기존 간격 그대로 (titleBottomSpacing 기본값 사용) */
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
                    label = applicant.healthEtcPlaceholder, // placeholder 텍스트
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
                                .clickable { showExpInput = true }
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
                        // 🔹 OutlinedTextField → 커스텀 얇은 텍스트박스로 교체
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

/* ============ 재사용 컴포넌트 (최상위) ============ */

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
            // 🔹 제목 바로 아래 간격만 따로 조절
            Spacer(Modifier.height(titleBottomSpacing))
            // 🔹 내용끼리 간격은 기존처럼 12dp 유지
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
        // 왼쪽 라벨 고정 폭
        Box(modifier = Modifier.width(120.dp)) {
            Text(
                text = label,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = Letter,
                color = labelColor
            )
        }
        // 오른쪽 값
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

/* 체크 점: autologin_checked / autologin_unchecked 사용 */
@Composable
private fun CheckDot(checked: Boolean) {
    Image(
        painter = painterResource(
            if (checked) R.drawable.autologin_checked
            else R.drawable.autologin_unchecked
        ),
        contentDescription = null,
        modifier = Modifier.size(24.dp) // 텍스트와 수평 정렬 잘 맞도록
    )
}

/** 기타: 체크 오른쪽 즉시 입력창 */
@Composable
fun CheckItemWithText(
    label: String, // placeholder 용
    checked: Boolean,
    text: String,
    onToggle: () -> Unit,
    onTextChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 체크
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

        // 🔹 기타 입력칸도 커스텀 텍스트박스로 교체 (높이 41dp 고정)
        GraySingleLineInput(
            value = text,
            onValueChange = onTextChange,
            placeholder = label,
            enabled = checked,
            modifier = Modifier.weight(1f)
        )
    }
}

/* ===== 커스텀 얇은 텍스트박스 (41dp, Figma 스타일) ===== */
@Composable
private fun GraySingleLineInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(41.dp) // Figma 높이 그대로
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
