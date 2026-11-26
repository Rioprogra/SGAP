package com.sgap.app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class Grupo : AppCompatActivity() {

    lateinit var lvMaterias: ListView
    var misGrupos = listOf<EquipoGrupo>()
    var grupoActual: EquipoGrupo? = null
    var materias = mutableListOf<Materia>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_grupo)

        Navegacion.configurar(this, R.id.navGrupo)

        lvMaterias = findViewById(R.id.lvMaterias)
        lvMaterias.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, DetalleMateria::class.java)
            intent.putExtra("idMateria", materias[position].id)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.btnAgregar).setOnClickListener { dialogoUnirseConCodigo() }

        findViewById<Button>(R.id.btnCargarHorario).setOnClickListener {
            val grupo = grupoActual ?: return@setOnClickListener
            val intent = Intent(this, CrearGrupo::class.java)
            intent.putExtra("idGrupo", grupo.id)
            intent.putExtra("nombreGrupo", grupo.nombre)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        mostrar()
        recargar()
    }

    fun recargar() {
        BaseDatos.cargarGrupos {
            BaseDatos.cargarMaterias {
                BaseDatos.cargarTareas(this) { mostrar() }
            }
        }
    }

    fun mostrar() {
        misGrupos = ListaGrupos.grupos.filter { it.esMio() }.sortedBy { it.nombre }
        var actual = grupoActual
        if (actual == null || misGrupos.none { it.id == actual?.id }) {
            actual = misGrupos.firstOrNull { it.id == Usuario.idGrupo } ?: misGrupos.firstOrNull()
        } else {
            actual = misGrupos.first { it.id == actual?.id }
        }
        grupoActual = actual

        pintarChipsGrupos()

        val txtNombre = findViewById<TextView>(R.id.txtNombreGrupo)
        val txtInfo = findViewById<TextView>(R.id.txtInfoGrupo)
        val contVacio = findViewById<LinearLayout>(R.id.contVacio)
        val txtVacio = findViewById<TextView>(R.id.txtVacio)
        val btnCargarHorario = findViewById<Button>(R.id.btnCargarHorario)
        val txtTituloMaterias = findViewById<TextView>(R.id.txtTituloMaterias)

        materias.clear()
        if (actual == null) {
            txtNombre.text = "Mi grupo"
            txtInfo.visibility = View.GONE
            txtTituloMaterias.visibility = View.GONE
            txtVacio.text = "Aún no estás en un grupo.\nPídele el código a tu jefe de grupo o tutor y toca \"Unirme con código\"."
            btnCargarHorario.visibility = View.GONE
            contVacio.visibility = View.VISIBLE
            lvMaterias.adapter = MateriaAdapter()
            return
        }

        txtNombre.text = actual.nombre
        var info = "Código: ${actual.codigoAcceso} · ${Usuario.nombreRol(actual.miRol ?: "MIEMBRO")}"
        val cuantos = actual.integrantes.size
        info += if (cuantos == 1) " · 1 integrante" else " · $cuantos integrantes"
        txtInfo.text = info
        txtInfo.visibility = View.VISIBLE

        for (materia in ListaMaterias.materias) {
            if (materia.idGrupo == actual.id) materias.add(materia)
        }
        materias.sortWith(compareBy({ -pendientesDe(it) }, { it.nombre }))
        lvMaterias.adapter = MateriaAdapter()

        if (materias.isEmpty()) {
            txtTituloMaterias.visibility = View.GONE
            val soyCreador = actual.idCreador == BaseDatos.uid()
            if (soyCreador) {
                txtVacio.text = "Tu grupo todavía no tiene materias. Carga la foto del horario para agregarlas."
                btnCargarHorario.visibility = View.VISIBLE
            } else {
                txtVacio.text = "Tu grupo todavía no tiene materias. El jefe de grupo o tutor debe cargar el horario."
                btnCargarHorario.visibility = View.GONE
            }
            contVacio.visibility = View.VISIBLE
        } else {
            txtTituloMaterias.text = "Materias (${materias.size})"
            txtTituloMaterias.visibility = View.VISIBLE
            contVacio.visibility = View.GONE
        }
    }

    fun pintarChipsGrupos() {
        val scroll = findViewById<HorizontalScrollView>(R.id.scrollGrupos)
        val cont = findViewById<LinearLayout>(R.id.contChipsGrupos)
        cont.removeAllViews()
        if (misGrupos.size < 2) {
            scroll.visibility = View.GONE
            return
        }
        scroll.visibility = View.VISIBLE
        for (grupo in misGrupos) {
            val chip = LayoutInflater.from(this).inflate(R.layout.item_chip, cont, false) as TextView
            chip.text = grupo.nombre
            Chips.pintar(this, chip, grupo.id == grupoActual?.id)
            chip.setOnClickListener {
                grupoActual = grupo
                mostrar()
            }
            cont.addView(chip)
        }
    }

    fun pendientesDe(materia: Materia): Int {
        var cuantas = 0
        for (tarea in ListaTareas.tareas) {
            if (tarea.idMateria == materia.id && !tarea.completada) cuantas++
        }
        return cuantas
    }

    fun totalDe(materia: Materia): Int {
        var cuantas = 0
        for (tarea in ListaTareas.tareas) {
            if (tarea.idMateria == materia.id) cuantas++
        }
        return cuantas
    }

    fun dialogoUnirseConCodigo() {
        val txtCodigo = EditText(this)
        txtCodigo.hint = "Ej. K7M2Q9XA"
        txtCodigo.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS

        AlertDialog.Builder(this)
            .setTitle("Unirme con código")
            .setMessage("Pídele el código a tu jefe de grupo o tutor.")
            .setView(txtCodigo)
            .setPositiveButton("Unirme") { _, _ ->
                val codigo = txtCodigo.text.toString().trim()
                if (codigo.isEmpty()) {
                    Toast.makeText(this, "Escribe el código", Toast.LENGTH_SHORT).show()
                } else {
                    BaseDatos.unirseConCodigo(codigo) { error, nombreGrupo ->
                        if (error == null) {
                            Toast.makeText(this, "Te uniste a $nombreGrupo", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "No se pudo: $error", Toast.LENGTH_LONG).show()
                        }
                        recargar()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    inner class MateriaAdapter : ArrayAdapter<Materia>(this, 0, materias) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val vista = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_materia_grupo, parent, false)
            val materia = materias[position]
            val pendientes = pendientesDe(materia)
            val total = totalDe(materia)

            val txtSiglas = vista.findViewById<TextView>(R.id.txtSiglas)
            val txtNombre = vista.findViewById<TextView>(R.id.txtNombre)
            val txtDetalle = vista.findViewById<TextView>(R.id.txtDetalle)
            val txtContador = vista.findViewById<TextView>(R.id.txtContador)

            txtNombre.text = materia.nombre
            txtSiglas.text = siglas(materia)

            if (total == 0) {
                txtSiglas.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9AA0AE"))
                txtDetalle.text = "${materia.profesor} · sin tareas"
                vista.alpha = 0.55f
            } else {
                txtSiglas.backgroundTintList = ColorStateList.valueOf(materia.colorEntero())
                txtDetalle.text = if (pendientes == 0) "${materia.profesor} · todo entregado ✓" else "${materia.profesor} · $pendientes por entregar"
                vista.alpha = 1f
            }

            if (pendientes > 0) {
                txtContador.text = if (pendientes > 9) "9+" else pendientes.toString()
                txtContador.visibility = View.VISIBLE
            } else {
                txtContador.visibility = View.GONE
            }
            return vista
        }

        fun siglas(materia: Materia): String {
            var resultado = ""
            for (palabra in materia.nombre.split(" ")) {
                if (palabra.length > 2 && resultado.length < 2) resultado += palabra[0].uppercaseChar()
            }
            if (resultado.isEmpty()) resultado = materia.nombre.take(2).uppercase()
            return resultado
        }
    }
}
