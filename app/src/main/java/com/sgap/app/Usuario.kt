package com.sgap.app

object Usuario {
    var nombre = ""
    var correo = ""

    fun iniciarSesion(correoUsuario: String, nombreFirebase: String?) {
        correo = correoUsuario
        if (!nombreFirebase.isNullOrBlank()) {
            nombre = nombreFirebase
        } else {
            nombre = correoUsuario.substringBefore("@")
        }
    }

    fun iniciales(): String {
        val palabras = nombre.trim().split(" ")
        if (palabras.size >= 2 && palabras[1].isNotEmpty()) {
            return "${palabras[0][0]}${palabras[1][0]}".uppercase()
        }
        return nombre.take(2).uppercase()
    }
}
