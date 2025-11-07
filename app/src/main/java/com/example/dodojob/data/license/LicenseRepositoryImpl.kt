// data/license/LicenseRepositorySupabase.kt
package com.example.dodojob.data.license

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class LicenseRepositoryImpl(
    private val client: SupabaseClient
) {
    suspend fun list(username: String): List<LicenseModels> =
        client.from("license_senior")
            .select { filter { eq("username", username) } }
            .decodeList<LicenseModels>()

    suspend fun add(username: String, name: String, location: String, number: String) {
        val row = LicenseModels(
            username = username,
            name = name.ifBlank { null },
            location = location.ifBlank { null },
            number = number.ifBlank { null }
        )
        client.from("license_senior").insert(row)
    }
}
