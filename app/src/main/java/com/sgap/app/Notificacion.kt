package com.sgap.app

data class Notificacion(
    var id: String,
    var titulo: String,
    var mensaje: String,
    var fechaProgramada: Long,
    var origen: String,
    var leida: Boolean,
    var idTarea: String? = null
) {
    fun icono(): String {
        if (origen == "AVISO") return "!"
        if (origen == "HORARIO") return "@"
        return "+"
    }

    fun colorIcono(): Int {
        if (origen == "AVISO") return R.color.acento_ambar
        if (origen == "HORARIO") return R.color.acento_violeta
        return R.color.acento_turquesa
    }

    fun fondoIcono(): Int {
        if (origen == "AVISO") return R.color.acento_ambar_suave
        if (origen == "HORARIO") return R.color.acento_violeta_suave
        return R.color.acento_turquesa_suave
    }

    fun hace(): String {
        val minutos = (System.currentTimeMillis() - fechaProgramada) / 60000
        if (minutos < 1) return "Ahora"
        if (minutos < 60) return "Hace $minutos min"
        val horas = minutos / 60
        if (horas < 24) return "Hace $horas h"
        val dias = horas / 24
        if (dias == 1L) return "Ayer"
        return "Hace $dias días"
    }
}
