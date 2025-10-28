package com.example.dodojob.data.announcement.price4

interface PriceRepo{
    suspend fun insertPrice(announcement:PriceDto)
}