package com.taskflow.app.domain.usecase.auth

import com.taskflow.app.data.repository.AuthRepositoryImpl
import com.taskflow.app.domain.model.User
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) {
    suspend operator fun invoke(
        name: String,
        username: String,
        email: String,
        password: String
    ): Result<User> =
        authRepository.register(name, username, email, password)
}
