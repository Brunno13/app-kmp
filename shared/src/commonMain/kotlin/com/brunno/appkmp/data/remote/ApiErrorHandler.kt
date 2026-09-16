package com.brunno.appkmp.data.remote

import com.brunno.appkmp.data.remote.models.ApiErrorResponse
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AuthError
import com.brunno.appkmp.domain.error.NetworkError
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlin.coroutines.cancellation.CancellationException

private val errorJsonParser = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

suspend fun parseNetworkError(exception: Exception): AppError {
    return when (exception) {
        is IOException -> NetworkError.NO_INTERNET
        is ResponseException -> parseResponseError(exception)
        else -> NetworkError.UNKNOWN
    }
}

private suspend fun parseResponseError(
    exception: ResponseException
): AppError {
    val errorDetails = readErrorDetails(exception)

    return when (exception.response.status) {
        HttpStatusCode.BadRequest -> mapBadRequest(errorDetails)
        HttpStatusCode.Unauthorized -> mapUnauthorized(errorDetails)
        HttpStatusCode.Forbidden -> AuthError.UNAUTHORIZED
        HttpStatusCode.NotFound -> NetworkError.SERVER_ERROR
        HttpStatusCode.TooManyRequests -> NetworkError.TOO_MANY_REQUESTS
        else -> mapUnhandledResponse(exception)
    }
}

private suspend fun readErrorDetails(
    exception: ResponseException
): String {
    val errorBody = try {
        exception.response.bodyAsText()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        ""
    }

    val apiError = try {
        if (errorBody.isNotBlank()) {
            errorJsonParser.decodeFromString<ApiErrorResponse>(errorBody)
        } else {
            null
        }
    } catch (_: IllegalArgumentException) {
        null
    }

    return "${apiError?.message} ${apiError?.error} ${apiError?.code}".uppercase()
}

private fun mapBadRequest(
    errorDetails: String
): AppError {
    return when {
        errorDetails.contains("INVALID_PASSWORD") ->
            AuthError.INVALID_PASSWORD

        errorDetails.contains("PASSWORD_TOO_SHORT") ||
                errorDetails.contains("PASSWORD TOO SHORT") ->
            AuthError.PASSWORD_TOO_SHORT

        else -> NetworkError.UNKNOWN
    }
}

private fun mapUnauthorized(
    errorDetails: String
): AppError {
    return if (
        errorDetails.contains("CREDENTIALS") ||
        errorDetails.contains("PASSWORD")
    ) {
        AuthError.INVALID_CREDENTIALS
    } else {
        AuthError.UNAUTHORIZED
    }
}

private fun mapUnhandledResponse(
    exception: ResponseException
): AppError {
    return if (exception is ServerResponseException) {
        NetworkError.SERVER_ERROR
    } else {
        NetworkError.UNKNOWN
    }
}
