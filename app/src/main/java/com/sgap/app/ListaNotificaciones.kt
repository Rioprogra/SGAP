package com.sgap.app

// Notificaciones de ejemplo (las del Figma). Todavia no hay base de datos.
object ListaNotificaciones {
    val notificaciones = mutableListOf(
        Notificacion("Ana te mencionó en #base-de-datos", "Hace 5 min", "@",
            R.color.acento_violeta, R.color.acento_violeta_suave),
        Notificacion("Prof. Ramírez publicó un aviso: mañana no hay clase de Redes", "Hace 1 h", "!",
            R.color.acento_ambar, R.color.acento_ambar_suave),
        Notificacion("Te uniste al grupo Equipo Alfa", "Ayer", "+",
            R.color.acento_turquesa, R.color.acento_turquesa_suave, leida = true)
    )

    // Para el puntito rojo de la campana en Inicio
    fun hayNoLeidas(): Boolean {
        for (notificacion in notificaciones) {
            if (!notificacion.leida) return true
        }
        return false
    }

    // Se usa cuando pasa algo en la app (ej. te unes a un grupo)
    fun agregar(texto: String, icono: String, color: Int, fondo: Int) {
        notificaciones.add(0, Notificacion(texto, "Ahora", icono, color, fondo))
    }
}
