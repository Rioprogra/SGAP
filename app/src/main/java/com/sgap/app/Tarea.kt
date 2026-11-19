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
