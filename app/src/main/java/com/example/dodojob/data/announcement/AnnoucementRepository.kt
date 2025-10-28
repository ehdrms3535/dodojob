package com.example.dodojob.data.announcement


interface AnnoucementRepository {
    suspend fun insertAnnouncement(announcement: AnnouncementDto):Long
    suspend fun insertAnnouncementUrl(announcementUrl: AnnoucementUrlDto)
    suspend fun fetchAnnouncements() : List<AnnouncementRow>
}
