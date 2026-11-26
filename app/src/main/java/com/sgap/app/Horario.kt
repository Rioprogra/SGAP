package com.sgap.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class Horario : AppCompatActivity() {

    lateinit var lvClases: ListView
    lateinit var txtVacio: TextView

    lateinit var chips: List<TextView>
    val dias = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")

    var diaElegido = "Lun"

    var clasesDelDia = mutableListOf<Clase>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_horario)

        Navegacion.configurar(this, R.id.navHorario)

        lvClases = findViewById(R.id.lvClases)
        txtVacio = findViewById(R.id.txtVacio)
        chips = listOf(
            findViewById(R.id.chipLun),
            findViewById(R.id.chipMar),
            findViewById(R.id.chipMie),
            findViewById(R.id.chipJue),
            findViewById(R.id.chipVie),
            findViewById(R.id.chipSab)
        )

        for (i in chips.indices) {
            chips[i].setOnClickListener {
                diaElegido = dias[i]
                mostrarClases()
            }
        }

        val hoy = Fechas.dias[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]
        if (dias.contains(hoy)) {
            diaElegido = hoy
        }

        lvClases.setOnItemClickListener { _, _, position, _ ->
            val clase = clasesDelDia[position]
            if (clase.idMateria != "") {
                val intent = Intent(this, DetalleMateria::class.java)
                intent.putExtra("idMateria", clase.idMateria)
                startActivity(intent)
            }
        }

        findViewById<TextView>(R.id.btnMaterias).setOnClickListener {
            startActivity(Intent(this, Materias::class.java))
        }

        mostrarClases()
        BaseDatos.cargarGrupos { mostrarGrupo() }
        BaseDatos.cargarHorario {
            mostrarClases()
            AvisoClases.programarTodas(this)
        }
    }

    fun mostrarGrupo() {
        val nombres = mutableListOf<String>()
        for (grupo in ListaGrupos.grupos) {
            if (grupo.esMio()) nombres.add(grupo.nombre)
        }
        findViewById<TextView>(R.id.txtGrupo).text = if (nombres.isEmpty()) "Sin grupo" else nombres.joinToString(" · ")
    }

    fun mostrarClases() {
        for (i in chips.indices) {
            Chips.pintar(this, chips[i], dias[i] == diaElegido)
        }

        clasesDelDia.clear()
        for (clase in ListaHorario.clases) {
            if (clase.dia == diaElegido) {
                clasesDelDia.add(clase)
            }
        }

        lvClases.adapter = ClaseAdapter(this, clasesDelDia)

        val hayClasesSabado = ListaHorario.clases.any { it.dia == "Sáb" }
        chips[5].visibility = if (hayClasesSabado || diaElegido == "Sáb") View.VISIBLE else View.GONE

        if (clasesDelDia.isEmpty()) {
            txtVacio.visibility = View.VISIBLE
        } else {
            txtVacio.visibility = View.GONE
        }
    }
}
