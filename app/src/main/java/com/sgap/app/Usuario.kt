package com.sgap.app

// Datos del alumno que inicio sesion (solo en memoria, todavia no hay base de datos)
object Usuario {
    var nombre = ""
    var correo = ""

    // Cuenta creada en Registro (para poner el nombre real al iniciar sesion)
    var nombreRegistrado = ""
    var correoRegistrado = ""

    fun iniciarSesion(correoEscrito: String) {
        correo = correoEscrito
        if (correoEscrito.equals(correoRegistrado, ignoreCase = true) && nombreRegistrado.isNotEmpty()) {
            nombre = nombreRegistrado
        } else {
            // Si no se registro, usamos lo que va antes de la @ (la matricula)
            nombre = correoEscrito.substringBefore("@")
        }
    }

    // "Ana López" -> "AL", "20231234" -> "20"
    fun iniciales(): String {
        val palabras = nombre.trim().split(" ")
        if (palabras.size >= 2 && palabras[1].isNotEmpty()) {
            return "${palabras[0][0]}${palabras[1][0]}".uppercase()
        }
        return nombre.take(2).uppercase()
    }
}
