package com.brunno.appkmp.data.remote

import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AuthError
import com.brunno.appkmp.domain.error.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ApiErrorHandlerTest {

    @Test
    fun invalidPasswordFrom400MapsToAuthInvalidPassword() = runTest {
        val error = parseHttpError(
            status = HttpStatusCode.BadRequest,
            body = """{"code":"INVALID_PASSWORD"}"""
        )

        assertEquals(AuthError.INVALID_PASSWORD, error)
    }

    @Test
    fun passwordTooShortFrom400MapsToAuthPasswordTooShort() = runTest {
        val error = parseHttpError(
            status = HttpStatusCode.BadRequest,
            body = """{"message":"Password too short"}"""
        )

        assertEquals(AuthError.PASSWORD_TOO_SHORT, error)
    }

    @Test
    fun unknown400MapsToNetworkUnknown() = runTest {
        val error = parseHttpError(
            status = HttpStatusCode.BadRequest,
            body = "not-json"
        )

        assertEquals(NetworkError.UNKNOWN, error)
    }

    @Test
    fun credentialsFrom401MapsToInvalidCredentials() = runTest {
        val error = parseHttpError(
            status = HttpStatusCode.Unauthorized,
            body = """{"message":"Invalid credentials"}"""
        )

        assertEquals(AuthError.INVALID_CREDENTIALS, error)
    }

    @Test
    fun generic401MapsToUnauthorized() = runTest {
        val error = parseHttpError(
            status = HttpStatusCode.Unauthorized,
            body = """{"message":"Session expired"}"""
        )

        assertEquals(AuthError.UNAUTHORIZED, error)
    }

    @Test
    fun forbiddenMapsToUnauthorized() = runTest {
        val error = parseHttpError(
            status = HttpStatusCode.Forbidden,
            body = "{}"
        )

        assertEquals(AuthError.UNAUTHORIZED, error)
    }

    @Test
    fun notFoundMapsToServerError() = runTest {
        val error = parseHttpError(
            status = HttpStatusCode.NotFound,
            body = "{}"
        )

        assertEquals(NetworkError.SERVER_ERROR, error)
    }

    @Test
    fun tooManyRequestsMapsToRateLimitError() = runTest {
        val error = parseHttpError(
            status = HttpStatusCode.TooManyRequests,
            body = "{}"
        )

        assertEquals(NetworkError.TOO_MANY_REQUESTS, error)
    }

    @Test
    fun serverErrorMapsToNetworkServerError() = runTest {
        val error = parseHttpError(
            status = HttpStatusCode.ServiceUnavailable,
            body = "{}"
        )

        assertEquals(NetworkError.SERVER_ERROR, error)
    }

    @Test
    fun unhandledHttpStatusMapsToNetworkUnknown() = runTest {
        val error = parseHttpError(
            status = HttpStatusCode(
                value = 418,
                description = "I'm a teapot"
            ),
            body = "{}"
        )

        assertEquals(NetworkError.UNKNOWN, error)
    }

    @Test
    fun ioExceptionMapsToNoInternet() = runTest {
        val error = parseNetworkError(
            IOException("No network")
        )

        assertEquals(NetworkError.NO_INTERNET, error)
    }

    @Test
    fun genericExceptionMapsToNetworkUnknown() = runTest {
        val error = parseNetworkError(
            IllegalStateException("Unexpected failure")
        )

        assertEquals(NetworkError.UNKNOWN, error)
    }

    private suspend fun parseHttpError(
        status: HttpStatusCode,
        body: String
    ): AppError {
        val engine = MockEngine {
            respond(
                content = body,
                status = status,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
            )
        }

        val client = HttpClient(engine) {
            expectSuccess = true
        }

        return try {
            client.get("https://test.local/api")

            fail(
                "Expected HTTP ${status.value} to throw an exception"
            )
        } catch (exception: Exception) {
            parseNetworkError(exception)
        } finally {
            client.close()
        }
    }
}