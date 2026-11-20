package com.sgap.app

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class Grupo : AppCompatActivity() {

    lateinit var lvGrupos: ListView
    lateinit var txtVacio: TextView
    lateinit var chipTodos: TextView
    lateinit var chipProyecto: TextView
    lateinit var chipEstudio: TextView
    lateinit var chipConLugar: TextView

    var filtro = "Todos"

    var gruposFiltrados = mutableListOf<EquipoGrupo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_grupo)

        Navegacion.configurar(this, R.id.navGrupo)

        lvGrupos = findViewById(R.id.lvGrupos)
        txtVacio = findViewById(R.id.txtVacio)
        chipTodos = findViewById(R.id.chipTodos)
        chipProyecto = findViewById(R.id.chipProyecto)
        chipEstudio = findViewById(R.id.chipEstudio)
        chipConLugar = findViewById(R.id.chipConLugar)
        val btnAgregar = findViewById<TextView>(R.id.btnAgregar)

        chipTodos.setOnClickListener { cambiarFiltro("Todos") }
        chipProyecto.setOnClickListener { cambiarFiltro("Proyecto") }
        chipEstudio.setOnClickListener { cambiarFiltro("Estudio") }
        chipConLugar.setOnClickListener { cambiarFiltro("Con lugar") }

        lvGrupos.setOnItemClickListener { _, _, position, _ ->
            val grupo = gruposFiltrados[position]

            if (grupo.esMio()) {
                AlertDialog.Builder(this)
                    .setTitle(grupo.nombre)
                    .setMessage("Tu rol: ${grupo.miRol}\n\nCódigo para invitar:\n${grupo.codigoAcceso}")
                    .setPositiveButton("Listo", null)
                    .show()
            } else if (!grupo.tieneLugar()) {
                Toast.makeText(this, "${grupo.nombre} ya está lleno", Toast.LENGTH_SHORT).show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Unirte a ${grupo.nombre}")
                    .setMessage("${grupo.tipo} · ${grupo.materia}\n${grupo.integrantes.size} de ${grupo.cupo} lugares ocupados")
                    .setPositiveButton("Unirme") { _, _ ->
                        BaseDatos.unirseAGrupo(grupo.id) { error ->
                            if (error == null) {
                                Toast.makeText(this, "Te uniste a ${grupo.nombre}", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "No se pudo: $error", Toast.LENGTH_LONG).show()
                            }
                            recargar()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        btnAgregar.setOnClickListener {
            val opciones = arrayOf("Crear grupo", "Unirme con código")
            AlertDialog.Builder(this)
                .setItems(opciones) { _, cual ->
                    if (cual == 0) dialogoCrearGrupo() else dialogoUnirseConCodigo()
                }
                .show()
        }

        pintarChips()
        mostrarGrupos()

        txtVacio.text = "Cargando grupos..."
        txtVacio.visibility = View.VISIBLE
        recargar()
    }

    fun recargar() {
        BaseDatos.cargarGrupos { error ->
            txtVacio.text = "No hay grupos. Toca + para crear uno"
            if (error != null) {
                Toast.makeText(this, "No se pudieron cargar los grupos: $error", Toast.LENGTH_LONG).show()
            }
            mostrarGrupos()
        }
    }

    fun dialogoCrearGrupo() {
        val vista = layoutInflater.inflate(R.layout.dialog_crear_grupo, null)
        val txtNombre = vista.findViewById<EditText>(R.id.txtNombreGrupo)
        val txtMateria = vista.findViewById<EditText>(R.id.txtMateria)
        val rbEstudio = vista.findViewById<RadioButton>(R.id.rbEstudio)
        val txtCupo = vista.findViewById<EditText>(R.id.txtCupo)
        val txtDescripcion = vista.findViewById<EditText>(R.id.txtDescripcionGrupo)

        AlertDialog.Builder(this)
            .setTitle("Crear grupo")
            .setView(vista)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = txtNombre.text.toString().trim()
                val materia = txtMateria.text.toString().trim()
                val cupo = txtCupo.text.toString().toIntOrNull() ?: 0
                val tipo = if (rbEstudio.isChecked) "Estudio" else "Proyecto"

                if (nombre.isEmpty() || materia.isEmpty()) {
                    Toast.makeText(this, "Escribe el nombre y la materia", Toast.LENGTH_SHORT).show()
                } else if (cupo < 2 || cupo > 50) {
                    Toast.makeText(this, "El cupo debe ser de 2 a 50", Toast.LENGTH_SHORT).show()
                } else {
                    BaseDatos.crearGrupo(nombre, txtDescripcion.text.toString().trim(), tipo, materia, cupo) { error, codigo ->
                        if (error == null) {
                            AlertDialog.Builder(this)
                                .setTitle("Grupo creado")
                                .setMessage("Eres ADMIN de $nombre.\n\nComparte este código para que se unan:\n$codigo")
                                .setPositiveButton("Listo", null)
                                .show()
                        } else {
                            Toast.makeText(this, "No se pudo crear: $error", Toast.LENGTH_LONG).show()
                        }
                        recargar()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    fun dialogoUnirseConCodigo() {
        val txtCodigo = EditText(this)
        txtCodigo.hint = "Ej. K7M2Q9XA"
        txtCodigo.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS

        AlertDialog.Builder(this)
            .setTitle("Unirme con código")
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

    fun cambiarFiltro(nuevoFiltro: String) {
        filtro = nuevoFiltro
        pintarChips()
        mostrarGrupos()
    }

    fun mostrarGrupos() {
        gruposFiltrados.clear()
        for (grupo in ListaGrupos.grupos) {
            if (filtro == "Todos"
                || (filtro == "Proyecto" && grupo.tipo == "Proyecto")
                || (filtro == "Estudio" && grupo.tipo == "Estudio")
                || (filtro == "Con lugar" && grupo.tieneLugar())
            ) {
                gruposFiltrados.add(grupo)
            }
        }

        lvGrupos.adapter = GrupoAdapter(this, gruposFiltrados)

        if (gruposFiltrados.isEmpty()) {
            txtVacio.visibility = View.VISIBLE
        } else {
            txtVacio.visibility = View.GONE
        }
    }

    fun pintarChips() {
        Chips.pintar(this, chipTodos, filtro == "Todos")
        Chips.pintar(this, chipProyecto, filtro == "Proyecto")
        Chips.pintar(this, chipEstudio, filtro == "Estudio")
        Chips.pintar(this, chipConLugar, filtro == "Con lugar")
    }
}
