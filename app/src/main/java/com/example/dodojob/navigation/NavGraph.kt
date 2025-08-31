package com.example.dodojob.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dodojob.ui.feature.account.ChangePasswordScreen

import com.example.dodojob.ui.feature.intro.IntroScreen
import com.example.dodojob.ui.feature.experience.ExperienceScreen
import com.example.dodojob.ui.feature.hope.HopeWorkFilterScreen
import com.example.dodojob.ui.feature.jobtype.JobTypeScreen
import com.example.dodojob.ui.feature.login.LoginScreen
import com.example.dodojob.ui.feature.onboarding.OnboardingScreen
import com.example.dodojob.ui.feature.prefer.PreferWorkScreen
import com.example.dodojob.ui.feature.prefer.PreferWorkMapScreen
import com.example.dodojob.ui.feature.signup.SignUpCompleteScreen
import com.example.dodojob.ui.feature.verify.VerifyScreen
import com.example.dodojob.ui.feature.announcement.Announcement1Route
import com.example.dodojob.ui.feature.announcement.Announcement4Route
import com.example.dodojob.ui.feature.announcement.Announcement5Route
import com.example.dodojob.ui.feature.application.ApplicationRoute
import com.example.dodojob.ui.feature.experience.ExperienceCompleteScreen
import com.example.dodojob.ui.feature.main.MainRoute
import com.example.dodojob.ui.feature.profile.ProfileRoute
import com.example.dodojob.ui.feature.signup.SignUpIdPwScreen
import com.example.dodojob.ui.feature.application.ApplyRoute
import com.example.dodojob.ui.todo.TodoScreen
import com.example.dodojob.ui.todo.RealtimeTodoScreen
import com.example.dodojob.ui.feature.login.PreLoginScreen
import com.example.dodojob.ui.feature.main.EmployerHomeRoute
import com.example.dodojob.ui.feature.support.SupportRoute
import com.example.dodojob.ui.feature.profile.ActivityLevelRoute
import com.example.dodojob.ui.feature.support.MapRoute
import com.example.dodojob.ui.feature.management.ManagementAnnouncementRoute


@Composable
fun AppNavGraph(nav: NavHostController) {
    NavHost(navController = nav, startDestination = Route.Main.path) {
        composable(Route.Intro.path) { IntroScreen(nav)}
        composable(Route.Onboarding.path) { OnboardingScreen(nav) }
        composable(Route.Login.path)      { LoginScreen(nav) }
        composable(Route.PreLogin.path) { PreLoginScreen(nav) }

        composable(Route.Verify.path)     { VerifyScreen(nav) }
        composable(Route.JobType.path)    { JobTypeScreen(nav) }
        composable(Route.Prefer.path)     { PreferWorkScreen(nav) }
        composable(Route.PreferMap.path)  { PreferWorkMapScreen(nav) }
        composable(Route.Experience.path) { ExperienceScreen(nav) }
        composable(Route.ExperienceComplete.path) { ExperienceCompleteScreen(nav) }
        composable(Route.Announcement.path) { Announcement1Route(nav) }
        composable(Route.Announcement4.path) { Announcement4Route(nav) }
        composable(Route.Announcement5.path) { Announcement5Route(nav) }

        composable(Route.SignUp.path)     { SignUpIdPwScreen(nav) }
        composable(Route.SignUpComplete.path) { SignUpCompleteScreen(nav) }
        composable(Route.Main.path) { MainRoute(nav) }
        composable(Route.Hope.path) { HopeWorkFilterScreen(nav) }
        composable(Route.My.path) { ProfileRoute(nav) }
        composable(ApplyRoute.path) { ApplicationRoute(nav) }
        composable(Route.Support.path) { SupportRoute(nav) }

        composable(Route.Todo.path) { TodoScreen(nav) }
        composable(Route.TodoRealtime.path) { RealtimeTodoScreen(nav) }

        composable(Route.EmployerHome.path) {EmployerHomeRoute(nav)}
        composable(Route.EmployerNotice.path) {ManagementAnnouncementRoute(nav)}
        composable(Route.ActivityLevel.path) { ActivityLevelRoute(nav)}
        composable(Route.Map.path) {MapRoute(nav)}
        composable(Route.ChangePassword.path) { ChangePasswordScreen(nav)}
    }
}


