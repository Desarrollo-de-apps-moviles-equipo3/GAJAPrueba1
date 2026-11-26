package com.example.gaja_prueba1.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

interface ThemeStorage {
    fun readDark(): Boolean
    fun writeDark(dark: Boolean)
}

class SharedPrefsThemeStorage(context: Context) : ThemeStorage {
    private val prefs = context.applicationContext.getSharedPreferences("gaja_settings", Context.MODE_PRIVATE)

    override fun readDark(): Boolean = prefs.getBoolean(KEY_DARK, false)

    override fun writeDark(dark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK, dark).apply()
    }

    private companion object {
        const val KEY_DARK = "dark_mode"
    }
}

object ThemeSettings {
    var isDark by mutableStateOf(false)
        private set

    private var storage: ThemeStorage? = null

    fun init(storage: ThemeStorage) {
        this.storage = storage
        isDark = storage.readDark()
    }

    fun chooseDark(dark: Boolean) {
        isDark = dark
        storage?.writeDark(dark)
    }
}
