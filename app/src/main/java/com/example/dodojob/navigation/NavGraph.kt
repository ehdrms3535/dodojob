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
import com.example.dodojob.ui.feature.prefer.PreferWorkSheetScreen
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
<<<<<<< HEAD
    NavHost(navController = nav, startDestination = Route.JobType.path) {
        composable(Route.Intro.path) { IntroScreen(nav)}              // 1. 시작화면
        composable(Route.Onboarding.path) { OnboardingScreen(nav) }   // 2. 직업 선택
        composable(Route.Login.path)      { LoginScreen(nav) }        // 3. 시니어 로그인
        composable(Route.PreLogin.path) { PreLoginScreen(nav) }       // 3-1. 고용주 로그인

        composable(Route.Verify.path)     { VerifyScreen(nav) }       // 4. 인증(회원가입)
        composable(Route.SignUp.path)     { SignUpIdPwScreen(nav) }   // 4-1. 회원가입
        composable(Route.SignUpComplete.path) { SignUpCompleteScreen(nav) } // 4-2. 회원가입 성공


        composable(Route.JobType.path)    { JobTypeScreen(nav) }      // 5. 회원가입 이후
        composable(Route.Hope.path) { HopeWorkFilterScreen(nav) }    //
        composable(Route.Prefer.path)     { PreferWorkSheetScreen(nav) } // 선호 직업
        composable(Route.PreferMap.path)  { PreferWorkMapScreen(nav) } // 선호 직업 지도

        composable(Route.Experience.path) { ExperienceScreen(nav) } // 프로필 사진(프로필 완성 직전)
        composable(Route.ExperienceComplete.path) { ExperienceCompleteScreen(nav) } // 프로필 완성

        composable(Route.Main.path) { MainRoute(nav) } // main
=======
    NavHost(navController = nav, startDestination = Route.Intro.path) {
        composable(Route.Intro.path) { IntroScreen(nav)}
        composable(Route.Onboarding.path) { OnboardingScreen(nav) }
        composable(Route.Login.path)      { LoginScreen(nav) }
        composable(Route.PreLogin.path) { PreLoginScreen(nav) }
>>>>>>> 5b5d2e97ec1fafdd8967cf0961e74c7b38f3cce3

        composable(Route.Announcement.path) { Announcement1Route(nav) }
        composable(Route.Announcement4.path) { Announcement4Route(nav) }
        composable(Route.Announcement5.path) { Announcement5Route(nav) }



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


