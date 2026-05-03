package com.taskflow.network.error

import com.google.gson.Gson
import com.taskflow.network.model.ApiErrorResponse
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

fun parseErrorMessage(errorBody: String?, gson: Gson = Gson()): String? {
    return try {
        gson.fromJson(errorBody, ApiErrorResponse::class.java)?.message
    } catch (_: Exception) {
        errorBody
    }
}

fun NetworkError.toUserMessage(): String = when (this) {
    NetworkError.Unauthorized       -> "Sessão expirada. Por favor inicia sessão novamente."
    NetworkError.Forbidden          -> "Não tens permissão para realizar esta ação."
    NetworkError.NotFound           -> "O recurso solicitado não foi encontrado."
    NetworkError.NoConnection       -> "Sem ligação à internet. Os dados serão sincronizados quando a ligação for restabelecida."
    NetworkError.ParseError         -> "Erro ao processar a resposta do servidor."
    is NetworkError.ClientError     -> "Erro no pedido (${this.code})."
    is NetworkError.ServerError     -> "Erro no servidor (${this.code}). Tenta novamente mais tarde."
    is NetworkError.Unknown         -> "Ocorreu um erro inesperado. Tenta novamente."
}