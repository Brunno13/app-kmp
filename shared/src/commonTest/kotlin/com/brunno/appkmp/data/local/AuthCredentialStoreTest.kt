package com.brunno.appkmp.data.local

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthCredentialStoreTest {

    @Test
    fun authTokenCanBeStoredAndRead() {
        val store = SettingsAuthCredentialStore(MapSettings())

        store.setAuthToken("auth-token")

        assertEquals(
            "auth-token",
            store.getAuthToken()
        )
    }

    @Test
    fun authTokenCanBeRemoved() {
        val store = SettingsAuthCredentialStore(MapSettings())

        store.setAuthToken("auth-token")
        store.removeAuthToken()

        assertNull(store.getAuthToken())
    }

    @Test
    fun apiCookiesCanBeStoredAndRead() {
        val store = SettingsAuthCredentialStore(MapSettings())

        store.setApiCookies("session=value; other=value")

        assertEquals(
            "session=value; other=value",
            store.getApiCookies()
        )
    }

    @Test
    fun apiCookiesCanBeRemoved() {
        val store = SettingsAuthCredentialStore(MapSettings())

        store.setApiCookies("session=value")
        store.removeApiCookies()

        assertNull(store.getApiCookies())
    }

    @Test
    fun clearRemovesCredentialsWithoutClearingOtherSettings() {
        val settings = MapSettings().apply {
            putString("theme_mode", "DARK")
            putBoolean("biometric_enabled", true)
        }

        val store = SettingsAuthCredentialStore(settings)

        store.setAuthToken("auth-token")
        store.setApiCookies("session=value")

        store.clear()

        assertNull(store.getAuthToken())
        assertNull(store.getApiCookies())

        assertEquals(
            "DARK",
            settings.getString("theme_mode", "")
        )

        assertTrue(
            settings.getBoolean("biometric_enabled", false)
        )
    }
}
