package com.example.dodojob.navigation

import com.example.dodojob.ui.feature.main.AdOneScreen
import com.example.dodojob.ui.feature.main.AdTwoScreen
import com.example.dodojob.ui.feature.main.AdThreeScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dodojob.ui.feature.account.ChangePasswordScreen
import com.example.dodojob.session.SessionViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
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
import com.example.dodojob.ui.feature.employ.EmployerHumanResourceScreen
import com.example.dodojob.ui.feature.employ.EmployerMyRoute
import com.example.dodojob.ui.feature.employ.ViewResourceDetailScreen
import com.example.dodojob.ui.feature.employ.ScrappedHumanResourceScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.navigation
import com.example.dodojob.ui.feature.education.*
import com.example.dodojob.ui.feature.education.EducationViewModel
import com.example.dodojob.ui.feature.employ.EditEmployerInformationScreen
import com.example.dodojob.ui.feature.main.EmployerAdOneScreen
import com.example.dodojob.ui.feature.main.EmployerAdThreeScreen
import com.example.dodojob.ui.feature.main.EmployerAdTwoScreen
import com.example.dodojob.ui.feature.signup.PostingRegisterCompleteScreen

@Composable
fun AppNavGraph(nav: NavHostController,sessionVm: SessionViewModel) {
    NavHost(navController = nav,startDestination = Route.Intro.path) {

        composable(Route.Intro.path) { IntroScreen(nav) }              // 1. 시작화면
        composable(Route.Onboarding.path) { OnboardingScreen(nav) }   // 2. 직업 선택
        composable(Route.Login.path) { LoginScreen(nav, sessionVm) }        // 3. 시니어 로그인
        composable(Route.PreLogin.path) { PreLoginScreen(nav, sessionVm) }       // 3-1. 고용주 로그인

        composable(Route.Verify.path) { VerifyScreen(nav, sessionVm) }       // 4. 인증(회원가입)
        composable(Route.SignUp.path) { SignUpIdPwScreen(nav) }   // 4-1. 회원가입
        composable(Route.SignUpComplete.path) { SignUpCompleteScreen(nav) } // 4-2. 회원가입 성공
        composable(Route.EmploySignupsec.path) { EmploySignUpIdPwScreen(nav) }
        composable(Route.EmploySignup.path) { EmployerSignupScreen(nav) }
        composable(Route.PostingRegisterCompleteScreen.path) {PostingRegisterCompleteScreen(nav)}

        composable(Route.JobType.path) { JobTypeScreen(nav) }      // 5. 회원가입 이후
        composable(Route.Hope.path) { HopeWorkFilterScreen(nav) }    //
        composable(Route.Prefer.path) { PreferWorkSheetScreen(nav) } // 선호 직업
        composable(Route.PreferMap.path) { PreferWorkMapScreen(nav) } // 선호 직업 지도

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

        composable(Route.EmployerHome.path) { EmployerHomeRoute(nav) } // 고용주 메인
        composable("employer/ad/1") { EmployerAdOneScreen(nav) }
        composable("employer/ad/2") { EmployerAdTwoScreen(nav) }
        composable("employer/ad/3") { EmployerAdThreeScreen(nav) }

        composable(Route.EmployerNotice.path) { ManagementAnnouncementRoute(nav) } // 공고관리

        composable(Route.EmployerApplicant.path) { ApplicantManagementRoute(nav) } // 지원자관리
        composable(Route.SuggestInterview.path) { SuggestInterviewScreen(nav) } // 면접지원}
        composable(Route.InformationOfApplicants.path) { ApplicantInformationScreen(nav) } // 지원자정보)
        composable(Route.EmployerHumanResource.path) { EmployerHumanResourceScreen(nav) } // 인재)
        composable(Route.ViewResourceDetail.path) { ViewResourceDetailScreen(nav) } //인재 상세보기
        composable(Route.ScrrapedHumanResource.path) { ScrappedHumanResourceScreen(nav) } //인재 스크랩

        composable(Route.EmployerMy.path) { EmployerMyRoute(nav) } // 고용주 마이
        composable(Route.EditEmployerInformation.path) { EditEmployerInformationScreen(nav) }

        composable(Route.ActivityLevel.path) { ActivityLevelRoute(nav) } // 활동 레벨
        composable(Route.Map.path) { MapRoute(nav) } // 지도
        composable(Route.ChangePassword.path) { ChangePasswordScreen(nav) } // 비밀번호 변경

        composable("ad/1") { AdOneScreen(nav) }
        composable("ad/2") { AdTwoScreen(nav) }
        composable("ad/3") { AdThreeScreen(nav) }

        //  복지 메인
        composable("welfare/home") {
            com.example.dodojob.ui.feature.welfare.WelfareHomeRoute(nav)
        }


        // 복지 카테고리 (탭 파라미터)
        composable(
            route = "welfare/category/{tab}",
            arguments = listOf(navArgument("tab") { type = NavType.StringType })
        ) { backStackEntry ->
            val tabArg = backStackEntry.arguments?.getString("tab")
            com.example.dodojob.ui.feature.welfare.HealthLeisureRoute(
                nav = nav,
                startTabArg = tabArg   // "health" 또는 "leisure"
            )
        }

        navigation(
            startDestination = Route.EduHome.path,
            route = Route.EduGraph.path
        ) {
            // 교육 홈
            composable(Route.EduHome.path) { entry ->
                val parentEntry = remember(entry) { nav.getBackStackEntry(Route.EduGraph.path) }
                val eduVm: EducationViewModel = viewModel(parentEntry)
                EducationHomeRoute(
                    nav = nav,
                    userName = "홍길동",
                    eduVm = eduVm
                )
            }

            // 내 강좌 (이어보기/찜한 강의)
            composable(Route.EduMy.path) { entry ->
                val parentEntry = remember(entry) { nav.getBackStackEntry(Route.EduGraph.path) }
                val eduVm: EducationViewModel = viewModel(parentEntry)
                val all = remember { recommendedCourses() + liveHotCourses() }
                EducationLibraryScreen(
                    nav = nav,
                    userName = "홍길동",
                    favorites = eduVm.favorites,
                    allCourses = all
                )
            }

            // ✅ 강의 초기 진입(수강신청 시트 자동 오픈) — courseId 인자 추가
            composable(
                route = Route.EduLectureInitial.path, // "edu_lecture_ini/{courseId}"
                arguments = listOf(navArgument("courseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                EducationLectureScreen(
                    courseId = courseId,
                    showEnrollOnLaunch = true,
                    showEnrollTrigger = false,
                    onNavigatePaymentComplete = { nav.navigate(Route.EduPaymentComplete.of(courseId)) },
                    onBack = { nav.popBackStack() }
                )
            }

            // 결제 완료
            composable(
                route = Route.EduPaymentComplete.path, // "edu_payment/{courseId}"
                arguments = listOf(navArgument("courseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""

                PaymentCompleteScreen(
                    onDone = {
                        // ✅ 같은 코스로 일반 강의 화면 진입 + 초기 진입 화면 제거
                        nav.navigate(Route.EduLectureNormal.of(courseId)) {
                            popUpTo(Route.EduLectureInitial.path) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }


            // ✅ 일반 강의 화면 — courseId 인자 추가
            composable(
                route = Route.EduLectureNormal.path, // "edu_lecture_nor/{courseId}"
                arguments = listOf(navArgument("courseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                EducationLectureScreen(
                    courseId = courseId,
                    showEnrollOnLaunch = false,
                    showEnrollTrigger = false,
                    onNavigatePaymentComplete = { /* not used */ },
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}

