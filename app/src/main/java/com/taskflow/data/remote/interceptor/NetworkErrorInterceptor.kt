package com.taskflow.data.remote.interceptor

import com.taskflow.util.NetworkError
import okhttp3.Interceptor
import okhttp3.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Converte códigos HTTP e exceções de IO em [NetworkError] tipadas,
 * legíveis pela camada de domínio sem dependência de OkHttp.
 */
class NetworkErrorInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            val response = chain.proceed(chain.request())

            when (response.code) {
                in 200..299 -> response   // sucesso
                401 -> response           // tratado pelo TokenRefreshInterceptor
                403 -> throw NetworkError.Forbidden
                404 -> throw NetworkError.NotFound
                in 400..499 -> {
                    val body = response.body?.string()
                    response.close()
                    throw NetworkError.ClientError(response.code, body)
                }
                in 500..599 -> {
                    response.close()
                    throw NetworkError.ServerError(response.code)
                }
                else -> response
            }
        } catch (e: NetworkError) {
            throw e
        } catch (e: UnknownHostException) {
            throw NetworkError.NoConnection
        } catch (e: SocketTimeoutException) {
            throw NetworkError.NoConnection
        } catch (e: Exception) {
            throw NetworkError.Unknown(e)
        }
    }
}