package com.brunno.appkmp.data.local

import com.russhwolf.settings.Settings

interface AuthCredentialStore {
    fun getAuthToken(): String?

    fun setAuthToken(token: String)

    fun removeAuthToken()

    fun getApiCookies(): String?

    fun setApiCookies(cookies: String)

    fun removeApiCookies()

    fun clear()
}

interface CredentialCipher {
    fun encrypt(plainText: String): String

    fun decrypt(cipherText: String): String?
}

class SettingsAuthCredentialStore(
    private val settings: Settings
) : AuthCredentialStore {

    override fun getAuthToken(): String? =
        settings.getStringOrNull(PREF_AUTH_TOKEN)

    override fun setAuthToken(token: String) {
        settings.putString(PREF_AUTH_TOKEN, token)
    }

    override fun removeAuthToken() {
        settings.remove(PREF_AUTH_TOKEN)
    }

    override fun getApiCookies(): String? =
        settings.getStringOrNull(PREF_API_COOKIES)

    override fun setApiCookies(cookies: String) {
        settings.putString(PREF_API_COOKIES, cookies)
    }

    override fun removeApiCookies() {
        settings.remove(PREF_API_COOKIES)
    }

    override fun clear() {
        removeAuthToken()
        removeApiCookies()
    }
}

class EncryptedSettingsAuthCredentialStore(
    private val settings: Settings,
    private val cipher: CredentialCipher
) : AuthCredentialStore {

    override fun getAuthToken(): String? =
        readEncrypted(PREF_AUTH_TOKEN)

    override fun setAuthToken(token: String) {
        writeEncrypted(
            key = PREF_AUTH_TOKEN,
            value = token
        )
    }

    override fun removeAuthToken() {
        settings.remove(PREF_AUTH_TOKEN)
    }

    override fun getApiCookies(): String? =
        readEncrypted(PREF_API_COOKIES)

    override fun setApiCookies(cookies: String) {
        writeEncrypted(
            key = PREF_API_COOKIES,
            value = cookies
        )
    }

    override fun removeApiCookies() {
        settings.remove(PREF_API_COOKIES)
    }

    override fun clear() {
        removeAuthToken()
        removeApiCookies()
    }

    private fun readEncrypted(
        key: String
    ): String? {
        val encrypted =
            settings.getStringOrNull(key)
                ?: return null

        val decrypted = cipher.decrypt(encrypted)

        if (decrypted == null) {
            settings.remove(key)
        }

        return decrypted
    }

    private fun writeEncrypted(
        key: String,
        value: String
    ) {
        settings.putString(
            key,
            cipher.encrypt(value)
        )
    }
}

private const val PREF_AUTH_TOKEN = "auth_token"
private const val PREF_API_COOKIES = "api_cookies"
