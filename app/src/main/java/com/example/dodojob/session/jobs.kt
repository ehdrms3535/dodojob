@file:Suppress("MemberVisibilityCanBePrivate", "unused")
package com.example.dodojob.session

object JobBits {

    /** 카테고리 정의 */
    enum class JobCategory { TALENT, SERVICE, MANAGE, CARE }

    /** 카테고리별 항목 라벨(순서 중요) */
    val ITEMS: Map<JobCategory, List<String>> = mapOf(
        JobCategory.TALENT to listOf(
            "영어 회화","요리 강사","공예 강의","독서 지도","상담·멘토링",
            "악기 지도","역사 강의","예술 지도","관광 가이드","홍보 컨설팅"
        ),
        JobCategory.SERVICE to listOf(
            "고객 응대","카운터/계산","상품 진열","청결 관리","안내 데스크","주차 관리"
        ),
        JobCategory.MANAGE to listOf(
            "환경미화","인력 관리","사서 보조","사무 보조","경비/보안"
        ),
        JobCategory.CARE to listOf(
            "등하원 도우미","가정 방문","보조 교사"
        )
    )

    // -----------------------------
    // 기본 변환기
    // -----------------------------

    /** bitString -> 선택된 라벨 목록 */
    fun parse(category: JobCategory, bitString: String?): List<String> {
        val items = ITEMS.getValue(category)
        if (bitString.isNullOrBlank()) return emptyList()
        return bitString.mapIndexedNotNull { idx, c ->
            if (c == '1' && idx < items.size) items[idx] else null
        }
    }

    /** bitString -> 선택된 인덱스 목록 (0-based) */
    fun selectedIndexes(bitString: String?): List<Int> {
        if (bitString.isNullOrBlank()) return emptyList()
        return buildList {
            bitString.forEachIndexed { i, c -> if (c == '1') add(i) }
        }
    }

    /** 선택된 라벨 목록 -> bitString (항목명 일치 필요, 미존재 라벨은 무시) */
    fun encode(category: JobCategory, selectedLabels: List<String>): String {
        val items = ITEMS.getValue(category)
        val set = selectedLabels.toSet()
        return buildString(items.size) {
            items.forEach { append(if (it in set) '1' else '0') }
        }
    }

    /** 선택된 인덱스 목록 -> bitString (범위 밖 인덱스는 무시) */
    fun encodeByIndexes(category: JobCategory, selectedIndexes: Collection<Int>): String {
        val items = ITEMS.getValue(category)
        val set = selectedIndexes.toSet()
        return buildString(items.size) {
            items.indices.forEach { idx -> append(if (idx in set) '1' else '0') }
        }
    }

    /** bitString 길이를 카테고리 항목 수에 맞게 보정(부족분 0패딩, 초과분 절단) */
    fun normalizeLength(category: JobCategory, bitString: String?): String {
        val size = ITEMS.getValue(category).size
        val src = (bitString ?: "").filter { it == '0' || it == '1' }
        return when {
            src.length == size -> src
            src.length <  size -> src + "0".repeat(size - src.length)
            else               -> src.take(size)
        }
    }

    /** bitString 에서 특정 인덱스 토글 (0→1, 1→0) */
    fun toggleAt(category: JobCategory, bitString: String?, index: Int): String {
        val items = ITEMS.getValue(category)
        if (index !in items.indices) return normalizeLength(category, bitString)
        val base = normalizeLength(category, bitString)
        val ch = if (base[index] == '1') '0' else '1'
        val arr = base.toCharArray()
        arr[index] = ch
        return String(arr)
    }

    // -----------------------------
    // 편의 함수
    // -----------------------------

    /** 라벨을 "·" 나 ", " 등 구분자로 합치기 */
    fun joinLabels(labels: List<String>, separator: String = ", "): String =
        if (labels.isEmpty()) "없음" else labels.joinToString(separator)

    /** 현재 bitString 을 사람이 읽기 좋은 문장으로 */
    fun pretty(category: JobCategory, bitString: String?, emptyText: String = "없음"): String =
        joinLabels(parse(category, bitString)).ifBlank { emptyText }

    /** 카테고리별 빈 bitString 생성 (모두 0) */
    fun empty(category: JobCategory): String =
        "0".repeat(ITEMS.getValue(category).size)
}
