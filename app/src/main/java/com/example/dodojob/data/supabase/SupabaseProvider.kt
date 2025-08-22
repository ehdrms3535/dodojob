package com.example.dodojob.data.supabase

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.example.dodojob.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

val LocalSupabase = androidx.compose.runtime.staticCompositionLocalOf<SupabaseClient> {
    error("SupabaseClient is not provided")
}

@Composable
fun ProvideSupabase(content: @Composable () -> Unit) {
    val url = BuildConfig.SUPABASE_URL
    val key = BuildConfig.SUPABASE_ANON_KEY

    if (url.isBlank() || key.isBlank()) {
        Log.e("Supabase", "Missing config. URL='${url.take(8)}…' KEY.len=${key.length}")
        Text("Supabase 설정(SUPABASE_URL/ANON_KEY)이 비어 있어요.")
        return
    }

    val client = remember {
        createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
            install(Auth); install(Postgrest); install(Realtime)
        }
    }
    CompositionLocalProvider(LocalSupabase provides client) { content() }
}