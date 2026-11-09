package com.sgap.app

// Una clase del horario
data class Clase(
    var dia: String,      // "Lun", "Mar", "Mié", "Jue" o "Vie"
    var inicio: String,   // ej. "07:00"
    var fin: String,      // ej. "08:40"
    var materia: String,
    var salon: String,
    var profesor: String,
    var color: Int        // color de la rayita, ej. R.color.acento_violeta
)
