package com.sgap.app

import android.app.DatePickerDialog
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Calendar

// Pantalla para agregar una tarea, examen o proyecto
class NuevaTarea : AppCompatActivity() {

    lateinit var opTarea: TextView
    lateinit var opExamen: TextView
    lateinit var opProyecto: TextView

    // Como en el Figma, "Tarea" viene elegido desde el inicio
    var tipo = "Tarea"

    // Fecha elegida en el calendario (null = todavia no elige)
    var fecha: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nueva_tarea)

        val btnRegresar = findViewById<TextView>(R.id.btnRegresar)
        val txtTitulo = findViewById<EditText>(R.id.txtTitulo)
        val txtFecha = findViewById<TextView>(R.id.txtFecha)
        val txtDescripcion = findViewById<EditText>(R.id.txtDescripcion)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        opTarea = findViewById(R.id.opTarea)
        opExamen = findViewById(R.id.opExamen)
        opProyecto = findViewById(R.id.opProyecto)

        opTarea.setOnClickListener { elegirTipo("Tarea") }
        opExamen.setOnClickListener { elegirTipo("Examen") }
        opProyecto.setOnClickListener { elegirTipo("Proyecto") }
        pintarOpciones()

        // Abre el calendario para elegir la fecha de entrega
        txtFecha.setOnClickListener {
            val hoy = Calendar.getInstance()
            val dialogo = DatePickerDialog(
                this,
                { _, anio, mes, dia ->
                    val elegida = Calendar.getInstance()
                    elegida.set(anio, mes, dia)
                    fecha = elegida
                    txtFecha.text = Fechas.corta(elegida) + " $anio"
                },
                hoy.get(Calendar.YEAR),
                hoy.get(Calendar.MONTH),
                hoy.get(Calendar.DAY_OF_MONTH)
            )
            // No se puede elegir un dia que ya paso
            dialogo.datePicker.minDate = hoy.timeInMillis
            dialogo.show()
        }

        btnGuardar.setOnClickListener {
            val titulo = txtTitulo.text.toString().trim()
            val descripcion = txtDescripcion.text.toString().trim()
            val fechaElegida = fecha

            if (titulo.isEmpty()) {
                Toast.makeText(this, "Escribe el título", Toast.LENGTH_SHORT).show()
            } else if (fechaElegida == null) {
                Toast.makeText(this, "Selecciona la fecha de entrega", Toast.LENGTH_SHORT).show()
            } else {
                ListaTareas.tareas.add(
                    Tarea(titulo, tipo, Fechas.corta(fechaElegida), fechaElegida.timeInMillis, descripcion)
                )
                Toast.makeText(this, "Se guardó: $titulo", Toast.LENGTH_SHORT).show()
                finish() // regresa a la pantalla anterior
            }
        }

        btnRegresar.setOnClickListener {
            finish()
        }
    }

    fun elegirTipo(nuevoTipo: String) {
        tipo = nuevoTipo
        pintarOpciones()
    }

    // La opcion elegida se pinta azul y las demas blancas
    fun pintarOpciones() {
        pintarOpcion(opTarea, tipo == "Tarea")
        pintarOpcion(opExamen, tipo == "Examen")
        pintarOpcion(opProyecto, tipo == "Proyecto")
    }

    fun pintarOpcion(opcion: TextView, elegida: Boolean) {
        if (elegida) {
            opcion.setBackgroundResource(R.drawable.bg_opcion_activa)
            opcion.setTextColor(ContextCompat.getColor(this, R.color.sobre_primario))
            opcion.setTypeface(null, Typeface.BOLD)
        } else {
            opcion.setBackgroundResource(R.drawable.bg_opcion)
            opcion.setTextColor(ContextCompat.getColor(this, R.color.texto_suave))
            opcion.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
    }
}
