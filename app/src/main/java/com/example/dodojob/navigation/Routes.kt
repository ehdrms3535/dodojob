package com.example.dodojob.navigation

sealed class Route(val path: String) {
    data object Onboarding : Route("onboarding")
    data object Login : Route("login")
    data object Verify : Route("verify")
    data object JobType : Route("job_type")
    data object Prefer : Route("prefer")
    data object PreferMap : Route("prefer_map")
    data object Experience : Route("experience")
    data object Announcement : Route("announcement")
}
