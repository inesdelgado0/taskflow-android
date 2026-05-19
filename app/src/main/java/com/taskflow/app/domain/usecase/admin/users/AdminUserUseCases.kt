package com.taskflow.app.domain.usecase.admin.users

import com.taskflow.app.data.repository.UserRepositoryImpl
import com.taskflow.app.domain.model.User
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetUsersUseCase @Inject constructor(
    private val userRepository: UserRepositoryImpl
) {
    operator fun invoke(): Flow<List<User>> = userRepository.getAllUsersFlow()
}

class CreateUserUseCase @Inject constructor(
    private val userRepository: UserRepositoryImpl
) {
    suspend operator fun invoke(user: User): Result<Long> =
        runCatching { userRepository.createUser(user) }
}

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepositoryImpl
) {
    suspend operator fun invoke(user: User): Result<Unit> =
        runCatching { userRepository.updateUser(user) }
}

class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepositoryImpl
) {
    suspend operator fun invoke(userId: Long): Result<Unit> =
        runCatching { userRepository.deleteUser(userId) }
}
