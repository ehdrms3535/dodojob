package com.example.dodojob.ui.feature.education

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dodojob.dao.LectureWeeklyRow
import com.example.dodojob.dao.fetchAssignedCourses
import com.example.dodojob.dao.fetchLectureWeekly
import com.example.dodojob.dao.upsertLectureAssignUser
import kotlinx.coroutines.launch

class EducationViewModel : ViewModel() {

    // courseId(문자열) 기준으로 관리 (실제로는 lecture.id.toString() 이라고 가정)
    var favorites by mutableStateOf<Set<String>>(emptySet())
        private set

    var purchased by mutableStateOf<Set<String>>(emptySet())
        private set

    // 이어보기: courseId -> 마지막 재생 위치(ms)
    var lastPositions by mutableStateOf<Map<String, Long>>(emptyMap())
        private set

    var weeklyRows by mutableStateOf<List<LectureWeeklyRow>>(emptyList())
        private set

    /** Supabase lecture_assign_user 상태 로딩 */
    fun loadAssigned(username: String?) {
        viewModelScope.launch {
            runCatching {
                fetchAssignedCourses(username)
            }.onSuccess { rows ->
                favorites = rows.mapNotNull { row ->
                    if (row.favorite == true) row.lecture?.id?.toString() else null
                }.toSet()

                purchased = rows.mapNotNull { row ->
                    if (row.buy == true) row.lecture?.id?.toString() else null
                }.toSet()

                lastPositions = rows.mapNotNull { row ->
                    val courseId = row.lecture?.id?.toString()
                    val pos = row.lastPositionMs
                    if (courseId != null && pos != null) {
                        courseId to pos
                    } else null
                }.toMap()
            }.onFailure {
                // TODO: 로그 처리 등
            }
        }
    }


    /** 특정 강의의 주차 정보 로딩 (lecture_weekly) */
    fun loadWeekly(courseId: String) {
        val lectureId = courseId.toLongOrNull() ?: return

        viewModelScope.launch {
            runCatching {
                fetchLectureWeekly(lectureId)
            }.onSuccess { rows ->
                // number 기준으로 정렬해서 상태에 저장
                weeklyRows = rows.sortedBy { it.number ?: Long.MAX_VALUE }
            }.onFailure {
                // 실패 시 일단 비워두기 (원하면 더미 넣어도 됨)
                weeklyRows = emptyList()
            }
        }
    }

    fun isFavorite(courseId: String) = courseId in favorites
    fun isPurchased(courseId: String) = courseId in purchased

    /** 해당 강의 마지막 재생 위치 가져오기 (없으면 0) */
    fun getLastPosition(courseId: String): Long {
        return lastPositions[courseId] ?: 0L
    }

    /** 즐겨찾기 토글 + Supabase upsert */
    fun toggleFavorite(courseId: String, username: String) {
        val lectureId = courseId.toLongOrNull() ?: return

        val nowFav = courseId !in favorites
        favorites = if (nowFav) favorites + courseId else favorites - courseId

        viewModelScope.launch {
            runCatching {
                upsertLectureAssignUser(
                    username = username,
                    lectureId = lectureId,
                    favorite = nowFav
                )
            }.onFailure {
            }
        }
    }

    /** 수강 신청(buy = true) + Supabase upsert */
    fun buyLecture(courseId: String, username: String?) {
        val lectureId = courseId.toLongOrNull() ?: return

        purchased = purchased + courseId

        viewModelScope.launch {
            runCatching {
                upsertLectureAssignUser(
                    username = username,
                    lectureId = lectureId,
                    buy = true
                )
            }.onFailure {
                // TODO: 실패 시 롤백/로그 등 필요하면 추가
            }
        }
    }

    /** 재생 위치 업데이트 + Supabase upsert (이어보기) */
    fun updateLastPosition(courseId: String, username: String?, positionMs: Long) {
        val lectureId = courseId.toLongOrNull() ?: return

        // 로컬 상태 업데이트
        lastPositions = lastPositions + (courseId to positionMs)

        viewModelScope.launch {
            runCatching {
                upsertLectureAssignUser(
                    username = username,
                    lectureId = lectureId,
                    lastPositionMs = positionMs
                )
            }.onFailure {
                // TODO: 실패 시 로그만
            }
        }
    }
}
