package com.example.dodojob

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import com.example.dodojob.navigation.AppNavGraph
import com.example.dodojob.data.supabase.ProvideSupabase
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProvideSupabase {                  // ⬅️ Supabase client 주입
                MaterialTheme {
                    val nav = rememberNavController()
                    AppNavGraph(nav)
                }
            }
        }
        val cid = getString(R.string.naver_map_client_id)
        Log.e("NAVER_SDK", "ClientID=$cid")
        Log.e("NAVER_SDK", "Pkg=" + packageName)

    }
}
