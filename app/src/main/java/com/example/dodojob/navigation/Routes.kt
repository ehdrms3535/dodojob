package com.example.dodojob.navigation



sealed class Route(val path: String) {
    data object Intro : Route("intro")
    data object Onboarding : Route("onboarding")
    data object Login : Route("login")
    data object PreLogin : Route("prelogin")
    data object EmploySignupIDPW : Route("employsignupsec")
    data object Verify : Route("verify")

    data object PreVerify : Route("preverify")

    data object  EmploySignup : Route("employsignup")

    data object JobDetail : Route("job_detail/{id}") {
        fun of(id: Long) = "job_detail/$id"
    }

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
    data object Announcement6 : Route("announcement6")
    data object Hope : Route("hope")
    data object SignUp : Route("signup")
    data object SignUpComplete : Route("signup_complete")
    data object Main : Route("main")
    data object My : Route("my")
    data object Resume : Route("resume")
    data object Application : Route("application/{announcementId}") {
        fun of(announcementId: Long) = "application/$announcementId"
    }

    data object ApplicationCompleted : Route("application_completed")
    data object Support : Route("support") {
        fun withTab(tab: Int) = "support?tab=$tab"
    }
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
    data object InformationOfApplicants : Route("information_of_applicants/{username}") {
        fun of(username: String) = "information_of_applicants/$username"
    }
    data object PostingRegisterCompleteScreen : Route("postiongregistercomplet")

    data object WelfareHome : Route("welfare/home")
    data object WelfareCategory : Route("welfare/category/{tab}")

    data object Ad1        : Route("ad/1")
    data object Ad2        : Route("ad/2")
    data object Ad3        : Route("ad/3")

    companion object {
        fun welfareCategoryOf(tab: String) = "welfare/category/$tab"
    }

    data object EduHome : Route("edu")      // 교육 홈
    data object EduMy   : Route("edu/my")   // 단일 화면(이어보기/찜한 강의 탭)

    data object EduGraph : Route("edu_graph")
    data object EduPaymentComplete : Route("edu_payment/{courseId}") {
        fun of(courseId: String) = "edu_payment/$courseId"
    }
    data object EduLectureInitial : Route("edu_lecture_ini/{courseId}") {
        fun of(courseId: String) = "edu_lecture_ini/$courseId"
    }
    data object EduLectureNormal  : Route("edu_lecture_nor/{courseId}") {
        fun of(courseId: String) = "edu_lecture_nor/$courseId"
    }

    data object EmployerHumanResource : Route("employer_human_resource")
    data object ViewResourceDetail : Route("view_resource_detail")
    data object ScrrapedHumanResource : Route("scrapped_human_resource")
    data object EditEmployerInformation : Route("edit_employer_information")


}
