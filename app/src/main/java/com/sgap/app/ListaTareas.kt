package com.sgap.app

object ListaTareas {
    val tareas = mutableListOf<Tarea>()

    fun pendientes(): Int {
        var cuantas = 0
        for (tarea in tareas) {
            if (!tarea.completada) cuantas++
        }
        return cuantas
    }
}
