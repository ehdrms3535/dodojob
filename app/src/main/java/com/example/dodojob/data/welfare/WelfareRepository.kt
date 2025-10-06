package com.example.dodojob.data.welfare

interface WelfareRepository {

    /** 카테고리/검색어로 복지 목록 조회 */
    suspend fun list(
        category: WelfareCategory? = null,
        query: String? = null,
        limit: Int = 50
    ): List<Welfare>

    /** 내 신청 목록 조회 */
    suspend fun myApplications(username: String): List<WelfareApplication>

    suspend fun myApplicationsJoined(username: String): List<WelfareSeniorRow>
}
