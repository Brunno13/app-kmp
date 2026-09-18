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
            SettingsAuthCredentialStore(
                MapSettings()
            )

        store.setAuthToken("auth-token")

        assertEquals(
            "auth-token",
            store.getAuthToken()
        )
    }

    @Test
    fun authTokenCanBeRemoved() {
        val store =
            SettingsAuthCredentialStore(
                MapSettings()
            )

        store.setAuthToken("auth-token")
        store.removeAuthToken()

        assertNull(
            store.getAuthToken()
        )
    }

    @Test
    fun apiCookiesCanBeStoredAndRead() {
        val store =
            SettingsAuthCredentialStore(
                MapSettings()
            )

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
            SettingsAuthCredentialStore(
                MapSettings()
            )

        store.setApiCookies(
            "session=value"
        )

        store.removeApiCookies()

        assertNull(
            store.getApiCookies()
        )
    }

    @Test
    fun sessionTokensCanBeStoredAndRead() {
        val store =
            SettingsAuthCredentialStore(
                MapSettings()
            )

        store.replaceSessionTokens(
            mapOf(
                "session-1" to "token-1",
                "session-2" to "token-2"
            )
        )

        assertEquals(
            "token-1",
            store.getSessionToken(
                "session-1"
            )
        )

        assertEquals(
            "token-2",
            store.getSessionToken(
                "session-2"
            )
        )
    }

    @Test
    fun replacingSessionTokensRemovesObsoleteEntries() {
        val store =
            SettingsAuthCredentialStore(
                MapSettings()
            )

        store.replaceSessionTokens(
            mapOf(
                "session-1" to "token-1",
                "session-2" to "token-2"
            )
        )

        store.replaceSessionTokens(
            mapOf(
                "session-2" to
                    "token-2-updated",
                "session-3" to "token-3"
            )
        )

        assertNull(
            store.getSessionToken(
                "session-1"
            )
        )

        assertEquals(
            "token-2-updated",
            store.getSessionToken(
                "session-2"
            )
        )

        assertEquals(
            "token-3",
            store.getSessionToken(
                "session-3"
            )
        )
    }

    @Test
    fun removeSessionTokenOnlyRemovesRequestedSession() {
        val store =
            SettingsAuthCredentialStore(
                MapSettings()
            )

        store.setAuthToken(
            "auth-token"
        )

        store.setApiCookies(
            "session=value"
        )

        store.replaceSessionTokens(
            mapOf(
                "session-1" to "token-1",
                "session-2" to "token-2"
            )
        )

        store.removeSessionToken(
            "session-1"
        )

        assertNull(
            store.getSessionToken(
                "session-1"
            )
        )

        assertEquals(
            "token-2",
            store.getSessionToken(
                "session-2"
            )
        )

        assertEquals(
            "auth-token",
            store.getAuthToken()
        )

        assertEquals(
            "session=value",
            store.getApiCookies()
        )
    }

    @Test
    fun clearRemovesCredentialsWithoutClearingOtherSettings() {
        val settings =
            MapSettings().apply {
                putString(
                    "theme_mode",
                    "DARK"
                )

                putBoolean(
                    "biometric_enabled",
                    true
                )
            }

        val store =
            SettingsAuthCredentialStore(
                settings
            )

        store.setAuthToken(
            "auth-token"
        )

        store.setApiCookies(
            "session=value"
        )

        store.replaceSessionTokens(
            mapOf(
                "session-1" to "token-1"
            )
        )

        store.clear()

        assertNull(
            store.getAuthToken()
        )

        assertNull(
            store.getApiCookies()
        )

        assertNull(
            store.getSessionToken(
                "session-1"
            )
        )

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
        val settings =
            MapSettings()

        val store =
            EncryptedSettingsAuthCredentialStore(
                settings = settings,
                cipher =
                    FakeCredentialCipher()
            )

        store.setAuthToken(
            "auth-token"
        )

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
        val settings =
            MapSettings()

        val store =
            EncryptedSettingsAuthCredentialStore(
                settings = settings,
                cipher =
                    FakeCredentialCipher()
            )

        store.setApiCookies(
            "session=value"
        )

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
    fun encryptedSessionTokensUseCredentialCipher() {
        val settings =
            MapSettings()

        val store =
            EncryptedSettingsAuthCredentialStore(
                settings = settings,
                cipher =
                    FakeCredentialCipher()
            )

        store.replaceSessionTokens(
            mapOf(
                "session-1" to "token-1"
            )
        )

        assertTrue(
            settings
                .getStringOrNull(
                    "session_tokens"
                )
                ?.startsWith(
                    "encrypted:"
                ) == true
        )

        assertEquals(
            "token-1",
            store.getSessionToken(
                "session-1"
            )
        )
    }

    @Test
    fun unreadableAuthTokenIsRemoved() {
        val settings =
            MapSettings().apply {
                putString(
                    "auth_token",
                    "invalid-value"
                )
            }

        val store =
            EncryptedSettingsAuthCredentialStore(
                settings = settings,
                cipher =
                    FakeCredentialCipher()
            )

        assertNull(
            store.getAuthToken()
        )

        assertNull(
            settings.getStringOrNull(
                "auth_token"
            )
        )
    }

    @Test
    fun unreadableApiCookiesAreRemoved() {
        val settings =
            MapSettings().apply {
                putString(
                    "api_cookies",
                    "invalid-value"
                )
            }

        val store =
            EncryptedSettingsAuthCredentialStore(
                settings = settings,
                cipher =
                    FakeCredentialCipher()
            )

        assertNull(
            store.getApiCookies()
        )

        assertNull(
            settings.getStringOrNull(
                "api_cookies"
            )
        )
    }

    @Test
    fun unreadableSessionTokensAreRemoved() {
        val settings =
            MapSettings().apply {
                putString(
                    "session_tokens",
                    "invalid-value"
                )
            }

        val store =
            EncryptedSettingsAuthCredentialStore(
                settings = settings,
                cipher =
                    FakeCredentialCipher()
            )

        assertNull(
            store.getSessionToken(
                "session-1"
            )
        )

        assertNull(
            settings.getStringOrNull(
                "session_tokens"
            )
        )
    }

    @Test
    fun encryptedClearDoesNotClearOtherSettings() {
        val settings =
            MapSettings().apply {
                putString(
                    "theme_mode",
                    "DARK"
                )

                putBoolean(
                    "biometric_enabled",
                    true
                )
            }

        val store =
            EncryptedSettingsAuthCredentialStore(
                settings = settings,
                cipher =
                    FakeCredentialCipher()
            )

        store.setAuthToken(
            "auth-token"
        )

        store.setApiCookies(
            "session=value"
        )

        store.replaceSessionTokens(
            mapOf(
                "session-1" to "token-1"
            )
        )

        store.clear()

        assertNull(
            store.getAuthToken()
        )

        assertNull(
            store.getApiCookies()
        )

        assertNull(
            store.getSessionToken(
                "session-1"
            )
        )

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
            if (
                !cipherText.startsWith(
                    PREFIX
                )
            ) {
                return null
            }

            return cipherText.removePrefix(
                PREFIX
            )
        }

        private companion object {
            const val PREFIX =
                "encrypted:"
        }
    }
}
