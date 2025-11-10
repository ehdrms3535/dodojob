package com.example.dodojob.ui.feature.education

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.example.dodojob.R
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.dodojob.session.CurrentUser
import kotlinx.coroutines.delay
import kotlin.math.abs

/* 색상 */
private val ScreenBg = Color(0xFFF1F5F7)
private val Blue = Color(0xFF005FFF)
private val BlueSoft = Color(0xFFDEEBFF)
private val LineGray = Color(0xFFD9D9D9)
private val TextGray = Color(0xFF848484)
private val NavBarBg = Color(0xFFF4F5F7)

/* 데이터 */
private enum class LecTab { Weekly, Tasks }

private data class Lesson(val title: String, val duration: String)
private data class Task(val title: String)
private data class CourseContent(
    val weekly: List<Lesson>,
    val tasks: List<Task>
)
private data class CourseMeta(
    val heroTitle: String,
    val headline: String,
    val meta: String,
    val desc: String
)

/* 더미 컨텐츠/메타 (프리뷰/폴백) */
private val courseContents: Map<String, CourseContent> = mapOf(
    "eng-conv-basic" to CourseContent(
        weekly = listOf(
            Lesson("알파벳/발음 기초", "10:12"),
            Lesson("인사와 자기소개", "12:34"),
            Lesson("카페·마트 필수 표현", "14:22"),
            Lesson("전화·예약 응대", "11:09")
        ),
        tasks = listOf(
            Task("자기소개 3문장 녹음 제출"),
            Task("카페 주문 대화 스크립트 작성"),
            Task("필수 단어 30개 테스트")
        )
    ),
    "pc-basic-master" to CourseContent(
        weekly = listOf(
            Lesson("윈도우 기본과 파일 관리", "11:40"),
            Lesson("한글/워드 문서 작성", "13:05"),
            Lesson("인터넷/메일 활용", "12:21"),
            Lesson("클라우드·보안 기초", "09:58")
        ),
        tasks = listOf(
            Task("이력서 템플릿으로 문서 작성"),
            Task("메일 보내기 실습"),
            Task("클라우드 폴더 만들기")
        )
    ),
    "home-cooking" to CourseContent(
        weekly = listOf(
            Lesson("기본 재료 손질", "08:45"),
            Lesson("국/찌개 베이스", "12:12"),
            Lesson("볶음·조림 실습", "13:09"),
            Lesson("일품요리 플레이트", "11:33")
        ),
        tasks = listOf(
            Task("야채 손질 사진 제출"),
            Task("된장국 레시피 카드 작성"),
            Task("일품요리 완성 사진 업로드")
        )
    ),
    "group-tutoring" to CourseContent(
        weekly = listOf(
            Lesson("오리엔테이션/목표 설정", "07:30"),
            Lesson("스터디 방법론", "10:02"),
            Lesson("중간 점검/피드백", "09:40"),
            Lesson("성과 공유/회고", "12:00")
        ),
        tasks = listOf(
            Task("개인 목표 시트 작성"),
            Task("주간 회고 2회 제출"),
            Task("최종 성과 발표 준비")
        )
    ),
    "cs-customer" to CourseContent(
        weekly = listOf(
            Lesson("응대 기본 매너", "09:10"),
            Lesson("전화 응대 시나리오", "11:29"),
            Lesson("대면·현장 응대", "10:55"),
            Lesson("클레임 대처", "12:48")
        ),
        tasks = listOf(
            Task("전화 응대 스크립트 작성"),
            Task("현장 응대 롤플레잉 영상"),
            Task("클레임 대응 체크리스트")
        )
    ),
    "smartphone-pro" to CourseContent(
        weekly = listOf(
            Lesson("스마트폰 기본 설정", "08:15"),
            Lesson("사진/갤러리 관리", "11:20"),
            Lesson("모바일 결제/보안", "12:05"),
            Lesson("생활편의 앱 활용", "10:42")
        ),
        tasks = listOf(
            Task("앨범 정리 스크린샷"),
            Task("모바일 결제 테스트"),
            Task("편의 앱 북마크 목록 제출")
        )
    ),
    "watercolor-begin" to CourseContent(
        weekly = listOf(
            Lesson("도구/재료 이해", "12:41"),
            Lesson("붓터치/물 조절", "09:20"),
            Lesson("과일 정물 표현", "15:05"),
            Lesson("그라데이션/번짐", "10:34")
        ),
        tasks = listOf(
            Task("브러시 스트로크 3종 연습"),
            Task("사과 정물 스케치"),
            Task("그라데이션 샘플 2장")
        )
    ),
    "english-news-listening" to CourseContent(
        weekly = listOf(
            Lesson("뉴스 핵심 단어 익히기", "09:55"),
            Lesson("헤드라인 듣기", "11:14"),
            Lesson("본문 요지 파악", "13:18"),
            Lesson("섀도잉/요약", "12:02")
        ),
        tasks = listOf(
            Task("헤드라인 받아쓰기 5개"),
            Task("본문 요약 3줄 제출"),
            Task("섀도잉 녹음 업로드")
        )
    )
)

private val courseMetas: Map<String, CourseMeta> = mapOf(
    "eng-conv-basic" to CourseMeta("영어 회화 입문","일상 표현부터 차근차근","언어 · DODO EDU","기초 패턴과 상황별 회화로 부담없이 시작"),
    "pc-basic-master" to CourseMeta("컴퓨터 기초 마스터","문서·인터넷·이메일 한 번에","IT · DODO EDU","실습 위주로 바로 따라하는 필수 기능"),
    "home-cooking" to CourseMeta("집에서 즐기는 홈쿠킹","기초 재료 손질과 간단한 레시피","요리 · DODO EDU","매일 먹는 반찬부터 근사한 일품요리까지"),
    "group-tutoring" to CourseMeta("그룹 스터디 튜터링","주 1회 온라인 그룹 학습","교육 · DODO EDU","함께 공부하며 동기부여 얻기"),
    "cs-customer" to CourseMeta("고객 응대 스킬","전화·대면 응대 기본","직무 · DODO EDU","상황별 말하기와 친절한 커뮤니케이션"),
    "smartphone-pro" to CourseMeta("스마트폰 200% 활용","결제·사진·앱 활용 전반","IT · DODO EDU","초보도 쉽게 따라하는 실전 가이드"),
    "watercolor-begin" to CourseMeta("물감과 친해지는 수채화","기초 드로잉과 색감 연습","취미 · DODO EDU","간단한 소묘부터 분위기 있는 채색까지"),
    "english-news-listening" to CourseMeta("영어 뉴스 리스닝","쉬운 뉴스로 리스닝 감 만들기","언어 · DODO EDU","핵심 단어·표현으로 이해력 향상")
)

/* =====================  Lecture Screen  ===================== */
@Composable
fun EducationLectureScreen(
    courseId: String = "",
    onBack: () -> Unit = {},
    showEnrollOnLaunch: Boolean = true,
    showEnrollTrigger: Boolean = false,
    onNavigatePaymentComplete: () -> Unit = {},
    videoUrl: String? = null,
    heroTitle: String? = null,
    heroSubtitle: String? = null,
    heroThumbnail: String? = null,
    viewModel: EducationViewModel = viewModel()
) {
    val username = CurrentUser.username

    // 화면 진입 시 유저 기준으로 Supabase 상태 로딩
    LaunchedEffect(username) {
        viewModel.loadAssigned(username)
    }

    var selectedTab by remember { mutableStateOf(LecTab.Weekly) }
    var weeklySelectedIndex by remember { mutableStateOf(0) }
    val tasksSelected = remember { mutableStateListOf<Int>() }

    // ViewModel에서 구매 여부 / 마지막 시청 위치 가져오기
    val isPurchased = viewModel.isPurchased(courseId)
    val lastPositionMs = viewModel.getLastPosition(courseId)

    // 결제 안 한 상태에서만 자동 오픈
    var showEnroll by remember { mutableStateOf(showEnrollOnLaunch && !isPurchased) }
    var play by remember { mutableStateOf(false) }

    val content = remember(courseId) {
        courseContents[courseId] ?: CourseContent(
            weekly = listOf(
                Lesson("오리엔테이션", "05:00"),
                Lesson("기본 개념 익히기", "08:30")
            ),
            tasks = listOf(
                Task("시작 설문 제출"),
                Task("1주차 복습 퀴즈")
            )
        )
    }

    val meta = remember(courseId, heroTitle, heroSubtitle) {
        val base = courseMetas[courseId] ?: CourseMeta(
            heroTitle = "온라인 강의",
            headline  = "학습을 시작해보세요",
            meta      = "DODO EDU",
            desc      = "주차별 커리큘럼과 과제를 확인하세요"
        )
        base.copy(
            heroTitle = heroTitle ?: base.heroTitle,
            headline  = heroSubtitle ?: base.headline
        )
    }

    Surface(color = ScreenBg) {
        Box(
            Modifier
                .fillMaxSize()
                .background(ScreenBg)
        ) {
            Column(Modifier.fillMaxSize()) {
                TopBar(onBack = onBack)

                HeroBlock(
                    meta = meta,
                    showEnrollTrigger = showEnrollTrigger && !isPurchased,
                    onEnrollClick = { showEnroll = true },
                    thumbnailUrl = heroThumbnail,
                    // 썸네일 클릭 시 로직
                    onPlayClick = {
                        if (!isPurchased) {
                            showEnroll = true
                        } else if (!videoUrl.isNullOrBlank()) {
                            play = true
                        }
                    },
                    isPurchased = isPurchased,
                    videoUrl = videoUrl,
                    play = play,
                    startPositionMs = lastPositionMs,
                    onPositionChange = { pos ->
                        viewModel.updateLastPosition(courseId, username, pos)
                    }
                )

                Column(Modifier.background(Color.White)) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(45.dp)
                            .background(Color.White),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(
                            Modifier.padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(60.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UnderlineTab("주차별", selectedTab == LecTab.Weekly) {
                                selectedTab = LecTab.Weekly
                            }
                            UnderlineTab("학습과제", selectedTab == LecTab.Tasks) {
                                selectedTab = LecTab.Tasks
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (selectedTab == LecTab.Weekly) {
                        LessonList(
                            lessons = content.weekly,
                            selectedIndex = weeklySelectedIndex,
                            onSelectSingle = { weeklySelectedIndex = it },
                            multiSelected = emptySet(),
                            isMulti = false
                        )
                    } else {
                        TaskList(
                            tasks = content.tasks,
                            selected = tasksSelected.toSet(),
                            onToggle = { idx ->
                                if (tasksSelected.contains(idx)) tasksSelected.remove(idx)
                                else tasksSelected.add(idx)
                            }
                        )
                    }
                }

                BottomNavBarStub()
            }

            // 수강신청 바텀시트
            EnrollBottomSheet(
                visible = showEnroll,
                priceText = "18,000원",
                onDismiss = { showEnroll = false },
                onPrimaryClick = {
                    // ViewModel에 구매 반영 + Supabase upsert
                    viewModel.buyLecture(courseId, username)
                    showEnroll = false
                    onNavigatePaymentComplete()
                }
            )
        }
    }
}

/* ===================== Video Player ===================== */
@Composable
private fun VideoPlayerBox(
    url: String,
    startPositionMs: Long,
    onPositionChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            if (startPositionMs > 0L) {
                seekTo(startPositionMs)
            }
            playWhenReady = true
        }
    }

    // startPositionMs 가 뒤늦게 로딩되는 경우 한 번 더 맞춰줌
    LaunchedEffect(startPositionMs) {
        if (startPositionMs > 0L &&
            abs(player.currentPosition - startPositionMs) > 1_000
        ) {
            player.seekTo(startPositionMs)
        }
    }

    // 일정 주기로 재생 위치 콜백 (Supabase 저장용)
    LaunchedEffect(player) {
        while (true) {
            delay(5_000) // 5초마다
            onPositionChange(player.currentPosition)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onPositionChange(player.currentPosition)
            player.release()
        }
    }

    Box(modifier.background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                }
            }
        )
    }
}

/* ===================== Sub UI ===================== */

@Composable
private fun TopBar(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Image(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "뒤로",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroBlock(
    meta: CourseMeta,
    onEnrollClick: () -> Unit = {},
    showEnrollTrigger: Boolean = false,
    onPlayClick: () -> Unit = {},
    thumbnailUrl: String? = null,
    isPurchased: Boolean,
    videoUrl: String?,
    play: Boolean,
    startPositionMs: Long,
    onPositionChange: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(195.dp)
                .clip(RoundedCornerShape(0.dp))
                .background(Color(0xFFD9D9D9))
                .clickable { onPlayClick() },
            contentAlignment = Alignment.Center
        ) {
            if (play && isPurchased && !videoUrl.isNullOrBlank()) {
                VideoPlayerBox(
                    url = videoUrl,
                    startPositionMs = startPositionMs,
                    onPositionChange = onPositionChange,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                if (!thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = meta.heroTitle,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.play_button),
                    contentDescription = "재생",
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            Text(
                text = meta.heroTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(14.dp))
            Text(text = meta.headline, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${meta.meta} · ${meta.desc}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (showEnrollTrigger) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Transparent)
                        .clickable { onEnrollClick() }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPurchased) "수강 중" else "수강신청",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Blue
                    )
                }
            }
        }
    }
}

@Composable
private fun UnderlineTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .height(35.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Blue else Color.Black
        )
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .width(68.dp)
                .height(4.dp)
                .background(if (selected) Blue else Color.Transparent)
        )
    }
}

@Composable
private fun LessonList(
    lessons: List<Lesson>,
    selectedIndex: Int,
    onSelectSingle: (Int) -> Unit,
    multiSelected: Set<Int>,
    isMulti: Boolean,
    onToggleMulti: (Int) -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
    ) {
        lessons.forEachIndexed { index, lesson ->
            if (index == 0) Divider(color = LineGray, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            val selected = if (isMulti) multiSelected.contains(index) else (index == selectedIndex)
            LessonRow(
                title = lesson.title,
                duration = lesson.duration,
                selected = selected,
                colorizeWhenSelected = true,
                showDuration = !isMulti
            ) {
                if (isMulti) onToggleMulti(index) else onSelectSingle(index)
            }
            if (index != lessons.lastIndex) Divider(color = LineGray, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun LessonRow(
    title: String,
    duration: String,
    selected: Boolean,
    colorizeWhenSelected: Boolean,
    showDuration: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected && colorizeWhenSelected) BlueSoft else Color.White
    val titleColor = if (selected && colorizeWhenSelected) Blue else Color(0xFF000000)
    val checkIcon = if (selected) R.drawable.checked_mark else R.drawable.unchecked_mark

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 27.dp, vertical = 24.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Image(
                painter = painterResource(id = checkIcon),
                contentDescription = if (selected) "선택됨" else "선택 안됨",
                modifier = Modifier
                    .size(27.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        }
        if (showDuration) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.play_button),
                    contentDescription = "재생",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = duration,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
private fun TaskList(tasks: List<Task>, selected: Set<Int>, onToggle: (Int) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
    ) {
        tasks.forEachIndexed { index, task ->
            if (index == 0) Divider(color = LineGray, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            val isChecked = selected.contains(index)
            TaskRow(title = task.title, checked = isChecked) { onToggle(index) }
            if (index != tasks.lastIndex) Divider(color = LineGray, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun TaskRow(title: String, checked: Boolean, onClick: () -> Unit) {
    val titleColor = if (checked) Blue else Color(0xFF000000)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (checked) BlueSoft else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 27.dp, vertical = 20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val icon = if (checked) R.drawable.checked_mark else R.drawable.unchecked_mark
            Image(
                painter = painterResource(id = icon),
                contentDescription = if (checked) "완료" else "미완료",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun BottomNavBarStub() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(43.dp)
            .background(NavBarBg)
    )
}

/* 수강신청 바텀시트 */
@Composable
private fun EnrollBottomSheet(
    visible: Boolean,
    priceText: String,
    onDismiss: () -> Unit,
    onPrimaryClick: () -> Unit
) {
    val sheetHeight = 179.dp
    val scrimColor = Color(0xFF3E454B).copy(alpha = 0.6f)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(scrimColor)
                    .clickable { onDismiss() }
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(sheetHeight)
                    .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
                    .background(Color.White)
                    .padding(start = 32.dp, top = 11.dp, end = 27.dp, bottom = 20.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "수강신청",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Blue
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            val painter = runCatching {
                                painterResource(id = R.drawable.x_circle)
                            }.getOrNull()
                            if (painter != null) {
                                Image(
                                    painter = painter,
                                    contentDescription = "닫기",
                                    modifier = Modifier.size(26.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "닫기",
                                    tint = LineGray,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Blue)
                        .clickable { onPrimaryClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${priceText} 결제하기",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewEducationLectureScreen() {
    EducationLectureScreen(
        courseId = "1", // 실제로는 lecture.id.toString() 같은 값
        showEnrollOnLaunch = true,
        showEnrollTrigger = true,
        videoUrl = null,
        heroTitle = "물감과 친해지는 수채화",
        heroSubtitle = "기초 드로잉과 색감 연습",
        heroThumbnail = null
    )
}

@Composable
private fun SetStatusBar(color: Color, darkIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = color.toArgb()
            WindowCompat.getInsetsController(window, window.decorView)
                .isAppearanceLightStatusBars = darkIcons
        }
    }
}
