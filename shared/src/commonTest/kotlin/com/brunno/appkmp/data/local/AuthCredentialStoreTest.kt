package com.brunno.appkmp.data.local

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthCredentialStoreTest {

    @Test
    fun authTokenCanBeStoredAndRead() {
        val store =
            SettingsAuthCredentialStore(MapSettings())

        store.setAuthToken("auth-token")

        assertEquals(
            "auth-token",
            store.getAuthToken()
        )
    }

    @Test
    fun authTokenCanBeRemoved() {
        val store =
            SettingsAuthCredentialStore(MapSettings())

        store.setAuthToken("auth-token")
        store.removeAuthToken()

        assertNull(store.getAuthToken())
    }

    @Test
    fun apiCookiesCanBeStoredAndRead() {
        val store =
            SettingsAuthCredentialStore(MapSettings())

        store.setApiCookies(
            "session=value; other=value"
        )

        assertEquals(
            "session=value; other=value",
            store.getApiCookies()
        )
    }

    @Test
    fun apiCookiesCanBeRemoved() {
        val store =
            SettingsAuthCredentialStore(MapSettings())

        store.setApiCookies("session=value")
        store.removeApiCookies()

        assertNull(store.getApiCookies())
    }

    @Test
    fun clearRemovesCredentialsWithoutClearingOtherSettings() {
        val settings = MapSettings().apply {
            putString("theme_mode", "DARK")
            putBoolean(
                "biometric_enabled",
                true
            )
        }

        val store =
            SettingsAuthCredentialStore(settings)

        store.setAuthToken("auth-token")
        store.setApiCookies("session=value")

        store.clear()

        assertNull(store.getAuthToken())
        assertNull(store.getApiCookies())

        assertEquals(
            "DARK",
            settings.getString(
                "theme_mode",
                ""
            )
        )

        assertTrue(
            settings.getBoolean(
                "biometric_enabled",
                false
            )
        )
    }

    @Test
    fun encryptedAuthTokenIsNotStoredAsPlainText() {
        val settings = MapSettings()

        val store =
            EncryptedSettingsAuthCredentialStore(
                settings = settings,
                cipher = FakeCredentialCipher()
            )

        store.setAuthToken("auth-token")

        assertEquals(
            "encrypted:auth-token",
            settings.getStringOrNull(
                "auth_token"
            )
        )

        assertEquals(
            "auth-token",
            store.getAuthToken()
        )
    }

    @Test
    fun encryptedApiCookiesAreNotStoredAsPlainText() {
        val settings = MapSettings()

        val store =
            EncryptedSettingsAuthCredentialStore(
                settings = settings,
                cipher = FakeCredentialCipher()
            )

        store.setApiCookies("session=value")

        assertEquals(
            "encrypted:session=value",
            settings.getStringOrNull(
                "api_cookies"
            )
        )

        assertEquals(
            "session=value",
            store.getApiCookies()
        )
    }

    @Test
    fun unreadableAuthTokenIsRemoved() {
        val settings = MapSettings().apply {
            putString(
                "auth_token",
                "invalid-value"
            )
        }

        val store =
            EncryptedSettingsAuthCredentialStore(
                settings = settings,
                cipher = FakeCredentialCipher()
            )

        assertNull(store.getAuthToken())

        assertNull(
            settings.getStringOrNull(
                "auth_token"
            )
        )
    }

    @Test
    fun unreadableApiCookiesAreRemoved() {
        val settings = MapSettings().apply {
            putString(
                "api_cookies",
                "invalid-value"
            )
        }

        val store =
            EncryptedSettingsAuthCredentialStore(
                settings = settings,
                cipher = FakeCredentialCipher()
            )

        assertNull(store.getApiCookies())

        assertNull(
            settings.getStringOrNull(
                "api_cookies"
            )
        )
    }

    @Test
    fun encryptedClearDoesNotClearOtherSettings() {
        val settings = MapSettings().apply {
            putString("theme_mode", "DARK")
            putBoolean(
                "biometric_enabled",
                true
            )
        }

        val store =
            EncryptedSettingsAuthCredentialStore(
                settings = settings,
                cipher = FakeCredentialCipher()
            )

        store.setAuthToken("auth-token")
        store.setApiCookies("session=value")

        store.clear()

        assertNull(store.getAuthToken())
        assertNull(store.getApiCookies())

        assertEquals(
            "DARK",
            settings.getString(
                "theme_mode",
                ""
            )
        )

        assertTrue(
            settings.getBoolean(
                "biometric_enabled",
                false
            )
        )
    }

    private class FakeCredentialCipher :
        CredentialCipher {

        override fun encrypt(
            plainText: String
        ): String =
            "$PREFIX$plainText"

        override fun decrypt(
            cipherText: String
        ): String? {
            if (!cipherText.startsWith(PREFIX)) {
                return null
            }

            return cipherText.removePrefix(PREFIX)
        }

        private companion object {
            const val PREFIX = "encrypted:"
        }
    }
}
