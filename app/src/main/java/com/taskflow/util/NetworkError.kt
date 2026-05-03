package com.taskflow.util


sealed class NetworkError : Exception() {

    /** 401 — token expirado ou inválido → redirecionar para login */
    object Unauthorized : NetworkError()

    /** 403 — sem permissão para esta ação */
    object Forbidden : NetworkError()

    /** 404 — recurso não encontrado */
    object NotFound : NetworkError()

    /** 4xx genérico */
    data class ClientError(val code: Int, val body: String?) : NetworkError()

    /** 5xx — erro do servidor */
    data class ServerError(val code: Int) : NetworkError()

    /** Sem ligação à internet ou timeout (RF16, RNF05) */
    object NoConnection : NetworkError()

    /** Erro ao fazer parse do JSON */
    object ParseError : NetworkError()

    /** Erro desconhecido */
    data class Unknown(override val cause: Throwable) : NetworkError()
}