package com.sgap.app

object ListaCanales {
    val canales = mutableListOf(
        Canal("avisos-del-salón", "Prof. Ramírez: Mañana no hay clase de Redes", "10:24", 2, "Aviso", R.color.borde, R.color.primario),
        Canal("base-de-datos", "Ana: Ya subí el script de la práctica 4", "09:51", 5, "Materia", R.color.fondo, R.color.primario),
        Canal("app-móvil", "Luis: ¿Quién tiene el diseño del login?", "Ayer", 3, "Materia", R.color.borde, R.color.texto),
        Canal("estructura-de-datos", "Recuerden: el examen es el lunes 5", "Ayer", 0, "Materia", R.color.fondo, R.color.texto),
        Canal("general", "Carla: ¿Alguien va a la biblioteca?", "Lun", 0, "General", R.color.fondo, R.color.primario)
    )
}
