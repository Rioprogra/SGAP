package com.sgap.app

data class Clase(
    var dia: String,
    var inicio: String,
    var fin: String,
    var materia: String,
    var salon: String,
    var profesor: String,
    var color: Int,
    var idMateria: String = "",
    var idHorario: String = "",
    var diaNumero: Int = 1
)
