package com.sgap.app

// Grupos de ejemplo (los del Figma). Todavia no hay base de datos.
object ListaGrupos {
    val grupos = mutableListOf(
        EquipoGrupo("Equipo Alfa", "Proyecto", "App móvil",
            mutableListOf("TÚ", "AM", "CR", "JS"), 4, true, R.color.fondo, R.color.primario),
        EquipoGrupo("Los Indexados", "Proyecto", "Base de Datos",
            mutableListOf("LV", "DM", "PR", "KG", "EH"), 5, false, R.color.borde, R.color.texto),
        EquipoGrupo("Byte Squad", "Proyecto", "Sitio web informativo",
            mutableListOf("RS", "MF", "OT", "BC"), 4, false, R.color.fondo, R.color.texto),
        EquipoGrupo("Club de Algoritmos", "Estudio", "Estructura de Datos",
            mutableListOf("IN", "SG", "AL", "VR"), 8, false, R.color.borde, R.color.primario)
    )
}
