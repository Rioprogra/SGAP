package com.sgap.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

// Pantalla para agregar una tarea, examen o proyecto
class NuevaTarea : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nueva_tarea)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnRegresar = findViewById<Button>(R.id.btnRegresar)
        val txtTitulo = findViewById<EditText>(R.id.txtTitulo)
        val rgTipo = findViewById<RadioGroup>(R.id.rgTipo)
        val btnFecha = findViewById<Button>(R.id.btnFecha)
        val txtDescripcion = findViewById<EditText>(R.id.txtDescripcion)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        var fecha = ""

        // Abre el calendario para elegir la fecha de entrega
        btnFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            val dialogo = DatePickerDialog(
                this,
                { _, anio, mes, dia ->
                    fecha = "$dia/${mes + 1}/$anio" // los meses empiezan en 0
                    btnFecha.text = fecha
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            )
            dialogo.show()
        }

        btnGuardar.setOnClickListener {
            val titulo = txtTitulo.text.toString().trim()
            val descripcion = txtDescripcion.text.toString().trim()

            if (titulo.isEmpty()) {
                Toast.makeText(this, "Escribe el titulo", Toast.LENGTH_SHORT).show()
            } else if (rgTipo.checkedRadioButtonId == -1) {
                Toast.makeText(this, "Selecciona el tipo", Toast.LENGTH_SHORT).show()
            } else if (fecha.isEmpty()) {
                Toast.makeText(this, "Selecciona la fecha de entrega", Toast.LENGTH_SHORT).show()
            } else {
                val rbTipo = findViewById<RadioButton>(rgTipo.checkedRadioButtonId)
                val tipo = rbTipo.text.toString()

                ListaTareas.tareas.add(Tarea(titulo, tipo, fecha, descripcion))
                Toast.makeText(this, "Tarea guardada", Toast.LENGTH_SHORT).show()
                finish() // regresa a Inicio
            }
        }

        btnRegresar.setOnClickListener {
            finish()
        }
    }
}
