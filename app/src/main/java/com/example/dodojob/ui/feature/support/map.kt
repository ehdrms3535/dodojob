// app/src/main/java/com/example/dodojob/ui/feature/support/MapRoute.kt
package com.example.dodojob.ui.feature.support

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dodojob.R
import com.example.dodojob.data.naver.rememberGeocodedLatLng
import com.example.dodojob.session.CurrentCompany
import com.example.dodojob.ui.components.DodoNaverMap
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import kotlinx.parcelize.Parcelize

/* 전달 받을 카드 데이터 */
@Parcelize
data class MapCardData(
    val badgeText: String,   // 지원, 합격 등 (Map에서 디데이로 변환)
    val company: String,     // 회사명
    val highlight: String,   // 열람, 미열람 등 (Map에서 경력자 우대로 변환)
    val title: String,       // 공고 제목
    val distanceText: String, // 내 위치에서 214m (placeholder)
    val imageUrl: String? = null // 지금은 안 쓰고 mapimage.png 사용
) : Parcelable

@Composable
fun MapRoute(nav: NavController) {
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
    val ScreenBg   = Color(0xFFF1F5F7)
    val MapBg      = Color(0xFFF1F5F7)
    val StatusBar  = Color(0xFFEFEFEF)
    val Primary    = Color(0xFF005FFF)
    val DangerRed  = Color(0xFFFF2F00)
    val BorderBlue = Color(0xFFC1D2ED)

    var keyword by remember { mutableStateOf("") }

    // 지도 중심 좌표
    var mapCenter by remember { mutableStateOf<LatLng?>(null) }
    val tempAddress = CurrentCompany.companylocate
    mapCenter = rememberGeocodedLatLng(tempAddress)

    // 카드 / GPS 버튼 위치 계산용
    val cardSidePadding   = 18.dp
    val cardBottomPadding = 10.dp
    val density           = LocalDensity.current
    var cardHeightDp by remember { mutableStateOf(170.dp) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        // 지도
        if (mapCenter != null) {
            DodoNaverMap(
                modifier = Modifier.fillMaxSize(),
                initialCameraPosition = CameraPosition(mapCenter!!, 16.0),
                enableMyLocation = true,
                markerPosition = mapCenter,
                markerCaption = data?.company ?: ""
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MapBg)
            )
        }

        /* 상태바 */
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
            // 뒤로가기 (리소스 그대로)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "뒤로가기",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBack() }
                )
            }

            // 검색창 (search.png 사용)
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
                Text(
                    text = if (keyword.isEmpty()) "위치 설정" else keyword,
                    color = if (keyword.isEmpty()) Color(0xFFA6A6A6) else Color(0xFF000000),
                    fontSize = 18.sp,
                    letterSpacing = (-0.019).em,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSearch(keyword) }
                )

                Image(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "검색",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onSearch(keyword) }
                )
            }
        }

        /* 하단 플로팅 카드 */
        data?.let { d ->
            // 여기서 텍스트 매핑
            val badgeDisplay = when (d.badgeText) {
                "지원" -> "D-8"
                else -> d.badgeText
            }
            val highlightDisplay = when (d.highlight) {
                "열람" -> "경력자 우대"
                else -> d.highlight
            }

            JobFloatingCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(
                        start = cardSidePadding,
                        end = cardSidePadding,
                        bottom = cardBottomPadding
                    )
                    .onGloballyPositioned { layout ->
                        cardHeightDp = with(density) { layout.size.height.toDp() }
                    },
                badgeText = badgeDisplay,
                company = d.company,
                highlight = highlightDisplay,
                title = d.title,
                distanceText = d.distanceText,
                onClick = onCardClick,
                primaryBlue = Primary,
                dangerRed = DangerRed,
                imageRes = R.drawable.mapimage   // 항상 mapimage.png 사용
            )
        } ?: run {
            Text(
                text = "선택된 항목이 없습니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF777777),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }

        /* GPS 버튼 (리소스 그대로) */
        val myLocBottomOffset: Dp = if (data != null) {
            cardBottomPadding + cardHeightDp + 8.dp
        } else {
            24.dp
        }

        Image(
            painter = painterResource(id = R.drawable.gps),
            contentDescription = "내 위치",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = cardSidePadding, bottom = myLocBottomOffset)
                .size(40.dp)
                .clickable { onLocate() }
        )
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
    @DrawableRes imageRes: Int? = null,
    imageUrl: String? = null // 지금은 안 쓰지만 확장용으로 남김
) {
    val cardShape = RoundedCornerShape(10.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(cardShape)
            .background(Color.White)
            .clickable { onClick() }
    ) {
        // 상단 이미지: mapimage.png 고정 (imageRes 우선)
        when {
            imageRes != null -> {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            imageUrl != null -> {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(Color(0xFFE7E9EE))
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // D-8 배지
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(dangerRed)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    letterSpacing = (-0.019).em,
                    fontWeight = FontWeight.Medium
                )
            }

            // 회사명 | 하이라이트
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = company,
                    fontSize = 18.sp,
                    lineHeight = 27.sp,
                    letterSpacing = (-0.019).em,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = " | $highlight",
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    letterSpacing = (-0.019).em,
                    fontWeight = FontWeight.Medium,
                    color = dangerRed
                )
            }

            // 설명 (제목)
            Row(
                modifier = Modifier
                    .padding(top = 0.dp, end = 10.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    letterSpacing = (-0.019).em,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF000000),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 거리 텍스트
            Row(
                modifier = Modifier.padding(end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = distanceText,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    letterSpacing = (-0.019).em,
                    fontWeight = FontWeight.Medium,
                    color = primaryBlue,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
