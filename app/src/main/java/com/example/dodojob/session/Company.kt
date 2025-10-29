package com.example.dodojob.data.session

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
/**
 * username → users_tmp.email → employ_tmp.company_id 를 조회
 */
class CompanyFetcher(
    private val client: SupabaseClient
) {

    /**
     * 주어진 username으로 연결된 company_id를 가져옵니다.
     * @param username users_tmp.username
     */
    suspend fun fetchCompanyId(username: String): CompanyIdModel? {
        // 1️⃣ username으로 email 가져오기
        val userEmail = client.from("users_tmp")
            .select { filter { eq("username", username) } }
            .decodeList<UserEmailModel>()
            .firstOrNull() ?: return null

        // 2️⃣ email로 company_id 가져오기
        val employ = client.from("employ_tmp")
            .select { filter { eq("id", userEmail.email) } }
            .decodeList<CompanyIdModel>()
            .firstOrNull()

        return employ
    }
}
