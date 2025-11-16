// app/src/main/java/com/example/dodojob/ui/feature/support/MapRoute.kt
package com.example.dodojob.ui.feature.support

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.data.naver.NaverGeocoding
import com.example.dodojob.data.naver.rememberGeocodedLatLng
import com.example.dodojob.session.CurrentCompany
import kotlinx.parcelize.Parcelize
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.example.dodojob.ui.components.DodoNaverMap
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/* 전달 받을 카드 데이터 */
@Parcelize
data class MapCardData(
    val badgeText: String,   // D-8, 합격, 지원 등
    val company: String,     // 회사명
    val highlight: String,   // | 경력자 우대, 면접예정, 미열람 등
    val title: String,       // 공고 제목
    val distanceText: String, // 내 위치에서 214m (placeholder)
    val imageUrl: String? = null
) : Parcelable

@Composable
fun MapRoute(nav: NavController) {
    // Support 화면에서 저장해둔 데이터를 꺼낸다
    val data = nav.previousBackStackEntry
        ?.savedStateHandle
        ?.get<MapCardData>("mapCard")

    MapScreen(
        data = data,
        onBack = { nav.popBackStack() },
        onSearch = { /* TODO: 검색 */ },
        onLocate = { /* TODO: 내 위치 */ },
        onCardClick = { /* TODO: 카드 클릭 */ }
    )
}

@Composable
fun MapScreen(
    data: MapCardData?,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onLocate: () -> Unit,
    onCardClick: () -> Unit
) {
    val MapBg      = Color(0xFFEDEFF3) // 지도 플레이스홀더
    val StatusBar  = Color(0xFFEFEFEF)
    val Primary    = Color(0xFF005FFF)
    val DangerRed  = Color(0xFFFF2F00)
    val BorderBlue = Color(0xFFC1D2ED)

    var keyword by remember { mutableStateOf("") }
// ✅ 1) 여기 추가: 지도 중심 좌표 + 임시 주소/좌표
    var mapCenter by remember { mutableStateOf<LatLng?>(null) }
    val tempAddress = CurrentCompany.companylocate
    mapCenter = rememberGeocodedLatLng(tempAddress)

    // ▼ 카드 위치/버튼 위치 계산용
    val cardSidePadding   = 18.dp
    val cardBottomPadding = 10.dp
    val density           = LocalDensity.current
    var cardHeightDp by remember { mutableStateOf(170.dp) } // 초기값(대략), 실제 렌더 후 측정됨

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // ✅ 2) 맨 아래에 실제 지도 영역 추가
        if (mapCenter != null) {
            DodoNaverMap(
                modifier = Modifier.fillMaxSize(),
                initialCameraPosition = CameraPosition(mapCenter!!, 16.0),
                enableMyLocation = true,
                markerPosition = mapCenter,
                markerCaption = data?.company ?: ""   // 없으면 빈 문자열
            )
        } else {
            // 좌표 아직 없을 때 임시 배경
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MapBg)
            )
        }
        /* 상태바 대체 */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .align(Alignment.TopCenter)
                .background(StatusBar)
        )

        /* 상단: 뒤로 + 검색창 */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            // 뒤로가기 (둥근 배경)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onBack() },
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.ChevronLeft,
                            contentDescription = "뒤로가기",
                            tint = Color.Black
                        )
                    }
                }
            }

            // 검색창
            val searchShape = RoundedCornerShape(10.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, start = 16.dp, end = 16.dp)
                    .height(57.dp)
                    .clip(searchShape)
                    .background(Color.White)
                    .border(1.dp, BorderBlue, searchShape)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    placeholder = {
                        Text("위치 설정", color = Color(0xFFA6A6A6), fontSize = 18.sp, letterSpacing = (-0.019).em)
                    },
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "검색",
                            tint = Color(0xFF62626D),
                            modifier = Modifier.clickable { onSearch(keyword) }
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        cursorColor = Primary
                    ),
                    shape = searchShape
                )
            }
        }

        /* 하단 플로팅 카드: Support에서 넘어온 data 바인딩 */
        data?.let { d ->
            JobFloatingCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = cardSidePadding, end = cardSidePadding, bottom = cardBottomPadding)
                    .onGloballyPositioned { layout ->
                        cardHeightDp = with(density) { layout.size.height.toDp() }
                    },
                badgeText = d.badgeText,
                company = d.company,
                highlight = d.highlight,
                title = d.title,
                distanceText = d.distanceText,
                onClick = onCardClick,
                primaryBlue = Primary,
                dangerRed = DangerRed
            )
        } ?: run {
            // (옵션) 데이터 없을 때 안내
            Text(
                text = "선택된 항목이 없습니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF777777),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }

        /* 우측 플로팅: 내 위치 —> 카드 오른쪽 위에 붙이기 */
        val myLocBottomOffset: Dp = if (data != null) {
            cardBottomPadding + cardHeightDp + 8.dp // 카드 상단에서 8dp 위
        } else {
            24.dp // 카드 없으면 화면 하단에서 살짝 띄움
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = cardSidePadding, bottom = myLocBottomOffset)
                .size(36.dp)
                .clip(CircleShape)
                .clickable { onLocate() },
            color = Color.White,
            shadowElevation = 3.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.MyLocation,
                    contentDescription = "내 위치",
                    tint = Primary
                )
            }
        }
    }
}

/* 카드 UI */
@Composable
private fun JobFloatingCard(
    modifier: Modifier = Modifier,
    badgeText: String,
    company: String,
    highlight: String,
    title: String,
    distanceText: String,
    onClick: () -> Unit,
    primaryBlue: Color,
    dangerRed: Color,
    imageUrl: String? = null        // ← 이미지 URL 추가
) {
    val shape = RoundedCornerShape(10.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(shape)
            .background(Color.White)
            .clickable { onClick() }
    ) {

        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(Color(0xFFE7E9EE))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 배지
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(dangerRed)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 16.sp,
                    letterSpacing = (-0.019).em,
                    fontWeight = FontWeight.Medium
                )
            }

            // 회사 | 하이라이트
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(company, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF000000))
                Spacer(Modifier.width(6.dp))
                Text(
                    buildAnnotatedString {
                        append("| ")
                        withStyle(SpanStyle(color = dangerRed, fontSize = 15.sp, fontWeight = FontWeight.Medium)) {
                            append(highlight)
                        }
                    }
                )
            }

            // 제목
            Text(
                text = title,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF000000)
            )

            // 거리
            Text(
                text = distanceText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = primaryBlue
            )
        }
    }
}
