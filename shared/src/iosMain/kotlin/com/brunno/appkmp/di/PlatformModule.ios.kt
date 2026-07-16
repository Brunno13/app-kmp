package com.brunno.appkmp.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.brunno.appkmp.data.local.AppDatabase
import com.brunno.appkmp.presentation.utils.IOSNetworkMonitor
import com.brunno.appkmp.presentation.utils.NetworkMonitor
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class, ExperimentalSettingsImplementation::class)
actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val dbFilePath = requireNotNull(documentDirectory?.path) + "/app_database.db"

        Room.databaseBuilder<AppDatabase>(
            name = dbFilePath
        )
    }

    single<Settings> {
        KeychainSettings(service = "AppKmpSecureVault")
    }

    single<NetworkMonitor> {
        IOSNetworkMonitor()
    }
}