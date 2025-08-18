package com.example.dodojob.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dodojob.ui.feature.experience.ExperienceScreen
import com.example.dodojob.ui.feature.jobtype.JobTypeScreen
import com.example.dodojob.ui.feature.login.LoginScreen
import com.example.dodojob.ui.feature.onboarding.OnboardingScreen
import com.example.dodojob.ui.feature.prefer.PreferWorkScreen
import com.example.dodojob.ui.feature.prefer_map.PreferWorkMapScreen
import com.example.dodojob.ui.feature.verify.VerifyScreen
import com.example.dodojob.ui.feature.announcement.Announcement1Route
import com.example.dodojob.ui.feature.announcement.Announcement1Route
@Composable
fun AppNavGraph(nav: NavHostController) {
    NavHost(navController = nav, startDestination = Route.Onboarding.path) {
        composable(Route.Onboarding.path) { OnboardingScreen(nav) }
        composable(Route.Login.path)      { LoginScreen(nav) }
        composable(Route.Verify.path)     { VerifyScreen(nav) }
        composable(Route.JobType.path)    { JobTypeScreen(nav) }
        composable(Route.Prefer.path)     { PreferWorkScreen(nav) }
        composable(Route.PreferMap.path)  { PreferWorkMapScreen(nav) }
        composable(Route.Experience.path) { ExperienceScreen(nav) }
        composable(Route.Announcement.path) { Announcement1Route(nav) }
    }
}
