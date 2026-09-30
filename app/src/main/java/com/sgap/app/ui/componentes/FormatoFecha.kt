package com.sgap.app.ui.componentes

import java.time.LocalDate
import java.time.temporal.ChronoUnit

// Nombres en español escritos a mano para que se vean igual en todos los celulares.
private val diasCortos = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
private val diasLargos = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
private val mesesCortos = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
private val mesesLargos = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

/** "Miércoles 30 de septiembre" (encabezado de Inicio). */
fun fechaLarga(fecha: LocalDate): String =
    "${diasLargos[fecha.dayOfWeek.value - 1]} ${fecha.dayOfMonth} de ${mesesLargos[fecha.monthValue - 1]}"

/** "Vie 2 oct" (campo de fecha y tarjetas). */
fun fechaCorta(fecha: LocalDate): String =
    "${diasCortos[fecha.dayOfWeek.value - 1]} ${fecha.dayOfMonth} ${mesesCortos[fecha.monthValue - 1]}"

/** Como en el Figma: "Hoy", "Mañana", "Ayer" o la fecha corta. */
fun fechaRelativa(fecha: LocalDate, hoy: LocalDate = LocalDate.now()): String =
    when (ChronoUnit.DAYS.between(hoy, fecha)) {
        0L -> "Hoy"
        1L -> "Mañana"
        -1L -> "Ayer"
        else -> fechaCorta(fecha)
    }
