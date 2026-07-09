package com.brunno.appkmp.di

import com.brunno.appkmp.data.remote.AuthApi
import com.brunno.appkmp.data.remote.createAuthApi
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    single {
        val httpClient = get<HttpClient>()
        Ktorfit.Builder()
            .httpClient(httpClient)
            .baseUrl("https://api.backend.com/") // TODO URL API
            .build()
    }

    single<AuthApi> {
        val ktorfit = get<Ktorfit>()
        ktorfit.createAuthApi()
    }
}