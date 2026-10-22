package com.sgap.app

// Una tarea, examen o proyecto del alumno
data class Tarea(
    var titulo: String,
    var tipo: String,
    var fecha: String,
    var descripcion: String,
    var completada: Boolean = false
)
