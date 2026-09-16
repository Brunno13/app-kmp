package com.brunno.appkmp.data.repository

import com.brunno.appkmp.data.local.SessionDao
import com.brunno.appkmp.data.local.UserDao
import com.brunno.appkmp.data.remote.AuthApi
import com.brunno.appkmp.domain.repository.AuthRepository
import com.brunno.appkmp.domain.repository.AuthenticationRepository
import com.brunno.appkmp.domain.repository.ProfileRepository
import com.brunno.appkmp.domain.repository.SecurityRepository
import com.russhwolf.settings.Settings

class AuthRepositoryImpl(
    api: AuthApi,
    dao: UserDao,
    sessionDao: SessionDao,
    settings: Settings
) : AuthRepository,
    AuthenticationRepository by AuthenticationRepositoryImpl(
        api = api,
        dao = dao,
        sessionDao = sessionDao,
        settings = settings
    ),
    ProfileRepository by ProfileRepositoryImpl(
        api = api,
        dao = dao
    ),
    SecurityRepository by SecurityRepositoryImpl(
        api = api,
        sessionDao = sessionDao,
        settings = settings
    )
