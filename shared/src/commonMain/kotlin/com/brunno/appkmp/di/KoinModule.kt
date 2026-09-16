package com.brunno.appkmp.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.brunno.appkmp.data.local.AppDatabase
import com.brunno.appkmp.data.repository.AuthRepositoryImpl
import com.brunno.appkmp.domain.repository.AuthRepository
import com.brunno.appkmp.presentation.utils.GlobalErrorHandler
import com.brunno.appkmp.presentation.viewmodels.AuthViewModel
import com.brunno.appkmp.presentation.viewmodels.ThemeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import com.brunno.appkmp.domain.repository.ProfileRepository
import com.brunno.appkmp.domain.repository.SecurityRepository
import com.brunno.appkmp.presentation.viewmodels.ProfileViewModel
import com.brunno.appkmp.presentation.viewmodels.SecurityViewModel

val appModule = module {
    single {
        val builder = get<RoomDatabase.Builder<AppDatabase>>()
        builder
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().sessionDao() }

    single<AuthRepository> {
        AuthRepositoryImpl(
            api = get(),
            dao = get(),
            sessionDao = get(),
            settings = get()
        )
    }

    single<ProfileRepository> {
        get<AuthRepository>()
    }

    single<SecurityRepository> {
        get<AuthRepository>()
    }

    single { GlobalErrorHandler() }

    viewModelOf(::AuthViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::SecurityViewModel)
    viewModelOf(::ThemeViewModel)
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
