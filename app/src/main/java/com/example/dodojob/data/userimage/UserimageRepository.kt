package com.example.dodojob.data.userimage

interface UserimageRepository {
    suspend fun insertUserimage(userimg: UserimageDto)
}