package com.example.dodojob.ui.feature.announcement

import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
import com.example.dodojob.dao.getUsernameById
import com.example.dodojob.data.announcement.AnnouncementDto
import com.example.dodojob.data.announcement.AnnoucementUrlDto

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
            // 시작 지점까지 스택 정리 필요 없으면 이 블록은 지워도 됩니다.
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

// ===== 메인 화면 =====
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
        runCatching { getUsernameById(username) }
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
    var contactName = prName
    var contactPhone = prPhone
    var contactEmail = prEmail
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
            .background(Color(0xFFF1F5F7))
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .height(76.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "공고등록",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    letterSpacing = (-0.46).sp
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
            if (preuser == null && !screenLoading) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF8E1))
                        .padding(12.dp)
                ) {
                    Text("사용자 정보를 불러오지 못했어요.", color = Color(0xFF8B6C00))
                }
            }

            // Tabs 01~04
            TabBar(
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
                    TitleRow(text = "01. 기본정보를 입력해주세요!")
                    Spacer(Modifier.height(6.dp))

                    LabelText(text = "근무회사명")
                    OutlinedInputM3(
                        value = companyName,
                        onValueChange = { companyName = it },
                        placeholder = "내용입력"
                    )

                    // === 공공기관 영역 (체크박스 + 토글 버튼) ===
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isPublicOrg,
                                onCheckedChange = {
                                    isPublicOrg = it
                                    navigator.onPublicToggle(it)
                                }
                            )
                            Text(
                                text = "공공기관",
                                fontSize = 15.sp,
                                color = Color(0xFF828282),
                                letterSpacing = (-0.29).sp
                            )
                        }
                        // 👉 공공기관 토글 버튼
                        PublicToggleButton(
                            enabled = isPublicOrg,
                            onToggle = {
                                isPublicOrg = !isPublicOrg
                                navigator.onPublicToggle(isPublicOrg)
                            }
                        )
                    }
                }

                // ✅ 구분선
                GrayDivider()

                // 사업자 등록번호 + 인증하기
                SectionCard(padding = 20.dp) {
                    LabelText(text = "사업자 등록번호")
                    OutlinedInputM3(
                        value = bizNo,
                        onValueChange = { bizNo = it },
                        placeholder = "000-00-00000"
                    )
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton(
                        text = "인증하기",
                        onClick = {
                            Toast.makeText(context, "사업자번호 인증이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // ✅ 구분선
                GrayDivider()

                // 담당자 정보
                SectionCard(padding = 20.dp) {
                    LabelText(text = "담당자명")
                    OutlinedInputM3(
                        value = contactName,
                        onValueChange = { contactName = it },
                        placeholder = "담당자 성함"
                    )
                    Spacer(Modifier.height(13.dp))

                    LabelText(text = "담당자 연락처")
                    OutlinedInputM3(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        placeholder = "010-0000-0000"
                    )
                    Spacer(Modifier.height(13.dp))

                    LabelText(text = "담당자 이메일")
                    OutlinedInputM3(
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
                        Checkbox(
                            checked = saveContact,
                            onCheckedChange = {
                                saveContact = it
                                navigator.onSaveContactToggle(it)
                            }
                        )
                        Text(
                            text = "입력한 담당자 정보 저장",
                            fontSize = 15.sp,
                            color = Color(0xFF828282),
                            letterSpacing = (-0.29).sp
                        )
                    }
                }

                // ✅ 구분선
                GrayDivider()

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
                            OutlinedInputM3(
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
                            OutlinedInputM3(
                                value = placeAddressDetail,
                                onValueChange = { placeAddressDetail = it },
                                placeholder = "상세주소를 입력해주세요"
                            )
                        }
                    }
                }

                // ✅ 구분선
                GrayDivider()

                // 근무지 사진 업로드
                SectionCard {
                    Text(
                        text = "근무지 사진",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        letterSpacing = (-0.34).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { slot ->
                            Box(
                                modifier = Modifier
                                    .size(74.5.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFF68A0FE), RoundedCornerShape(10.dp))
                                    .clickable {
                                        pendingSlot = slot
                                        galleryLauncher.launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isUploading[slot] -> {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    imageUris[slot] != null -> {
                                        AsyncImage(
                                            model = imageUris[slot],
                                            contentDescription = "근무지 사진 $slot",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                    else -> {
                                        DashedAddBox(size = 74.5.dp) {
                                            pendingSlot = slot
                                            galleryLauncher.launch("image/*")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // ✅ 구분선
                GrayDivider()

                // 다음단계 버튼 (업로드 확인 후 이동)
                SectionCard {
                    PrimaryButton(text = if (nextLoading) "저장 중..." else "다음단계") {
                        if (nextLoading) return@PrimaryButton
                        val urls = uploadedUrls.filterNotNull()
                        if (urls.isEmpty()) {
                            Toast.makeText(context, "근무지 사진을 1장 이상 업로드해 주세요.", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }
                        scope.launch {
                            nextLoading = true
                            runCatching {
                                val Save1 = AnnouncementDto(
                                    company_name   = companyName,
                                    public         = isPublicOrg,
                                    company_id     = bizNo,
                                    company_locate = placeAddressSearch,
                                    detail_locate  = placeAddressDetail
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
                                // 반드시 메인 스레드에서 네비게이트
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    navigator.onNextStep()}
                            }.onFailure {
                                android.util.Log.e("HopeWorkFilter", "insert failed", it)
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

// ===== 공통 UI =====

@Composable
private fun PublicToggleButton(
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val shape = RoundedCornerShape(100)
    val borderColor = if (enabled) Color(0xFF005FFF) else Color(0xFF828282)
    val textColor = if (enabled) Color(0xFF005FFF) else Color(0xFF828282)
    val bg = if (enabled) Color(0x1A005FFF) else Color.Transparent

    Box(
        modifier = Modifier
            .height(32.dp)
            .border(1.dp, borderColor, shape)
            .background(bg, shape)
            .padding(horizontal = 12.dp)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (enabled) "공공기관: ON" else "공공기관: OFF",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun TabBar(
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
            .background(Color.White)
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
                        .onGloballyPositioned { coords ->
                            val center = coords.positionInParent().x + coords.size.width / 2f
                            if (centersPx[idx] != center) centersPx[idx] = center
                        }
                        .clickable { onClick(idx) }
                ) {
                    val color = if (idx == selected) Color(0xFF005FFF) else Color.Black
                    Text(
                        text = text,
                        color = color,
                        fontSize = 16.sp,
                        fontWeight = if (idx == selected) FontWeight.Bold else FontWeight.Medium,
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
            val animatedX by animateDpAsState(targetValue = targetX, label = "tab-indicator")

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = animatedX)
                    .width(indicatorWidth)
                    .height(4.dp)
                    .background(Color(0xFF005FFF))
            )
        }
    }
}

@Composable
private fun SectionCard(
    padding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { content() }
    }
}

@Composable
private fun TitleRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            letterSpacing = (-0.38).sp
        )
    }
}

@Composable
private fun LabelText(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        letterSpacing = (-0.34).sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 6.dp)
    )
}

@Composable
private fun UnderlineField( // (현재 미사용이지만 남겨둠: 디자인 복원 시 활용)
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val textColor = if (value.isEmpty()) Color(0xFF828282) else Color.Black
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 15.sp, color = textColor),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 23.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 15.sp,
                            color = Color(0xFF828282),
                            letterSpacing = (-0.29).sp
                        )
                    }
                    inner()
                }
            },
            modifier = Modifier.fillMaxWidth()
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

/** Material3 OutlinedTextField 버전 (표시/재구성 이슈 최소화) */
@Composable
private fun OutlinedInputM3(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        textStyle = TextStyle(fontSize = 15.sp),
        modifier = Modifier.fillMaxWidth() // 고정 높이 제거 → 텍스트 잘림 방지
    )
}

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
                    width = 1.dp.toPx(),
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
            val strokeWidth = 5f
            val side = this.size.minSide
            val cx = this.size.width * 0.5f
            val cy = this.size.height * 0.5f
            val half = side * 0.275f

            drawLine(color = plusColor, start = Offset(cx, cy - half), end = Offset(cx, cy + half), strokeWidth = strokeWidth)
            drawLine(color = plusColor, start = Offset(cx - half, cy), end = Offset(cx + half, cy), strokeWidth = strokeWidth)
        }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(47.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF005FFF),
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.34).sp
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

// ===== 회색 구분선 =====
@Composable
private fun GrayDivider(
    thickness: Dp = 10.dp,
    color: Color = Color(0xFFE6E8EC),
    horizontalPadding: Dp = 0.dp
) {
    Divider(
        color = color,
        thickness = thickness,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
    )
}


/*
서울 송파구 올림픽로 300 (특별시 생략)

잠실 롯데월드타워 (POI)

서울특별시 송파구 신천동 29 (지번)
 */

