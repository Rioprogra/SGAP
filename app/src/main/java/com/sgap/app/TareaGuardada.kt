package com.sgap.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tareas_guardadas")
data class TareaGuardada(
    @PrimaryKey val idAsignacion: String,
    val idUsuario: String,
    val idTarea: String,
    val titulo: String,
    val tipo: String,
    val fechaEntrega: Long,
    val descripcion: String,
    val origen: String,
    val idGrupo: String?,
    val idMateria: String?,
    val idCreador: String,
    val completada: Boolean
) {
    fun aTarea(): Tarea {
        val tarea = Tarea(titulo, tipo, fechaEntrega, descripcion, origen, idGrupo, idMateria, completada)
        tarea.id = idTarea
        tarea.idAsignacion = idAsignacion
        tarea.idCreador = idCreador
        return tarea
    }

    companion object {
        fun desde(tarea: Tarea, idUsuario: String): TareaGuardada {
            return TareaGuardada(
                tarea.idAsignacion,
                idUsuario,
                tarea.id,
                tarea.titulo,
                tarea.tipo,
                tarea.fechaEntrega,
                tarea.descripcion,
                tarea.origen,
                tarea.idGrupo,
                tarea.idMateria,
                tarea.idCreador,
                tarea.completada
            )
        }
    }
}
