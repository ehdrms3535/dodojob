package com.example.dodojob.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dodojob.ui.feature.experience.ExperienceScreen
import com.example.dodojob.ui.feature.experience.MainScreen
import com.example.dodojob.ui.feature.hope.HopeWorkFilterScreen
import com.example.dodojob.ui.feature.jobtype.JobTypeScreen
import com.example.dodojob.ui.feature.login.LoginScreen
import com.example.dodojob.ui.feature.onboarding.OnboardingScreen
import com.example.dodojob.ui.feature.prefer.PreferWorkScreen
import com.example.dodojob.ui.feature.prefer.PreferWorkMapScreen
import com.example.dodojob.ui.feature.signup.SignUpCompleteScreen
import com.example.dodojob.ui.feature.verify.VerifyScreen
import com.example.dodojob.ui.feature.announcement.Announcement1Route
import com.example.dodojob.ui.feature.announcement.Announcement1Route
import com.example.dodojob.ui.feature.experience.ExperienceCompleteScreen
import com.example.dodojob.ui.feature.signup.SignUpIdPwScreen

@Composable
fun AppNavGraph(nav: NavHostController) {
    NavHost(navController = nav, startDestination = Route.JobType.path) {
        composable(Route.Onboarding.path) { OnboardingScreen(nav) }
        composable(Route.Login.path)      { LoginScreen(nav) }
        composable(Route.Verify.path)     { VerifyScreen(nav) }
        composable(Route.JobType.path)    { JobTypeScreen(nav) }
        composable(Route.Prefer.path)     { PreferWorkScreen(nav) }
        composable(Route.PreferMap.path)  { PreferWorkMapScreen(nav) }
        composable(Route.Experience.path) { ExperienceScreen(nav) }
        composable(Route.ExperienceComplete.path) { ExperienceCompleteScreen(nav) }
        composable(Route.Announcement.path) { Announcement1Route(nav) }
        composable(Route.SignUp.path)     { SignUpIdPwScreen(nav) }
        composable(Route.SignUpComplete.path) { SignUpCompleteScreen(nav) }
        composable(Route.Main.path) { MainScreen(nav) }
        composable(Route.Hope.path) { HopeWorkFilterScreen(nav) }
    }
}

