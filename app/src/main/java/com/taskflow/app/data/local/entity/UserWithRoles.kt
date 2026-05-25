package com.taskflow.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class UserWithRoles(
    @Embedded
    val user: UserEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = UserRoleEntity::class,
            parentColumn = "user_id",
            entityColumn = "role_id"
        )
    )
    val roles: List<RoleEntity>
)

