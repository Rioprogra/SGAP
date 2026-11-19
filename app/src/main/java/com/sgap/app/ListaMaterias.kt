package com.sgap.app

object ListaMaterias {
    val materias = mutableListOf<Materia>()

    fun nombre(idMateria: String?): String {
        for (materia in materias) {
            if (materia.id == idMateria) return materia.nombre
        }
        return ""
    }
}
