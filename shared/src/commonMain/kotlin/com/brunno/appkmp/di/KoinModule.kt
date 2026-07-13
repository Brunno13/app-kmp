package com.brunno.appkmp.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.brunno.appkmp.data.local.AppDatabase
import com.brunno.appkmp.data.repository.AuthRepositoryImpl
import com.brunno.appkmp.domain.repository.AuthRepository
import com.brunno.appkmp.presentation.viewmodels.AuthViewModel
import org.koin.core.module.dsl.viewModelOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single {
        val builder = get<RoomDatabase.Builder<AppDatabase>>()
        builder
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single { get<AppDatabase>().userDao() }

    single<AuthRepository> { AuthRepositoryImpl(api = get(), dao = get()) }

    viewModelOf(::AuthViewModel)
}

fun initKoin(baseUrl: String, appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()

        val configModule = module {
            single(named("baseUrl")) { baseUrl }
        }

        modules(configModule, platformModule, appModule, networkModule)
    }
}

//fun initKoin() = initKoin {}