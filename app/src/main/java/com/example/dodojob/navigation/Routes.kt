package com.example.dodojob.navigation



sealed class Route(val path: String) {
    data object Intro : Route("intro")
    data object Onboarding : Route("onboarding")
    data object Login : Route("login")
    data object PreLogin : Route("prelogin")
    data object EmploySignupsec : Route("employsignupsec")
    data object Verify : Route("verify")

    data object  EmploySignup : Route("employsignup")

    data object JobType : Route("job_type")
    data object Prefer : Route("prefer")
    data object PreferMap : Route("prefer_map")
    data object Experience : Route("experience")
    data object ExperienceComplete : Route("experience_complete")
    data object Announcement : Route("announcement")
    data object Announcement2 : Route("announcement2")
    data object Announcement3 : Route("announcement3")
    data object Announcement4 : Route("announcement4")
    data object Announcement5 : Route("announcement5")
    data object Hope : Route("hope")
    data object SignUp : Route("signup")
    data object SignUpComplete : Route("signup_complete")
    data object Main : Route("main")
    data object My : Route("my")
    data object Application : Route("application")
    data object Support : Route("support")
    data object Todo : Route("todo")
    data object TodoRealtime : Route("todo_realtime")
    data object EmployerHome : Route("employer_home")
    data object EmployerNotice : Route("employer_notice")
    data object EmployerApplicant : Route("employer_applicant")
    data object EmployerMy : Route("employer_my")
    data object ActivityLevel : Route("activity_level")
    data object Map : Route("map")
    data object ChangePassword : Route("change_password")
    data object RecentlyViewed : Route("recently_viewed")
    data object LikedJob : Route("liked_job")
    data object ManagementApplicants : Route("management_applicants")
    data object SuggestInterview : Route("suggest_interview")
    data object InformationOfApplicants : Route("information_of_applicants")

    data object WelfareHome : Route("welfare/home")
    data object WelfareCategory : Route("welfare/category/{tab}")

    companion object {
        // ✅ 편의 함수 (탭에 따라 실제 네비게이션 경로 생성)
        fun welfareCategoryOf(tab: String) = "welfare/category/$tab"
        // 혹은 enum 쓰면: fun welfareCategoryOf(tab: CategoryTab) = "welfare/category/${ if (tab==CategoryTab.Health) "health" else "leisure" }"
    }

    data object EduHome : Route("edu")      // 교육 홈
    data object EduMy   : Route("edu/my")   // 단일 화면(이어보기/찜한 강의 탭)

    data object EduGraph : Route("edu_graph")


    data object EmployerHumanResource : Route("employer_human_resource")
    data object ViewResourceDetail : Route("view_resource_detail")
    data object ScrrapedHumanResource : Route("scrapped_human_resource")
}
