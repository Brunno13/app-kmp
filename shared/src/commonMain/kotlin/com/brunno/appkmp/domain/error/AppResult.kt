package com.brunno.appkmp.domain.error

// 'T' é o dado de sucesso (ex: Unit, User, List)
// 'E' é o nosso erro tipado que herda de AppError
sealed interface AppResult<out T, out E : AppError> {
    data class Success<out T>(val data: T) : AppResult<T, Nothing>
    data class Error<out E : AppError>(val error: E) : AppResult<Nothing, E>
}