package com.sgap.app

data class Solicitud(
    val idUsuario: String,
    val nombre: String,
    val correo: String,
    val rolSolicitado: String,
    val idGrupo: String
)
