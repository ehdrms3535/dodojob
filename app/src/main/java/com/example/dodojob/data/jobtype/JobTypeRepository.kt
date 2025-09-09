// com/example/dodojob/data/jobtype/JobTypeRepository.kt
package com.example.dodojob.data.jobtype

import com.example.dodojob.data.user.UserDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

interface JobTypeRepository {
    suspend fun insertJobtype(user: JobtypeDto)
}