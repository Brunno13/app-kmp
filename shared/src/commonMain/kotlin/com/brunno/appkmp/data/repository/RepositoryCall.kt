package com.brunno.appkmp.data.repository

import com.brunno.appkmp.data.remote.parseNetworkError
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import kotlin.coroutines.cancellation.CancellationException

internal suspend fun <T> executeRepositoryCall(
    exceptionMapper: (Exception) -> AppError? = { null },
    block: suspend () -> AppResult<T, AppError>
): AppResult<T, AppError> {
    return try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (expectedFailure: Exception) {
        val mappedError = exceptionMapper(expectedFailure)

        if (mappedError != null) {
            AppResult.Error(mappedError)
        } else {
            AppResult.Error(parseNetworkError(expectedFailure))
        }
    }
}
