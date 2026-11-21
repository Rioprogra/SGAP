package com.sgap.app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class DetalleMateria : AppCompatActivity() {

    var idMateria = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_detalle_materia)

        idMateria = intent.getStringExtra("idMateria") ?: ""
        findViewById<TextView>(R.id.btnRegresar).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnNuevaTarea).setOnClickListener {
            val intent = Intent(this, NuevaTarea::class.java)
            intent.putExtra("idMateria", idMateria)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        mostrar()
        BaseDatos.cargarHorario {
            BaseDatos.cargarTareas(this) { mostrar() }
        }
    }

    fun mostrar() {
        val materia = ListaMaterias.buscar(idMateria) ?: return

        findViewById<TextView>(R.id.txtTitulo).text = materia.abreviatura.ifBlank { materia.nombre }
        findViewById<TextView>(R.id.txtNombre).text = materia.nombre
        findViewById<TextView>(R.id.txtProfesor).text = "👤 ${materia.profesor.ifBlank { "Profesor sin registrar" }}"
        findViewById<TextView>(R.id.txtHoras).text = "${materia.horasSemana} horas por semana"
        findViewById<View>(R.id.barraColor).backgroundTintList = ColorStateList.valueOf(materia.colorEntero())

        val contHorario = findViewById<LinearLayout>(R.id.contHorario)
        contHorario.removeAllViews()
        val orden = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
        val clases = ListaHorario.clases.filter { it.idMateria == idMateria }
            .sortedWith(compareBy({ orden.indexOf(it.dia) }, { it.inicio }))
        for (clase in clases) {
            val vista = LayoutInflater.from(this).inflate(R.layout.item_materia, contHorario, false)
            vista.findViewById<TextView>(R.id.txtNombre).text = "${nombreDia(clase.dia)} · ${clase.inicio} a ${clase.fin}"
            vista.findViewById<TextView>(R.id.txtDetalle).text = "Aula ${clase.salon}"
            vista.findViewById<View>(R.id.barraColor).backgroundTintList = ColorStateList.valueOf(materia.colorEntero())
            contHorario.addView(vista)
        }

        val contTareas = findViewById<LinearLayout>(R.id.contTareas)
        contTareas.removeAllViews()
        val tareas = ListaTareas.tareas.filter { it.idMateria == idMateria }
            .sortedWith(compareBy({ it.completada }, { it.fechaEntrega }))
        val adaptador = TareaAdapter(this, tareas) { tarea ->
            tarea.completada = !tarea.completada
            BaseDatos.cambiarCompletada(tarea)
            Recordatorios.programar(this, tarea)
            mostrar()
        }
        for (i in tareas.indices) {
            val vista = adaptador.getView(i, null, contTareas)
            val margen = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            margen.topMargin = (10 * resources.displayMetrics.density).toInt()
            vista.layoutParams = margen
            vista.setOnClickListener {
                val intent = Intent(this, DetalleTarea::class.java)
                intent.putExtra("idTarea", tareas[i].id)
                startActivity(intent)
            }
            contTareas.addView(vista)
        }
        findViewById<TextView>(R.id.txtSinTareas).visibility = if (tareas.isEmpty()) View.VISIBLE else View.GONE
    }

    fun nombreDia(dia: String): String {
        return when (dia) {
            "Lun" -> "Lunes"
            "Mar" -> "Martes"
            "Mié" -> "Miércoles"
            "Jue" -> "Jueves"
            "Vie" -> "Viernes"
            "Sáb" -> "Sábado"
            else -> dia
        }
    }
}
