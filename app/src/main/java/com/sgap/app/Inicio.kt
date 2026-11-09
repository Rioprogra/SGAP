package com.sgap.app

import android.content.Intent
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

// Pantalla principal: lista de canales del salon
class Inicio : AppCompatActivity() {

    lateinit var lvCanales: ListView
    lateinit var txtVacio: TextView
    lateinit var puntoAviso: View
    lateinit var chipTodos: TextView
    lateinit var chipNoLeidos: TextView
    lateinit var chipMaterias: TextView
    lateinit var chipAvisos: TextView

    // Filtro elegido: "Todos", "No leídos", "Materias" o "Avisos"
    var filtro = "Todos"

    // Los canales que se ven en la lista segun el filtro
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

        // Fecha de hoy arriba, con la primera letra en mayuscula
        val formato = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "MX"))
        txtFecha.text = formato.format(Date()).replaceFirstChar { it.uppercase() }

        // Barra de abajo
        Navegacion.configurar(this, R.id.navInicio)

        // Chips de filtro
        chipTodos.setOnClickListener { cambiarFiltro("Todos") }
        chipNoLeidos.setOnClickListener { cambiarFiltro("No leídos") }
        chipMaterias.setOnClickListener { cambiarFiltro("Materias") }
        chipAvisos.setOnClickListener { cambiarFiltro("Avisos") }

        // Al tocar un canal se marcan sus mensajes como leidos
        // (la pantalla del chat todavia no esta en el Figma)
        lvCanales.setOnItemClickListener { _, _, position, _ ->
            val canal = canalesFiltrados[position]
            canal.noLeidos = 0
            Toast.makeText(this, "#${canal.nombre}: el chat viene pronto", Toast.LENGTH_SHORT).show()
            mostrarCanales()
        }

        // La campana abre Pendientes (notificaciones y tareas por entregar)
        btnNotificaciones.setOnClickListener {
            startActivity(Intent(this, Pendientes::class.java))
        }

        // El "+" abre Nueva tarea
        btnAgregar.setOnClickListener {
            startActivity(Intent(this, NuevaTarea::class.java))
        }

        pintarChips()
    }

    // Cuando regresamos de otra pantalla se vuelve a cargar la lista
    override fun onResume() {
        super.onResume()
        mostrarCanales()

        // El puntito rojo de la campana solo sale si hay notificaciones sin leer
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

    // El chip elegido se pinta azul y los demas blancos
    fun pintarChips() {
        Chips.pintar(this, chipTodos, filtro == "Todos")
        Chips.pintar(this, chipNoLeidos, filtro == "No leídos", R.color.acento_rojo) // en el Figma va en rojo
        Chips.pintar(this, chipMaterias, filtro == "Materias")
        Chips.pintar(this, chipAvisos, filtro == "Avisos")
    }
}
