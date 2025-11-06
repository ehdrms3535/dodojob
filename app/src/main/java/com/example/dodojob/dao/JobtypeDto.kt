@file:Suppress("unused")

package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import com.example.dodojob.ui.feature.education.Course
import com.example.dodojob.session.JobBits
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import kotlin.math.min

/* ==============================
 * DB Row & Fetch
 * ============================== */
@Serializable
data class JobtypeRow(
    val id: String,
    val job_talent: String? = null,
    val job_manage: String? = null,
    val job_service: String? = null,
    val job_care: String? = null,
    val term: String? = null,
    val days: Boolean? = null,
    val weekend: Boolean? = null,
    val week: String? = null,
    val time: Boolean? = null,
    val locate: String? = null
)

suspend fun fetchJobtypeByUsername(
    username: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    anonKey: String = BuildConfig.SUPABASE_ANON_KEY
): JobtypeRow? {
    val url = "$supabaseUrl/rest/v1/jobtype"
    val rows: List<JobtypeRow> = http.get(url) {
        url {
            parameters.append("id", "eq.$username")
            parameters.append("select", "*")
            parameters.append("limit", "1")
        }
        header("apikey", anonKey)
        header("Authorization", "Bearer $anonKey")
    }.body<List<JobtypeRow>>()
    return rows.firstOrNull()
}

/* ==============================
 * 추천 벡터 & 스코어링
 * ============================== */
private val LABEL_TO_TAG: Map<String, String> = mapOf(
    // TALENT
    "영어 회화" to "영어",
    "요리 강사" to "요리",
    "공예 강의" to "기타",
    "독서 지도" to "교육",
    "상담·멘토링" to "교육",
    "악기 지도" to "기타",
    "역사 강의" to "교육",
    "예술 지도" to "기타",
    "관광 가이드" to "기타",
    "홍보 컨설팅" to "기타",
    // SERVICE
    "고객 응대" to "응대",
    "카운터/계산" to "응대",
    "상품 진열" to "기타",
    "청결 관리" to "기타",
    "안내 데스크" to "응대",
    "주차 관리" to "기타",
    // MANAGE
    "환경미화" to "기타",
    "인력 관리" to "관리",
    "사서 보조" to "교육",
    "사무 보조" to "컴퓨터",
    "경비/보안" to "기타",
    // CARE
    "등하원 도우미" to "교육",
    "가정 방문" to "기타",
    "보조 교사" to "교육",
)

/* 관심 벡터 */
data class InterestVectors(
    val labels: Set<String>,
    val tags: Set<String>,
    val keywords: Set<String>
)

/* 비트 문자열 → 관심 벡터 */
fun buildInterestVectors(
    talentBits: String?,
    serviceBits: String?,
    manageBits: String?,
    careBits: String?
): InterestVectors {
    val allLabels = buildSet {
        addAll(JobBits.parse(JobBits.JobCategory.TALENT, talentBits))
        addAll(JobBits.parse(JobBits.JobCategory.SERVICE, serviceBits))
        addAll(JobBits.parse(JobBits.JobCategory.MANAGE,  manageBits))
        addAll(JobBits.parse(JobBits.JobCategory.CARE,    careBits))
    }
    val tags = allLabels.mapNotNull { LABEL_TO_TAG[it] }.toSet()

    val kw = buildSet {
        addAll(allLabels)
        allLabels.forEach { label ->
            add(label.replace(" ", ""))
            label.split('·', ' ', '/')
                .filter { it.length >= 2 }
                .forEach { add(it) }
        }
    }
    return InterestVectors(allLabels, tags, kw)
}

/** 개별 강의 스코어 */
private fun scoreCourse(c: Course, v: InterestVectors): Int {
    var s = 0
    // 태그 일치(강)
    if (c.tag in v.tags) s += 8
    // 제목/설명 키워드 매치(중)
    var hits = 0
    v.keywords.forEach { k ->
        if (k.isNotBlank() &&
            (c.title.contains(k, true) || c.sub.contains(k, true))
        ) hits++
    }
    s += min(3, hits) * 3 // 최대 9점
    // 태그 불명/기타 보정(약)
    if (c.tag.isBlank() || c.tag == "전체" || c.tag == "기타") s += 1
    return s
}

/* 추천: 점수 내림차순 상위 N개 */
fun recommendCoursesForUser(
    courses: List<Course>,
    vectors: InterestVectors,
    topN: Int = 3
): List<Course> =
    courses.sortedByDescending { scoreCourse(it, vectors) }
        .take(topN)
