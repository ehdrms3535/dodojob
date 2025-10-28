package com.example.dodojob.data.announcement.needlicense3

interface NeedlicenseRepo{
    suspend fun insertNeedlisence(announcement: NeedlicenseDto)
}
