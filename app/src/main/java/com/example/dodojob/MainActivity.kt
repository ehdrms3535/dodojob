package com.example.dodojob

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import com.example.dodojob.navigation.AppNavGraph
import com.example.dodojob.data.supabase.ProvideSupabase
import com.example.dodojob.session.SessionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dodojob.ui.theme.DodoJobTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProvideSupabase {  // App 싱글턴을 내부에서 제공
                DodoJobTheme {
                    val nav = rememberNavController()
                    val sessionVm: SessionViewModel = viewModel()
                    AppNavGraph(nav, sessionVm)
                }
            }
        }
    }
}
