package com.taskflow.app.domain.usecase.auth

import com.taskflow.app.data.repository.AuthRepositoryImpl
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.util.UserRole
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) {
    suspend operator fun invoke(email: String, password: String, role: UserRole): Result<User> =
        authRepository.login(email, password, role)
}
