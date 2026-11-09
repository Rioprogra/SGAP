package com.sgap.app

// Un equipo de proyecto o grupo de estudio del salon.
// (Se llama EquipoGrupo para no chocar con la pantalla Grupo)
data class EquipoGrupo(
    var nombre: String,
    var tipo: String,                  // "Proyecto" o "Estudio"
    var materia: String,
    var integrantes: MutableList<String>, // iniciales de cada integrante, ej. "LV"
    var cupo: Int,                     // cuantos caben como maximo
    var esMio: Boolean,                // true si tu estas en este grupo
    var fondoIcono: Int,               // color del cuadrito con las siglas
    var colorIcono: Int
) {
    fun tieneLugar(): Boolean {
        return integrantes.size < cupo
    }

    // "Club de Algoritmos" -> "CA" (se salta palabras cortas como "de")
    fun siglas(): String {
        var resultado = ""
        for (palabra in nombre.split(" ")) {
            if (palabra.length > 2 && resultado.length < 2) {
                resultado += palabra[0].uppercaseChar()
            }
        }
        return resultado
    }
}
