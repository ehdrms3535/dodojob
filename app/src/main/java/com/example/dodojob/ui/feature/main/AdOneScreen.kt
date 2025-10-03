package com.example.dodojob.ui.feature.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R

@Composable
fun AdOneScreen(nav: NavController) {
    val url =
        "https://www.dmall.co.kr/product/list.html?cate_no=56&NaPm=ct%3D..."

    Scaffold(
        containerColor = Color.White,
        topBar = { AdTopBar(nav) },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // title 18sp
                Text(
                    "더 편하게 관리하고 싶다면?",
                    color = Color(0xFF005FFF),
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    textAlign = TextAlign.Center
                )
                // 버튼 24sp
                OpenUrlButton("오쏘몰 건강보조제 보러가기", url)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier.padding(inner).verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = painterResource(R.drawable.main_ad1),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                contentScale = ContentScale.FillWidth
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AdTopBar(nav: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { nav.popBackStack() },modifier = Modifier.offset(x = 0.dp, y = 12.dp)) {
            Icon(
                painter = painterResource(R.drawable.back),
                contentDescription = "뒤로가기",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// 버튼 텍스트만 24sp
@Composable
private fun OpenUrlButton(text: String, url: String, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    Button(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            ctx.startActivity(intent)
        },
        modifier = modifier.fillMaxWidth().height(64.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF005FFF), // 🔥 버튼 배경색
            contentColor = Color.White         // 텍스트 색상
        )
    ) {
        Text(text, fontSize = 24.sp)
    }
}
