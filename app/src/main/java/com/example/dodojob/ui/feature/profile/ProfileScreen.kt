package com.example.dodojob.ui.feature.profile

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.dao.CountRecentView
import com.example.dodojob.dao.getSeniorInformation
import com.example.dodojob.data.senior.SeniorJoined
import com.example.dodojob.session.CurrentUser
import com.example.dodojob.ui.components.AppBottomBar

import com.example.dodojob.dao.fetchAppliedCount
import com.example.dodojob.dao.fetchAnnounceSeniorCount

import kotlinx.datetime.Month

@Composable
fun ProfileRoute(nav: NavController) {
    var showLogout by remember { mutableStateOf(false) }
    var showLeave by remember { mutableStateOf(false) }

    val username = CurrentUser.username

    var senior by remember { mutableStateOf<SeniorJoined?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    var recentCount by remember { mutableStateOf(0L) }
    var recentError by remember { mutableStateOf<String?>(null) }


    var applyCount by remember { mutableStateOf(0L)}
    var resumeViews by remember { mutableStateOf(0L)}

    LaunchedEffect(username) {
        loading = true
        error = null
        senior = null
        recentError = null

        // 1) 시니어 기본 정보 로드
        val s = runCatching {
            if (!username.isNullOrBlank()) getSeniorInformation(username) else null
        }.onFailure { t ->
            error = t.message ?: "알 수 없는 오류"
        }.getOrNull()

        senior = s

        // 2) 최근 본 공고 개수 로드
        if (!username.isNullOrBlank()) {
            runCatching {
                CountRecentView(username)
            }.onSuccess { cnt ->
                recentCount = cnt.toLong()
            }.onFailure { t ->
                recentError = t.message
            }
            runCatching {
                fetchAppliedCount(username)
            }.onSuccess { cnt ->
                applyCount = cnt.toLong()
            }.onFailure {
                applyCount = 0
            }
            runCatching {
                fetchAnnounceSeniorCount(username)
            }.onSuccess { cnt->
                resumeViews = cnt.toLong()
            }.onFailure {
                resumeViews=0
            }


        }



        loading = false
    }

    when {
        loading -> { LoadingOrErrorBox("프로필 정보를 불러오는 중입니다…"); return }
        error != null -> { LoadingOrErrorBox("불러오기 실패: $error"); return }
        username.isNullOrBlank() -> {
            LoadingOrErrorBox("로그인이 필요합니다.", "마이 탭의 전체 기능을 사용하려면 로그인하세요."); return
        }
        senior == null -> {
            LoadingOrErrorBox("사용자 정보를 찾을 수 없습니다.", "프로필을 먼저 등록해 주세요."); return
        }
    }

    val s = senior!!
    val displayName = s.user?.name ?: s.username
    //val applyCount = s.applyCount
    //val resumeViews = s.resumeViews
    val likedCount = s.likedCount
    val activityLevel = s.activityLevel
    val applyWithinYear = s.applyWithinYear
    val realWorkExpCount = s.realWorkExpCount
    val eduCompleted = s.eduCompleted
    val createdAt = s.user?.created_at

    val sb = StringBuilder()
    sb.append(
        createdAt?.substring(0, 4) + "." +
                createdAt?.substring(5, 7) + "." +
                createdAt?.substring(8, 10)
    )
    val joinedDate = sb.toString()

    ProfileScreen(
        name = displayName,
        applyCount = applyCount,
        resumeViews = resumeViews,
        recentCount = recentCount,
        likedCount = likedCount,
        activityLevel = activityLevel,
        onClickResumeCreate = {},
        onClickResumeManage = { nav.navigate("Resume") },
        onClickBookmarks = { nav.navigate("liked_job") },
        onClickRecent = { nav.navigate("recently_viewed") },
        onClickActivityLevel = {
            val payload = ActivityLevelData(
                name = displayName ?: "사용자",
                level = activityLevel.toInt().coerceIn(1, 3).toLong(),
                applyWithinYear = applyWithinYear,
                realWorkExpCount = realWorkExpCount,
                eduCompleted = eduCompleted,
                joinedDate = joinedDate
            )

            nav.currentBackStackEntry
                ?.savedStateHandle
                ?.set("activity_level_payload", payload)

            nav.navigate("activity_level")
        },
        onClickChangePw = { nav.navigate("change_password") },
        onClickLogout = { showLogout = true },
        onClickLeave = { showLeave = true },
        onClickApplyStatus = {
            nav.navigate("support")
        },
        onShortcut = { key ->
            when (key) {
                "home" -> nav.navigate("main") { launchSingleTop = true }
                "edu" -> nav.navigate("edu")
                "welfare" -> nav.navigate("welfare")
                "my" -> Unit
            }
        }
    )

    if (showLeave) {
        LeaveDialog(
            currentUsername = CurrentUser.username,
            nav = nav,
            onClosed = { showLeave = false }
        )
    }

    if (showLogout) {
        LogoutDialog(
            nav = nav,
            onClosed = { showLogout = false }
        )
    }
}

@Composable
private fun LoadingOrErrorBox(text: String, sub: String? = null) {
    val screenBg = Color(0xFFF1F5F7)
    Scaffold(
        containerColor = screenBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text(text, fontSize = 16.sp, color = Color(0xFF222222))
                if (sub != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(sub, fontSize = 13.sp, color = Color(0xFF777777))
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    name: String?,
    applyCount: Long,
    resumeViews: Long,
    recentCount: Long,
    likedCount: Long,
    activityLevel: Long,
    onClickResumeCreate: () -> Unit,
    onClickResumeManage: () -> Unit,
    onClickBookmarks: () -> Unit,
    onClickRecent: () -> Unit,
    onClickActivityLevel: () -> Unit,
    onClickChangePw: () -> Unit,
    onClickLogout: () -> Unit,
    onClickLeave: () -> Unit,
    onClickApplyStatus: () -> Unit,
    onShortcut: (String) -> Unit
) {
    val brandBlue = Color(0xFF005FFF)
    val screenBg = Color(0xFFF1F5F7)
    var notifOn by remember { mutableStateOf(true) }

    val levelInt = activityLevel.toInt().coerceIn(1, 3)
    val medalRes = when (levelInt) {
        1 -> R.drawable.red_medal
        2 -> R.drawable.yellow_medal
        else -> R.drawable.blue_medal
    }

    Scaffold(
        containerColor = screenBg,
        bottomBar = { AppBottomBar(current = "my", onClick = onShortcut) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 상단 그라데이션 헤더 (ActivityLevelScreen 과 위치 맞춤)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(264.dp)
                        .clip(
                            RoundedCornerShape(
                                bottomStart = 10.dp,
                                bottomEnd = 10.dp
                            )
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF005FFF),
                                    Color(0xFF76A9FF)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Spacer(modifier = Modifier.height(3.dp))
                        // 로고 + 알림 아이콘 (ActivityLevel 의 back 버튼 자리)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.profile_logo),
                                contentDescription = "프로필 로고",
                                modifier = Modifier.size(28.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(Modifier.weight(1f))
                            Image(
                                painter = painterResource(id = R.drawable.profile_bell),
                                contentDescription = "알림",
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // 프로필 사진 + 이름 + 메달 (ActivityLevel 과 동일한 레이아웃)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.senior_id),
                                contentDescription = "프로필 사진",
                                modifier = Modifier
                                    .size(104.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = " ${name ?: "사용자"}님",
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Image(
                                        painter = painterResource(id = medalRes),
                                        contentDescription = "활동 레벨 메달",
                                        modifier = Modifier
                                            .width(22.dp)
                                            .height(36.dp)
                                            .align(Alignment.CenterVertically)
                                            .offset(y = 3.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 헤더 위로 겹쳐 올라오는 이력서 카드 (ActivityLevel 의 배너 카드 위치와 동일)
                ResumeCard(
                    brandBlue = brandBlue,
                    applyCount = applyCount,
                    resumeViews = resumeViews,
                    onClickResumeCreate = onClickResumeCreate,
                    onClickResumeManage = onClickResumeManage,
                    onClickApplyStatus = onClickApplyStatus,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .offset(y = (-85).dp)
                )

                // 나머지 카드들 (ActivityLevel 의 "나의 활동" 카드와 유사 오프셋)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-70).dp)
                ) {
                    // 알림 카드
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp),
                        shape = RoundedCornerShape(7.38.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "알림",
                                fontSize = 24.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold
                            )
                            Switch(
                                checked = notifOn,
                                onCheckedChange = { notifOn = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = brandBlue,
                                    checkedTrackColor = Color(0xFFB2D4FF),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFE0E0E0)
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    SectionListCard(
                        title = "나의 활동",
                        items = listOf(
                            RowItem("최근 본 공고", suffix = "${recentCount}건", onClick = onClickRecent),
                            RowItem("좋아한 일자리", suffix = "${likedCount}건", onClick = onClickBookmarks),
                            RowItem("활동 레벨", onClick = onClickActivityLevel)
                        )
                    )

                    Spacer(Modifier.height(20.dp))

                    SectionListCard(
                        title = "개인정보 관리",
                        items = listOf(
                            RowItem("비밀번호 변경", onClick = onClickChangePw),
                            RowItem("로그아웃", onClick = onClickLogout),
                            RowItem("회원 탈퇴", onClick = onClickLeave)
                        )
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ResumeCard(
    brandBlue: Color,
    applyCount: Long,
    resumeViews: Long,
    onClickResumeCreate: () -> Unit,
    onClickResumeManage: () -> Unit,
    onClickApplyStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(7.38.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Button(
                    onClick = onClickResumeCreate,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
                ) {
                    Text(
                        "이력서 등록",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onClickResumeManage,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
                ) {
                    Text(
                        "이력서 관리",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatCell(
                    "입사지원 현황",
                    "${applyCount}건",
                    Modifier.weight(1f),
                    onClick = onClickApplyStatus
                )
                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = Color(0xFFDDDDDD)
                )
                StatCell("이력서 열람", "${resumeViews}건", Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun StatCell(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val brandBlue = Color(0xFF005FFF)

    val clickableModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Column(
        modifier = clickableModifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            fontSize = 14.sp,
            color = Color(0xFF000000),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            value,
            fontSize = 24.sp,
            color = brandBlue,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class RowItem(
    val label: String,
    val suffix: String? = null,
    val onClick: () -> Unit
)

@Composable
private fun SectionListCard(title: String, items: List<RowItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(
                    top = 20.dp,
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 12.dp
                )
            )
            items.forEach { item ->
                SectionRow(item.label, item.suffix, item.onClick)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionRow(text: String, suffix: String? = null, onClick: () -> Unit) {
    val brandBlue = Color(0xFF005FFF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            fontSize = 20.sp,
            color = Color(0xFF9C9C9C),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (suffix != null) {
            val suffixColor =
                if (suffix.endsWith("건")) brandBlue else Color(0xFF222222)
            Text(
                suffix,
                fontSize = 20.sp,
                color = suffixColor,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(6.dp))
        }
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF9BA2A8)
        )
    }
}

@Preview(
    name = "Profile Screen Preview",
    showBackground = true,
    backgroundColor = 0xFFF1F5F7
)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        name = "홍길동",
        applyCount = 4,
        resumeViews = 2,
        recentCount = 5,
        likedCount = 7,
        activityLevel = 2L,
        onClickResumeCreate = { },
        onClickResumeManage = { },
        onClickBookmarks = { },
        onClickRecent = { },
        onClickActivityLevel = { },
        onClickChangePw = { },
        onClickLogout = { },
        onClickLeave = { },
        onClickApplyStatus = { },
        onShortcut = { }
    )
}
