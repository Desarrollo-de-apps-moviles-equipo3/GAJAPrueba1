
package com.example.gaja_prueba1.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException

object ProfilePhotoStorage {
    fun copyToInternal(context: Context, source: Uri): String? {
        return try {
            val directory = File(context.filesDir, "profile").apply { mkdirs() }
            val target = File(directory, "avatar.jpg")
            val input = context.contentResolver.openInputStream(source) ?: return null
            input.use { inputStream ->
                target.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }
            }
            Uri.fromFile(target).toString()
        } catch (_: IOException) {
            null
        }
    }
}
