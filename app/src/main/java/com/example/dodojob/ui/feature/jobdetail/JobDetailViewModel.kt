package com.example.dodojob.ui.feature.jobdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dodojob.dao.fetchJobDetailDto
import com.example.dodojob.dao.toggleJobLikeDao
import com.example.dodojob.session.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JobDetailViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<JobDetailUiState?>(null)
    val uiState: StateFlow<JobDetailUiState?> = _uiState

    private val announcementId: Long =
        savedStateHandle.get<Long>("announcementId") ?: 0L

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            try {
                val username = CurrentUser.username
                val dto = fetchJobDetailDto(
                    announcementId = announcementId,
                    username = username
                ) ?: return@launch

                _uiState.value = dto.toUiState()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onToggleLike(liked: Boolean) {
        val current = _uiState.value ?: return
        val seniorUsername = CurrentUser.username ?: return

        // 1) UI Optimistic Update
        _uiState.update { it?.copy(isLiked = liked) }

        // 2) RPC 호출
        viewModelScope.launch {
            try {
                toggleJobLikeDao(
                    seniorUsername = seniorUsername,
                    announcementId = current.announcementId,
                    liked = liked
                )
                // likedCount는 서버에서 재계산
            } catch (e: Exception) {
                e.printStackTrace()
                // 실패하면 UI 롤백
                _uiState.update { it?.copy(isLiked = !liked) }
            }
        }
    }
}
