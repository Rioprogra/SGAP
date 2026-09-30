package com.sgap.app.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.sgap.app.data.repository.AuthRepository
import com.sgap.app.data.repository.Repositorios

/** Todo lo que la pantalla de login necesita dibujar, en un solo objeto. */
data class LoginEstado(
    val correo: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val loginExitoso: Boolean = false
)

/**
 * Guarda el estado de la pantalla de login y decide qué pasa al pulsar "Entrar".
 * Sobrevive a rotaciones de pantalla (por eso el estado no vive en la UI).
 */
class LoginViewModel(
    private val repositorio: AuthRepository = Repositorios.auth
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginEstado())
    val estado: StateFlow<LoginEstado> = _estado.asStateFlow()

    fun onCorreoCambio(valor: String) = _estado.update { it.copy(correo = valor, error = null) }

    fun onContrasenaCambio(valor: String) = _estado.update { it.copy(contrasena = valor, error = null) }

    fun iniciarSesion() {
        val actual = _estado.value
        // Validaciones básicas antes de "llamar al servidor"
        val error = when {
            actual.correo.isBlank() || actual.contrasena.isBlank() -> "Llena correo y contraseña"
            !Patterns.EMAIL_ADDRESS.matcher(actual.correo.trim()).matches() -> "El correo no es válido"
            else -> null
        }
        if (error != null) {
            _estado.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            val resultado = repositorio.iniciarSesion(actual.correo.trim(), actual.contrasena)
            _estado.update {
                it.copy(
                    cargando = false,
                    loginExitoso = resultado.isSuccess,
                    error = resultado.exceptionOrNull()?.message
                )
            }
        }
    }
}
