package com.example.dodojob.data.announcement

import ApplicantRow
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

@Serializable
data class AnnouncementRow(
    val id: Long? = null,              // 자동 증가 PK
    val company_name: String,
    val public: Boolean,
    val company_id: String?=null,
    val company_locate: String,
    val detail_locate: String,
    val created_at: String? = null
)

@Serializable
data class AnnouncementUrlRow(
    val id: Long? = null,
    val company_imgurl: String? = null,
    val company_imgurl2: String? = null,
    val company_imgurl3: String? = null,
    val company_imgurl4: String? = null
)


class AnnouncementRepositorySupabase(
    private val client: SupabaseClient
) : AnnoucementRepository {

    /** announcement insert 후 자동증가 id 반환 */
    override suspend fun insertAnnouncement(announcement: AnnouncementDto): Long {
        val row = AnnouncementRow(
            company_name   = announcement.company_name,
            public         = announcement.public,
            company_id     = announcement.company_id,      // 🔧 기존 코드의 매핑 실수 수정
            company_locate = announcement.company_locate,
            detail_locate  = announcement.detail_locate
        )

        // ✅ Returning 대신 select()로 inserted row 반환 받기
        val inserted = client
            .from("announcement")
            .insert(row) { select() }                       // ← returning 역할
            .decodeSingle<AnnouncementRow>()                // ← 한 행을 디코드

        return inserted.id ?: error("Inserted id is null. Check PK definition.")
    }

    /** announcement_url 테이블에 FK 포함 insert (id 반환) */
    override suspend fun insertAnnouncementUrl(announcementUrl: AnnoucementUrlDto) {
        val urlRow = AnnouncementUrlRow(
            id = announcementUrl.id,   // 🔧 dto 필드명에 맞춤
            company_imgurl = announcementUrl.url,
            company_imgurl2 = announcementUrl.url,
            company_imgurl3 = announcementUrl.url,
            company_imgurl4 = announcementUrl.url
        )
        // 필요하면 여기서도 select()로 생성된 행 반환
        client
            .from("company_images")
            .insert(urlRow) { select() }
            .decodeSingle<AnnouncementUrlRow>()
    }

    override suspend fun fetchAnnouncements(): List<AnnouncementRow> {
        return client.from("announcement")
            .select()
            .decodeList<AnnouncementRow>()
    }

    suspend fun getannounceRows(companyId: String? = null): List<ApplicantRow> {
        val params = buildMap {
            if (companyId != null) put("p_company_id", companyId)
        }
        return client.postgrest
            .rpc("getannounce_rows", params)
            .decodeList<ApplicantRow>()
    }
}

