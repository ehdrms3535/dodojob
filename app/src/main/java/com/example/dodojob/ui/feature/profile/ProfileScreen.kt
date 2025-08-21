package com.example.dodojob.ui.feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R

@Composable
fun ProfileRoute(nav: NavController) {
    ProfileScreen(
        name = "홍길동",
        applyCount = 4,
        resumeViews = 2,
        recentCount = 3,
        likedCount = 2,
        onClickResumeCreate = {},
        onClickResumeManage = {},
        onClickBookmarks = {},
        onClickRecent = {},
        onClickActivityLevel = {},
        onClickEditProfile = {},
        onClickChangePw = {},
        onClickLogout = {},
        onClickLeave = {},
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
}

@Composable
fun ProfileScreen(
    name: String,
    applyCount: Int,
    resumeViews: Int,
    recentCount: Int,
    likedCount: Int,
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
        bottomBar = { BottomNavBarProfile(current = "my", onClick = onShortcut) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 상단바: 로고(좌) / 알림(우)
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
                IconButton(onClick = { /* 알림센터 이동 등 */ }) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "알림",
                        tint = Color(0xFF696969)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 프로필: 증명사진 + 이름
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.senior_id), // ← 증명사진
                    contentDescription = "프로필 사진",
                    modifier = Modifier
                        .size(75.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = " ${name}님",
                    fontSize = 27.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.019).em
                )
            }

            Spacer(Modifier.height(12.dp))

            // 이력서 카드
            ResumeCard(
                brandBlue = brandBlue,
                applyCount = applyCount,
                resumeViews = resumeViews,
                onClickResumeCreate = onClickResumeCreate,
                onClickResumeManage = onClickResumeManage
            )

            Spacer(Modifier.height(12.dp))

            // 알림 카드
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

            // 나의 활동
            SectionListCard(
                title = "나의 활동",
                items = listOf(
                    RowItem("최근 본 공고", suffix = "${recentCount}건", onClick = onClickRecent),
                    RowItem("좋아한 일자리", suffix = "${likedCount}건", onClick = onClickBookmarks),
                    RowItem("활동 레벨", onClick = onClickActivityLevel)
                )
            )

            Spacer(Modifier.height(12.dp))

            // 개인정보 관리
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

/* ---------------- 공용 컴포넌트 ---------------- */

@Composable
private fun ResumeCard(
    brandBlue: Color,
    applyCount: Int,
    resumeViews: Int,
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
private fun BottomNavBarProfile(current: String, onClick: (String) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        listOf(
            "home" to ("🏠" to "홈"),
            "edu" to ("📚" to "교육"),
            "welfare" to ("💰" to "복지"),
            "community" to ("💬" to "소통"),
            "my" to ("👤" to "마이")
        ).forEach { (key, pair) ->
            val (icon, labelText) = pair
            NavigationBarItem(
                selected = (key == current),
                onClick = { onClick(key) },
                icon = { Text(icon, fontSize = 18.sp) },
                label = { Text(labelText) }
            )
        }
    }
}
