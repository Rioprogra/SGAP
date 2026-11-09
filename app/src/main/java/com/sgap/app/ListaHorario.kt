package com.sgap.app

// Horario de ejemplo del 5°A. Todavia no hay base de datos.
object ListaHorario {

    // Cada materia con su color, para que se vea igual todos los dias
    val bd = R.color.acento_violeta
    val ed = R.color.acento_coral
    val app = R.color.acento_turquesa
    val redes = R.color.acento_ambar
    val ingles = R.color.primario

    val clases = listOf(
        Clase("Lun", "07:00", "08:40", "Base de Datos", "Lab. 3", "Mtra. López", bd),
        Clase("Lun", "08:40", "10:20", "Estructura de Datos", "Aula 12", "Mtro. Pérez", ed),
        Clase("Lun", "10:40", "12:20", "Inglés V", "Aula 7", "Mtra. Smith", ingles),

        Clase("Mar", "07:00", "08:40", "Desarrollo de Apps Móviles", "Lab. 1", "Mtro. García", app),
        Clase("Mar", "08:40", "10:20", "Redes de Computadoras", "Lab. 2", "Prof. Ramírez", redes),

        Clase("Mié", "07:00", "08:40", "Base de Datos", "Lab. 3", "Mtra. López", bd),
        Clase("Mié", "08:40", "10:20", "Estructura de Datos", "Aula 12", "Mtro. Pérez", ed),
        Clase("Mié", "10:40", "12:20", "Desarrollo de Apps Móviles", "Lab. 1", "Mtro. García", app),

        Clase("Jue", "07:00", "08:40", "Redes de Computadoras", "Lab. 2", "Prof. Ramírez", redes),
        Clase("Jue", "08:40", "10:20", "Inglés V", "Aula 7", "Mtra. Smith", ingles),

        Clase("Vie", "07:00", "08:40", "Desarrollo de Apps Móviles", "Lab. 1", "Mtro. García", app),
        Clase("Vie", "08:40", "10:20", "Base de Datos", "Lab. 3", "Mtra. López", bd)
    )
}
