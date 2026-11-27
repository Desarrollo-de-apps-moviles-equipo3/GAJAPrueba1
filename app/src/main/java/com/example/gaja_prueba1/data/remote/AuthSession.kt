package com.example.gaja_prueba1.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Guarda el token que entrega el login y avisa cuando el servidor lo rechaza (401).
 * El token se guarda CIFRADO (AES-GCM con una llave del Android Keystore, que no sale del teléfono)
 * y se borra al cerrar sesión. Si el teléfono no logra descifrarlo, se pide iniciar sesión de nuevo.
 */
object AuthSession {

    private const val PREFS = "gaja_auth"
    private const val KEY_TOKEN_CIFRADO = "token_cifrado"
    private const val KEY_TOKEN_VIEJO = "token" // versión anterior, sin cifrar: se migra y se borra

    private var prefs: SharedPreferences? = null

    @Volatile
    private var cachedToken: String? = null

    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Emite cuando un servicio responde 401 con un token puesto: la sesión venció o ya no es válida. */
    val expired: SharedFlow<Unit> = _expired.asSharedFlow()

    fun init(context: Context) {
        val almacen = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = almacen

        val cifrado = almacen.getString(KEY_TOKEN_CIFRADO, null)
        if (cifrado != null) {
            cachedToken = TokenCipher.decrypt(cifrado)
            return
        }

        // Migración: si había un token sin cifrar de una versión anterior, se guarda cifrado y se borra el viejo.
        val viejo = almacen.getString(KEY_TOKEN_VIEJO, null)
        cachedToken = viejo
        if (viejo != null) {
            save(viejo)
        }
    }

    fun token(): String? = cachedToken

    fun save(token: String) {
        cachedToken = token
        val almacen = prefs ?: return
        val cifrado = TokenCipher.encrypt(token)
        val editor = almacen.edit().remove(KEY_TOKEN_VIEJO)
        if (cifrado != null) {
            editor.putString(KEY_TOKEN_CIFRADO, cifrado)
        } else {
            // Si no se pudo cifrar, mejor no guardarlo: la próxima vez se pide iniciar sesión.
            editor.remove(KEY_TOKEN_CIFRADO)
        }
        editor.apply()
    }

    fun clear() {
        cachedToken = null
        prefs?.edit()?.remove(KEY_TOKEN_CIFRADO)?.remove(KEY_TOKEN_VIEJO)?.apply()
    }

    /** Borra el token y avisa a la pantalla principal para que vuelva al login. */
    fun notifyExpired() {
        clear()
        _expired.tryEmit(Unit)
    }
}

/** Cifra y descifra texto con una llave AES guardada en el Android Keystore. Devuelve null si algo falla. */
private object TokenCipher {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "gaja_token_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    fun encrypt(plain: String): String? = try {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
    } catch (exception: Exception) {
        null
    }

    fun decrypt(stored: String): String? = try {
        val all = Base64.decode(stored, Base64.NO_WRAP)
        val iv = all.copyOfRange(0, IV_BYTES)
        val encrypted = all.copyOfRange(IV_BYTES, all.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, iv))
        String(cipher.doFinal(encrypted), Charsets.UTF_8)
    } catch (exception: Exception) {
        null
    }
}
