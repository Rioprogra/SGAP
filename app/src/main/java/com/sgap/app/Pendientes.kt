package com.sgap.app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

// Pantalla de notificaciones y tareas por entregar (se abre con la campana de Inicio)
class Pendientes : AppCompatActivity() {

    lateinit var contNotificaciones: LinearLayout
    lateinit var contPendientes: LinearLayout
    lateinit var txtVerTodo: TextView
    lateinit var txtSinPendientes: TextView

    // Al principio solo se ven 3 tareas. "Ver todo" muestra las demas y las entregadas
    var verTodo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
            for (notificacion in ListaNotificaciones.notificaciones) {
                notificacion.leida = true
            }
            mostrarNotificaciones()
        }

        txtVerTodo.setOnClickListener {
            verTodo = !verTodo
            mostrarPendientes()
        }

        btnNuevaTarea.setOnClickListener {
            startActivity(Intent(this, NuevaTarea::class.java))
        }
    }

    // Se recarga al regresar de Nueva tarea
    override fun onResume() {
        super.onResume()
        mostrarNotificaciones()
        mostrarPendientes()
    }

    fun mostrarNotificaciones() {
        // Borramos lo que habia y volvemos a crear una tarjeta por notificacion
        contNotificaciones.removeAllViews()

        for (notificacion in ListaNotificaciones.notificaciones) {
            val vista = LayoutInflater.from(this).inflate(R.layout.item_notificacion, contNotificaciones, false)

            val txtIcono = vista.findViewById<TextView>(R.id.txtIcono)
            val txtTexto = vista.findViewById<TextView>(R.id.txtTexto)
            val txtHace = vista.findViewById<TextView>(R.id.txtHace)
            val puntoNoLeida = vista.findViewById<View>(R.id.puntoNoLeida)

            txtIcono.text = notificacion.icono
            txtIcono.setTextColor(color(notificacion.colorIcono))
            txtIcono.backgroundTintList = ColorStateList.valueOf(color(notificacion.fondoIcono))
            txtTexto.text = notificacion.texto
            txtHace.text = notificacion.hace

            if (notificacion.leida) {
                puntoNoLeida.visibility = View.INVISIBLE
            } else {
                puntoNoLeida.visibility = View.VISIBLE
            }

            // Al tocarla se marca como leida
            vista.setOnClickListener {
                notificacion.leida = true
                puntoNoLeida.visibility = View.INVISIBLE
            }

            contNotificaciones.addView(vista)
        }
    }

    fun mostrarPendientes() {
        contPendientes.removeAllViews()

        // Primero las que faltan (la que vence primero arriba), al final las entregadas
        val ordenadas = ListaTareas.tareas.sortedWith(compareBy({ it.completada }, { it.fechaOrden }))

        var mostradas = 0
        for (tarea in ordenadas) {
            if (!verTodo && (tarea.completada || mostradas == 3)) {
                continue
            }
            contPendientes.addView(crearTarjeta(tarea))
            mostradas++
        }

        if (verTodo) {
            txtVerTodo.text = "Ver menos"
        } else {
            txtVerTodo.text = "Ver todo"
        }

        if (mostradas == 0) {
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
        txtFecha.text = tarea.fecha

        // Colores segun el tipo: Tarea morado, Examen rojo, Proyecto verde
        barraTipo.backgroundTintList = ColorStateList.valueOf(color(tarea.color()))
        txtTipo.setTextColor(color(tarea.color()))
        txtTipo.backgroundTintList = ColorStateList.valueOf(color(tarea.colorSuave()))

        if (tarea.completada) {
            imgCheck.setImageResource(R.drawable.ic_check_lleno)
            txtTitulo.paintFlags = txtTitulo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG // texto tachado
            vista.alpha = 0.6f
        } else {
            imgCheck.setImageResource(R.drawable.ic_check_vacio)
        }

        // El circulo marca o desmarca la tarea como entregada
        imgCheck.setOnClickListener {
            tarea.completada = !tarea.completada
            mostrarPendientes()
        }

        return vista
    }

    fun color(id: Int): Int {
        return ContextCompat.getColor(this, id)
    }
}
