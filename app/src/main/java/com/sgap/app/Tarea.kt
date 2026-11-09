package com.sgap.app

// Una tarea, examen o proyecto del alumno
data class Tarea(
    var titulo: String,
    var tipo: String,          // "Tarea", "Examen" o "Proyecto"
    var fecha: String,         // como se ve en pantalla, ej. "Vie 2 oct"
    var fechaOrden: Long,      // la fecha en milisegundos, sirve para ordenar
    var descripcion: String,
    var completada: Boolean = false
) {
    // Color fuerte segun el tipo (rayita de la izquierda y texto de la etiqueta)
    fun color(): Int {
        if (tipo == "Examen") return R.color.acento_coral
        if (tipo == "Proyecto") return R.color.acento_turquesa
        return R.color.acento_violeta
    }

    // Color clarito segun el tipo (fondo de la etiqueta)
    fun colorSuave(): Int {
        if (tipo == "Examen") return R.color.acento_coral_suave
        if (tipo == "Proyecto") return R.color.acento_turquesa_suave
        return R.color.acento_violeta_suave
    }
}
