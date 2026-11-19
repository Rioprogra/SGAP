package com.sgap.app

import java.util.Calendar

object Fechas {
    val dias = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
    val meses = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

    fun corta(calendario: Calendar): String {
        val dia = dias[calendario.get(Calendar.DAY_OF_WEEK) - 1]
        val numero = calendario.get(Calendar.DAY_OF_MONTH)
        val mes = meses[calendario.get(Calendar.MONTH)]
        return "$dia $numero $mes"
    }

    fun enDias(dias: Int): Calendar {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_MONTH, dias)
        return calendario
    }
}
