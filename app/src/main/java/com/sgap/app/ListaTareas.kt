package com.sgap.app

// Aqui se guardan las tareas mientras la app esta abierta.
// Es un object para que Pendientes, Perfil y NuevaTarea usen la misma lista.
object ListaTareas {
    val tareas = mutableListOf<Tarea>()

    // Tareas de ejemplo (las del Figma). Las fechas se calculan desde hoy
    // para que siempre salgan como "por entregar".
    init {
        agregar("Tarea de Base de Datos", "Tarea", 2, "Script de la práctica 4")
        agregar("Examen de Estructura de Datos", "Examen", 5, "Árboles y grafos")
        agregar("Sitio web informativo", "Proyecto", 9, "Entrega final con el Equipo Alfa")
    }

    fun agregar(titulo: String, tipo: String, enDias: Int, descripcion: String) {
        val fecha = Fechas.enDias(enDias)
        tareas.add(Tarea(titulo, tipo, Fechas.corta(fecha), fecha.timeInMillis, descripcion))
    }

    fun pendientes(): Int {
        var cuantas = 0
        for (tarea in tareas) {
            if (!tarea.completada) cuantas++
        }
        return cuantas
    }
}
