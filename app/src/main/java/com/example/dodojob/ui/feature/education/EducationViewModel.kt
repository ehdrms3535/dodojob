package com.example.dodojob.ui.feature.education

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dodojob.dao.fetchAssignedCourses
import com.example.dodojob.dao.upsertLectureAssignUser
import kotlinx.coroutines.launch

class EducationViewModel : ViewModel() {

    // courseId(문자열) 기준으로 관리 (실제로는 lecture.id.toString() 이라고 가정)
    var favorites by mutableStateOf<Set<String>>(emptySet())
        private set

    var purchased by mutableStateOf<Set<String>>(emptySet())
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
            }.onFailure {
                // TODO: 로그 처리 등
            }
        }
    }

    fun isFavorite(courseId: String) = courseId in favorites
    fun isPurchased(courseId: String) = courseId in purchased

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
            }
        }
    }
}
