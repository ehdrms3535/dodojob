package com.example.dodojob.ui.feature.jobdetail

import com.example.dodojob.data.jobdetail.JobDetailDto

fun JobDetailDto.toUiState(): JobDetailUiState {
    val dutyList = duties
        ?.split('\n', ',', '·', '•', ';', '|')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?: emptyList()

    val chips = listOf(
        InfoChip(
            small = "급여",
            value = payText ?: "협의",
            style = ChipStyle.Primary,
            emoji = "💵"
        ),
        InfoChip(
            small = "시간",
            value = workDurationText ?: timeText ?: "시간협의",
            style = ChipStyle.Neutral,
            emoji = "⏰"
        ),
        InfoChip(
            small = "요일",
            value = (weekText?.replace(Regex("\\s*\\(주\\s*\\d+일\\)"), "") ?: "근무일 협의"),
            style = ChipStyle.Neutral,
            emoji = "📅"
        ),
        InfoChip(
            small = "기타",
            value = when {
                !careerText.isNullOrBlank() -> careerText
                !benefit.isNullOrBlank()    -> benefit
                else                        -> "기타 조건"
            },
            style = ChipStyle.Danger,
            emoji = "👔"
        )
    )

    val recruitment = listOf(
        LabelValue("모집기간", recruitmentPeriod ?: "상시모집"),
        LabelValue("자격요건", careerText ?: "무관"),
        LabelValue("모집인원", "미정"),
        LabelValue("우대조건", benefit ?: "없음"),
        LabelValue("기타조건", "없음")
    )

    val working = listOf(
        LabelValue("급여",     payText ?: "협의"),
        LabelValue("근무기간", "협의"),
        LabelValue("근무일",   weekText ?: "근무일 협의"),
        LabelValue("근무시간", workDurationText ?: timeText ?: "시간협의")
    )

    val mapHint = buildString {
        if (!companyLocate.isNullOrBlank()) append(companyLocate)
        if (!title.isNullOrBlank()) {
            if (isNotEmpty()) append(' ')
            append(title)
        }
    }.ifBlank { "-" }

    return JobDetailUiState(
        title = title ?: "채용공고",
        companyName = companyName ?: "회사명",
        chips = chips,
        recruitment = recruitment,
        workplaceMapHint = mapHint,
        working = working,
        duties = dutyList.ifEmpty { listOf("업무 내용 협의") },
        isLiked = isLiked,
        imageUrl = imageUrl
    )
}
