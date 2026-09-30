package com.sgap.app.viewmodel

import androidx.lifecycle.ViewModel
import com.sgap.app.data.model.TipoTarea
import com.sgap.app.data.repository.Repositorios
import com.sgap.app.data.repository.TareaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

/** Estado del formulario "Nueva tarea". */
data class NuevaTareaEstado(
    val titulo: String = "",
    val tipo: TipoTarea = TipoTarea.TAREA,
    val fecha: LocalDate? = null,
    val descripcion: String = "",
    val errorTitulo: String? = null,
    val errorFecha: String? = null,
    val guardada: Boolean = false
)

class NuevaTareaViewModel(
    private val repositorio: TareaRepository = Repositorios.tareas
) : ViewModel() {

    private val _estado = MutableStateFlow(NuevaTareaEstado())
    val estado: StateFlow<NuevaTareaEstado> = _estado.asStateFlow()

    fun onTituloCambio(valor: String) = _estado.update { it.copy(titulo = valor, errorTitulo = null) }

    fun onTipoCambio(valor: TipoTarea) = _estado.update { it.copy(tipo = valor) }

    fun onFechaCambio(valor: LocalDate) = _estado.update { it.copy(fecha = valor, errorFecha = null) }

    fun onDescripcionCambio(valor: String) = _estado.update { it.copy(descripcion = valor) }

    fun guardar() {
        val actual = _estado.value
        val errorTitulo = if (actual.titulo.isBlank()) "Escribe un título" else null
        val errorFecha = if (actual.fecha == null) "Elige la fecha de entrega" else null
        if (errorTitulo != null || errorFecha != null) {
            _estado.update { it.copy(errorTitulo = errorTitulo, errorFecha = errorFecha) }
            return
        }

        repositorio.agregar(
            titulo = actual.titulo.trim(),
            tipo = actual.tipo,
            fechaEntrega = actual.fecha!!,
            descripcion = actual.descripcion.trim()
        )
        _estado.update { it.copy(guardada = true) }
    }
}
