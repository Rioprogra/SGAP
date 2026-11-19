package com.sgap.app

import android.app.DatePickerDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar

class NuevaTarea : AppCompatActivity() {

    lateinit var opTarea: TextView
    lateinit var opExamen: TextView
    lateinit var opProyecto: TextView

    var tipo = "Tarea"

    var fecha: Calendar? = null

    lateinit var spPara: Spinner
    var gruposParaPublicar = listOf<EquipoGrupo>()

    lateinit var spMateria: Spinner
    var materias = listOf<Materia>()

    var tareaEditando: Tarea? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nueva_tarea)

        val btnRegresar = findViewById<TextView>(R.id.btnRegresar)
        val txtTitulo = findViewById<EditText>(R.id.txtTitulo)
        val txtFecha = findViewById<TextView>(R.id.txtFecha)
        val txtDescripcion = findViewById<EditText>(R.id.txtDescripcion)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        spPara = findViewById(R.id.spPara)
        spMateria = findViewById(R.id.spMateria)
        opTarea = findViewById(R.id.opTarea)
        opExamen = findViewById(R.id.opExamen)
        opProyecto = findViewById(R.id.opProyecto)

        opTarea.setOnClickListener { elegirTipo("Tarea") }
        opExamen.setOnClickListener { elegirTipo("Examen") }
        opProyecto.setOnClickListener { elegirTipo("Proyecto") }

        val idTarea = intent.getStringExtra("idTarea")
        for (t in ListaTareas.tareas) {
            if (t.id == idTarea) tareaEditando = t
        }
        val editando = tareaEditando
        if (editando != null) {
            findViewById<TextView>(R.id.txtTituloPantalla).text = "Editar tarea"
            btnGuardar.text = "Guardar cambios"
            txtTitulo.setText(editando.titulo)
            txtDescripcion.setText(editando.descripcion)
            tipo = editando.tipo
            val calendario = Calendar.getInstance()
            calendario.timeInMillis = editando.fechaEntrega
            fecha = calendario
            txtFecha.text = Fechas.corta(calendario) + " ${calendario.get(Calendar.YEAR)}"

            findViewById<TextView>(R.id.lblPara).visibility = View.GONE
            spPara.visibility = View.GONE

            val btnEliminar = findViewById<TextView>(R.id.btnEliminar)
            btnEliminar.visibility = View.VISIBLE
            btnEliminar.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Seguro que quieres eliminar \"${editando.titulo}\"?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        BaseDatos.eliminarTarea(editando)
                        Recordatorios.cancelar(this, editando)
                        Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
        pintarOpciones()

        llenarPara()
        BaseDatos.cargarGrupos { llenarPara() }

        llenarMaterias()
        BaseDatos.cargarMaterias { lista ->
            materias = lista
            llenarMaterias()
        }

        txtFecha.setOnClickListener {
            val hoy = Calendar.getInstance()
            val inicial = fecha ?: hoy
            val dialogo = DatePickerDialog(
                this,
                { _, anio, mes, dia ->
                    val elegida = Calendar.getInstance()
                    elegida.set(anio, mes, dia)
                    fecha = elegida
                    txtFecha.text = Fechas.corta(elegida) + " $anio"
                },
                inicial.get(Calendar.YEAR),
                inicial.get(Calendar.MONTH),
                inicial.get(Calendar.DAY_OF_MONTH)
            )
            dialogo.datePicker.minDate = hoy.timeInMillis
            dialogo.show()
        }

        btnGuardar.setOnClickListener {
            val titulo = txtTitulo.text.toString().trim()
            val descripcion = txtDescripcion.text.toString().trim()
            val fechaElegida = fecha

            if (titulo.isEmpty()) {
                txtTitulo.error = "Escribe el título"
                txtTitulo.requestFocus()
            } else if (titulo.length < 3) {
                txtTitulo.error = "El título debe tener al menos 3 letras"
                txtTitulo.requestFocus()
            } else if (fechaElegida == null) {
                Toast.makeText(this, "Selecciona la fecha de entrega", Toast.LENGTH_SHORT).show()
            } else if (tareaEditando != null) {
                val tarea = tareaEditando!!
                tarea.titulo = titulo
                tarea.tipo = tipo
                tarea.descripcion = descripcion
                tarea.fechaEntrega = fechaElegida.timeInMillis
                tarea.idMateria = materiaElegida()
                BaseDatos.actualizarTarea(tarea)
                Recordatorios.programar(this, tarea)
                Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                val posicion = spPara.selectedItemPosition
                var grupo: EquipoGrupo? = null
                if (posicion > 0) {
                    grupo = gruposParaPublicar[posicion - 1]
                }

                val tarea = Tarea(titulo, tipo, fechaElegida.timeInMillis, descripcion)
                if (grupo != null) {
                    tarea.origen = "GRUPO"
                    tarea.idGrupo = grupo.id
                }
                tarea.idMateria = materiaElegida()
                BaseDatos.guardarTarea(tarea, grupo)
                Recordatorios.programar(this, tarea)
                Toast.makeText(this, "Se guardó: $titulo", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val btnSugerirIA = findViewById<TextView>(R.id.btnSugerirIA)
        btnSugerirIA.setOnClickListener {
            val titulo = txtTitulo.text.toString().trim()
            if (titulo.isEmpty()) {
                Toast.makeText(this, "Escribe primero el título", Toast.LENGTH_SHORT).show()
            } else {
                btnSugerirIA.isEnabled = false
                btnSugerirIA.text = "Pensando..."
                lifecycleScope.launch {
                    try {
                        txtDescripcion.setText(AsistenteIA.sugerirDescripcion(titulo, tipo))
                    } catch (e: Exception) {
                        Toast.makeText(this@NuevaTarea, "No se pudo usar la IA: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    btnSugerirIA.isEnabled = true
                    btnSugerirIA.text = "✨ Sugerir descripción con IA"
                }
            }
        }

        btnRegresar.setOnClickListener {
            finish()
        }
    }

    fun llenarPara() {
        gruposParaPublicar = ListaGrupos.dondePuedoPublicar()
        val opciones = mutableListOf("Solo para mí")
        for (grupo in gruposParaPublicar) {
            opciones.add("Grupo: ${grupo.nombre}")
        }
        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spPara.adapter = adaptador
    }

    fun llenarMaterias() {
        val opciones = mutableListOf("Sin materia")
        for (materia in materias) {
            opciones.add(materia.nombre)
        }
        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spMateria.adapter = adaptador

        val editando = tareaEditando
        if (editando != null) {
            for (i in materias.indices) {
                if (materias[i].id == editando.idMateria) spMateria.setSelection(i + 1)
            }
        }
    }

    fun materiaElegida(): String? {
        val posicion = spMateria.selectedItemPosition
        if (posicion > 0) {
            return materias[posicion - 1].id
        }
        return null
    }

    fun elegirTipo(nuevoTipo: String) {
        tipo = nuevoTipo
        pintarOpciones()
    }

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
