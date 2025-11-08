package com.example.dodojob.data.greatuser

interface ScrappedGreatUserRepo {
    suspend fun insertSGU(SGU: ScrappedGreatUserDto)
    suspend fun deleteSGU(SGU: ScrappedGreatUserDto)
}