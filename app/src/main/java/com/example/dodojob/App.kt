package com.example.dodojob

import android.app.Application
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

class App : Application() {
    lateinit var supabase: SupabaseClient
        private set

    override fun onCreate() {
        super.onCreate()

        val url = BuildConfig.SUPABASE_URL
        val key = BuildConfig.SUPABASE_ANON_KEY
        require(url.isNotBlank() && key.isNotBlank()) {
            "Supabase URL/KEY가 비어 있습니다. local.properties 또는 환경변수 확인."
        }

        supabase = createSupabaseClient(url, key) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }
}