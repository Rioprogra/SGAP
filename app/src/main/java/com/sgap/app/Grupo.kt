package com.sgap.app

import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

// Pestana Grupo: equipos y grupos de estudio del salon
class Grupo : AppCompatActivity() {

    lateinit var lvGrupos: ListView
    lateinit var txtVacio: TextView
    lateinit var chipTodos: TextView
    lateinit var chipProyecto: TextView
    lateinit var chipEstudio: TextView
    lateinit var chipConLugar: TextView

    // Filtro elegido: "Todos", "Proyecto", "Estudio" o "Con lugar"
    var filtro = "Todos"

    var gruposFiltrados = mutableListOf<EquipoGrupo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

        // Al tocar un grupo: si tiene lugar te puedes unir
        lvGrupos.setOnItemClickListener { _, _, position, _ ->
            val grupo = gruposFiltrados[position]

            if (grupo.esMio) {
                Toast.makeText(this, "Ya estás en ${grupo.nombre}", Toast.LENGTH_SHORT).show()
            } else if (!grupo.tieneLugar()) {
                Toast.makeText(this, "${grupo.nombre} ya está lleno", Toast.LENGTH_SHORT).show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Unirte a ${grupo.nombre}")
                    .setMessage("${grupo.tipo} · ${grupo.materia}\n${grupo.integrantes.size} de ${grupo.cupo} lugares ocupados")
                    .setPositiveButton("Unirme") { _, _ ->
                        grupo.integrantes.add("TÚ")
                        grupo.esMio = true
                        ListaNotificaciones.agregar("Te uniste al grupo ${grupo.nombre}", "+",
                            R.color.acento_turquesa, R.color.acento_turquesa_suave)
                        Toast.makeText(this, "Te uniste a ${grupo.nombre}", Toast.LENGTH_SHORT).show()
                        mostrarGrupos()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        // Crear grupo todavia no esta en el Figma
        btnAgregar.setOnClickListener {
            Toast.makeText(this, "Crear grupo: próximamente", Toast.LENGTH_SHORT).show()
        }

        pintarChips()
        mostrarGrupos()
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
