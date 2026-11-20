package com.sgap.app

data class EquipoGrupo(
    var id: String,
    var nombre: String,
    var descripcion: String,
    var tipo: String,
    var materia: String,
    var cupo: Int,
    var codigoAcceso: String,
    var idCreador: String
) {
    var integrantes = mutableListOf<String>()
    var miembrosUid = mutableListOf<String>()
    var correos = mutableListOf<String>()
    var miRol: String? = null

    fun tieneLugar(): Boolean {
        return integrantes.size < cupo
    }

    fun esMio(): Boolean {
        return miRol != null
    }

    fun puedoPublicar(): Boolean {
        return miRol == "ADMIN" || miRol == "JEFE" || miRol == "TUTOR"
    }

    fun color(): Int {
        return Math.abs(id.hashCode()) % 4
    }

    fun fondoIcono(): Int {
        if (color() == 1 || color() == 3) return R.color.borde
        return R.color.fondo
    }

    fun colorIcono(): Int {
        if (color() == 1 || color() == 2) return R.color.texto
        return R.color.primario
    }

    fun siglas(): String {
        var resultado = ""
        for (palabra in nombre.split(" ")) {
            if (palabra.length > 2 && resultado.length < 2) {
                resultado += palabra[0].uppercaseChar()
            }
        }
        if (resultado.isEmpty()) resultado = nombre.take(2).uppercase()
        return resultado
    }
}
