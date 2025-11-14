@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.dodojob.ui.feature.employ

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.data.career.CareerModels
import com.example.dodojob.data.license.LicenseModels
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.session.JobBits
import kotlinx.serialization.Serializable
import com.example.dodojob.dao.*
import com.example.dodojob.ui.feature.application.formatPhoneNumber
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest


/* ===== 컬러 ===== */
private val BrandBlue = Color(0xFF005FFF)
private val TextGray  = Color(0xFF828282)
private val LineGray  = Color(0xFFDDDDDD)
private val LabelGray = Color(0xFF9C9C9C)
private val BgGray    = Color(0xFFF1F5F7)

/* ===== 폰트 ===== */
private val PretendardSemi = FontFamily(Font(R.font.pretendard_semibold, FontWeight.SemiBold))
private val PretendardMed  = FontFamily(Font(R.font.pretendard_medium,  FontWeight.Medium))

/* ===== 기본값 ===== */
object ApplicantFakeDB {
    val defaultJob = "서비스업"
}

/* ===== Supabase 매핑용 데이터 클래스 ===== */
@Serializable
data class UserTmpRow(
    val username: String,
    val name: String? = null,
    val gender: String? = null,
    val birthdate: String? = null,
    val region: String? = null,
    val phone: String? = null,
    val email: String? = null
)

/* ===== 공통 컴포넌트 ===== */
@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) { content() }
}

@Composable
private fun SectionTitle(
    title: String,
    iconRes: Int,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .clickable { onToggle() }
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontFamily = PretendardSemi,
                color = Color.Black
            )
        }
        Spacer(Modifier.height(16.dp))
        ThinDivider(insetStart = 4.dp, insetEnd = 4.dp)
    }
}

@Composable
private fun KeyValueRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    startPadding: Dp = 8.dp,
    endPadding: Dp = 8.dp
) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = startPadding, end = endPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontSize = 16.sp,
                fontFamily = PretendardSemi,
                color = LabelGray,
                modifier = Modifier.weight(1f)
            )
            Text(
                value,
                fontSize = 16.sp,
                fontFamily = PretendardSemi,
                color = valueColor,
                textAlign = TextAlign.Right
            )
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun ThinDivider(insetStart: Dp = 4.dp, insetEnd: Dp = 4.dp) {
    Spacer(Modifier.height(6.dp))
    Divider(
        modifier = Modifier.padding(start = insetStart, end = insetEnd),
        color = LineGray,
        thickness = 1.dp
    )
    Spacer(Modifier.height(6.dp))
}

/* ===== 스크롤되는 헤더 ===== */
@Composable
private fun ScrollHeader(
    title: String,
    onBack: () -> Unit
) {
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .height(70.dp)
                .padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBackIosNew,
                contentDescription = "뒤로가기",
                tint = Color.Unspecified,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(20.dp)
                    .clickable { onBack() }
            )
            Text(
                text = title,
                fontSize = 28.sp,
                fontFamily = PretendardSemi,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center),
                maxLines = 1
            )
        }
        Divider(color = LineGray, thickness = 1.dp)
    }
}

/* ===== 메인 진입점 ===== */
@Composable
fun ApplicantInformationScreen(
    navController: NavController,
    username: String
) {
    Log.d("ApplicantInfo", "[SCREEN] start, username param = $username")

    val client = LocalSupabase.current
    val scroll = rememberScrollState()
    val context = LocalContext.current



    var personalExpanded by remember { mutableStateOf(true) }
    var careerExpanded   by remember { mutableStateOf(true) }
    var licenseExpanded  by remember { mutableStateOf(true) }
    var hopeExpanded     by remember { mutableStateOf(true) }
    var userImageUrl by remember { mutableStateOf<String?>(null) }


    var selectedJob by remember { mutableStateOf(ApplicantFakeDB.defaultJob) }

    var userInfo by remember { mutableStateOf<UserTmpRow?>(null) }
    var careers by remember { mutableStateOf<List<CareerModels>>(emptyList()) }
    var licenses by remember { mutableStateOf<List<LicenseModels>>(emptyList()) }
    var hopeJobs by remember { mutableStateOf<List<String>>(emptyList()) }

    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(username) {
        loading = true
        errorMessage = null

        Log.d("ApplicantInfo", "[LOAD] start (REST), username = $username")

        runCatching {
            // 1) 인적사항
            val u = fetchUserTmp(username)
            userInfo = u

            // 1-1) 프로필 이미지
            val imgRow = fetchUserImage(username)
            userImageUrl = imgRow?.imgUrl

            // 2) 경력
            val cList = fetchCareers(username)
            careers = cList

            // 3) 자격증
            val lList = fetchLicenses(username)
            licenses = lList

            // 4) 희망직무
            val jobRow = fetchJobtype(username)

            if (jobRow != null) {
                val jobtalent  = JobBits.parse(JobBits.JobCategory.TALENT,  jobRow.job_talent)
                val jobmanage  = JobBits.parse(JobBits.JobCategory.MANAGE,  jobRow.job_manage)
                val jobservice = JobBits.parse(JobBits.JobCategory.SERVICE, jobRow.job_service)
                val jobcare    = JobBits.parse(JobBits.JobCategory.CARE,    jobRow.job_care)

                val allJobs = sequenceOf(
                    jobtalent,
                    jobmanage,
                    jobservice,
                    jobcare
                ).flatten()
                    .filter { it.isNotBlank() }
                    .distinct()
                    .toList()

                hopeJobs = allJobs
                    .shuffled()
                    .take(minOf(4, allJobs.size))
            } else {
                hopeJobs = emptyList()
            }

        }.onFailure { e ->
            Log.e("ApplicantInfo", "[ERROR] REST load applicant failed", e)
            errorMessage = e.message ?: e.toString()
        }

        loading = false
    }



    val triplescareer: List<Triple<String?, String?, String?>> =
        careers.map { career ->
            Triple(career.title, career.startDate, career.endDate)
        }

    val triplelicense: List<Triple<String?, String?, String?>> =
        licenses.map { lic ->
            Triple(lic.location, lic.name, lic.number)
        }


    Scaffold(
        containerColor = BgGray,
        topBar = { }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // 스크롤 헤더
            ScrollHeader(
                title = "지원자 정보",
                onBack = { navController.popBackStack() }
            )

            when {
                loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red,
                            fontFamily = PretendardMed
                        )
                    }
                }

                else -> {
                    // 실제 내용
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(scroll)
                    ) {
                        Spacer(Modifier.height(12.dp))

                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            /* ===== 인적사항 ===== */
                            SectionCard {
                                SectionTitle(
                                    title = " 인적사항",
                                    iconRes = R.drawable.identification,
                                    expanded = personalExpanded,
                                    onToggle = { personalExpanded = !personalExpanded }
                                )
                                if (personalExpanded) {
                                    Spacer(Modifier.height(16.dp))
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(96.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color(0xFFE0E0E0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (userImageUrl.isNullOrBlank()) {
                                                Text(
                                                    text = "사진 없음",
                                                    fontSize = 10.sp,
                                                    color = TextGray,
                                                    fontFamily = PretendardMed,
                                                    textAlign = TextAlign.Center
                                                )
                                            } else {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(context)
                                                        .data(userImageUrl)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = "프로필 사진",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(18.dp))
                                    }

                                    Spacer(Modifier.height(16.dp))

                                    val u = userInfo
                                    KeyValueRow("이름",     u?.name ?: "-")
                                    KeyValueRow("생년월일", u?.birthdate ?: "-")
                                    KeyValueRow("전화번호", formatPhoneNumber(u?.phone) ?: "-")
                                    KeyValueRow("주소",     u?.region ?: "-")
                                    KeyValueRow("이메일",   u?.email ?: "-")
                                }
                            }

                            Spacer(Modifier.height(28.dp))

                            /* ===== 경력 ===== */
                            SectionCard {
                                SectionTitle(
                                    title = " 경력",
                                    iconRes = R.drawable.career,
                                    expanded = careerExpanded,
                                    onToggle = { careerExpanded = !careerExpanded }
                                )
                                if (careerExpanded) {
                                    if (triplescareer.isEmpty()) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = "등록된 경력이 없습니다.",
                                            fontSize = 14.sp,
                                            fontFamily = PretendardMed,
                                            color = TextGray,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                    } else {
                                        triplescareer.forEachIndexed { i, (title, start, end) ->
                                            if (i > 0) {
                                                Spacer(Modifier.height(16.dp))
                                                ThinDivider()
                                                Spacer(Modifier.height(16.dp))
                                            } else {
                                                Spacer(Modifier.height(8.dp))
                                            }
                                            CareerItem(
                                                title = title.orEmpty(),
                                                start = start.orEmpty(),
                                                end   = end.orEmpty()
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                    }
                                }
                            }

                            Spacer(Modifier.height(28.dp))

                            /* ===== 자격증 ===== */
                            SectionCard {
                                SectionTitle(
                                    title = " 자격증",
                                    iconRes = R.drawable.license,
                                    expanded = licenseExpanded,
                                    onToggle = { licenseExpanded = !licenseExpanded }
                                )
                                if (licenseExpanded) {
                                    if (triplelicense.isEmpty()) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = "등록된 자격증이 없습니다.",
                                            fontSize = 14.sp,
                                            fontFamily = PretendardMed,
                                            color = TextGray,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                    } else {
                                        triplelicense.forEachIndexed { i, (org, title, code) ->
                                            if (i > 0) {
                                                Spacer(Modifier.height(16.dp))
                                                ThinDivider()
                                                Spacer(Modifier.height(16.dp))
                                            } else {
                                                Spacer(Modifier.height(8.dp))
                                            }
                                            LicenseItem(
                                                org   = org.orEmpty(),
                                                title = title.orEmpty(),
                                                code  = code.orEmpty()
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                    }
                                }
                            }

                            Spacer(Modifier.height(28.dp))

                            /* ===== 희망직무 ===== */
                            SectionCard {
                                SectionTitle(
                                    title = " 희망직무",
                                    iconRes = R.drawable.hope_work,
                                    expanded = hopeExpanded,
                                    onToggle = { hopeExpanded = !hopeExpanded }
                                )
                                if (hopeExpanded) {
                                    Spacer(Modifier.height(20.dp))

                                    val jobsForChips = hopeJobs.ifEmpty {
                                        listOf("서비스업", "교육/강의", "관리/운영", "돌봄서비스")
                                    }
                                    val row1 = jobsForChips.take(2)
                                    val row2 = jobsForChips.drop(2).take(2)

                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        row1.getOrNull(0)?.let { title ->
                                            JobChip(
                                                title = title,
                                                desc = "매장관리, 고객 응대",
                                                selected = selectedJob == title,
                                                onClick = { selectedJob = title },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        row1.getOrNull(1)?.let { title ->
                                            JobChip(
                                                title = title,
                                                desc = "강사, 지도사",
                                                selected = selectedJob == title,
                                                onClick = { selectedJob = title },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        row2.getOrNull(0)?.let { title ->
                                            JobChip(
                                                title = title,
                                                desc = "시설, 인력관리",
                                                selected = selectedJob == title,
                                                onClick = { selectedJob = title },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        row2.getOrNull(1)?.let { title ->
                                            JobChip(
                                                title = title,
                                                desc = "방문, 요양, 돌봄",
                                                selected = selectedJob == title,
                                                onClick = { selectedJob = title },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(22.dp))
                        }
                    }
                }
            }
        }
    }
}

/* ===== 보조 컴포넌트 ===== */
@Composable
private fun CareerItem(title: String, start: String, end: String) {
    Column(Modifier.padding(horizontal = 8.dp)) {
        Text(title, fontSize = 18.sp, fontFamily = PretendardSemi, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(start, color = BrandBlue, fontSize = 14.sp, fontFamily = PretendardMed)
            Text(" ~ ",  color = TextGray,  fontSize = 14.sp, fontFamily = PretendardMed)
            Text(end,   color = BrandBlue, fontSize = 14.sp, fontFamily = PretendardMed)
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun LicenseItem(org: String, title: String, code: String) {
    Column(Modifier.padding(horizontal = 8.dp)) {
        Text(org, fontSize = 12.sp, color = Color(0xFF616161), fontFamily = PretendardMed)
        Spacer(Modifier.height(6.dp))
        Text(title, fontSize = 18.sp, fontFamily = PretendardSemi, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Text(
            buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = LabelGray,
                        fontFamily = PretendardMed,
                        fontSize = 14.sp
                    )
                ) {
                    append("자격번호 ")
                }
                withStyle(
                    SpanStyle(
                        color = BrandBlue,
                        fontFamily = PretendardMed,
                        fontSize = 14.sp
                    )
                ) {
                    append(code)
                }
            }
        )
        Spacer(Modifier.height(2.dp))
    }
}

/* ===== 칩(설명 1~2줄) ===== */
private val ChipRadius = 14.dp
private val ChipBorder = Color(0xFFD9D9D9)
private val ChipTitleGray = Color(0xFF6B7280)
private val ChipDescGray  = Color(0xFF9CA3AF)

@Composable
private fun JobChip(
    title: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(ChipRadius)
    val bg   = if (selected) BrandBlue else Color.White
    val tCol = if (selected) Color.White else ChipTitleGray
    val dCol = if (selected) Color.White.copy(alpha = 0.9f) else ChipDescGray

    Column(
        modifier = modifier
            .border(
                width = 1.5.dp,
                color = if (selected) Color.Transparent else ChipBorder,
                shape = shape
            )
            .clip(shape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .heightIn(min = 64.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontFamily = PretendardSemi,
            color = tCol,
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = desc,
            fontSize = 12.sp,
            fontFamily = PretendardMed,
            color = dCol,
            lineHeight = 14.sp,
            maxLines = 2
        )
    }
}

