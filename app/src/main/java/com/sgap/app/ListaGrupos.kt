package com.sgap.app

object ListaGrupos {
    val grupos = mutableListOf<EquipoGrupo>()

    fun misGrupos(): Int {
        var cuantos = 0
        for (grupo in grupos) {
            if (grupo.esMio()) cuantos++
        }
        return cuantos
    }

    fun dondePuedoPublicar(): List<EquipoGrupo> {
        val lista = mutableListOf<EquipoGrupo>()
        for (grupo in grupos) {
            if (grupo.puedoPublicar()) lista.add(grupo)
        }
        return lista
    }
}
