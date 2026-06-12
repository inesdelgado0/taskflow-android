package com.taskflow.app.util

import com.google.gson.Gson
import com.taskflow.app.data.remote.dto.ApiErrorResponse
import retrofit2.Response
import java.io.IOException



suspend fun <T> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = call()

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                // 204 No Content é sucesso sem corpo — tratado aqui
                @Suppress("UNCHECKED_CAST")
                ApiResult.Success(Unit as T)
            }
        } else {
            val error = when (response.code()) {
                401  -> NetworkError.Unauthorized
                403  -> NetworkError.Forbidden
                404  -> NetworkError.NotFound
                in 400..499 -> {
                    val errorBody = response.errorBody()?.string()
                    NetworkError.ClientError(response.code(), errorBody)
                }
                in 500..599 -> NetworkError.ServerError(response.code())
                else        -> NetworkError.Unknown(Exception("HTTP ${response.code()}"))
            }
            ApiResult.Error(error)
        }

    } catch (e: NetworkError) {
        ApiResult.Error(e)
    } catch (e: IOException) {
        ApiResult.Error(NetworkError.NoConnection)
    } catch (e: Exception) {
        ApiResult.Error(NetworkError.Unknown(e))
    }
}

