@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dodojob.ui.feature.experience

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider

@Composable
fun ExperienceScreen(nav: NavController) {
    // ================== UI State ==================
    val Bg = Color(0xFFF1F5F7)
    val Primary = Color(0xFF005FFF)

    var description by remember { mutableStateOf("") }
    var showPhotoSheet by remember { mutableStateOf(false) }

    // 최종 프로필 이미지 Uri (성공 후에만 세팅)
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // 촬영 준비/진행 상태
    var isCapturing by remember { mutableStateOf(false) }

    // 촬영 전 임시로 들고 있을 Uri/File
    var tempCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var tempLastCreatedFile by remember { mutableStateOf<File?>(null) }

    // 권한 허용 후 실행할 지연 함수 (전역 ❌)
    var pendingCameraLaunch by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Composable 밖(콜백)에서도 안전하게 context 접근
    val contextState = rememberUpdatedState(LocalContext.current)

    // ================== Launchers ==================
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        isCapturing = false
        if (success && tempCaptureUri != null) {
            imageUri = tempCaptureUri
        } else {
            // 실패/취소 시 임시 파일 정리
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
            // 영구 거부이면 설정 이동 안내를 띄우면 좋아요
        }
        pendingCameraLaunch = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it }
    }

    // ================== UI ==================
    Scaffold(
        containerColor = Bg,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Bg)
                    .padding(horizontal = 18.dp, vertical = 50.dp)
            ) {
                Button(
                    onClick = { nav.navigate(Route.ExperienceComplete.path) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("완료", fontSize = 25.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    ) { inner ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            val W = maxWidth
            val H = maxHeight

            val hPad = (W * 0.045f)
            val titleTop = (H * 0.04f)
            val titleSp = (W.value * 0.085f).sp
            val backSp = (W.value * 0.065f).sp
            val subTop = (H * 0.008f)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = hPad)
            ) {
                Spacer(Modifier.height(titleTop))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "<",
                        fontSize = backSp,
                        color = Color.Black,
                        modifier = Modifier.clickable { nav.popBackStack() }
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "프로필을 완성해볼까요?",
                    fontSize = titleSp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Spacer(Modifier.height(subTop))
                Spacer(Modifier.height(20.dp))

                // ===== 프로필 이미지 영역 =====
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
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
                            isCapturing -> {
                                CircularProgressIndicator(strokeWidth = 3.dp)
                            }
                            imageUri != null -> {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "프로필 사진",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Filled.AddAPhoto,
                                    contentDescription = "사진 추가",
                                    tint = Color(0xFF606060),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(35.dp))

                Text(
                    text = "경력사항을 적어주세요",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(10.dp), clip = true)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp),
                        placeholder = {
                            Text(
                                "담당업무와 근무했던 회사명을 적어주세요",
                                color = Color(0xFF999999),
                                fontSize = 16.sp
                            )
                        },
                        singleLine = false,
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            errorContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        )
                    )
                }

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = { showPhotoSheet = true },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(39.dp),
                    shape = RoundedCornerShape(31.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x2B005FFF),
                        contentColor = Primary
                    ),
                    contentPadding = PaddingValues(horizontal = 15.dp, vertical = 8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("추가", fontSize = 16.sp, textAlign = TextAlign.Center)
                }

                Spacer(Modifier.height(12.dp))
            }

            // ===== 사진 선택 모달 =====
            if (showPhotoSheet) {
                PhotoOptionsDialog(
                    onDismiss = { showPhotoSheet = false },
                    onPickCamera = {
                        // 1) 촬영용 파일/Uri 생성
                        val (file, uri) = createTempImageUri(contextState.value)
                        tempLastCreatedFile = file
                        tempCaptureUri = uri
                        isCapturing = true

                        // 2) 권한 체크 후 촬영 실행
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
}

/** 촬영 결과를 저장할 임시 이미지 파일과 해당 Uri(FileProvider)를 생성 */
private fun createTempImageUri(context: Context): Pair<File, Uri> {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
    val imageFileName = "IMG_${timeStamp}.jpg"
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(imagesDir, imageFileName)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    return file to uri
}

@Composable
private fun PhotoOptionsDialog(
    onDismiss: () -> Unit,
    onPickCamera: () -> Unit,
    onPickGallery: () -> Unit,
    onUseDefault: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x6A3E454B))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 48.dp)
                    .width(340.dp)
                    .wrapContentHeight()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 18.dp, vertical = 9.dp)
                    .clickable(enabled = false) {},
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "프로필 사진 설정",
                        color = Color(0xFF828282),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFCFCFCF))

                    OptionRow(text = "카메라로 찍기", onClick = onPickCamera)
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFCFCFCF))

                    OptionRow(text = "앨범에서 사진 선택", onClick = onPickGallery)
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFCFCFCF))

                    OptionRow(text = "기본 이미지 적용", onClick = onUseDefault)

                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun OptionRow(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(51.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF005FFF),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
