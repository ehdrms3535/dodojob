package com.example.dodojob.data.supabase

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.example.dodojob.App
import io.github.jan.supabase.SupabaseClient
import androidx.compose.runtime.staticCompositionLocalOf


val LocalSupabase = staticCompositionLocalOf<SupabaseClient> {
    error("Supabase client not provided")
}

@Composable
fun ProvideSupabase(content: @Composable () -> Unit) {
    val app = LocalContext.current.applicationContext as App
    val client = app.supabase  // ← App.kt에서 만든 싱글턴만 사용
    CompositionLocalProvider(LocalSupabase provides client) {
        content()
    }
}
