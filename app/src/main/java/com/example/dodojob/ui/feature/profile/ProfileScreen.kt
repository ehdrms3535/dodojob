package com.example.dodojob.ui.feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.ui.feature.common.LeaveDialog
import com.example.dodojob.ui.feature.common.LogoutDialog
import com.example.dodojob.dao.getSeniorInformation
import com.example.dodojob.data.senior.SeniorJoined
import com.example.dodojob.session.CurrentUser

@Composable
fun ProfileRoute(nav: NavController) {
    var showLogout by remember { mutableStateOf(false) }
    var showLeave by remember { mutableStateOf(false) }

    val username = CurrentUser.username

    var senior by remember { mutableStateOf<SeniorJoined?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(username) {
        loading = true
        error = null
        senior = null
        runCatching {
            if (!username.isNullOrBlank()) getSeniorInformation(username) else null
        }.onSuccess { s ->
            senior = s
        }.onFailure { t ->
            error = t.message ?: "알 수 없는 오류"
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
    val applyCount = s.applyCount
    val resumeViews = s.resumeViews
    val recentCount = s.recentCount
    val likedCount = s.likedCount
    val activityLevel = s.activityLevel
    val applyWithinYear = s.applyWithinYear
    val realWorkExpCount = s.realWorkExpCount
    val eduCompleted = s.eduCompleted

    ProfileScreen(
        name = displayName,
        applyCount = applyCount,
        resumeViews = resumeViews,
        recentCount = recentCount,
        likedCount = likedCount,
        onClickResumeCreate = {},
        onClickResumeManage = {},
        onClickBookmarks = { nav.navigate("liked_job") },
        onClickRecent = { nav.navigate("recently_viewed") },
        onClickActivityLevel = {
            // 화면에 보이는 현재 사용자 지표들을 바로 페이로드로 구성
            val payload = ActivityLevelData(
                name = displayName ?: "사용자",
                level = (activityLevel.coerceIn(1, 3)),
                applyWithinYear = applyWithinYear,
                realWorkExpCount = realWorkExpCount,
                eduCompleted = eduCompleted,
                joinedDate = "2025년 9월 3일"
            )

            nav.currentBackStackEntry
                ?.savedStateHandle
                ?.set("activity_level_payload", payload)

            // 이동
            nav.navigate("activity_level")
        },
        onClickEditProfile = {},
        onClickChangePw = { nav.navigate("change_password") },
        onClickLogout = { showLogout = true },
        onClickLeave = { showLeave = true },
        onShortcut = { key ->
            when (key) {
                "home" -> nav.navigate("main") { launchSingleTop = true }
                "edu" -> nav.navigate("edu")
                "welfare" -> nav.navigate("welfare")
                "community" -> nav.navigate("community")
                "my" -> Unit
            }
        }
    )

    if (showLogout) {
        LogoutDialog(
            onConfirm = { showLogout = false /* TODO: signOut & nav */ },
            onCancel = { showLogout = false }
        )
    }
    if (showLeave) {
        LeaveDialog(
            onConfirm = { showLeave = false /* TODO: delete & nav */ },
            onCancel = { showLeave = false }
        )
    }
}

@Composable
private fun LoadingOrErrorBox(text: String, sub: String? = null) {
    val screenBg = Color(0xFFF1F5F7)
    Scaffold(containerColor = screenBg) { padding ->
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
    onClickResumeCreate: () -> Unit,
    onClickResumeManage: () -> Unit,
    onClickBookmarks: () -> Unit,
    onClickRecent: () -> Unit,
    onClickActivityLevel: () -> Unit,
    onClickEditProfile: () -> Unit,
    onClickChangePw: () -> Unit,
    onClickLogout: () -> Unit,
    onClickLeave: () -> Unit,
    onShortcut: (String) -> Unit
) {
    val brandBlue = Color(0xFF005FFF)
    val screenBg = Color(0xFFF1F5F7)
    var notifOn by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = screenBg,
        bottomBar = { BottomNavBar(current = "my", onClick = onShortcut) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "앱 로고",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { /* 알림센터 이동 */ }) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "알림",
                        tint = Color(0xFF696969)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.senior_id),
                    contentDescription = "프로필 사진",
                    modifier = Modifier
                        .size(75.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = " ${name ?: "사용자"}님",
                    fontSize = 27.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.019).em
                )
            }

            Spacer(Modifier.height(12.dp))

            ResumeCard(
                brandBlue = brandBlue,
                applyCount = applyCount,
                resumeViews = resumeViews,
                onClickResumeCreate = onClickResumeCreate,
                onClickResumeManage = onClickResumeManage
            )

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("알림", fontSize = 22.sp, color = Color.Black, fontWeight = FontWeight.Bold)
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

            Spacer(Modifier.height(12.dp))

            SectionListCard(
                title = "나의 활동",
                items = listOf(
                    RowItem("최근 본 공고", suffix = "${recentCount}건", onClick = onClickRecent),
                    RowItem("좋아한 일자리", suffix = "${likedCount}건", onClick = onClickBookmarks),
                    RowItem("활동 레벨", onClick = onClickActivityLevel)
                )
            )

            Spacer(Modifier.height(12.dp))

            SectionListCard(
                title = "개인정보 관리",
                items = listOf(
                    RowItem("회원정보 수정", onClick = onClickEditProfile),
                    RowItem("비밀번호 변경", onClick = onClickChangePw),
                    RowItem("로그아웃", onClick = onClickLogout),
                    RowItem("회원 탈퇴", onClick = onClickLeave)
                )
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ResumeCard(
    brandBlue: Color,
    applyCount: Long,
    resumeViews: Long,
    onClickResumeCreate: () -> Unit,
    onClickResumeManage: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onClickResumeCreate,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
                ) { Text("이력서 등록", fontSize = 14.sp) }

                Button(
                    onClick = onClickResumeManage,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
                ) { Text("이력서 관리", fontSize = 14.sp) }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatCell("입사지원 현황", "${applyCount}건", Modifier.weight(1f))
                VerticalDivider(modifier = Modifier.height(36.dp), color = Color(0xFFDDDDDD))
                StatCell("이력서 열람", "${resumeViews}건", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatCell(title: String, value: String, modifier: Modifier = Modifier) {
    val brandBlue = Color(0xFF005FFF)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 12.sp, color = Color(0xFF8E8E8E))
        Text(value, fontSize = 18.sp, color = brandBlue, fontWeight = FontWeight.Bold)
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
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(top = 14.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            )
            items.forEachIndexed { idx, item ->
                SectionRow(item.label, item.suffix, item.onClick)
                if (idx < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 1.dp,
                        color = Color(0xFFF0F0F0)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionRow(text: String, suffix: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 14.sp, color = Color(0xFF222222), modifier = Modifier.weight(1f))
        if (suffix != null) {
            Text(suffix, fontSize = 14.sp, color = Color(0xFF222222))
            Spacer(Modifier.width(6.dp))
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF9BA2A8))
    }
}

/* 하단 네비 */
@Composable
fun BottomNavBar(current: String, onClick: (String) -> Unit) {
    val brandBlue = Color(0xFF005FFF)

    data class NavItem(
        val key: String,
        val unselectedRes: Int,
        val selectedRes: Int? = null
    )
    val items = listOf(
        NavItem("home",      R.drawable.unselected_home,      R.drawable.selected_home),
        NavItem("edu",       R.drawable.unselected_education, null),
        NavItem("welfare",   R.drawable.unselected_welfare,   null),
        NavItem("community", R.drawable.unselected_talent,    null),
        NavItem("my",        R.drawable.unselected_my,        R.drawable.selected_my),
    )

    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            val isSelected = item.key == current
            val iconRes = if (isSelected && item.selectedRes != null) item.selectedRes else item.unselectedRes

            NavigationBarItem(
                selected = isSelected,
                onClick = { onClick(item.key) },
                icon = {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = item.key,
                        modifier = Modifier.size(55.dp),
                        colorFilter = if (isSelected && item.selectedRes == null)
                            ColorFilter.tint(brandBlue)
                        else null
                    )
                },
                label = null,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Color.Unspecified,
                    selectedTextColor   = Color.Unspecified,
                    unselectedIconColor = Color.Unspecified,
                    unselectedTextColor = Color.Unspecified,
                    indicatorColor      = Color.Transparent
                )
            )
        }
    }
}
