package com.sgap.app

// Aqui se guardan las tareas mientras la app esta abierta.
// Es un object para que Inicio y NuevaTarea usen la misma lista.
object ListaTareas {
    val tareas = mutableListOf<Tarea>()
}
