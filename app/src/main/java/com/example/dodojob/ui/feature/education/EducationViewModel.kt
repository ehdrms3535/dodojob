package com.example.dodojob.ui.feature.education

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class EducationViewModel : ViewModel() {
    var favorites by mutableStateOf<Set<String>>(emptySet())
        private set

    fun toggleFavorite(title: String) {
        favorites = if (title in favorites) favorites - title else favorites + title
    }
}
