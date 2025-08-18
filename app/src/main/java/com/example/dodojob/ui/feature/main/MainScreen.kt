package com.example.dodojob.ui.feature.experience

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun MainScreen(nav: NavController) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("메인 화면 (임시)", fontSize = 20.sp)
    }
}
