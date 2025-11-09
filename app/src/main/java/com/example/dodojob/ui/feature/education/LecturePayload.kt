package com.example.dodojob.ui.feature.education

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LecturePayload(
    val title: String? = null,
    val subtitle: String? = null,
    val thumbnail: String? = null,
    val videoUrl: String? = null
) : Parcelable
