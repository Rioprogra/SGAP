package com.sgap.app

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

object ErroresFirebase {
    fun mensaje(error: Exception?): String {
        return when (error) {
            is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil, usa al menos 6 caracteres"
            is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
            is FirebaseAuthInvalidUserException -> "No hay ninguna cuenta con ese correo"
            is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
            is FirebaseNetworkException -> "Sin conexión a internet"
            is FirebaseTooManyRequestsException -> "Demasiados intentos, espera un momento"
            else -> "Ocurrió un error: ${error?.message}"
        }
    }
}
