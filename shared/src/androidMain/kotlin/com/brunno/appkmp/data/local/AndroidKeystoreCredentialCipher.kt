package com.brunno.appkmp.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidKeystoreCredentialCipher : CredentialCipher {

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    override fun encrypt(
        plainText: String
    ): String {
        val cipher =
            Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateKey()
        )

        val encrypted = cipher.doFinal(
            plainText.toByteArray(Charsets.UTF_8)
        )

        val encodedIv = Base64.encodeToString(
            cipher.iv,
            Base64.NO_WRAP
        )

        val encodedCipherText = Base64.encodeToString(
            encrypted,
            Base64.NO_WRAP
        )

        return "$encodedIv:$encodedCipherText"
    }

    override fun decrypt(
        cipherText: String
    ): String? {
        return try {
            val separatorIndex =
                cipherText.indexOf(SEPARATOR)

            if (
                separatorIndex <= 0 ||
                separatorIndex >= cipherText.lastIndex
            ) {
                return null
            }

            val encodedIv =
                cipherText.substring(
                    startIndex = 0,
                    endIndex = separatorIndex
                )

            val encodedPayload =
                cipherText.substring(
                    startIndex = separatorIndex + 1
                )

            val iv = Base64.decode(
                encodedIv,
                Base64.NO_WRAP
            )

            val encrypted = Base64.decode(
                encodedPayload,
                Base64.NO_WRAP
            )

            val cipher =
                Cipher.getInstance(TRANSFORMATION)

            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(
                    GCM_TAG_LENGTH_BITS,
                    iv
                )
            )

            val decrypted =
                cipher.doFinal(encrypted)

            decrypted.toString(Charsets.UTF_8)
        } catch (_: GeneralSecurityException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    @Synchronized
    private fun getOrCreateKey(): SecretKey {
        val existing =
            keyStore.getKey(
                KEY_ALIAS,
                null
            )

        if (existing is SecretKey) {
            return existing
        }

        val keyGenerator =
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

        val keySpec =
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(
                    KeyProperties.BLOCK_MODE_GCM
                )
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .setKeySize(KEY_SIZE_BITS)
                .build()

        keyGenerator.init(keySpec)

        return keyGenerator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE =
            "AndroidKeyStore"

        const val KEY_ALIAS =
            "app_kmp_auth_credentials"

        const val TRANSFORMATION =
            "AES/GCM/NoPadding"

        const val KEY_SIZE_BITS = 256

        const val GCM_TAG_LENGTH_BITS = 128

        const val SEPARATOR = ':'
    }
}
