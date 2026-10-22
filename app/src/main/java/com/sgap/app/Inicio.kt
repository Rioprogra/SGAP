package com.sgap.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Pantalla principal con la lista de pendientes
class Inicio : AppCompatActivity() {

    lateinit var lvTareas: ListView
    lateinit var spFiltro: Spinner
    lateinit var txtVacio: TextView

    // Las tareas que se ven en la lista segun el filtro
    var tareasFiltradas = mutableListOf<Tarea>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)

        val txtFecha = findViewById<TextView>(R.id.txtFecha)
        val btnNueva = findViewById<Button>(R.id.btnNueva)
        val btnSalir = findViewById<Button>(R.id.btnSalir)
        lvTareas = findViewById(R.id.lvTareas)
        spFiltro = findViewById(R.id.spFiltro)
        txtVacio = findViewById(R.id.txtVacio)

        // Fecha de hoy arriba
        val formato = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "MX"))
        txtFecha.text = formato.format(Date())

        // Spinner para filtrar por tipo
        val filtros = listOf("Todos", "Tarea", "Examen", "Proyecto")
        val adaptadorFiltro = ArrayAdapter(this, android.R.layout.simple_spinner_item, filtros)
        adaptadorFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFiltro.adapter = adaptadorFiltro

        spFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mostrarTareas()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // Al tocar una tarea se marca o desmarca como completada
        lvTareas.setOnItemClickListener { _, _, position, _ ->
            val tarea = tareasFiltradas[position]
            tarea.completada = !tarea.completada
        }

        btnNueva.setOnClickListener {
            val intent = Intent(this, NuevaTarea::class.java)
            startActivity(intent)
        }

        btnSalir.setOnClickListener {
            finish() // regresa al login
        }
    }

    // Cuando regresamos de NuevaTarea se vuelve a cargar la lista
    override fun onResume() {
        super.onResume()
        mostrarTareas()
    }

    fun mostrarTareas() {
        val filtro = spFiltro.selectedItem.toString()

        tareasFiltradas.clear()
        for (tarea in ListaTareas.tareas) {
            if (filtro == "Todos" || tarea.tipo == filtro) {
                tareasFiltradas.add(tarea)
            }
        }

        // Texto que se ve en cada renglon
        val textos = mutableListOf<String>()
        for (tarea in tareasFiltradas) {
            textos.add("${tarea.tipo}: ${tarea.titulo}\nEntrega: ${tarea.fecha}")
        }

        // simple_list_item_multiple_choice ya trae un CheckBox en cada renglon
        val adaptador = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, textos)
        lvTareas.adapter = adaptador

        // Marcar los checkbox de las que ya estan completadas
        for (i in tareasFiltradas.indices) {
            lvTareas.setItemChecked(i, tareasFiltradas[i].completada)
        }

        if (tareasFiltradas.isEmpty()) {
            txtVacio.visibility = View.VISIBLE
        } else {
            txtVacio.visibility = View.GONE
        }
    }
}
