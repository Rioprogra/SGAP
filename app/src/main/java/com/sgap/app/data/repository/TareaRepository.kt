package com.sgap.app.data.repository

import com.sgap.app.data.model.Tarea
import com.sgap.app.data.model.TipoTarea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

/**
 * Contrato para leer y guardar tareas. Los ViewModels solo conocen esta
 * interfaz; para usar Room o Firebase después se crea otra implementación
 * y se cambia en [Repositorios], sin tocar pantallas ni ViewModels.
 */
interface TareaRepository {
    /** Lista de tareas que se actualiza sola cuando algo cambia. */
    val tareas: StateFlow<List<Tarea>>

    fun agregar(titulo: String, tipo: TipoTarea, fechaEntrega: LocalDate, descripcion: String)

    fun alternarCompletada(id: Long)
}

/**
 * Guarda las tareas solo en memoria: se pierden al cerrar la app.
 * Suficiente para el primer avance.
 */
class TareaRepositoryEnMemoria : TareaRepository {
    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    override val tareas: StateFlow<List<Tarea>> = _tareas.asStateFlow()

    private var siguienteId = 1L

    override fun agregar(titulo: String, tipo: TipoTarea, fechaEntrega: LocalDate, descripcion: String) {
        val nueva = Tarea(
            id = siguienteId++,
            titulo = titulo,
            tipo = tipo,
            fechaEntrega = fechaEntrega,
            descripcion = descripcion
        )
        _tareas.update { it + nueva }
    }

    override fun alternarCompletada(id: Long) {
        _tareas.update { lista ->
            lista.map { if (it.id == id) it.copy(completada = !it.completada) else it }
        }
    }
}

/**
 * Lugar único donde se crean los repositorios. Así Inicio y Nueva tarea
 * comparten la MISMA lista de tareas.
 */
object Repositorios {
    val tareas: TareaRepository by lazy { TareaRepositoryEnMemoria() }
    val auth: AuthRepository by lazy { AuthRepositoryFalso() }
}
