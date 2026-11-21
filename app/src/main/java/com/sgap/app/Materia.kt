package com.sgap.app

import android.graphics.Color

data class Materia(
    var id: String,
    var nombre: String,
    var idGrupo: String = "",
    var abreviatura: String = "",
    var profesor: String = "",
    var horasSemana: Int = 0,
    var color: String = ""
) {
    fun colorEntero(): Int {
        try {
            if (color.startsWith("#")) return Color.parseColor(color)
        } catch (e: Exception) {
        }
        val paleta = listOf("#7C5CFF", "#14B8A6", "#F97362", "#F5A524", "#3B82F6", "#22C55E", "#EC4899")
        return Color.parseColor(paleta[Math.abs(nombre.hashCode()) % paleta.size])
    }
}
