package com.sgap.app

// Un canal de chat del salon (ej. #base-de-datos)
data class Canal(
    var nombre: String,
    var ultimoMensaje: String,
    var hora: String,
    var noLeidos: Int,
    var tipo: String,        // "Materia", "Aviso" o "General" (para los filtros)
    var fondoIcono: Int,     // color del cuadrito del "#", ej. R.color.borde
    var colorIcono: Int      // color del "#"
)
