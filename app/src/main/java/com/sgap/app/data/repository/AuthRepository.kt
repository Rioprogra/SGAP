package com.sgap.app.data.repository

import kotlinx.coroutines.delay
import com.sgap.app.data.model.Usuario

/**
 * Contrato para iniciar sesión y registrarse. La pantalla y el ViewModel solo
 * conocen esta interfaz, así que cambiar a Firebase (u otra API) después no toca la UI.
 */
interface AuthRepository {
    /** Devuelve el usuario si las credenciales son válidas, o un error. */
    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Usuario>

    /** Crea una cuenta nueva y devuelve el usuario, o un error. */
    suspend fun registrar(nombre: String, correo: String, contrasena: String): Result<Usuario>
}

/**
 * Implementación temporal SIN backend, solo para el primer avance.
 * Acepta cualquier correo con una contraseña de 6 caracteres o más.
 * Se reemplazará cuando se confirme el backend (Firebase está por confirmar).
 */
class AuthRepositoryFalso : AuthRepository {
    override suspend fun iniciarSesion(correo: String, contrasena: String): Result<Usuario> {
        delay(800) // simula la espera de una red
        return if (contrasena.length >= 6) {
            Result.success(Usuario(id = "demo", correo = correo, nombre = correo.substringBefore("@")))
        } else {
            Result.failure(Exception("Correo o contraseña incorrectos"))
        }
    }

    override suspend fun registrar(nombre: String, correo: String, contrasena: String): Result<Usuario> {
        delay(800)
        return Result.success(Usuario(id = "demo", correo = correo, nombre = nombre))
    }
}
