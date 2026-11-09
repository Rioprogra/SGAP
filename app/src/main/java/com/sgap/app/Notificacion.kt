package com.sgap.app

// Un aviso: mencion, aviso del profe, te uniste a un grupo, etc.
data class Notificacion(
    var texto: String,
    var hace: String,       // ej. "Hace 5 min"
    var icono: String,      // "@", "!" o "+"
    var colorIcono: Int,    // ej. R.color.acento_violeta
    var fondoIcono: Int,    // ej. R.color.acento_violeta_suave
    var leida: Boolean = false
)
