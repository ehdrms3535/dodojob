package com.example.dodojob.data.announcement.salary3

interface SalaryRepository {
    suspend fun insertSalary(announcement : SalaryDto)
}