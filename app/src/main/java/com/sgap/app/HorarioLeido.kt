package com.sgap.app

data class MateriaLeida(
    var nombre: String,
    var abreviatura: String,
    var profesor: String,
    var profesorCorto: String,
    var horas: Int,
    var color: String
)

data class ClaseLeida(
    var dia: Int,
    var inicio: String,
    var fin: String,
    var materia: String,
    var aula: String
)

data class HorarioLeido(
    var grupo: String,
    var escuela: String,
    val materias: MutableList<MateriaLeida>,
    val clases: MutableList<ClaseLeida>
) {
    fun materiaDe(clase: ClaseLeida): MateriaLeida? {
        val buscada = clase.materia.trim().lowercase()
        for (materia in materias) {
            if (materia.abreviatura.trim().lowercase() == buscada) return materia
        }
        for (materia in materias) {
            if (materia.nombre.trim().lowercase() == buscada) return materia
        }
        for (materia in materias) {
            val abreviatura = materia.abreviatura.trim().lowercase()
            if (abreviatura != "" && (abreviatura.startsWith(buscada) || buscada.startsWith(abreviatura))) return materia
        }
        return null
    }

    fun horasDeLaTabla(): Int {
        var total = 0
        for (materia in materias) total += materia.horas
        return total
    }

    fun diferencia(): Int {
        if (horasDeLaTabla() == 0) return 0
        return Math.abs(horasDeLaTabla() - horasTotales())
    }

    fun cuadra(): Boolean {
        return diferencia() == 0
    }

    fun horasTotales(): Int {
        var total = 0
        for (clase in clases) {
            val inicio = clase.inicio.substringBefore(":").toIntOrNull() ?: 0
            val fin = clase.fin.substringBefore(":").toIntOrNull() ?: 0
            total += fin - inicio
        }
        return total
    }
}
