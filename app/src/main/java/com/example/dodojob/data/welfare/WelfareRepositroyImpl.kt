package com.example.dodojob.data.welfare

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class WelfareRepositoryImpl(
    private val client: SupabaseClient
) : WelfareRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun list(
        category: WelfareCategory?,
        query: String?,
        limit: Int
    ): List<Welfare> {
        val res = client.from("welfare").select(
            columns = Columns.list(
                "id",
                "welfare_name",
                "welfare_locate",
                "type",
                "explain",
                "offer"
            )
        ) {
            filter {
                if (category != null) {
                    eq("type", category.toDbBoolean())
                }
                if (!query.isNullOrBlank()) {
                    or {
                        ilike("welfare_name", "%$query%")
                        ilike("welfare_locate", "%$query%")
                        ilike("explain", "%$query%")
                        ilike("offer", "%$query%")
                    }
                }
            }
            order(column = "id", order = Order.DESCENDING)
            limit(limit.toLong())
        }

        val rows = json.decodeFromString(
            ListSerializer(WelfareRow.serializer()),
            res.data
        )
        return rows.map { it.toDomain() }
    }

    override suspend fun myApplications(username: String): List<WelfareApplication> {
        val res = client.from("welfare_senior").select(
            columns = Columns.raw(
                "id,username,created_at,welfare_status,welfare_id," +
                        "welfare(id,welfare_name,welfare_locate,type,explain,offer)"
            )
        ) {
            filter { eq("username", username) }
            order(column = "created_at", order = Order.DESCENDING)
        }

        val rows = json.decodeFromString(
            ListSerializer(WelfareSeniorRow.serializer()),
            res.data
        )

        return rows.map { a ->
            WelfareApplication(
                id        = a.id,
                welfareId = a.welfareId,
                username  = a.username,
                status    = a.status ?: WelfareStatus.REVIEW,
                createdAt = a.createdAt
            )
        }
    }


    override suspend fun myApplicationsJoined(username: String): List<WelfareSeniorRow> {
        val res = client.from("welfare_senior").select(
            columns = Columns.raw(
                "id,username,created_at,welfare_status,welfare_id," +
                        "welfare(id,welfare_name,welfare_locate,type,explain,offer)"
            )
        ) {
            filter { eq("username", username) }
            order(column = "created_at", order = Order.DESCENDING)
        }

        return json.decodeFromString(
            ListSerializer(WelfareSeniorRow.serializer()),
            res.data
        )
    }


}
