package com.sgap.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Progreso : AppCompatActivity() {

    lateinit var contProgreso: LinearLayout
    lateinit var txtVacio: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_progreso)

        contProgreso = findViewById(R.id.contProgreso)
        txtVacio = findViewById(R.id.txtVacio)
        findViewById<TextView>(R.id.btnRegresar).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        mostrarProgreso()
        BaseDatos.cargarMaterias {
            BaseDatos.cargarTareas(this) { mostrarProgreso() }
        }
    }

    fun mostrarProgreso() {
        contProgreso.removeAllViews()
        val tareas = ListaTareas.tareas

        if (tareas.isEmpty()) {
            txtVacio.visibility = View.VISIBLE
            return
        }
        txtVacio.visibility = View.GONE

        agregarTarjeta("General", tareas)

        for (materia in ListaMaterias.materias) {
            val deLaMateria = tareas.filter { it.idMateria == materia.id }
            if (deLaMateria.isNotEmpty()) {
                agregarTarjeta(materia.nombre, deLaMateria)
            }
        }

        val sinMateria = tareas.filter { it.nombreMateria() == "" }
        if (sinMateria.isNotEmpty()) {
            agregarTarjeta("Sin materia", sinMateria)
        }
    }

    fun agregarTarjeta(nombre: String, tareas: List<Tarea>) {
        val vista = LayoutInflater.from(this).inflate(R.layout.item_progreso, contProgreso, false)

        val total = tareas.size
        val entregadas = tareas.count { it.completada }
        val vencidas = tareas.count { it.vencida() }
        val porcentaje = entregadas * 100 / total

        vista.findViewById<TextView>(R.id.txtMateria).text = nombre
        vista.findViewById<TextView>(R.id.txtPorcentaje).text = "$porcentaje%"
        vista.findViewById<ProgressBar>(R.id.barraProgreso).progress = porcentaje

        var detalle = "$entregadas de $total entregadas"
        if (vencidas > 0) detalle += " · $vencidas vencidas"
        vista.findViewById<TextView>(R.id.txtDetalle).text = detalle

        contProgreso.addView(vista)
    }
}
