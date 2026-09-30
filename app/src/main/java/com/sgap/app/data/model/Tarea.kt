package com.sgap.app.data.model

import java.time.LocalDate

/** Los tres tipos de pendiente que maneja la app (RF-03). */
enum class TipoTarea(val etiqueta: String) {
    TAREA("Tarea"),
    EXAMEN("Examen"),
    PROYECTO("Proyecto")
}

/** Un pendiente del alumno: tarea, examen o proyecto. */
data class Tarea(
    val id: Long,
    val titulo: String,
    val tipo: TipoTarea,
    val fechaEntrega: LocalDate,
    val descripcion: String = "",
    val completada: Boolean = false
)
