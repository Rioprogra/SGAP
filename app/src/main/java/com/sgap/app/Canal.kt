package com.sgap.app

data class Canal(
    var nombre: String,
    var ultimoMensaje: String,
    var hora: String,
    var noLeidos: Int,
    var tipo: String,
    var fondoIcono: Int,
    var colorIcono: Int
)
