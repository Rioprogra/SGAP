package com.sgap.app.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sgap.app.data.repository.AuthRepository
import com.sgap.app.data.repository.Repositorios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la pantalla de registro. */
data class RegistroEstado(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val registroExitoso: Boolean = false
)

/** Igual que el login, pero para crear cuenta (también falso por ahora). */
class RegistroViewModel(
    private val repositorio: AuthRepository = Repositorios.auth
) : ViewModel() {

    private val _estado = MutableStateFlow(RegistroEstado())
    val estado: StateFlow<RegistroEstado> = _estado.asStateFlow()

    fun onNombreCambio(valor: String) = _estado.update { it.copy(nombre = valor, error = null) }

    fun onCorreoCambio(valor: String) = _estado.update { it.copy(correo = valor, error = null) }

    fun onContrasenaCambio(valor: String) = _estado.update { it.copy(contrasena = valor, error = null) }

    fun registrar() {
        val actual = _estado.value
        val error = when {
            actual.nombre.isBlank() -> "Escribe tu nombre"
            !Patterns.EMAIL_ADDRESS.matcher(actual.correo.trim()).matches() -> "El correo no es válido"
            actual.contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
        if (error != null) {
            _estado.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            val resultado = repositorio.registrar(actual.nombre.trim(), actual.correo.trim(), actual.contrasena)
            _estado.update {
                it.copy(
                    cargando = false,
                    registroExitoso = resultado.isSuccess,
                    error = resultado.exceptionOrNull()?.message
                )
            }
        }
    }
}
