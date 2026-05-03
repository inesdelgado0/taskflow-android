package com.taskflow.network.interceptor

import com.taskflow.network.auth.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject


class TokenRefreshInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    private val lock = Object()

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.code != 401) return response

        response.close()

        synchronized(lock) {
            // Verificar se outro thread já limpou a sessão
            val currentToken = runBlocking { tokenManager.getAccessToken() }
            val requestToken = chain.request().header("Authorization")
                ?.removePrefix("Bearer ")

            // Se já foi limpo por outro thread, repetir sem token
            if (currentToken == null || currentToken != requestToken) {
                return chain.proceed(
                    chain.request().newBuilder()
                        .removeHeader("Authorization")
                        .build()
                )
            }

            // Limpar sessão — a UI observa accessTokenFlow e navega para login
            runBlocking { tokenManager.clearTokens() }

            // Repetir o pedido sem token (vai resultar em 401 que a UI trata)
            return chain.proceed(
                chain.request().newBuilder()
                    .removeHeader("Authorization")
                    .build()
            )
        }
    }
}