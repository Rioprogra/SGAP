package com.sgap.app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Calendar

class DetalleTarea : AppCompatActivity() {

    var idTarea = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_detalle_tarea)

        idTarea = intent.getStringExtra("idTarea") ?: ""

        findViewById<TextView>(R.id.btnRegresar).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        if (buscarTarea() != null) {
            mostrar()
        }
        BaseDatos.cargarMaterias {
            BaseDatos.cargarTareas(this) { mostrar() }
        }
    }

    fun buscarTarea(): Tarea? {
        for (tarea in ListaTareas.tareas) {
            if (tarea.id == idTarea) return tarea
        }
        return null
    }

    fun mostrar() {
        val txtCargando = findViewById<TextView>(R.id.txtCargando)
        val contDetalle = findViewById<LinearLayout>(R.id.contDetalle)
        val btnEntregar = findViewById<Button>(R.id.btnEntregar)
        val btnEditar = findViewById<TextView>(R.id.btnEditar)

        val tarea = buscarTarea()
        if (tarea == null) {
            txtCargando.text = "Esta tarea ya no existe o fue eliminada"
            txtCargando.visibility = View.VISIBLE
            contDetalle.visibility = View.GONE
            btnEntregar.visibility = View.GONE
            btnEditar.visibility = View.GONE
            return
        }

        txtCargando.visibility = View.GONE
        contDetalle.visibility = View.VISIBLE

        val txtTipo = findViewById<TextView>(R.id.txtTipo)
        txtTipo.text = tarea.tipo
        txtTipo.setTextColor(color(tarea.color()))
        txtTipo.backgroundTintList = ColorStateList.valueOf(color(tarea.colorSuave()))

        findViewById<TextView>(R.id.txtTitulo).text = tarea.titulo

        val txtEstado = findViewById<TextView>(R.id.txtEstado)
        if (tarea.completada) {
            txtEstado.text = "✓ Entregada"
            txtEstado.setTextColor(color(R.color.acento_turquesa))
        } else if (tarea.vencida()) {
            txtEstado.text = "Vencida"
            txtEstado.setTextColor(color(R.color.error))
        } else {
            txtEstado.text = "Pendiente"
            txtEstado.setTextColor(color(R.color.texto_suave))
        }

        val calendario = Calendar.getInstance()
        calendario.timeInMillis = tarea.fechaEntrega
        findViewById<TextView>(R.id.txtFecha).text = Fechas.corta(calendario) + " ${calendario.get(Calendar.YEAR)}"

        val materia = tarea.nombreMateria()
        mostrarCampo(R.id.lblMateria, R.id.txtMateria, materia)

        var nombreGrupo = ""
        if (tarea.origen == "GRUPO") {
            nombreGrupo = "Tarea de grupo"
            for (grupo in ListaGrupos.grupos) {
                if (grupo.id == tarea.idGrupo) nombreGrupo = grupo.nombre
            }
        }
        mostrarCampo(R.id.lblGrupo, R.id.txtGrupo, nombreGrupo)

        val txtDescripcion = findViewById<TextView>(R.id.txtDescripcion)
        if (tarea.descripcion == "") {
            txtDescripcion.text = "Sin descripción"
            txtDescripcion.setTextColor(color(R.color.placeholder))
        } else {
            txtDescripcion.text = tarea.descripcion
            txtDescripcion.setTextColor(color(R.color.texto))
        }

        btnEntregar.visibility = View.VISIBLE
        if (tarea.completada) {
            btnEntregar.text = "Marcar como pendiente"
        } else {
            btnEntregar.text = "Marcar como entregada"
        }
        btnEntregar.setOnClickListener {
            tarea.completada = !tarea.completada
            BaseDatos.cambiarCompletada(tarea)
            Recordatorios.programar(this, tarea)
            mostrar()
        }

        if (tarea.idCreador == BaseDatos.uid()) {
            btnEditar.visibility = View.VISIBLE
            btnEditar.setOnClickListener {
                val intent = Intent(this, NuevaTarea::class.java)
                intent.putExtra("idTarea", tarea.id)
                startActivity(intent)
            }
        } else {
            btnEditar.visibility = View.GONE
        }
    }

    fun mostrarCampo(idEtiqueta: Int, idTexto: Int, valor: String) {
        val visibilidad = if (valor == "") View.GONE else View.VISIBLE
        findViewById<TextView>(idEtiqueta).visibility = visibilidad
        val texto = findViewById<TextView>(idTexto)
        texto.visibility = visibilidad
        texto.text = valor
    }

    fun color(id: Int): Int {
        return ContextCompat.getColor(this, id)
    }
}
