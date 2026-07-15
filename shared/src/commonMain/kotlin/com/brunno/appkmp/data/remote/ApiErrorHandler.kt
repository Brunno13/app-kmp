package com.brunno.appkmp.data.remote

import com.brunno.appkmp.data.remote.models.ApiErrorResponse
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AuthError
import com.brunno.appkmp.domain.error.NetworkError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.json.Json

private val errorJsonParser = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

suspend fun parseNetworkError(exception: Exception): AppError {
    return when (exception) {
        is IOException -> NetworkError.NO_INTERNET

        is ClientRequestException -> {
            val status = exception.response.status.value
            val errorBody = try { exception.response.bodyAsText() } catch (e: Exception) { "" }
            val apiError = try {
                if (errorBody.isNotBlank()) {
                    errorJsonParser.decodeFromString<ApiErrorResponse>(errorBody)
                } else null
            } catch (e: Exception) {
                null
            }

            val errorDetails = "${apiError?.message} ${apiError?.error} ${apiError?.code}".uppercase()

            when (status) {
                400 -> {
                    if (errorDetails.contains("INVALID_PASSWORD")) {
                        AuthError.INVALID_PASSWORD
                    } else if (errorDetails.contains("PASSWORD_TOO_SHORT") || errorDetails.contains("PASSWORD TOO SHORT")) {
                        AuthError.PASSWORD_TOO_SHORT
                    } else {
                        NetworkError.UNKNOWN
                    }
                }
                401 -> {
                    if (errorDetails.contains("CREDENTIALS") || errorDetails.contains("PASSWORD")) {
                        AuthError.INVALID_CREDENTIALS
                    } else {
                        AuthError.UNAUTHORIZED
                    }
                }
                403 -> AuthError.UNAUTHORIZED
                404 -> NetworkError.SERVER_ERROR
                429 -> NetworkError.TOO_MANY_REQUESTS
                in 500..599 -> NetworkError.SERVER_ERROR
                else -> NetworkError.UNKNOWN
            }
        }
        else -> NetworkError.UNKNOWN
    }
}