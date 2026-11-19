package com.sgap.app

object ListaNotificaciones {
    val notificaciones = mutableListOf<Notificacion>()

    fun hayNoLeidas(): Boolean {
        for (notificacion in notificaciones) {
            if (!notificacion.leida) return true
        }
        return false
    }
}
