package com.sgap.app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Materias : AppCompatActivity() {

    lateinit var lvMaterias: ListView
    lateinit var txtVacio: TextView
    var idGrupo = ""
    var materias = mutableListOf<Materia>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_materias)

        idGrupo = intent.getStringExtra("idGrupo") ?: ""
        lvMaterias = findViewById(R.id.lvMaterias)
        txtVacio = findViewById(R.id.txtVacio)
        findViewById<TextView>(R.id.btnRegresar).setOnClickListener { finish() }

        lvMaterias.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, DetalleMateria::class.java)
            intent.putExtra("idMateria", materias[position].id)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        mostrar()
        BaseDatos.cargarMaterias { mostrar() }
    }

    fun mostrar() {
        materias.clear()
        for (materia in ListaMaterias.materias) {
            if (idGrupo == "" || materia.idGrupo == idGrupo) materias.add(materia)
        }

        val txtGrupo = findViewById<TextView>(R.id.txtGrupo)
        for (grupo in ListaGrupos.grupos) {
            if (grupo.id == idGrupo) {
                findViewById<TextView>(R.id.txtTitulo).text = grupo.nombre
                var texto = "Código para invitar: ${grupo.codigoAcceso}"
                if (grupo.miRol != null) texto = "Tu rol: ${Usuario.nombreRol(grupo.miRol ?: "")} · $texto"
                txtGrupo.text = texto
                txtGrupo.visibility = View.VISIBLE
            }
        }

        lvMaterias.adapter = MateriaAdapter()
        if (materias.isEmpty()) {
            txtVacio.text = "Aún no hay materias. Se agregan solas cuando el jefe de grupo o el tutor crea el grupo con la foto del horario."
            txtVacio.visibility = View.VISIBLE
        } else {
            txtVacio.visibility = View.GONE
        }
    }

    inner class MateriaAdapter : ArrayAdapter<Materia>(this, 0, materias) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val vista = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_materia, parent, false)
            val materia = materias[position]

            var pendientes = 0
            for (tarea in ListaTareas.tareas) {
                if (tarea.idMateria == materia.id && !tarea.completada) pendientes++
            }
            var detalle = "${materia.profesor} · ${materia.horasSemana} h por semana"
            if (pendientes == 1) detalle += " · 1 pendiente"
            if (pendientes > 1) detalle += " · $pendientes pendientes"

            vista.findViewById<TextView>(R.id.txtNombre).text = materia.nombre
            vista.findViewById<TextView>(R.id.txtDetalle).text = detalle
            vista.findViewById<View>(R.id.barraColor).backgroundTintList = ColorStateList.valueOf(materia.colorEntero())
            return vista
        }
    }
}
