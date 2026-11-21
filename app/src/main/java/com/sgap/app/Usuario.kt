package com.sgap.app

object Usuario {
    var nombre = ""
    var correo = ""

    var rol = "ALUMNO"
    var rolSolicitado = "ALUMNO"
    var estadoRol = "APROBADO"
    var idGrupo = ""
    var perfilCompleto = false
    var esAdmin = false

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

    fun puedeCrearGrupos(): Boolean {
        return estadoRol == "APROBADO" && (rol == "JEFE" || rol == "TUTOR")
    }

    fun nombreRol(clave: String): String {
        if (clave == "JEFE") return "Jefe de grupo"
        if (clave == "TUTOR") return "Tutor"
        return "Alumno"
    }

    fun descripcionRol(): String {
        if (estadoRol == "PENDIENTE") return "${nombreRol(rolSolicitado)} · esperando autorización"
        if (estadoRol == "RECHAZADO") return "Alumno · no se autorizó ${nombreRol(rolSolicitado).lowercase()}"
        return nombreRol(rol)
    }

    fun limpiar() {
        nombre = ""
        correo = ""
        rol = "ALUMNO"
        rolSolicitado = "ALUMNO"
        estadoRol = "APROBADO"
        idGrupo = ""
        perfilCompleto = false
        esAdmin = false
    }
}
