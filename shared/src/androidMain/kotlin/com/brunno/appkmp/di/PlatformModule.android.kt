package com.brunno.appkmp.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.brunno.appkmp.data.local.AndroidKeystoreCredentialCipher
import com.brunno.appkmp.data.local.AppDatabase
import com.brunno.appkmp.data.local.AuthCredentialStore
import com.brunno.appkmp.data.local.EncryptedSettingsAuthCredentialStore
import com.brunno.appkmp.presentation.utils.AndroidNetworkMonitor
import com.brunno.appkmp.presentation.utils.NetworkMonitor
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val context = androidContext()
        val dbFile =
            context.getDatabasePath("app_database.db")

        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
    }

    single<Settings> {
        val context = androidContext()

        SharedPreferencesSettings(
            context.getSharedPreferences(
                APP_PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
        )
    }

    single<AuthCredentialStore> {
        val context = androidContext()

        val credentialSettings =
            SharedPreferencesSettings(
                context.getSharedPreferences(
                    AUTH_CREDENTIALS_NAME,
                    Context.MODE_PRIVATE
                )
            )

        EncryptedSettingsAuthCredentialStore(
            settings = credentialSettings,
            cipher = AndroidKeystoreCredentialCipher()
        )
    }

    single<NetworkMonitor> {
        AndroidNetworkMonitor(
            context = androidContext()
        )
    }
}

private const val APP_PREFERENCES_NAME =
    "app_preferences"

private const val AUTH_CREDENTIALS_NAME =
    "auth_credentials"
