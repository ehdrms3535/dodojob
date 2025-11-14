@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dodojob.data.naver

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.dodojob.ui.components.DodoNaverMap
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton


@Composable
fun InterviewPlaceMapScreen(
    navController: NavController,
    lat: Double,
    lng: Double,
    label: String
) {
    val center = remember(lat, lng) { LatLng(lat, lng) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("면접 장소 위치") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            DodoNaverMap(
                modifier = Modifier.fillMaxSize(),
                initialCameraPosition = CameraPosition(center, 16.0),
                enableMyLocation = false
            )

            // 필요하면 하단에 주소 표시용 박스 추가
        }
    }
}
