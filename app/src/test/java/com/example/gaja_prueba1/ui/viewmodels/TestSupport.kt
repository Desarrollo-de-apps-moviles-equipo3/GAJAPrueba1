package com.example.gaja_prueba1.ui.viewmodels

import com.example.gaja_prueba1.data.remote.BloqueoDto
import com.example.gaja_prueba1.model.AdminSpacesRepository
import com.example.gaja_prueba1.model.BlocksRepository
import com.example.gaja_prueba1.model.Booking
import com.example.gaja_prueba1.model.BookingsRepository
import com.example.gaja_prueba1.model.CategoriesRepository
import com.example.gaja_prueba1.model.Category
import com.example.gaja_prueba1.model.CategoryException
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.model.SpacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * viewModelScope corre en Dispatchers.Main, que no existe en un test de JVM.
 * Esta regla lo reemplaza por un dispatcher de prueba que ejecuta todo al tiro.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(UnconfinedTestDispatcher())
    override fun finished(description: Description) = Dispatchers.resetMain()
}

fun space(
    id: String,
    name: String = id,
    category: String = "Fútbol",
    capacity: Int = 10,
    available: Boolean = true
) = Space(
    id = id,
    name = name,
    category = category,
    description = "",
    capacity = capacity,
    available = available,
    colorHex = "#2F6B4F"
)

/** Repositorio de espacios en memoria; [failing] hace que cualquier llamada lance error. */
class FakeAdminSpacesRepository(
    initial: List<Space> = emptyList(),
    var failing: Boolean = false
) : AdminSpacesRepository {
    val spaces = initial.toMutableList()
    var createCalls = 0

    private fun check() {
        if (failing) throw RuntimeException("sin conexión")
    }

    override suspend fun getAll(): List<Space> { check(); return spaces.toList() }

    override suspend fun categories(): List<String> {
        check()
        return spaces.map { it.category }.distinct().sorted()
    }

    override suspend fun create(space: Space): Space {
        createCalls++
        check()
        return space.copy(id = "nuevo-${spaces.size + 1}").also { spaces.add(it) }
    }

    override suspend fun update(space: Space): Space {
        check()
        val i = spaces.indexOfFirst { it.id == space.id }
        spaces[i] = space
        return space
    }

    override suspend fun setAvailable(id: String, available: Boolean): Space {
        check()
        val i = spaces.indexOfFirst { it.id == id }
        spaces[i] = spaces[i].copy(available = available)
        return spaces[i]
    }

    override suspend fun delete(id: String) {
        check()
        spaces.removeAll { it.id == id }
    }
}

class FakeCategoriesRepository(
    initial: List<Category> = emptyList(),
    var failing: Boolean = false,
    /** Si no es null, borrar lanza este error de negocio (como el 409 del servicio). */
    var deleteError: String? = null
) : CategoriesRepository {
    val items = initial.toMutableList()
    private var nextId = 100L

    override suspend fun getAll(): List<Category> {
        if (failing) throw RuntimeException("sin conexión")
        return items.toList()
    }

    override suspend fun create(name: String): Category =
        Category(nextId++, name).also { items.add(it) }

    override suspend fun rename(id: Long, name: String): Category {
        val i = items.indexOfFirst { it.id == id }
        items[i] = items[i].copy(name = name)
        return items[i]
    }

    override suspend fun delete(id: Long) {
        deleteError?.let { throw CategoryException(it) }
        items.removeAll { it.id == id }
    }
}

class FakeSpacesRepository(
    var spaces: List<Space> = emptyList(),
    var failing: Boolean = false
) : SpacesRepository {
    override suspend fun getSpaces(): List<Space> {
        if (failing) throw RuntimeException("sin conexión")
        return spaces
    }
}

class FakeBlocksRepository(initial: List<BloqueoDto> = emptyList()) : BlocksRepository {
    val blocks = initial.toMutableList()
    var failOnCreate = false
    private var nextId = 500L

    override suspend fun getAll(): List<BloqueoDto> = blocks.toList()

    override suspend fun create(spaceId: String, date: String, timeSlot: String, reason: String?): BloqueoDto {
        if (failOnCreate) throw RuntimeException("sin conexión")
        return BloqueoDto(nextId++, spaceId, date, timeSlot, reason).also { blocks.add(it) }
    }

    override suspend fun delete(id: Long) {
        blocks.removeAll { it.id == id }
    }
}

class FakeBookingsRepository(var bookings: List<Booking> = emptyList()) : BookingsRepository {
    override suspend fun getActive(): List<Booking> = bookings
}
