package com.example.skalelit.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val pass: String,
    val role: String = "user", // "admin" or "user"
    val profileImage: String? = null,
    val phone: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)