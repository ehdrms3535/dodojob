package com.example.dodojob

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import com.example.dodojob.navigation.AppNavGraph
import com.example.dodojob.data.supabase.ProvideSupabase

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
    }
}
