package com.sgap.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sgap.app.data.model.Tarea
import com.sgap.app.data.model.TipoTarea
import com.sgap.app.data.repository.Repositorios
import com.sgap.app.data.repository.TareaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Chips de filtro de la pantalla Inicio. `tipo = null` significa "Todos". */
enum class Filtro(val etiqueta: String, val tipo: TipoTarea?) {
    TODOS("Todos", null),
    TAREAS("Tareas", TipoTarea.TAREA),
    EXAMENES("Exámenes", TipoTarea.EXAMEN),
    PROYECTOS("Proyectos", TipoTarea.PROYECTO)
}

/** Lo que dibuja Inicio: el filtro elegido, la lista ya filtrada y si hay tareas en total. */
data class InicioEstado(
    val filtro: Filtro = Filtro.TODOS,
    val tareas: List<Tarea> = emptyList(),
    val hayTareas: Boolean = false
)

class InicioViewModel(
    private val repositorio: TareaRepository = Repositorios.tareas
) : ViewModel() {

    private val filtro = MutableStateFlow(Filtro.TODOS)

    /**
     * `combine` junta dos flujos: cada vez que cambian las tareas O el filtro,
     * se vuelve a calcular la lista que se muestra.
     */
    val estado: StateFlow<InicioEstado> = combine(repositorio.tareas, filtro) { tareas, filtroActual ->
        val visibles = tareas
            .filter { filtroActual.tipo == null || it.tipo == filtroActual.tipo }
            // Pendientes primero, luego por fecha de entrega más cercana
            .sortedWith(compareBy<Tarea> { it.completada }.thenBy { it.fechaEntrega })
        InicioEstado(filtro = filtroActual, tareas = visibles, hayTareas = tareas.isNotEmpty())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InicioEstado())

    fun cambiarFiltro(nuevo: Filtro) {
        filtro.value = nuevo
    }

    fun alternarCompletada(id: Long) = repositorio.alternarCompletada(id)
}
