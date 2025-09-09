package com.example.dodojob.data.jobtype

import io.github.jan.supabase.postgrest.from
import com.example.dodojob.util.Bits

class JobTypeRepositorySupabase(
    private val client: io.github.jan.supabase.SupabaseClient
) : JobTypeRepository {

    override suspend fun insertJobtype(user: JobtypeDto) {

        client.from("jobtype").upsert(
            JobTypeRow(
                id = user.id,
                jobtype = user.jobtype,
                locate = user.locate,
                job_talent = user.job_talent,
                job_manage = user.job_manage,
                job_service = user.job_service,
                job_care = user.job_care,
                term = user.term,
                days = user.days,
                weekend = user.weekend, // 실제로는 해시 or GoTrue 사용
                week = user.week,
                time = user.time
            )
        ){
            onConflict = "id"   // ⚡ PK/Unique 컬럼 지정
        }
    }
}