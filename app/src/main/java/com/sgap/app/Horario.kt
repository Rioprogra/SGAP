package com.sgap.app

import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

// Pestana Horario: las clases de cada dia
class Horario : AppCompatActivity() {

    lateinit var lvClases: ListView
    lateinit var txtVacio: TextView

    // Chip de cada dia, en orden de Lunes a Viernes
    lateinit var chips: List<TextView>
    val dias = listOf("Lun", "Mar", "Mié", "Jue", "Vie")

    var diaElegido = "Lun"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_horario)

        Navegacion.configurar(this, R.id.navHorario)

        lvClases = findViewById(R.id.lvClases)
        txtVacio = findViewById(R.id.txtVacio)
        chips = listOf(
            findViewById(R.id.chipLun),
            findViewById(R.id.chipMar),
            findViewById(R.id.chipMie),
            findViewById(R.id.chipJue),
            findViewById(R.id.chipVie)
        )

        for (i in chips.indices) {
            chips[i].setOnClickListener {
                diaElegido = dias[i]
                mostrarClases()
            }
        }

        // Empieza en el dia de hoy (sabado y domingo muestran el lunes)
        val hoy = Fechas.dias[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]
        if (dias.contains(hoy)) {
            diaElegido = hoy
        }

        mostrarClases()
    }

    fun mostrarClases() {
        for (i in chips.indices) {
            Chips.pintar(this, chips[i], dias[i] == diaElegido)
        }

        val clasesDelDia = mutableListOf<Clase>()
        for (clase in ListaHorario.clases) {
            if (clase.dia == diaElegido) {
                clasesDelDia.add(clase)
            }
        }

        lvClases.adapter = ClaseAdapter(this, clasesDelDia)

        if (clasesDelDia.isEmpty()) {
            txtVacio.visibility = View.VISIBLE
        } else {
            txtVacio.visibility = View.GONE
        }
    }
}
