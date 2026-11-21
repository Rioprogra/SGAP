package com.sgap.app

data class Tarea(
    var titulo: String,
    var tipo: String,
    var fechaEntrega: Long,
    var descripcion: String,
    var origen: String = "PERSONAL",
    var idGrupo: String? = null,
    var idMateria: String? = null,
    var completada: Boolean = false
) {
    var id = ""
    var idAsignacion = ""
    var idCreador = ""

    fun vencida(): Boolean {
        val hoy = java.util.Calendar.getInstance()
        hoy.set(java.util.Calendar.HOUR_OF_DAY, 0)
        hoy.set(java.util.Calendar.MINUTE, 0)
        hoy.set(java.util.Calendar.SECOND, 0)
        hoy.set(java.util.Calendar.MILLISECOND, 0)
        return !completada && fechaEntrega < hoy.timeInMillis
    }

    fun diasRestantes(): Int {
        val hoy = java.util.Calendar.getInstance()
        hoy.set(java.util.Calendar.HOUR_OF_DAY, 0)
        hoy.set(java.util.Calendar.MINUTE, 0)
        hoy.set(java.util.Calendar.SECOND, 0)
        hoy.set(java.util.Calendar.MILLISECOND, 0)
        val entrega = java.util.Calendar.getInstance()
        entrega.timeInMillis = fechaEntrega
        entrega.set(java.util.Calendar.HOUR_OF_DAY, 0)
        entrega.set(java.util.Calendar.MINUTE, 0)
        entrega.set(java.util.Calendar.SECOND, 0)
        entrega.set(java.util.Calendar.MILLISECOND, 0)
        return Math.round((entrega.timeInMillis - hoy.timeInMillis) / 86400000.0).toInt()
    }

    fun cuando(): String {
        if (completada) return "Entregada"
        val dias = diasRestantes()
        if (dias < -1) return "Vencida hace ${-dias} días"
        if (dias == -1) return "Venció ayer"
        if (dias == 0) return "Se entrega hoy"
        if (dias == 1) return "Se entrega mañana"
        if (dias < 7) return "Faltan $dias días"
        return fecha()
    }

    fun detalle(): String {
        var texto = cuando()
        if (texto != fecha()) texto += " · ${fecha()}"
        if (nombreMateria() != "") texto += " · ${nombreMateria()}"
        return texto
    }

    fun nombreMateria(): String {
        return ListaMaterias.nombre(idMateria)
    }

    fun fecha(): String {
        val calendario = java.util.Calendar.getInstance()
        calendario.timeInMillis = fechaEntrega
        return Fechas.corta(calendario)
    }

    fun color(): Int {
        if (tipo == "Examen") return R.color.acento_coral
        if (tipo == "Proyecto") return R.color.acento_turquesa
        return R.color.acento_violeta
    }

    fun colorSuave(): Int {
        if (tipo == "Examen") return R.color.acento_coral_suave
        if (tipo == "Proyecto") return R.color.acento_turquesa_suave
        return R.color.acento_violeta_suave
    }
}
