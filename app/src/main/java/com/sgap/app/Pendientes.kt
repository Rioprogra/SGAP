package com.sgap.app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class Pendientes : AppCompatActivity() {

    lateinit var contNotificaciones: LinearLayout
    lateinit var contPendientes: LinearLayout
    lateinit var txtVerTodo: TextView
    lateinit var txtSinPendientes: TextView

    var verTodo = false

    var filtro = "Todas"
    lateinit var chips: List<TextView>

    lateinit var spFiltroMateria: Spinner
    var idMateriaFiltro: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_pendientes)

        val btnRegresar = findViewById<TextView>(R.id.btnRegresar)
        val txtMarcarLeidas = findViewById<TextView>(R.id.txtMarcarLeidas)
        val btnNuevaTarea = findViewById<TextView>(R.id.btnNuevaTarea)
        contNotificaciones = findViewById(R.id.contNotificaciones)
        contPendientes = findViewById(R.id.contPendientes)
        txtVerTodo = findViewById(R.id.txtVerTodo)
        txtSinPendientes = findViewById(R.id.txtSinPendientes)

        btnRegresar.setOnClickListener { finish() }

        txtMarcarLeidas.setOnClickListener {
            BaseDatos.marcarTodasLeidas()
            mostrarNotificaciones()
        }

        txtVerTodo.setOnClickListener {
            verTodo = !verTodo
            mostrarPendientes()
        }

        btnNuevaTarea.setOnClickListener {
            startActivity(Intent(this, NuevaTarea::class.java))
        }

        chips = listOf(
            findViewById(R.id.chipTodas), findViewById(R.id.chipTarea),
            findViewById(R.id.chipExamen), findViewById(R.id.chipProyecto),
            findViewById(R.id.chipVencidas), findViewById(R.id.chipEntregadas)
        )
        for (chip in chips) {
            chip.setOnClickListener {
                filtro = chip.text.toString()
                pintarChips()
                mostrarPendientes()
            }
        }
        pintarChips()

        spFiltroMateria = findViewById(R.id.spFiltroMateria)
        llenarFiltroMateria()
        spFiltroMateria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    idMateriaFiltro = null
                } else {
                    idMateriaFiltro = ListaMaterias.materias[position - 1].id
                }
                mostrarPendientes()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        BaseDatos.cargarMaterias {
            llenarFiltroMateria()
            mostrarPendientes()
        }
    }

    fun llenarFiltroMateria() {
        val opciones = mutableListOf("Todas las materias")
        for (materia in ListaMaterias.materias) {
            opciones.add(materia.nombre)
        }
        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFiltroMateria.adapter = adaptador
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

    fun pasaFiltro(tarea: Tarea): Boolean {
        if (idMateriaFiltro != null && tarea.idMateria != idMateriaFiltro) return false
        return when (filtro) {
            "Vencidas" -> tarea.vencida()
            "Entregadas" -> tarea.completada
            "Tarea", "Examen", "Proyecto" -> tarea.tipo == filtro && (verTodo || !tarea.completada)
            else -> verTodo || !tarea.completada
        }
    }

    override fun onResume() {
        super.onResume()
        mostrarNotificaciones()
        mostrarPendientes()

        BaseDatos.cargarNotificaciones { mostrarNotificaciones() }

        BaseDatos.cargarTareas(this) { error ->
            if (error != null) {
                Toast.makeText(this, "No se pudieron cargar las tareas, revisa tu conexión", Toast.LENGTH_LONG).show()
            }
            mostrarPendientes()
            Recordatorios.programarTodas(this)
            if (BaseDatos.sinConexion) {
                Toast.makeText(this, "Sin conexión: se muestran tus datos guardados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun mostrarNotificaciones() {
        contNotificaciones.removeAllViews()

        for (notificacion in ListaNotificaciones.notificaciones) {
            val vista = LayoutInflater.from(this).inflate(R.layout.item_notificacion, contNotificaciones, false)

            val txtIcono = vista.findViewById<TextView>(R.id.txtIcono)
            val txtTexto = vista.findViewById<TextView>(R.id.txtTexto)
            val txtHace = vista.findViewById<TextView>(R.id.txtHace)
            val puntoNoLeida = vista.findViewById<View>(R.id.puntoNoLeida)

            txtIcono.text = notificacion.icono()
            txtIcono.setTextColor(color(notificacion.colorIcono()))
            txtIcono.backgroundTintList = ColorStateList.valueOf(color(notificacion.fondoIcono()))
            txtTexto.text = "${notificacion.titulo}: ${notificacion.mensaje}"
            txtHace.text = notificacion.hace()

            if (notificacion.leida) {
                puntoNoLeida.visibility = View.INVISIBLE
            } else {
                puntoNoLeida.visibility = View.VISIBLE
            }

            vista.setOnClickListener {
                BaseDatos.marcarLeida(notificacion)
                puntoNoLeida.visibility = View.INVISIBLE
                val idTarea = notificacion.idTarea
                if (idTarea != null) {
                    abrirTarea(idTarea)
                }
            }

            contNotificaciones.addView(vista)
        }

        val vacio = ListaNotificaciones.notificaciones.isEmpty()
        findViewById<TextView>(R.id.txtSinNotificaciones).visibility = if (vacio) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.txtMarcarLeidas).visibility = if (ListaNotificaciones.hayNoLeidas()) View.VISIBLE else View.GONE
    }

    fun mostrarPendientes() {
        contPendientes.removeAllViews()

        val ordenadas = ListaTareas.tareas.sortedWith(compareBy({ it.completada }, { it.fechaEntrega }))

        val sinFiltros = filtro == "Todas" && idMateriaFiltro == null
        var mostradas = 0
        for (tarea in ordenadas) {
            if (!pasaFiltro(tarea)) continue
            if (sinFiltros && !verTodo && mostradas == 3) continue
            contPendientes.addView(crearTarjeta(tarea))
            mostradas++
        }

        if (verTodo) {
            txtVerTodo.text = "Ver menos"
        } else {
            txtVerTodo.text = "Ver todo"
        }

        if (mostradas == 0) {
            if (ListaTareas.tareas.isEmpty()) {
                txtSinPendientes.text = "Aún no tienes tareas. Agrega una con \"+ Nueva tarea\""
            } else if (sinFiltros) {
                txtSinPendientes.text = "No tienes nada por entregar 🎉"
            } else {
                txtSinPendientes.text = "No hay tareas con ese filtro"
            }
            txtSinPendientes.visibility = View.VISIBLE
        } else {
            txtSinPendientes.visibility = View.GONE
        }
    }

    fun crearTarjeta(tarea: Tarea): View {
        val vista = LayoutInflater.from(this).inflate(R.layout.item_pendiente, contPendientes, false)

        val barraTipo = vista.findViewById<View>(R.id.barraTipo)
        val txtTitulo = vista.findViewById<TextView>(R.id.txtTitulo)
        val txtTipo = vista.findViewById<TextView>(R.id.txtTipo)
        val txtFecha = vista.findViewById<TextView>(R.id.txtFecha)
        val imgCheck = vista.findViewById<ImageView>(R.id.imgCheck)

        txtTitulo.text = tarea.titulo
        txtTipo.text = tarea.tipo
        txtFecha.text = tarea.detalle()
        if (tarea.vencida()) {
            txtFecha.setTextColor(color(R.color.error))
        } else if (!tarea.completada && tarea.diasRestantes() <= 1) {
            txtFecha.setTextColor(color(R.color.acento_coral))
        }

        vista.setOnClickListener { abrirTarea(tarea.id) }

        barraTipo.backgroundTintList = ColorStateList.valueOf(color(tarea.color()))
        txtTipo.setTextColor(color(tarea.color()))
        txtTipo.backgroundTintList = ColorStateList.valueOf(color(tarea.colorSuave()))

        if (tarea.completada) {
            imgCheck.setImageResource(R.drawable.ic_check_lleno)
            txtTitulo.paintFlags = txtTitulo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            vista.alpha = 0.6f
        } else {
            imgCheck.setImageResource(R.drawable.ic_check_vacio)
        }

        imgCheck.setOnClickListener {
            tarea.completada = !tarea.completada
            BaseDatos.cambiarCompletada(tarea)
            Recordatorios.programar(this, tarea)
            mostrarPendientes()
        }

        return vista
    }

    fun abrirTarea(idTarea: String) {
        val intent = Intent(this, DetalleTarea::class.java)
        intent.putExtra("idTarea", idTarea)
        startActivity(intent)
    }

    fun color(id: Int): Int {
        return ContextCompat.getColor(this, id)
    }
}
