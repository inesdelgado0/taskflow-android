package com.taskflow.util

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val error: NetworkError) : ApiResult<Nothing>()
}

/** Transforma o valor em caso de sucesso */
inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.Error -> this
}

/** Obtém o valor ou null */
fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data

/** Executa bloco em caso de erro */
inline fun <T> ApiResult<T>.onError(block: (NetworkError) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) block(error)
    return this
}

/** Executa bloco em caso de sucesso */
inline fun <T> ApiResult<T>.onSuccess(block: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) block(data)
    return this
}