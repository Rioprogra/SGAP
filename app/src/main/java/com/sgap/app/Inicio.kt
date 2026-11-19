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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Inicio : AppCompatActivity() {

    lateinit var lvCanales: ListView
    lateinit var txtVacio: TextView
    lateinit var puntoAviso: View
    lateinit var chipTodos: TextView
    lateinit var chipNoLeidos: TextView
    lateinit var chipMaterias: TextView
    lateinit var chipAvisos: TextView

    var filtro = "Todos"

    var canalesFiltrados = mutableListOf<Canal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)

        val txtFecha = findViewById<TextView>(R.id.txtFecha)
        val btnNotificaciones = findViewById<FrameLayout>(R.id.btnNotificaciones)
        val btnAgregar = findViewById<TextView>(R.id.btnAgregar)
        lvCanales = findViewById(R.id.lvCanales)
        txtVacio = findViewById(R.id.txtVacio)
        puntoAviso = findViewById(R.id.puntoAviso)
        chipTodos = findViewById(R.id.chipTodos)
        chipNoLeidos = findViewById(R.id.chipNoLeidos)
        chipMaterias = findViewById(R.id.chipMaterias)
        chipAvisos = findViewById(R.id.chipAvisos)

        val formato = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "MX"))
        txtFecha.text = formato.format(Date()).replaceFirstChar { it.uppercase() }

        Navegacion.configurar(this, R.id.navInicio)

        chipTodos.setOnClickListener { cambiarFiltro("Todos") }
        chipNoLeidos.setOnClickListener { cambiarFiltro("No leídos") }
        chipMaterias.setOnClickListener { cambiarFiltro("Materias") }
        chipAvisos.setOnClickListener { cambiarFiltro("Avisos") }

        lvCanales.setOnItemClickListener { _, _, position, _ ->
            val canal = canalesFiltrados[position]
            canal.noLeidos = 0
            Toast.makeText(this, "#${canal.nombre}: el chat viene pronto", Toast.LENGTH_SHORT).show()
            mostrarCanales()
        }

        btnNotificaciones.setOnClickListener {
            startActivity(Intent(this, Pendientes::class.java))
        }

        btnAgregar.setOnClickListener {
            startActivity(Intent(this, NuevaTarea::class.java))
        }

        pintarChips()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        mostrarCanales()

        pintarCampana()
        BaseDatos.cargarNotificaciones { pintarCampana() }

        BaseDatos.cargarTareas(this) { Recordatorios.programarTodas(this) }
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
        mostrarCanales()
    }

    fun mostrarCanales() {
        canalesFiltrados.clear()
        for (canal in ListaCanales.canales) {
            if (filtro == "Todos"
                || (filtro == "No leídos" && canal.noLeidos > 0)
                || (filtro == "Materias" && canal.tipo == "Materia")
                || (filtro == "Avisos" && canal.tipo == "Aviso")
            ) {
                canalesFiltrados.add(canal)
            }
        }

        lvCanales.adapter = CanalAdapter(this, canalesFiltrados)

        if (canalesFiltrados.isEmpty()) {
            txtVacio.visibility = View.VISIBLE
        } else {
            txtVacio.visibility = View.GONE
        }
    }

    fun pintarChips() {
        Chips.pintar(this, chipTodos, filtro == "Todos")
        Chips.pintar(this, chipNoLeidos, filtro == "No leídos", R.color.acento_rojo)
        Chips.pintar(this, chipMaterias, filtro == "Materias")
        Chips.pintar(this, chipAvisos, filtro == "Avisos")
    }
}
