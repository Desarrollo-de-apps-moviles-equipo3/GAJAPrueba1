package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.ReservationEntity
import com.example.gaja_prueba1.data.remote.BloqueoDto
import com.example.gaja_prueba1.validation.ReservationValidator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Estado de un horario de un espacio en un día. */
enum class SlotStatus { Libre, Reservado, Bloqueado, Pasado }

/** Cuadrícula de un mes: [cells] va de lunes a domingo, con null en los huecos antes y después del mes. */
data class MonthGrid(val year: Int, val month: Int, val cells: List<String?>)

/**
 * Lógica del calendario de disponibilidad, sin pantallas, para poder probarla.
 * Las fechas van como texto "yyyy-MM-dd": así se comparan y ordenan igual que en Room.
 */
object SpaceAvailability {

    fun slotStatus(
        space: Space,
        isoDate: String,
        slot: String,
        todayIso: String,
        blocks: List<BloqueoDto>,
        reservations: List<ReservationEntity>
    ): SlotStatus = when {
        isoDate < todayIso -> SlotStatus.Pasado
        blockFor(space, isoDate, slot, blocks) != null -> SlotStatus.Bloqueado
        reservations.any { it.space == space.name && it.date == isoDate && it.timeSlot.trim() == slot } ->
            SlotStatus.Reservado
        else -> SlotStatus.Libre
    }

    fun blockFor(space: Space, isoDate: String, slot: String, blocks: List<BloqueoDto>): BloqueoDto? =
        blocks.firstOrNull { it.spaceId == space.id && it.date == isoDate && it.timeSlot.trim() == slot }

    fun daySlots(
        space: Space,
        isoDate: String,
        todayIso: String,
        blocks: List<BloqueoDto>,
        reservations: List<ReservationEntity>
    ): List<Pair<String, SlotStatus>> = ReservationValidator.SLOTS.map { slot ->
        slot to slotStatus(space, isoDate, slot, todayIso, blocks, reservations)
    }

    fun freeCount(
        space: Space,
        isoDate: String,
        todayIso: String,
        blocks: List<BloqueoDto>,
        reservations: List<ReservationEntity>
    ): Int = daySlots(space, isoDate, todayIso, blocks, reservations).count { it.second == SlotStatus.Libre }

    /** Los 7 días (lunes a domingo) de la semana de [nowMillis], desplazada [weekOffset] semanas. */
    fun weekDays(nowMillis: Long, weekOffset: Int): List<String> {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        cal.add(Calendar.DAY_OF_YEAR, 7 * weekOffset)
        cal.add(Calendar.DAY_OF_YEAR, -mondayBasedIndex(cal))
        return List(7) { i ->
            iso(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).also {
                if (i < 6) cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    /** El mes de [nowMillis], desplazado [monthOffset] meses. */
    fun monthGrid(nowMillis: Long, monthOffset: Int): MonthGrid {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, monthOffset)
        }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val leading = mondayBasedIndex(cal)
        val days = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val cells = MutableList<String?>(leading) { null }
        for (day in 1..days) cells.add(iso(year, month, day))
        while (cells.size % 7 != 0) cells.add(null)
        return MonthGrid(year, month, cells)
    }

    fun monthLabel(grid: MonthGrid): String {
        val cal = Calendar.getInstance().apply { clear(); set(grid.year, grid.month, 1) }
        return SimpleDateFormat("MMMM yyyy", Locale("es", "CL")).format(cal.time)
            .replaceFirstChar { it.uppercase() }
    }

    fun parseIso(isoDate: String): Long =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate)?.time ?: 0L

    fun dayOfMonth(isoDate: String): Int = isoDate.takeLast(2).toInt()

    /** Lunes = 0 ... domingo = 6. Calendar parte el domingo en 1. */
    private fun mondayBasedIndex(cal: Calendar): Int = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7

    private fun iso(year: Int, month0: Int, day: Int): String =
        String.format(Locale.US, "%04d-%02d-%02d", year, month0 + 1, day)
}
