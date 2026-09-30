package mx.edu.utcj.sgap.data.model

/** Datos básicos de un usuario que inició sesión. */
data class Usuario(
    val id: String,
    val correo: String,
    val nombre: String
)
