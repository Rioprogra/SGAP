package com.sgap.app

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import androidx.activity.SystemBarStyle
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
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
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
                btnGuardar.isEnabled = false
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
                btnGuardar.isEnabled = false
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
                if (grupo != null) {
                    enviarCorreo(grupo, tarea)
                }
                finish()
            }
        }

        val btnSugerirIA = findViewById<Button>(R.id.btnSugerirIA)
        val btnPrioridadIA = findViewById<Button>(R.id.btnPrioridadIA)
        val btnPlanIA = findViewById<Button>(R.id.btnPlanIA)
        val btnUsarResultado = findViewById<Button>(R.id.btnUsarResultado)
        val txtResultadoIA = findViewById<TextView>(R.id.txtResultadoIA)
        val progresoIA = findViewById<ProgressBar>(R.id.progresoIA)
        val botonesIA = listOf(btnSugerirIA, btnPrioridadIA, btnPlanIA)

        fun usarIA(accion: String) {
            val titulo = txtTitulo.text.toString().trim()
            if (titulo.isEmpty()) {
                txtTitulo.error = "Escribe primero el título"
                txtTitulo.requestFocus()
                return
            }
            val elegida = fecha
            val textoFecha = if (elegida != null) Fechas.corta(elegida) + " ${elegida.get(Calendar.YEAR)}" else "sin fecha"
            for (b in botonesIA) b.isEnabled = false
            progresoIA.visibility = View.VISIBLE
            btnUsarResultado.visibility = View.GONE
            txtResultadoIA.visibility = View.VISIBLE
            txtResultadoIA.text = "Gemini está pensando..."
            lifecycleScope.launch {
                try {
                    when (accion) {
                        "descripcion" -> {
                            val descripcion = AsistenteIA.sugerirDescripcion(titulo, tipo)
                            txtDescripcion.setText(descripcion)
                            txtResultadoIA.text = "✓ Listo, agregué la descripción arriba."
                        }
                        "prioridad" -> {
                            txtResultadoIA.text = AsistenteIA.sugerirPrioridad(titulo, tipo, textoFecha)
                            btnUsarResultado.visibility = View.VISIBLE
                        }
                        else -> {
                            txtResultadoIA.text = AsistenteIA.planDeEstudio(titulo, tipo, textoFecha)
                            btnUsarResultado.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    txtResultadoIA.text = "⚠️ " + AsistenteIA.mensajeError(e)
                }
                for (b in botonesIA) b.isEnabled = true
                progresoIA.visibility = View.GONE
            }
        }

        btnSugerirIA.setOnClickListener { usarIA("descripcion") }
        btnPrioridadIA.setOnClickListener { usarIA("prioridad") }
        btnPlanIA.setOnClickListener { usarIA("plan") }
        btnUsarResultado.setOnClickListener {
            val actual = txtDescripcion.text.toString().trim()
            val nuevo = txtResultadoIA.text.toString()
            val combinado = if (actual.isEmpty()) nuevo else actual + "\n\n" + nuevo
            txtDescripcion.setText(combinado.take(300))
            btnUsarResultado.visibility = View.GONE
            Toast.makeText(this, "Agregado a la descripción", Toast.LENGTH_SHORT).show()
        }

        btnRegresar.setOnClickListener {
            finish()
        }
    }

    fun enviarCorreo(grupo: EquipoGrupo, tarea: Tarea) {
        val destinatarios = mutableListOf<String>()
        for (correo in grupo.correos) {
            if (correo != Usuario.correo.lowercase() && !destinatarios.contains(correo)) {
                destinatarios.add(correo)
            }
        }
        if (destinatarios.isEmpty()) {
            Toast.makeText(this, "Nadie más del grupo tiene correo registrado todavía", Toast.LENGTH_LONG).show()
            return
        }

        var cuerpo = "Hola, se publicó una nueva actividad en el grupo ${grupo.nombre}.\n\n"
        cuerpo += "${tarea.tipo}: ${tarea.titulo}\n"
        cuerpo += "Entrega: ${tarea.fecha()}\n"
        if (tarea.nombreMateria() != "") cuerpo += "Materia: ${tarea.nombreMateria()}\n"
        if (tarea.descripcion != "") cuerpo += "\n${tarea.descripcion}\n"
        cuerpo += "\nÁbrela en StudyGest para ver los detalles."

        val correo = Intent(Intent.ACTION_SEND)
        correo.putExtra(Intent.EXTRA_EMAIL, destinatarios.toTypedArray())
        correo.putExtra(Intent.EXTRA_SUBJECT, "Nueva ${tarea.tipo.lowercase()} en ${grupo.nombre}: ${tarea.titulo}")
        correo.putExtra(Intent.EXTRA_TEXT, cuerpo)
        correo.selector = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))

        try {
            startActivity(Intent.createChooser(correo, "Avisar por correo al grupo"))
        } catch (e: Exception) {
            Toast.makeText(this, "No hay una app de correo instalada", Toast.LENGTH_LONG).show()
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

        val idMateriaInicial = intent.getStringExtra("idMateria")
        if (tareaEditando == null && idMateriaInicial != null) {
            for (i in materias.indices) {
                if (materias[i].id == idMateriaInicial) spMateria.setSelection(i + 1)
            }
        }

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
