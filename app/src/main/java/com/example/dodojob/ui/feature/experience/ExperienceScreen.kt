@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dodojob.ui.feature.experience

import androidx.compose.ui.unit.em
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dodojob.App
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import com.example.dodojob.data.userimage.UserimageDto
import com.example.dodojob.data.userimage.UserimageRepository
import com.example.dodojob.data.userimage.UserimageSupabase
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.dodojob.session.CurrentUser

/* ===============================
   업로드 헬퍼
   =============================== */

data class UploadedImage(val url: String, val path: String)

private suspend fun uploadProfileImage(
    client: io.github.jan.supabase.SupabaseClient,
    context: Context,
    userId: String?,
    uri: Uri
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

    val path = "profiles/$userId/${System.currentTimeMillis()}.$ext"
    val bucket = client.storage.from("user-images")

    val ct = when (ext) {
        "png"        -> ContentType.Image.PNG
        "jpg","jpeg" -> ContentType.Image.JPEG
        "webp"       -> ContentType("image","webp")
        else         -> ContentType.Image.JPEG
    }

    bucket.upload(path, bytes) {
        upsert = true
        contentType = ct
    }

    val url = bucket.publicUrl(path) // private 버킷이면 createSignedUrl 사용
    return UploadedImage(url = url, path = path)
}

/* ===============================
   ExperienceScreen (Figma 반영)
   =============================== */

private val ScreenBg = Color(0xFFF1F5F7)
private val Primary = Color(0xFF005FFF)
private val BorderGray = Color(0xFF828282)
private val PlaceholderGray = Color(0xFF727272)
private val DividerGray = Color(0xFFCFCFCF)
private val DimMask = Color(0x6A3E454B)

@Composable
fun ExperienceScreen(nav: NavController) {
    val app = LocalContext.current.applicationContext as App
    val client = app.supabase
    val repo: UserimageRepository = remember(client) { UserimageSupabase(client) }

    var description by remember { mutableStateOf("") }
    var showPhotoSheet by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var tempCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var tempLastCreatedFile by remember { mutableStateOf<File?>(null) }
    var pendingCameraLaunch by remember { mutableStateOf<(() -> Unit)?>(null) }
    val contextState = rememberUpdatedState(LocalContext.current)

    // 추가 섹션 상태 (Figma 입력 필드들)
    var workplace by remember { mutableStateOf("") }
    var mainTasks by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("") }
    var isHealthy by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    /* ----------------- 런처들 ----------------- */
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        Log.d("Camera", "TakePicture success=$success, tempUri=$tempCaptureUri")
        isCapturing = false
        if (success && tempCaptureUri != null) {
            imageUri = tempCaptureUri
        } else {
            tempLastCreatedFile?.delete()
        }
        tempLastCreatedFile = null
        tempCaptureUri = null
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingCameraLaunch?.invoke()
        } else {
            isCapturing = false
            tempLastCreatedFile?.delete()
            tempLastCreatedFile = null
            tempCaptureUri = null
        }
        pendingCameraLaunch = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it }
    }

    /* ----------------- UI ----------------- */
    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            // Figma: 하단 고정 White + 완료 버튼(328x55, radius 10)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 20.dp, bottom = 28.dp)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val ctx = contextState.value
                            val uid = CurrentUser.username
                            var finalUrl = ""

                            try {
                                if (imageUri != null) {
                                    finalUrl = withContext(Dispatchers.IO) {
                                        uploadProfileImage(client, ctx, uid, imageUri!!).url
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("XL", "stage A: upload failed", e)
                                return@launch
                            }

                            val dto = try {
                                UserimageDto(
                                    id = uid,
                                    img_url = finalUrl,
                                    user_imform = description
                                )
                            } catch (e: Exception) {
                                Log.e("XL", "stage C: build dto failed", e)
                                return@launch
                            }

                            try {
                                withContext(Dispatchers.IO) {
                                    repo.insertUserimage(dto)
                                }
                            } catch (e: Exception) {
                                Log.e("XL", "stage D: insert failed", e)
                                return@launch
                            }

                            runCatching { nav.navigate(Route.ExperienceComplete.path) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 9.dp)
                ) {
                    Text("완료", fontSize = 24.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
        ) {
            // 상단 White 영역 (뒤로가기 + 타이틀 + 프로필)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                // Back: drawable/back.png
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "뒤로가기",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { nav.popBackStack() }
                    )
                }
                Spacer(Modifier.height(26.dp))
                Text(
                    text = "프로필을 완성해볼까요?",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    lineHeight = 48.sp // 150%
                )
                Spacer(Modifier.height(20.dp))

                // 프로필 원형(178) — 사진 없으면 camera.png
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(178.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD9D9D9))
                            .clickable { showPhotoSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isCapturing -> CircularProgressIndicator(strokeWidth = 3.dp)
                            imageUri != null -> AsyncImage(
                                model = imageUri,
                                contentDescription = "프로필 사진",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            else -> Image(
                                painter = painterResource(R.drawable.camera),
                                contentDescription = "카메라",
                                modifier = Modifier.size(83.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            Spacer(Modifier.height(18.dp))

            // 섹션 1: 경력 입력 카드
            SectionCard {
                Label("근무장소를 적어주세요")
                OutlinedBoxField(
                    value = workplace,
                    onValueChange = { workplace = it },
                    placeholder = "ex) 카카오 주식회사"
                )
                Spacer(Modifier.height(15.dp))

                Label("주요 업무 내용을 적어주세요")
                OutlinedBoxField(
                    value = mainTasks,
                    onValueChange = { mainTasks = it },
                    placeholder = "ex) 디지털 콘텐츠 기획/운영"
                )
                Spacer(Modifier.height(15.dp))

                Label("근무기간")
                OutlinedBoxField(
                    value = period,
                    onValueChange = { period = it },
                    placeholder = "ex) 2013.01.01 ~ 2025.09.26"
                )
                Spacer(Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(
                            if (isHealthy) R.drawable.checked_mark else R.drawable.unchecked_mark
                        ),
                        contentDescription = if (isHealthy) "체크됨" else "미체크",
                        modifier = Modifier
                            .size(width = 24.dp, height = 25.dp)
                            .clickable { isHealthy = !isHealthy }
                    )
                    Spacer(Modifier.width(11.dp))
                    Text(
                        text = "(필수) 개인정보 제 3자 제공 동의",
                        fontSize = 16.sp,
                        color = Color(0xFFFF2F00),
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(20.dp))

                PrimaryFullButton(text = "추가하기") {
                    // TODO: 항목 추가 핸들러
                }
            }

            Spacer(Modifier.height(18.dp))

            // 섹션 2: 공고등록
            SectionCard {
                Label("관련 자격증")
                OutlinedBoxField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "예: 컴퓨터 활용, 영어회화, 운전가능"
                )
                Spacer(Modifier.height(20.dp))
                PrimaryFullButton(text = "추가하기") {
                    // TODO: 공고등록 액션
                }
            }

            Spacer(Modifier.height(18.dp))
        }

        // 사진 선택 모달 (Figma 수치 그대로)
        if (showPhotoSheet) {
            PhotoOptionsDialog330(
                onDismiss = { showPhotoSheet = false },
                onPickCamera = {
                    val (file, uri) = createTempImageUri(contextState.value)
                    Log.d("Camera", "created temp file=${file.absolutePath}, exists=${file.exists()}, uri=$uri")
                    tempLastCreatedFile = file
                    tempCaptureUri = uri
                    isCapturing = true

                    val granted = ContextCompat.checkSelfPermission(
                        contextState.value, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (granted) {
                        takePictureLauncher.launch(uri)
                    } else {
                        pendingCameraLaunch = { takePictureLauncher.launch(uri) }
                        requestCameraPermission.launch(Manifest.permission.CAMERA)
                    }
                    showPhotoSheet = false
                },
                onPickGallery = {
                    galleryLauncher.launch("image/*")
                    showPhotoSheet = false
                },
                onUseDefault = {
                    imageUri = Uri.parse("android.resource://${contextState.value.packageName}/${R.drawable.basic_profile}")
                    showPhotoSheet = false
                }
            )
        }
    }
}

/* ===============================
   다이얼로그 (Figma 330 x 280.7)
   =============================== */

@Composable
private fun PhotoOptionsDialog330(
    onDismiss: () -> Unit,
    onPickCamera: () -> Unit,
    onPickGallery: () -> Unit,
    onUseDefault: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Dim overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DimMask)
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Frame 1707480511
            Box(
                modifier = Modifier
                    .padding(bottom = 48.dp)
                    .width(330.dp)
                    .height(280.7.dp)
            ) {
                // Frame 1707480510 (카드)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(330.dp)
                        .height(260.7.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(start = 18.dp, end = 18.dp, top = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 타이틀 (Frame 3468994/3468993)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(47.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(135.dp)
                                .height(47.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "프로필 사진 설정",
                                color = Color(0xFF828282),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = (-0.019).em
                            )
                        }
                    }

                    // Divider
                    HorizontalDivider(
                        color = DividerGray,
                        thickness = 1.dp,
                        modifier = Modifier.width(294.dp)
                    )

                    Spacer(Modifier.height(15.dp))

                    // 옵션 섹션 (Frame 3468998)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(169.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 카메라로 찍기 (Frame 3468995)
                        Column(
                            modifier = Modifier
                                .width(294.dp)
                                .height(51.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(294.dp)
                                    .height(39.dp)
                                    .clickable { onPickCamera() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "카메라로 찍기",
                                    color = Primary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = (-0.019).em,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Divider
                        HorizontalDivider(
                            color = DividerGray,
                            thickness = 1.dp,
                            modifier = Modifier.width(294.dp)
                        )

                        Spacer(Modifier.height(15.dp))

                        // 앨범/기본 이미지 (Frame 3468997)
                        Column(
                            modifier = Modifier
                                .width(294.dp)
                                .height(103.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 앨범에서 사진 선택 (Frame 3468996)
                            Column(
                                modifier = Modifier
                                    .width(294.dp)
                                    .height(49.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(294.dp)
                                        .height(39.dp)
                                        .clickable { onPickGallery() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "앨범에서 사진 선택",
                                        color = Primary,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = (-0.019).em,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Divider
                            HorizontalDivider(
                                color = DividerGray,
                                thickness = 1.dp,
                                modifier = Modifier.width(294.dp)
                            )

                            Spacer(Modifier.height(15.dp))

                            // 기본 이미지 적용
                            Box(
                                modifier = Modifier
                                    .width(294.dp)
                                    .height(39.dp)
                                    .clickable { onUseDefault() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "기본 이미지 적용",
                                    color = Primary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = (-0.019).em,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ===============================
   재사용 컴포넌트
   =============================== */

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 20.dp, horizontal = 18.dp), // 👈 여기서 양옆 패딩 조절
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun PrimaryFullButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()  // Figma width
            .height(55.dp),  // Figma height
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(vertical = 9.dp)
    ) {
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 15.dp)
    )
}

@Composable
private fun OutlinedBoxField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(47.dp)
            .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(placeholder, color = PlaceholderGray, fontSize = 18.sp)
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(27.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
        )
    }
}

/* ===============================
   촬영용 임시 파일/Uri
   =============================== */
private fun createTempImageUri(context: Context): Pair<File, Uri> {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())
        val file = File(File(context.cacheDir, "images").apply { mkdirs() }, "IMG_${timeStamp}.jpg")
        val authority = "${context.packageName}.fileprovider"
        Log.d("Camera", "authority=$authority")
        val uri = FileProvider.getUriForFile(context, authority, file)
        file to uri
    } catch (e: Exception) {
        Log.e("Camera", "createTempImageUri failed", e)
        throw e
    }
}