package com.example.dodojob.ui.feature.jobdetail

import com.example.dodojob.data.jobdetail.JobDetailDto

fun JobDetailDto.toUiState(): JobDetailUiState {
    // duties 문자열을 점 리스트로 변환
    val dutyList = duties
        ?.split('\n', ',', '·', '•', ';', '|')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?: emptyList()

    val careerTextDisplay = careerYears?.let{"${it}년 이상"} ?: "경력무관"

    // 칩 (급여 / 시간 / 요일 / 기타)
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
            value = weekText ?: "근무일 협의",
            style = ChipStyle.Neutral,
            emoji = "📅"
        ),
        InfoChip(
            small = "기타",
            value = careerTextDisplay,
            style = ChipStyle.Danger,
            emoji = "👔"
        )
    )

    val benefitText = if (benefits.isEmpty()) {
        "없음"
    } else {
        benefits
            .map { it.trim().trim('/') }
            .joinToString(" / ")
    }


    // 모집조건 섹션
    val recruitment = listOf(
        LabelValue("모집기간", recruitmentPeriod ?: "상시모집"),
        LabelValue("자격요건", careerTextDisplay),
        LabelValue("모집인원", "미정"),
        LabelValue("우대조건", benefitText),
        LabelValue("기타조건", "없음")
    )

    // 근무조건 섹션
    val working = listOf(
        LabelValue("급여", payText ?: "협의"),
        LabelValue("근무기간", "협의"),
        LabelValue("근무일", weekText ?: "근무일 협의"),
        LabelValue("근무시간", workDurationText ?: timeText ?: "시간협의")
    )

    val mapHint = listOfNotNull(companyLocate, title)
        .filter { !it.isNullOrBlank() }
        .joinToString(" ")

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
