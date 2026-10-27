package com.example.gaja_prueba1.model

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

interface SpacesRepository {
    suspend fun getSpaces(): List<Space>
}

class FakeSpacesRepository(
    private val context: Context,
    private val delayMillis: Long = 800,
    private val failWith: Exception? = null
) : SpacesRepository {

    override suspend fun getSpaces(): List<Space> = withContext(Dispatchers.IO) {
        delay(delayMillis)
        failWith?.let { throw it }
        val json = context.assets.open("spaces.json").bufferedReader().use { it.readText() }
        SpacesJsonParser.parse(json)
    }
}