package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.BloqueoDto
import com.example.gaja_prueba1.validation.ReservationValidator

/**
 * Lo que hay que hacer en el servicio para que los horarios bloqueados de un día queden como el admin los marcó.
 * [affected] son las reservas que ya existían en los horarios que se van a bloquear.
 */
data class BlockPlan(
    val toCreate: List<String>,
    val toDelete: List<BloqueoDto>,
    val affected: List<Booking>
) {
    val hasChanges: Boolean get() = toCreate.isNotEmpty() || toDelete.isNotEmpty()
}

object BlockPlanner {

    /** Horarios de [day] que hoy ya están bloqueados. */
    fun initialChecked(space: Space, day: String, blocks: List<BloqueoDto>): Set<String> =
        ReservationValidator.SLOTS.filter { SpaceAvailability.blockFor(space, day, it, blocks) != null }.toSet()

    fun plan(
        space: Space,
        day: String,
        checked: Set<String>,
        blocks: List<BloqueoDto>,
        reservations: List<Booking>
    ): BlockPlan {
        val current = initialChecked(space, day, blocks)
        val toCreate = ReservationValidator.SLOTS.filter { it in checked && it !in current }
        val toDelete = blocks.filter {
            it.spaceId == space.id && it.date == day && it.timeSlot.trim() !in checked
        }
        val affected = reservations.filter {
            it.space == space.name && it.date == day && it.timeSlot.trim() in toCreate
        }
        return BlockPlan(toCreate, toDelete, affected)
    }
}
