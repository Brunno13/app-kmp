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

    private companion object {
        const val PREF_AUTH_TOKEN = "auth_token"
        const val PREF_API_COOKIES = "api_cookies"
    }
}
