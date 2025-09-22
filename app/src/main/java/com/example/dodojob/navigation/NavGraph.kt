package com.example.dodojob.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dodojob.ui.feature.account.ChangePasswordScreen
import com.example.dodojob.session.SessionViewModel

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
import com.example.dodojob.ui.feature.employ.ApplicantManagementRoute
import com.example.dodojob.ui.feature.profile.LikedJobsRoute
import com.example.dodojob.ui.todo.TodoScreen
import com.example.dodojob.ui.todo.RealtimeTodoScreen
import com.example.dodojob.ui.feature.login.PreLoginScreen
import com.example.dodojob.ui.feature.main.EmployerHomeRoute
import com.example.dodojob.ui.feature.support.SupportRoute
import com.example.dodojob.ui.feature.profile.ActivityLevelRoute
import com.example.dodojob.ui.feature.support.MapRoute
import com.example.dodojob.ui.feature.employ.ManagementAnnouncementRoute
import com.example.dodojob.ui.feature.signup.EmployerSignupScreen
import com.example.dodojob.ui.feature.signup.EmploySignUpIdPwScreen
import com.example.dodojob.ui.feature.profile.RecentViewedRoute
import com.example.dodojob.ui.feature.employ.SuggestInterviewScreen
import com.example.dodojob.ui.feature.employ.ApplicantInformationScreen
@Composable
fun AppNavGraph(nav: NavHostController,sessionVm: SessionViewModel) {
    NavHost(navController = nav,startDestination = Route.Intro.path) {

        composable(Route.Intro.path) { IntroScreen(nav)}              // 1. 시작화면
        composable(Route.Onboarding.path) { OnboardingScreen(nav) }   // 2. 직업 선택
        composable(Route.Login.path)      { LoginScreen(nav,sessionVm) }        // 3. 시니어 로그인
        composable(Route.PreLogin.path) { PreLoginScreen(nav,sessionVm) }       // 3-1. 고용주 로그인

        composable(Route.Verify.path)     { VerifyScreen(nav,sessionVm) }       // 4. 인증(회원가입)
        composable(Route.SignUp.path)     { SignUpIdPwScreen(nav) }   // 4-1. 회원가입
        composable(Route.SignUpComplete.path) { SignUpCompleteScreen(nav) } // 4-2. 회원가입 성공
        composable(Route.EmploySignupsec.path) { EmploySignUpIdPwScreen(nav) }
        composable(Route.EmploySignup.path) { EmployerSignupScreen(nav)}

        composable(Route.JobType.path)    { JobTypeScreen(nav) }      // 5. 회원가입 이후
        composable(Route.Hope.path) { HopeWorkFilterScreen(nav) }    //
        composable(Route.Prefer.path)     { PreferWorkSheetScreen(nav) } // 선호 직업
        composable(Route.PreferMap.path)  { PreferWorkMapScreen(nav) } // 선호 직업 지도

        composable(Route.Experience.path) { ExperienceScreen(nav) } // 프로필 사진(프로필 완성 직전)
        composable(Route.ExperienceComplete.path) { ExperienceCompleteScreen(nav) } // 프로필 완성

        composable(Route.Main.path) { MainRoute(nav) } // main

        composable(Route.Announcement.path) { Announcement1Route(nav) } // 공고등록1
        composable(Route.Announcement4.path) { Announcement4Route(nav) } // 공고등록 4
        composable(Route.Announcement5.path) { Announcement5Route(nav) } // 공고등록 5



        composable(Route.My.path) { ProfileRoute(nav) } // 시니어 프로필
        composable(ApplyRoute.path) { ApplicationRoute(nav) } // 지원서 작성
        composable(Route.Support.path) { SupportRoute(nav) } // 지원 내역
        composable(Route.RecentlyViewed.path) { RecentViewedRoute(nav) } // 최근 본 공고
        composable(Route.LikedJob.path) { LikedJobsRoute(nav) } // 좋아요한 공고

        composable(Route.Todo.path) { TodoScreen(nav) } // 테스트
        composable(Route.TodoRealtime.path) { RealtimeTodoScreen(nav) } // 테스트

        composable(Route.EmployerHome.path) {EmployerHomeRoute(nav)} // 고용주 메인
        composable(Route.EmployerNotice.path) {ManagementAnnouncementRoute(nav)} // 공고관리
        composable(Route.EmployerApplicant.path) {ApplicantManagementRoute(nav)} // 지원자관리
        composable(Route.SuggestInterview.path) {SuggestInterviewScreen(nav)} // 면접지원}
        composable(Route.InformationOfApplicants.path) {ApplicantInformationScreen(nav)} // 지원자정보)

        composable(Route.ActivityLevel.path) { ActivityLevelRoute(nav)} // 활동 레벨
        composable(Route.Map.path) {MapRoute(nav)} // 지도
        composable(Route.ChangePassword.path) { ChangePasswordScreen(nav)} // 비밀번호 변경
    }
}

