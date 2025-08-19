package com.example.dodojob.navigation

sealed class Route(val path: String) {
    data object Onboarding : Route("onboarding")
    data object Login : Route("login")
    data object Verify : Route("verify")
    data object JobType : Route("job_type")
    data object Prefer : Route("prefer")
    data object PreferMap : Route("prefer_map")
    data object Experience : Route("experience")

    data object ExperienceComplete : Route("experience_complete")
    data object Announcement : Route("announcement")
    data object Hope : Route("hope")
    data object SignUp : Route("signup")

    data object SignUpComplete : Route("signup_complete")

    data object Main : Route("main")

    data object My : Route("my")
}
