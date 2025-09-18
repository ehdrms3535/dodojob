package com.example.dodojob.data.employ

interface EmployRepository {
    suspend fun insertEmploy(employ: EmployDto)
}