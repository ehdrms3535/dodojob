package com.example.dodojob.ui.feature.announcement

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.dodojob.R
import com.example.dodojob.dao.getannouncebycom
import com.example.dodojob.data.announcement.fullannouncement.fetchAnnouncementFull
import com.example.dodojob.data.announcement.fullannouncement.AnnouncementFullRow
import com.example.dodojob.session.CurrentUser
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.dodojob.session.AnnouncementSession
import java.time.LocalTime
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/* -------- Colors -------- */
private val Blue     = Color(0xFF005FFF)
private val TextGray = Color(0xFF828282)
private val BgGray   = Color(0xFFFFFFFF)
private val CardBg   = Color.White
private val LineGray = Color(0xFFE6EDF7)



fun parseKoreanTime(text: String): LocalTime {
    val trimmed = text.replace(" ", "")

    val isPM = trimmed.startsWith("오후")
    val timePart = trimmed.removePrefix("오전").removePrefix("오후")

    val parts = timePart.split("시", "분")
    val hour = parts[0].toInt()
    val minute = parts[1].toInt()

    val adjustedHour = when {
        isPM && hour != 12 -> hour + 12   // 오후 1–11시는 +12
        !isPM && hour == 12 -> 0          // 오전 12시는 00시
        else -> hour
    }

    return LocalTime.of(adjustedHour, minute)
}

fun toKoreanTimeText(time: LocalTime): String {
    val isPM = time.hour >= 12
    val hour12 = when {
        time.hour == 0 -> 12
        time.hour > 12 -> time.hour - 12
        else -> time.hour
    }
    val ampm = if (isPM) "오후" else "오전"
    return "$ampm ${hour12}시 ${time.minute}분"
}

fun generateSerial(): String {
    val now = LocalDate.now()
    val datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    val random = (1..999).random().toString().padStart(3, '0')
    return "SN-$datePart-$random"
}
/* ====== Screen ====== */
@Composable
fun Announcement6Screen(
    onManageClick: () -> Unit = {},
    onNewPostClick: () -> Unit = {}
) {
    val scroll = rememberScrollState()

    Scaffold(
        containerColor = BgGray,
        /* 하단 고정 바 */
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBg)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onManageClick,
                        modifier = Modifier
                            .height(60.dp) // 세로 키움
                            .weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White)
                    ) {
                        Text("내 공고 관리", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onNewPostClick,
                        modifier = Modifier
                            .height(60.dp) // 세로 키움
                            .weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp, brush = SolidColor(Blue)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = CardBg)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Blue)
                        Spacer(Modifier.width(6.dp))
                        Text("새 공고 작성", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Blue)
                    }
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            /* Header */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .background(CardBg)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("공고등록", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }

            /* 본문 스크롤 */
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scroll)
            ) {
                // 섹션 1: 완료 메시지
                // ▶ 헤더와 아이콘 사이 간격 ↑ (topPadding = 36.dp)
                // ▶ 다음 카드와 간격 ↓ (bottomPadding = 8.dp)
                AnnSectionCard(topPadding = 8.dp, bottomPadding = 6.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.complete_image),
                            contentDescription = "완료",
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.height(15.dp))
                        Text(
                            text = "공고 등록 완료!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "축하합니다! 공고가 성공적으로 등록되었어요.\n이제 경험이 풍부한 멋진 시니어 분들이 찾아올 거예요!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 섹션 2: 상세정보 (제목을 카드 안으로)
                // 섹션 2: 상세정보 (제목을 카드 안으로)
                AnnSectionCard(bottomPadding = 0.dp) {
                    // ▶ 카드 안 공통 인너 패딩: 좌우/상하 12.dp
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Blue, RoundedCornerShape(12.dp))
                            .background(CardBg)
                            .padding(horizontal = 10.dp, vertical = 12.dp)
                    ) {
                        // 카드 안의 제목 행
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "   등록된 공고 상세정보",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }

                        // ---------- 상태 ----------
                        var announcement by remember { mutableStateOf<AnnouncementFullRow?>(null) }

                        // ---------- 데이터 로딩 ----------
                        LaunchedEffect(Unit) {
                            val comid = CurrentUser.companyid
                            val announceId = getannouncebycom(comid)?.id ?: return@LaunchedEffect

                            val data = fetchAnnouncementFull(announceId)
                            announcement = data
                        }

                        // ---------- null 분기 ----------
                        val ann = announcement
                        if (ann == null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "공고 정보를 불러오는 중입니다...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                        } else {
                            // ann 은 여기서부터 non-null

                            val companyName = ann.company_name ?: "(회사명 없음)"
                            val job = ann.work_category ?: "(업무 없음)"

                            // 지원 방법
                            val applyMethod = AnnouncementSession.applyMethod
                            val method = when (applyMethod?.name) {
                                "PhoneSms" -> "문자/전화 지원"
                                "PhoneOnly" -> "전화 지원"
                                "SmsOnly" -> "문자 지원"
                                else -> "온라인 지원"
                            }

                            // 근무시간
                            val starttime = ann.starttime
                            val endtime = ann.endtime
                            val timeText = if (!starttime.isNullOrBlank() && !endtime.isNullOrBlank()) {
                                try {
                                    val start = parseKoreanTime(starttime)
                                    val end = parseKoreanTime(endtime)
                                    val diffMinutes = Duration.between(start, end).toMinutes()
                                    "$starttime ~ $endtime (${diffMinutes}분)"
                                } catch (e: Exception) {
                                    "$starttime ~ $endtime"
                                }
                            } else {
                                "시간 정보 없음"
                            }

                            // 급여
                            val moneyType = ann.salary_type ?: ""
                            val money = ann.salary_amount ?: 0
                            val moneyText = if (moneyType.isBlank()) {
                                "급여 정보 없음"
                            } else {
                                "$moneyType ${money}원"
                            }

                            // 공고번호는 한 번만 생성
                            val serial by remember { mutableStateOf(generateSerial()) }

                            InfoRow("공고 제목", "${companyName}에서 $job")
                            BlueDivider()
                            InfoRow("회사명", companyName)
                            BlueDivider()
                            InfoRow("공고번호", serial)
                            BlueDivider()
                            InfoRow("등록일시", ann.created_at?.toString() ?: "-")
                            BlueDivider()
                            InfoRow("근무지역", ann.company_locate ?: "-")
                            BlueDivider()
                            InfoRow("근무시간", timeText)
                            BlueDivider()
                            InfoRow("급여", moneyText)
                            BlueDivider()
                            InfoRow("지원 방법", method)
                            BlueDivider()

                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/* --------- Reusable --------- */

@Composable
private fun BlueDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = LineGray,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 12.dp), // 16→12 (카드 인너패딩과 합쳐 과도해지지 않게)
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.width(88.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Blue,
            textAlign = TextAlign.Right,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** 섹션 카드(흰 배경) */
@Composable
fun AnnSectionCard(
    topPadding: Dp = 20.dp,
    bottomPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .padding(top = topPadding, bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(horizontal = 16.dp),
            content = content
        )
    }
}
