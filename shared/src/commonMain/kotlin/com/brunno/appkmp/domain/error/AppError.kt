package com.brunno.appkmp.domain.error

sealed interface AppError

// Erros de Autenticação
enum class AuthError : AppError {
    INVALID_CREDENTIALS,
    USER_NOT_FOUND,
    UNAUTHORIZED,
    INVALID_PASSWORD,
    PASSWORD_TOO_SHORT
}

// Erros de Rede/Globais
enum class NetworkError : AppError {
    NO_INTERNET,
    SERVER_ERROR,
    REQUEST_TIMEOUT,
    TOO_MANY_REQUESTS,
    UNKNOWN
}