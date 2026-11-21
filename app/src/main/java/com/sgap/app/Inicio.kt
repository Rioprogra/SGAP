package com.sgap.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Inicio : AppCompatActivity() {

    lateinit var lvTareas: ListView
    lateinit var txtVacio: TextView
    lateinit var txtResumen: TextView
    lateinit var puntoAviso: View
    lateinit var chips: List<TextView>

    var filtro = "Por entregar"

    var tareasFiltradas = mutableListOf<Tarea>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_inicio)

        val txtFecha = findViewById<TextView>(R.id.txtFecha)
        val btnNotificaciones = findViewById<FrameLayout>(R.id.btnNotificaciones)
        val btnAgregar = findViewById<TextView>(R.id.btnAgregar)
        lvTareas = findViewById(R.id.lvTareas)
        txtVacio = findViewById(R.id.txtVacio)
        txtResumen = findViewById(R.id.txtResumen)
        puntoAviso = findViewById(R.id.puntoAviso)
        chips = listOf(
            findViewById(R.id.chipPendientes), findViewById(R.id.chipSemana),
            findViewById(R.id.chipVencidas), findViewById(R.id.chipEntregadas)
        )

        val formato = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "MX"))
        txtFecha.text = formato.format(Date()).replaceFirstChar { it.uppercase() }

        Navegacion.configurar(this, R.id.navInicio)

        val nombre = Usuario.nombre.split(" ").firstOrNull().orEmpty()
        if (nombre.isNotBlank()) {
            findViewById<TextView>(R.id.txtSaludo).text = "Hola, ${nombre.replaceFirstChar { it.uppercase() }} 👋"
        }

        for (chip in chips) {
            chip.setOnClickListener { cambiarFiltro(chip.text.toString()) }
        }

        lvTareas.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, DetalleTarea::class.java)
            intent.putExtra("idTarea", tareasFiltradas[position].id)
            startActivity(intent)
        }

        btnNotificaciones.setOnClickListener {
            startActivity(Intent(this, Pendientes::class.java))
        }

        btnAgregar.setOnClickListener {
            startActivity(Intent(this, NuevaTarea::class.java))
        }

        pintarChips()

        AvisosNuevos.programar(this)
        AvisosNuevos.revisarAhora(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        mostrarTareas()

        pintarCampana()
        BaseDatos.cargarNotificaciones { pintarCampana() }

        BaseDatos.cargarMaterias {
            BaseDatos.cargarTareas(this) { error ->
                if (error != null) {
                    Toast.makeText(this, "No se pudieron cargar las tareas", Toast.LENGTH_SHORT).show()
                }
                mostrarTareas()
                Recordatorios.programarTodas(this)
                BaseDatos.cargarHorario { AvisoClases.programarTodas(this) }
            }
        }
    }

    fun pintarCampana() {
        if (ListaNotificaciones.hayNoLeidas()) {
            puntoAviso.visibility = View.VISIBLE
        } else {
            puntoAviso.visibility = View.GONE
        }
    }

    fun cambiarFiltro(nuevoFiltro: String) {
        filtro = nuevoFiltro
        pintarChips()
        mostrarTareas()
    }

    fun pasaFiltro(tarea: Tarea): Boolean {
        return when (filtro) {
            "Esta semana" -> !tarea.completada && tarea.diasRestantes() in 0..6
            "Vencidas" -> tarea.vencida()
            "Entregadas" -> tarea.completada
            else -> !tarea.completada
        }
    }

    fun mostrarTareas() {
        tareasFiltradas.clear()
        val ordenadas = ListaTareas.tareas.sortedBy { it.fechaEntrega }
        for (tarea in ordenadas) {
            if (pasaFiltro(tarea)) tareasFiltradas.add(tarea)
        }
        if (filtro == "Entregadas") tareasFiltradas.reverse()

        lvTareas.adapter = TareaAdapter(this, tareasFiltradas) { tarea ->
            tarea.completada = !tarea.completada
            BaseDatos.cambiarCompletada(tarea)
            Recordatorios.programar(this, tarea)
            if (tarea.completada) {
                Toast.makeText(this, "¡Bien! Marcaste \"${tarea.titulo}\" como entregada", Toast.LENGTH_SHORT).show()
            }
            mostrarTareas()
        }

        val pendientes = ListaTareas.pendientes()
        if (pendientes == 1) {
            txtResumen.text = "1 por entregar"
        } else {
            txtResumen.text = "$pendientes por entregar"
        }

        if (tareasFiltradas.isEmpty()) {
            txtVacio.text = when {
                ListaTareas.tareas.isEmpty() -> "Aún no tienes tareas.\nToca \"Nueva tarea\" para agregar una"
                filtro == "Esta semana" -> "Nada que entregar esta semana 🎉"
                filtro == "Vencidas" -> "No tienes tareas vencidas 👏"
                filtro == "Entregadas" -> "Todavía no has entregado ninguna"
                else -> "No tienes tareas por entregar 🎉"
            }
            txtVacio.visibility = View.VISIBLE
        } else {
            txtVacio.visibility = View.GONE
        }
    }

    fun pintarChips() {
        for (chip in chips) {
            if (chip.id == R.id.chipVencidas) {
                Chips.pintar(this, chip, filtro == chip.text.toString(), R.color.error)
            } else {
                Chips.pintar(this, chip, filtro == chip.text.toString())
            }
        }
    }
}
