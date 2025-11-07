package com.example.dodojob.ui.feature.announcement

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dodojob.R
import com.example.dodojob.dao.fetchDisplayNameByUsername
import com.example.dodojob.data.supabase.LocalSupabase
import com.example.dodojob.data.announcement.AnnoucementRepository
import com.example.dodojob.data.announcement.AnnouncementRepositorySupabase
import com.example.dodojob.navigation.Route
import kotlin.math.min
import com.example.dodojob.dao.getPreuserInformation
import com.example.dodojob.dao.preuserRow
import com.example.dodojob.data.naver.NaverGeocoding
import com.example.dodojob.session.CurrentUser
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.launch
import com.example.dodojob.data.announcement.AnnouncementDto
import com.example.dodojob.data.announcement.AnnoucementUrlDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ===== 네비게이터 분리 =====
interface AnnouncementNavigator {
    fun onBack(): Boolean
    fun onTabClick(index: Int) {}
    fun onNextStep() {}
    fun onVerifyBizNo(bizNo: String) {}
    fun onFindAddress(keyword: String) {}
    fun onUploadPhoto(slotIndex: Int) {}
    fun onPublicToggle(isPublic: Boolean) {}
    fun onSaveContactToggle(save: Boolean) {}
}

class AnnouncementNavigatorImpl(
    private val nav: NavController
) : AnnouncementNavigator {

    override fun onBack(): Boolean = nav.popBackStack()

    override fun onTabClick(index: Int) {
        val target = when (index) {
            0 -> Route.Announcement.path
            1 -> Route.Announcement2.path
            2 -> Route.Announcement3.path
            else -> Route.Announcement4.path
        }
        val current = nav.currentBackStackEntry?.destination?.route
        if (current != target) nav.navigate(target) { launchSingleTop = true }
    }

    override fun onNextStep() {
        val target = Route.Announcement2.path
        val current = nav.currentBackStackEntry?.destination?.route
        if (current == target) return

        nav.navigate(target) {
            launchSingleTop = true
            restoreState = true
            val start = nav.graph.startDestinationId
            popUpTo(start) { saveState = true }
        }
    }

    override fun onVerifyBizNo(bizNo: String) {}
    override fun onFindAddress(keyword: String) {}
    override fun onUploadPhoto(slotIndex: Int) {}
    override fun onPublicToggle(isPublic: Boolean) {}
    override fun onSaveContactToggle(save: Boolean) {}
}

// Size 확장: 짧은 변 길이
private val androidx.compose.ui.geometry.Size.minSide: Float
    get() = min(width, height)

// ===== 업로드 유틸 =====
data class UploadedImage(val url: String, val path: String)

private suspend fun uploadAnyImageToSupabase(
    client: io.github.jan.supabase.SupabaseClient,
    context: android.content.Context,
    userId: String?,
    bucket: String = "company_images",
    pathPrefix: String = "workplace",
    uri: android.net.Uri
): UploadedImage {
    val cr = context.contentResolver
    val mime = cr.getType(uri) ?: "image/jpeg"
    val ext = when {
        mime.contains("png") -> "png"
        mime.contains("webp") -> "webp"
        else -> "jpg"
    }
    val bytes = cr.openInputStream(uri)?.use { it.readBytes() }
        ?: error("이미지를 읽을 수 없습니다.")

    val fileName = "${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.$ext"
    val path = "$pathPrefix/${userId ?: "anon"}/$fileName"

    val bucketRef = client.storage.from(bucket)
    val ct = when (ext) {
        "png"        -> ContentType.Image.PNG
        "jpg","jpeg" -> ContentType.Image.JPEG
        "webp"       -> ContentType("image","webp")
        else         -> ContentType.Image.JPEG
    }

    bucketRef.upload(path, bytes) {
        upsert = true
        contentType = ct
    }

    val url = bucketRef.publicUrl(path) // private 버킷이면 createSignedUrl 사용
    return UploadedImage(url = url, path = path)
}

// ===== Route: 메인 진입 =====
@Composable
fun Announcement1Route(
    nav: NavController,
    navigator: AnnouncementNavigator = remember(nav) { AnnouncementNavigatorImpl(nav) }
) {
    Announcement1Screen(navigator = navigator)
}

/* ----------------------------------------------------------
   /02 스타일 토큰 & 유틸
---------------------------------------------------------- */
private val Blue       = Color(0xFF005FFF) // 프라이머리
private val TextGray   = Color(0xFF828282) // 문구/보더 그레이
private val BorderGray = Color(0xFF828282)
private val BgGray     = Color(0xFFF1F5F7) // 화면 배경
private val CardBg     = Color.White

private fun Double.em() = (this * 16).sp // -0.019em 같은 값 보정용
private fun Float.em() = (this * 16).sp

@Composable
private fun StatusBarBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Color(0xFFEFEFEF))
    )
}

/* ----------------------------------------------------------
   메인 화면 (/01 → /02 스타일로 통일)
---------------------------------------------------------- */
@Composable
fun Announcement1Screen(
    navigator: AnnouncementNavigator
) {
    val scroll = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // 로딩 상태 분리
    var screenLoading by remember { mutableStateOf(false) }   // 초기 사용자 정보
    var geocodeLoading by remember { mutableStateOf(false) }  // 주소찾기
    var nextLoading by remember { mutableStateOf(false) }     // 다음단계 저장

    var preuser by remember { mutableStateOf<preuserRow?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val username = CurrentUser.username
    LaunchedEffect(username) {
        // 필요 시 사전 검증
        runCatching { fetchDisplayNameByUsername(username) }
    }
    LaunchedEffect(username) {
        screenLoading = true
        error = null
        preuser = null
        runCatching {
            if (!username.isNullOrBlank()) getPreuserInformation(username) else null
        }.onSuccess { pr ->
            preuser = pr
        }.onFailure { t ->
            error = t.message ?: "알 수 없는 오류"
        }
        screenLoading = false
    }

    // 초기값 안전 바인딩 (preuser 없어도 UI 유지)
    val prName  = preuser?.name  ?: ""
    val prPhone = preuser?.phone ?: ""
    val prEmail = preuser?.email ?: ""

    var companyName by rememberSaveable { mutableStateOf("") }
    var bizNo by rememberSaveable { mutableStateOf("") }
    var isPublicOrg by rememberSaveable { mutableStateOf(false) } // 공공기관 토글/체크
    var contactName by rememberSaveable { mutableStateOf(prName) }
    var contactPhone by rememberSaveable { mutableStateOf(prPhone) }
    var contactEmail by rememberSaveable { mutableStateOf(prEmail) }
    var saveContact by rememberSaveable { mutableStateOf(false) }

    var placeAddressSearch by rememberSaveable { mutableStateOf("") }
    var placeAddressDetail by rememberSaveable { mutableStateOf("") }

    val client = LocalSupabase.current
    val repo: AnnoucementRepository = AnnouncementRepositorySupabase(client)

    // ---- 근무지 사진 상태 (4칸) ----
    val uid = CurrentUser.username
    val imageUris = remember { mutableStateListOf<android.net.Uri?>(null, null, null, null) }
    val uploadedUrls = remember { mutableStateListOf<String?>(null, null, null, null) }
    val isUploading = remember { mutableStateListOf(false, false, false, false) }
    var pendingSlot by remember { mutableStateOf<Int?>(null) }

    // 갤러리 런처
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val slot = pendingSlot
        pendingSlot = null
        if (uri == null || slot == null) return@rememberLauncherForActivityResult

        // 썸네일 먼저
        imageUris[slot] = uri

        // 업로드 시작
        scope.launch {
            try {
                isUploading[slot] = true
                val uploaded = uploadAnyImageToSupabase(
                    client = client,
                    context = context,
                    userId = uid,
                    pathPrefix = "workplace",
                    uri = uri
                )
                uploadedUrls[slot] = uploaded.url
            } catch (e: Exception) {
                android.util.Log.e("Upload", "slot=$slot upload failed", e)
                Toast.makeText(context, "이미지 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isUploading[slot] = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
    ) {
        Column(Modifier.fillMaxSize()) {

            // ✅ StatusBar (24dp)
            StatusBarBar()

            // ✅ Header (76dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .background(CardBg)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "공고등록",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    letterSpacing = (-0.019).em()
                )
            }

            // 오류/안내 배너 (언마운트 없이 표시)
            if (error != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFECEC))
                        .padding(12.dp)
                ) {
                    Text("오류: $error", color = Color(0xFFD21B1B))
                }
            }

            // ✅ Tabs 01~04 (02 스타일)
            TabBar02(
                selected = 0,
                labels = listOf("01", "02", "03", "04"),
                onClick = navigator::onTabClick
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scroll)
            ) {
                // 01. 기본정보
                SectionCard {
                    Spacer(modifier = Modifier.height(18.dp))

                    TitleRow(text = "01. 기본정보를 입력해주세요!")

                    Spacer(Modifier.height(30.dp))

                    // 근무회사명: 밑줄형
                    LabelText(text = "근무회사명")
                    Spacer(modifier = Modifier.height(8.dp))

                    UnderlineField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        placeholder = "내용입력"
                    )

                    // === 공공기관 영역 (이미지 체크로 교체) ===
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ImageCheck(
                            checked = isPublicOrg,
                            onToggle = {
                                isPublicOrg = !isPublicOrg
                                navigator.onPublicToggle(isPublicOrg)
                            }
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("공공기관", fontSize = 15.sp, color = TextGray, letterSpacing = (-0.019).em())
                    }
                }

                // ✅ 구분선
                SectionSpacer()

                // 사업자 등록번호 + 인증하기
                SectionCard(padding = 20.dp) {
                    LabelText(text = "사업자 등록번호")

                    Spacer(Modifier.height(8.dp))

                    SinglelineInputBox(
                        value = bizNo,
                        onValueChange = { bizNo = it },
                        placeholder = "000-00-00000"
                    )
                    Spacer(Modifier.height(12.dp))
                    // 공공기관 체크 시 회색(비활성)
                    PrimaryButton(
                        text = "인증하기",
                        enabled = !isPublicOrg, // 체크되면 false → 회색/disabled
                        onClick = {
                            Toast.makeText(context, "사업자번호 인증이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // ✅ 구분선
                SectionSpacer()

                // 담당자 정보
                SectionCard(padding = 20.dp) {
                    LabelText(text = "담당자명")
                    Spacer(Modifier.height(8.dp))
                    SinglelineInputBox(
                        value = contactName,
                        onValueChange = { contactName = it },
                        placeholder = "담당자 성함"
                    )
                    Spacer(Modifier.height(13.dp))

                    LabelText(text = "담당자 연락처")
                    Spacer(Modifier.height(8.dp))
                    SinglelineInputBox(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        placeholder = "010-0000-0000"
                    )
                    Spacer(Modifier.height(13.dp))

                    LabelText(text = "담당자 이메일")
                    Spacer(Modifier.height(8.dp))
                    SinglelineInputBox(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        placeholder = "이메일을 입력해주세요"
                    )

                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ImageCheck(
                            checked = saveContact,
                            onToggle = {
                                saveContact = !saveContact
                                navigator.onSaveContactToggle(saveContact)
                            }
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "입력한 담당자 정보 저장",
                            fontSize = 15.sp,
                            color = TextGray,
                            letterSpacing = (-0.019).em()
                        )
                    }

                }

                // ✅ 구분선
                SectionSpacer()

                // 주소 블록
                SectionCard(padding = 20.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 328.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            LabelText(text = "회사주소")
                            Spacer(Modifier.height(8.dp))
                            SinglelineInputBox(
                                value = placeAddressSearch,
                                onValueChange = { placeAddressSearch = it },
                                placeholder = "주소를 검색해주세요"
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 328.dp)
                        ) {
                            PrimaryButton(
                                text = if (geocodeLoading) "주소찾는 중..." else "주소찾기",
                                onClick = {
                                    if (geocodeLoading) return@PrimaryButton
                                    scope.launch {
                                        geocodeLoading = true
                                        try {
                                            val q = placeAddressSearch.trim()
                                            if (q.isEmpty()) {
                                                Toast.makeText(context, "주소를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                                                return@launch
                                            }
                                            val r = NaverGeocoding.geocode(context, q)
                                            if (r != null) {
                                                fun stripHtml(s: String?) =
                                                    s?.replace(Regex("<.*?>"), "")?.trim().orEmpty()
                                                val best = listOf(
                                                    r.roadAddress,
                                                    r.jibunAddress,
                                                    r.display
                                                ).map(::stripHtml).firstOrNull { it.isNotEmpty() }.orEmpty()

                                                if (best.isNotEmpty()) {
                                                    placeAddressSearch = best
                                                    focusManager.clearFocus()
                                                    Toast.makeText(
                                                        context,
                                                        "찾음: $best (${r.lat}, ${r.lng})",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "주소 문자열이 비어 있습니다.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "주소를 찾을 수 없어요. 다른 표현으로 검색해 보세요.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "오류: ${e.message ?: "네트워크/권한/키 확인"}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } finally {
                                            geocodeLoading = false
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 328.dp)
                        ) {
                            LabelText(text = "상세주소")
                            Spacer(Modifier.height(8.dp))

                            SinglelineInputBox(
                                value = placeAddressDetail,
                                onValueChange = { placeAddressDetail = it },
                                placeholder = "상세주소를 입력해주세요"
                            )
                        }
                    }
                }

                // ✅ 구분선
                SectionSpacer()

                // 근무지 사진 업로드 (라벨 16dp 정렬 유지 + 그리드만 12dp)
                SectionCardCustomPadding(paddingHorizontal = 0.dp) {
                    // 라벨은 다른 섹션과 동일하게 16dp
                    LabelText(text = "근무지 사진", modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(10.dp))

                    // 그리드의 바깥 여백을 12dp로 통일 (아이템 간도 12dp)
                    PhotoGridRow(
                        gap = 12.dp,
                        outerPadding = 12.dp,
                        isUploading = isUploading,
                        imageUris = imageUris,
                        onAddClick = { slot ->
                            pendingSlot = slot
                            galleryLauncher.launch("image/*")
                        }
                    )
                }

                // ✅ 구분선
                SectionSpacer()

                // 다음단계 버튼 (업로드 확인 후 이동)
                SectionCard {
                    PrimaryButton(text = if (nextLoading) "저장 중..." else "다음단계") {
                        if (nextLoading) return@PrimaryButton
                        val urls = uploadedUrls.filterNotNull()
                        if (urls.isEmpty()) {
                            Toast.makeText(context, "근무지 사진을 1장 이상 업로드해 주세요.", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }
                        val companyNameSnapshot = companyName
                        val isPublicOrgSnapshot = isPublicOrg
                        val bizNoSnapshot = bizNo
                        val placeAddressSearchSnapshot = placeAddressSearch
                        val placeAddressDetailSnapshot = placeAddressDetail
                        CurrentUser.setCompanyid(bizNoSnapshot)
                        scope.launch {
                            nextLoading = true
                            runCatching {
                                val Save1 = AnnouncementDto(
                                    username = CurrentUser.username.toString(),
                                    company_name   = companyNameSnapshot,
                                    public         = isPublicOrgSnapshot,
                                    company_id     = bizNoSnapshot,
                                    company_locate = placeAddressSearchSnapshot,
                                    detail_locate  = placeAddressDetailSnapshot
                                )
                                val id = repo.insertAnnouncement(Save1)

                                val Save2 = AnnoucementUrlDto(
                                    id   = id,
                                    url  = uploadedUrls.getOrNull(0) ?: "",
                                    url2 = uploadedUrls.getOrNull(1) ?: "",
                                    url3 = uploadedUrls.getOrNull(2) ?: "",
                                    url4 = uploadedUrls.getOrNull(3) ?: ""
                                )
                                repo.insertAnnouncementUrl(Save2)
                            }.onSuccess {
                                withContext(Dispatchers.Main) {
                                    navigator.onNextStep()
                                }
                            }.onFailure {
                                android.util.Log.e("Announcement1", "insert failed", it)
                                Toast.makeText(context, "저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                            }.also {
                                nextLoading = false
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            BottomNavPlaceholder()
        }

        // ==== 전체 화면 로딩 오버레이 (초기 사용자 정보 로딩 전용) ====
        if (screenLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

/* ----------------------------------------------------------
   공통 UI 컴포넌트 (02 스타일)
---------------------------------------------------------- */

// TabBar (02 스타일)
@Composable
private fun TabBar02(
    selected: Int,
    labels: List<String>,
    onClick: (Int) -> Unit
) {
    val density = LocalDensity.current
    val centersPx = remember(labels.size) {
        mutableStateListOf<Float>().apply { repeat(labels.size) { add(0f) } }
    }
    val indicatorWidth = 41.dp
    val rowPaddingStart = 24.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = rowPaddingStart),
            horizontalArrangement = Arrangement.spacedBy(61.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            labels.forEachIndexed { idx, text ->
                Box(
                    modifier = Modifier
                        .onGloballyPositioned { c ->
                            val center = c.positionInParent().x + c.size.width / 2f
                            if (centersPx[idx] != center) centersPx[idx] = center
                        }
                        .clickable { onClick(idx) }
                ) {
                    val isSel = idx == selected
                    Text(
                        text = text,
                        fontSize = 16.sp,
                        letterSpacing = (-0.5).sp, // Pretendard -0.5px
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Blue else Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(24.dp)
                    )
                }
            }
        }

        val centerPx = centersPx.getOrNull(selected) ?: 0f
        if (centerPx > 0f) {
            val startInRow = with(density) { (centerPx - indicatorWidth.toPx() / 2f).toDp() }
            val targetX = rowPaddingStart + startInRow
            val animatedX by animateDpAsState(targetValue = targetX, label = "tab-indicator-01")

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = animatedX)
                    .width(indicatorWidth)
                    .height(4.dp)
                    .background(Blue)
            )
        }
    }
}

// 섹션 카드
@Composable
private fun SectionCard(
    padding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .padding(vertical = padding),
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

// 타이틀/라벨
@Composable
private fun TitleRow(text: String) {
    Text(
        text,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.019).em()
    )
}

@Composable
private fun LabelText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        letterSpacing = (-0.019).em(),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 6.dp)
    )
}

// === 이미지 체크 박스 (공공기관 전용, 16x16 + 여유 여백) ===
@Composable
private fun ImageCheck(
    checked: Boolean,
    onToggle: () -> Unit,
    boxSize: Dp = 24.dp,  // 터치 영역
    iconSize: Dp = 16.dp  // 피그마 16x16
) {
    val iconRes = if (checked) R.drawable.announce_checked_button
    else R.drawable.announce_unchecked_button

    Box(
        modifier = Modifier
            .size(boxSize)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        // 아이콘 주변에 1.dp 여백을 둬 테두리가 더 잘 보임
        Box(
            modifier = Modifier
                .size(iconSize + 2.dp)
                .background(Color.White, RoundedCornerShape(3.dp)) // 바탕을 흰색으로 고정
                .padding(2.dp), // 테두리 여유
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = iconRes),
                contentDescription = if (checked) "checked" else "unchecked",
                modifier = Modifier.size(iconSize),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }
    }
}


// === 저장 체크 (담당자 정보 저장용, 공공기관 체크박스와 동일 스타일) ===
@Composable
private fun CheckBoxLike(
    checked: Boolean,
    onToggle: () -> Unit,
    boxSize: Dp = 24.dp,   // 전체 클릭 영역
    innerSize: Dp = 14.dp  // 실제 박스(시각 크기)
) {
    val shape = RoundedCornerShape(3.dp)
    Box(
        modifier = Modifier
            .size(boxSize)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        // ✅ 외곽 테두리 + 배경 (살짝 여유 있게)
        Box(
            modifier = Modifier
                .size(innerSize)
                .border(width = 1.dp, color = Blue, shape = shape)
                .background(if (checked) Blue else Color.Transparent, shape = shape),
            contentAlignment = Alignment.Center
        ) {
            // ✅ 체크 표시
            if (checked) {
                Canvas(modifier = Modifier.size(innerSize * 0.65f)) {
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.15f, size.height * 0.55f),
                        end = Offset(size.width * 0.45f, size.height * 0.8f),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.45f, size.height * 0.8f),
                        end = Offset(size.width * 0.85f, size.height * 0.2f),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }
}


// 입력 박스 (싱글라인 라운드 + 회색보더)
@Composable
private fun SinglelineInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .clip(shape)
            .border(1.dp, BorderGray, shape)
            .background(Color.White, shape)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 13.sp,
                color = Color.Black,
                letterSpacing = (-0.019).em()
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        fontSize = 13.sp,
                        color = TextGray,
                        letterSpacing = (-0.019).em()
                    )
                }
                inner()
            }
        )
    }
}

// 밑줄형 인풋 (회사명)
@Composable
private fun UnderlineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 15.sp,
                color = if (value.isEmpty()) TextGray else Color.Black,
                letterSpacing = (-0.019).em()
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier.heightIn(min = 23.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(placeholder, fontSize = 15.sp, color = TextGray, letterSpacing = (-0.019).em())
                    }
                    inner()
                }
            }
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFC0C0C0))
        )
    }
}

@Composable
private fun SaveCheckBox(
    checked: Boolean,
    onToggle: () -> Unit,
    boxSize: Dp = 24.dp,  // 터치 영역
    innerSize: Dp = 16.dp // 시각 박스 (정수 px 근처 권장)
) {
    val shape = RoundedCornerShape(3.dp)

    Box(
        modifier = Modifier
            .size(boxSize)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        // 테두리를 안쪽으로 inset 해서 그리면 위/아래가 끊기지 않음
        Box(
            modifier = Modifier
                .size(innerSize)
                .drawBehind {
                    val strokeW = 1.25.dp.toPx()
                    val inset   = 0.75.dp.toPx()   // 상하좌우 안쪽으로 밀어 그리기
                    drawRoundRect(
                        color = Blue,
                        topLeft = Offset(inset, inset),
                        size = androidx.compose.ui.geometry.Size(
                            width  = size.width  - inset * 2,
                            height = size.height - inset * 2
                        ),
                        style = Stroke(width = strokeW),
                        cornerRadius = CornerRadius(3.dp.toPx())
                    )
                }
                .background(
                    if (checked) Blue else Color.White,
                    shape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Canvas(modifier = Modifier.size(innerSize * 0.65f)) {
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.15f, size.height * 0.55f),
                        end   = Offset(size.width * 0.45f, size.height * 0.8f),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.45f, size.height * 0.8f),
                        end   = Offset(size.width * 0.85f, size.height * 0.2f),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }
}

// 점선 추가 박스 (사진)
@Composable
private fun DashedAddBox(
    size: Dp,
    onClick: () -> Unit
) {
    val dashColor = Color(0xFF68A0FE)
    val plusColor = Color(0xFF7EAAF3)

    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                val stroke = Stroke(
                    width = 0.5.dp.toPx(), // 02 스타일: 얇은 점선
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
                drawRoundRect(
                    color = dashColor,
                    style = stroke,
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
            }
            .background(Color.White, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.55f)) {
            val strokeWidth = 3f
            val side = this.size.minSide
            val cx = this.size.width * 0.5f
            val cy = this.size.height * 0.5f
            val half = side * 0.275f

            drawLine(color = plusColor, start = Offset(cx, cy - half), end = Offset(cx, cy + half), strokeWidth = strokeWidth)
            drawLine(color = plusColor, start = Offset(cx - half, cy), end = Offset(cx + half, cy), strokeWidth = strokeWidth)
        }
    }
}

// 기본 파란 버튼 (enabled 지원/비활성 회색)
@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(47.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Blue,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFE0E6EE),  // 회색톤
            disabledContentColor = Color(0xFF98A2B3)
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.019).em()
        )
    }
}

@Composable
private fun BottomNavPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .background(Color(0xFFF4F5F7))
    )
}

// 회색 구분선
@Composable
private fun SectionSpacer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(BgGray)
    )
}

@Composable
private fun AreaAddBox(
    size: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.area_picture),
            contentDescription = "추가",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun SectionCardCustomPadding(
    paddingVertical: Dp = 20.dp,
    paddingHorizontal: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .padding(vertical = paddingVertical),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(horizontal = paddingHorizontal),
            content = content
        )
    }
}

@Composable
private fun PhotoSlot(
    slot: Int,
    isUploading: Boolean,
    imageUri: android.net.Uri?,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)                     // 정사각형
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF68A0FE), RoundedCornerShape(10.dp))
                .background(Color.White)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            when {
                isUploading -> {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                }
                imageUri != null -> {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = " 근무지 사진 $slot",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                else -> {
                    // 비어있을 때 area_picture.png 표시
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.area_picture),
                        contentDescription = "추가",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("추가", fontSize = 12.sp, color = TextGray)
    }
}

@Composable
private fun PhotoGridRow(
    gap: Dp = 12.dp,               // 아이템 간 간격
    outerPadding: Dp = 24.dp,      // 양옆 바깥 여백
    isUploading: List<Boolean>,
    imageUris: List<android.net.Uri?>,
    onAddClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = outerPadding),     // ← 여기서만 바깥 여백을 준다
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { slot ->
            Box(modifier = Modifier.weight(1f)) {
                PhotoSlot(
                    slot = slot,
                    isUploading = isUploading[slot],
                    imageUri = imageUris[slot],
                    onClick = { onAddClick(slot) }
                )
            }
        }
    }
}

/*
돌봄 서비스
컴퓨터활용
운전 가능

초등학생 독서 지도

힘이 좋으신 분

서울 송파구 올림픽로 300 (특별시 생략)
잠실 롯데월드타워 (POI)
서울특별시 송파구 신천동 29 (지번)
서울특별시

서울특별시 강남구 테헤란로 152 (강남파이낸스센터)

서울특별시 송파구 올림픽로 300 (롯데월드타워)

서울특별시 마포구 월드컵북로 400 (상암DMC)

서울특별시 종로구 세종대로 175 (정부서울청사)

서울특별시 영등포구 국제금융로 10 (여의도파이낸스빌딩)

🏢 경기도

경기도 성남시 분당구 불정로 90 (네이버 그린팩토리)

경기도 용인시 수지구 포은대로 499 (수지이마트)

경기도 고양시 일산서구 중앙로 1436 (일산롯데백화점)

경기도 수원시 영통구 센트럴타운로 107 (광교중앙역 인근)

경기도 하남시 미사강변동로 95 (스타필드 하남)

🌉 인천광역시 / 부산광역시

인천광역시 연수구 송도국제대로 123 (송도컨벤시아)

인천광역시 남동구 예술로 198 (인천시청)

부산광역시 해운대구 센텀서로 30 (KNN타워)

부산광역시 남구 유엔평화로 76 (부산예술회관)

🌆 대전 / 대구 / 광주

대전광역시 서구 둔산대로 100 (대전시청)

대구광역시 수성구 달구벌대로 2430 (수성롯데캐슬)

광주광역시 서구 상무중앙로 110 (상무지구 금융센터)

*/