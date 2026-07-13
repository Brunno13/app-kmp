package com.brunno.appkmp.di

import com.brunno.appkmp.data.remote.AuthApi
import com.brunno.appkmp.data.remote.createAuthApi
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val networkModule = module {

    single {
        val client = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
        client.sendPipeline.intercept(io.ktor.client.request.HttpSendPipeline.Monitoring) {
            println("➔ KTOR SENDING TO: ${context.url.buildString()}")
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