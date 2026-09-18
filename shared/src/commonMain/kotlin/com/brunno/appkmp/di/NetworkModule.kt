package com.brunno.appkmp.di

import com.brunno.appkmp.data.local.AuthCredentialStore
import com.brunno.appkmp.data.local.SessionDao
import com.brunno.appkmp.data.local.UserDao
import com.brunno.appkmp.data.remote.AuthApi
import com.brunno.appkmp.data.remote.createAuthApi
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import io.ktor.http.HttpStatusCode

val networkModule = module {

    single {
        val credentialStore = get<AuthCredentialStore>()
        val baseUrl = get<String>(named("baseUrl"))
        val userDao = get<UserDao>()
        val sessionDao = get<SessionDao>()
        val originUrl = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl

        val client = HttpClient {
            expectSuccess = true

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                })
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
                header("Origin", originUrl)
            }

            HttpResponseValidator {
                validateResponse { response ->
                    val setCookies = response.headers.getAll("Set-Cookie")
                    if (!setCookies.isNullOrEmpty()) {
                        val parsedCookies = setCookies.joinToString("; ") { it.substringBefore(";") }
                        credentialStore.setApiCookies(parsedCookies)
                    }
                }

                handleResponseExceptionWithRequest { exception, request ->
                    if (exception is ClientRequestException) {
                        val status = exception.response.status
                        println("❌ KTOR ERRO: ${request.url}")
                        println("❌ STATUS: $status")
                        if (status == HttpStatusCode.Unauthorized || status == HttpStatusCode.Forbidden) {
                            println("🔒 Sessão expirada/inválida detetada no Ktor. Forçando logout local...")
                            credentialStore.clear()
                            userDao.clearSession()
                            sessionDao.clearAll()
                        }
                    }
                }
            }
        }

        client.requestPipeline.intercept(io.ktor.client.request.HttpRequestPipeline.State) {
            val cookies = credentialStore.getApiCookies()
            if (!cookies.isNullOrBlank()) {
                context.headers.append("Cookie", cookies)
            }

            val token = credentialStore.getAuthToken()
            if (!token.isNullOrBlank()) {
                context.headers.append("Authorization", "Bearer $token")
            }
        }

        client.sendPipeline.intercept(io.ktor.client.request.HttpSendPipeline.Monitoring) {
            println("➔ KTOR REQUEST: ${context.method.value} ${context.url.buildString()}")
        }

        client
    }

    single {
        val httpClient = get<HttpClient>()
        val baseUrl = get<String>(named("baseUrl"))

        Ktorfit.Builder()
            .httpClient(httpClient)
            .baseUrl(baseUrl)
            .build()
    }

    single<AuthApi> {
        val ktorfit = get<Ktorfit>()
        ktorfit.createAuthApi()
    }
}
