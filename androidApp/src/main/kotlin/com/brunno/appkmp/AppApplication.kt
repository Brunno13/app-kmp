package com.brunno.appkmp

import android.app.Application
import com.brunno.appkmp.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin (baseUrl = BuildConfig.BASE_URL) {
            androidLogger()
            androidContext(this@AppApplication)
        }
    }
}