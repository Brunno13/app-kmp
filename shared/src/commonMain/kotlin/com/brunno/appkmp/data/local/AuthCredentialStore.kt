package com.brunno.appkmp.data.local

import com.russhwolf.settings.Settings
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface AuthCredentialStore {
    fun getAuthToken(): String?

    fun setAuthToken(token: String)

    fun removeAuthToken()

    fun getApiCookies(): String?

    fun setApiCookies(cookies: String)

    fun removeApiCookies()

    fun getSessionToken(sessionId: String): String?

    fun replaceSessionTokens(
        tokensBySessionId: Map<String, String>
    )

    fun removeSessionToken(sessionId: String)

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

    override fun getSessionToken(
        sessionId: String
    ): String? =
        readPlainSessionTokens(settings)[sessionId]

    override fun replaceSessionTokens(
        tokensBySessionId: Map<String, String>
    ) {
        writePlainSessionTokens(
            settings = settings,
            tokensBySessionId = tokensBySessionId
        )
    }

    override fun removeSessionToken(
        sessionId: String
    ) {
        val updated =
            readPlainSessionTokens(settings)
                .toMutableMap()

        updated.remove(sessionId)

        writePlainSessionTokens(
            settings = settings,
            tokensBySessionId = updated
        )
    }

    override fun clear() {
        removeAuthToken()
        removeApiCookies()
        settings.remove(PREF_SESSION_TOKENS)
    }
}

class EncryptedSettingsAuthCredentialStore(
    private val settings: Settings,
    private val cipher: CredentialCipher
) : AuthCredentialStore {

    override fun getAuthToken(): String? =
        readEncrypted(
            settings = settings,
            cipher = cipher,
            key = PREF_AUTH_TOKEN
        )

    override fun setAuthToken(token: String) {
        writeEncrypted(
            settings = settings,
            cipher = cipher,
            key = PREF_AUTH_TOKEN,
            value = token
        )
    }

    override fun removeAuthToken() {
        settings.remove(PREF_AUTH_TOKEN)
    }

    override fun getApiCookies(): String? =
        readEncrypted(
            settings = settings,
            cipher = cipher,
            key = PREF_API_COOKIES
        )

    override fun setApiCookies(cookies: String) {
        writeEncrypted(
            settings = settings,
            cipher = cipher,
            key = PREF_API_COOKIES,
            value = cookies
        )
    }

    override fun removeApiCookies() {
        settings.remove(PREF_API_COOKIES)
    }

    override fun getSessionToken(
        sessionId: String
    ): String? =
        readEncryptedSessionTokens(
            settings = settings,
            cipher = cipher
        )[sessionId]

    override fun replaceSessionTokens(
        tokensBySessionId: Map<String, String>
    ) {
        writeEncryptedSessionTokens(
            settings = settings,
            cipher = cipher,
            tokensBySessionId = tokensBySessionId
        )
    }

    override fun removeSessionToken(
        sessionId: String
    ) {
        val updated =
            readEncryptedSessionTokens(
                settings = settings,
                cipher = cipher
            ).toMutableMap()

        updated.remove(sessionId)

        writeEncryptedSessionTokens(
            settings = settings,
            cipher = cipher,
            tokensBySessionId = updated
        )
    }

    override fun clear() {
        removeAuthToken()
        removeApiCookies()
        settings.remove(PREF_SESSION_TOKENS)
    }
}

private fun readEncrypted(
    settings: Settings,
    cipher: CredentialCipher,
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
    settings: Settings,
    cipher: CredentialCipher,
    key: String,
    value: String
) {
    settings.putString(
        key,
        cipher.encrypt(value)
    )
}

private fun readPlainSessionTokens(
    settings: Settings
): Map<String, String> {
    val serialized =
        settings.getStringOrNull(
            PREF_SESSION_TOKENS
        )

    val decoded =
        serialized?.let {
            decodeSessionTokens(it)
        }

    if (
        serialized != null &&
        decoded == null
    ) {
        settings.remove(
            PREF_SESSION_TOKENS
        )
    }

    return decoded.orEmpty()
}

private fun writePlainSessionTokens(
    settings: Settings,
    tokensBySessionId: Map<String, String>
) {
    if (tokensBySessionId.isEmpty()) {
        settings.remove(PREF_SESSION_TOKENS)
        return
    }

    settings.putString(
        PREF_SESSION_TOKENS,
        encodeSessionTokens(tokensBySessionId)
    )
}

private fun readEncryptedSessionTokens(
    settings: Settings,
    cipher: CredentialCipher
): Map<String, String> {
    val serialized =
        readEncrypted(
            settings = settings,
            cipher = cipher,
            key = PREF_SESSION_TOKENS
        )

    val decoded =
        serialized?.let {
            decodeSessionTokens(it)
        }

    if (
        serialized != null &&
        decoded == null
    ) {
        settings.remove(
            PREF_SESSION_TOKENS
        )
    }

    return decoded.orEmpty()
}

private fun writeEncryptedSessionTokens(
    settings: Settings,
    cipher: CredentialCipher,
    tokensBySessionId: Map<String, String>
) {
    if (tokensBySessionId.isEmpty()) {
        settings.remove(PREF_SESSION_TOKENS)
        return
    }

    writeEncrypted(
        settings = settings,
        cipher = cipher,
        key = PREF_SESSION_TOKENS,
        value = encodeSessionTokens(tokensBySessionId)
    )
}

private fun encodeSessionTokens(
    tokensBySessionId: Map<String, String>
): String =
    credentialJson.encodeToString(tokensBySessionId)

private fun decodeSessionTokens(
    serialized: String
): Map<String, String>? =
    runCatching {
        credentialJson.decodeFromString<
            Map<String, String>
        >(serialized)
    }.getOrNull()

private val credentialJson = Json

private const val PREF_AUTH_TOKEN =
    "auth_token"

private const val PREF_API_COOKIES =
    "api_cookies"

private const val PREF_SESSION_TOKENS =
    "session_tokens"
