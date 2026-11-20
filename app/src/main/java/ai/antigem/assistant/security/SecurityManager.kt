package ai.antigem.assistant.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore

// ============================================================================
// Security Manager - Android Keystore + Encryption
// ============================================================================

class SecurityManager(private val context: Context) {

    private companion object {
        const val KEYSTORE_ALIAS = "antigem_master_key"
        const val PREFS_NAME = "antigem_secure_prefs"
    }

    private val masterKeyAlias: String = KEYSTORE_ALIAS

    init {
        initKeystore()
    }

    private fun initKeystore() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(false)
                    .build()
                val keyGenerator = javax.crypto.KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
                )
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            // Keystore init failed - encryption will degrade to software
        }
    }

    fun encryptToKeystore(plaintext: String): String? {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val key = keyStore.getKey(KEYSTORE_ALIAS, null) as javax.crypto.SecretKey
            val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
            android.util.Base64.encodeToString(combined, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

    fun decryptFromKeystore(ciphertextBase64: String): String? {
        return try {
            val combined = android.util.Base64.decode(ciphertextBase64, android.util.Base64.DEFAULT)
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val key = keyStore.getKey(KEYSTORE_ALIAS, null) as javax.crypto.SecretKey
            val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
            val ivSize = 12
            val iv = ByteArray(ivSize)
            System.arraycopy(combined, 0, iv, 0, ivSize)
            val encrypted = ByteArray(combined.size - ivSize)
            System.arraycopy(combined, ivSize, encrypted, 0, encrypted.size)
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key, javax.crypto.spec.GCMParameterSpec(128, iv))
            val decrypted = cipher.doFinal(encrypted)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    fun storeSecret(key: String, value: String) {
        val encrypted = encryptToKeystore(value)
        if (encrypted != null) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(key, encrypted).apply()
        }
    }

    fun getSecret(key: String): String? {
        val encrypted = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(key, null)
        return encrypted?.let { decryptFromKeystore(it) }
    }

    fun removeSecret(key: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().remove(key).apply()
    }

    fun isPermissionGranted(permission: String): Boolean {
        return context.checkSelfPermission(permission) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(activity: android.app.Activity, permissions: Array<String>, requestCode: Int) {
        activity.requestPermissions(permissions, requestCode)
    }
}
