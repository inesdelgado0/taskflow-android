package com.taskflow.domain.model

import com.taskflow.domain.util.UserRole

data class User(
    val id: Long = 0,
    val name: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val photoUrl: String? = null,
    val role: UserRole,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)
