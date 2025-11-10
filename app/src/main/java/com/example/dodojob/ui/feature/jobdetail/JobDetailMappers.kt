package com.example.dodojob.ui.feature.jobdetail

import com.example.dodojob.R
import com.example.dodojob.data.jobdetail.JobDetailDto

fun JobDetailDto.toUiState(): JobDetailUiState {
    // 담당업무 문자열을 점 리스트로 변환 (UI 표시용 전처리)
    val dutyList = duties
        ?.split('\n', ',', '·', '•', ';', '|')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?: emptyList()

    // 경력 텍스트 (UI 표시용)
    val careerTextDisplay = careerYears?.let { "${it}년 이상" } ?: "경력무관"

    // 우대/복지 텍스트 (UI 표시용)
    val benefitText = if (benefits.isEmpty()) {
        "없음"
    } else {
        benefits
            .map { it.trim().trim('/') }
            .filter { it.isNotEmpty() }
            .joinToString(" / ")
    }

    // 칩 (아이콘은 drawable 리소스, 백엔드 건드리지 않음)
    val chips = listOf(
        InfoChip(
            small = "급여",
            value = payText ?: "협의",
            style = ChipStyle.Primary,
            iconRes = R.drawable.dollar
        ),
        InfoChip(
            small = "시간",
            value = workDurationText ?: timeText ?: "시간협의",
            style = ChipStyle.Neutral,
            iconRes = R.drawable.time
        ),
        InfoChip(
            small = "요일",
            value = weekText ?: "근무일 협의",
            style = ChipStyle.Neutral,
            iconRes = R.drawable.calendar2
        ),
        InfoChip(
            small = "우대사항",
            value = careerTextDisplay,
            style = ChipStyle.Danger,
            iconRes = R.drawable.suit
        )
    )

    // 5) 섹션: 모집조건  (⚠️ 네가 준 형태 '그대로' 유지)
    val recruitment = listOf(
        LabelValue("모집기간", recruitmentPeriod ?: "상시모집"),
        LabelValue("자격요건", careerTextDisplay),
        LabelValue("모집인원", "미정"),
        LabelValue("우대조건", benefitText),
        LabelValue("기타조건", "없음")
    )

    // 6) 섹션: 근무조건  (⚠️ 네가 준 형태 '그대로' 유지)
    val working = listOf(
        LabelValue("급여", payText ?: "협의"),
        LabelValue("근무기간", "협의"),
        LabelValue("근무일", weekText ?: "근무일 협의"),
        LabelValue("근무시간", workDurationText ?: timeText ?: "시간협의")
    )

    // 7) 지도/주소 힌트  (⚠️ 네가 준 형태 '그대로' 유지)
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
