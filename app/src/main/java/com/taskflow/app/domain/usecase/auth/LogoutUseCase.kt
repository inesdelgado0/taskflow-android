package com.taskflow.app.domain.usecase.auth

import com.taskflow.app.data.repository.AuthRepositoryImpl
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
