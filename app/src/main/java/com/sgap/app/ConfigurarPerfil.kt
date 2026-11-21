package com.sgap.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ConfigurarPerfil : AppCompatActivity() {

    lateinit var opAlumno: TextView
    lateinit var opJefe: TextView
    lateinit var opTutor: TextView
    lateinit var txtAyudaRol: TextView
    lateinit var txtAyudaGrupo: TextView
    lateinit var spGrupo: Spinner

    var rolElegido = "ALUMNO"
    var grupos = listOf<EquipoGrupo>()
    var editando = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_configurar_perfil)

        editando = intent.getBooleanExtra("editar", false)

        opAlumno = findViewById(R.id.opAlumno)
        opJefe = findViewById(R.id.opJefe)
        opTutor = findViewById(R.id.opTutor)
        txtAyudaRol = findViewById(R.id.txtAyudaRol)
        txtAyudaGrupo = findViewById(R.id.txtAyudaGrupo)
        spGrupo = findViewById(R.id.spGrupo)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        val nombre = Usuario.nombre.split(" ").firstOrNull().orEmpty().replaceFirstChar { it.uppercase() }
        findViewById<TextView>(R.id.txtHola).text = "Hola, $nombre. Dinos quién eres en tu salón."

        if (editando) {
            val btnRegresar = findViewById<TextView>(R.id.btnRegresar)
            btnRegresar.visibility = View.VISIBLE
            btnRegresar.setOnClickListener { finish() }
            btnGuardar.text = "Guardar cambios"
            rolElegido = if (Usuario.estadoRol == "PENDIENTE") Usuario.rolSolicitado else Usuario.rol
        }

        opAlumno.setOnClickListener { elegirRol("ALUMNO") }
        opJefe.setOnClickListener { elegirRol("JEFE") }
        opTutor.setOnClickListener { elegirRol("TUTOR") }
        elegirRol(rolElegido)

        llenarGrupos()
        BaseDatos.cargarGrupos { llenarGrupos() }

        btnGuardar.setOnClickListener {
            val posicion = spGrupo.selectedItemPosition
            var idGrupo = ""
            if (posicion > 0) idGrupo = grupos[posicion - 1].id

            if (rolElegido == "ALUMNO" && idGrupo == "" && grupos.isNotEmpty()) {
                Toast.makeText(this, "Elige tu grupo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnGuardar.isEnabled = false
            btnGuardar.text = "Guardando..."
            BaseDatos.guardarPerfil(rolElegido, idGrupo) { error ->
                if (error != null && error != "Ya estás en este grupo") {
                    Toast.makeText(this, "Se guardó tu perfil, pero no se pudo entrar al grupo: $error", Toast.LENGTH_LONG).show()
                } else if (Usuario.estadoRol == "PENDIENTE") {
                    Toast.makeText(this, "Listo. Un administrador revisará tu solicitud de ${Usuario.nombreRol(rolElegido).lowercase()}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Perfil guardado", Toast.LENGTH_SHORT).show()
                }
                if (editando) {
                    finish()
                } else {
                    val intentInicio = Intent(this, Inicio::class.java)
                    intentInicio.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intentInicio)
                }
            }
        }
    }

    fun elegirRol(rol: String) {
        rolElegido = rol
        pintarOpcion(opAlumno, rol == "ALUMNO")
        pintarOpcion(opJefe, rol == "JEFE")
        pintarOpcion(opTutor, rol == "TUTOR")

        val yaAutorizado = Usuario.rol == rol && Usuario.estadoRol == "APROBADO"
        if (rol == "ALUMNO") {
            txtAyudaRol.text = "Podrás ver y entregar las tareas de tu grupo."
            txtAyudaGrupo.text = "Si tu grupo no aparece, pídele el código a tu jefe de grupo o tutor y únete desde la pestaña Grupos."
        } else if (yaAutorizado) {
            txtAyudaRol.text = "✓ Ya tienes autorizado este rol."
            txtAyudaGrupo.text = "Puedes crear más grupos desde la pestaña Grupos."
        } else {
            txtAyudaRol.text = "Este rol necesita la autorización de un administrador. Mientras tanto usarás la app como alumno."
            txtAyudaGrupo.text = "Si tu grupo aún no existe, deja \"Sin grupo por ahora\". Cuando te autoricen podrás crearlo en la pestaña Grupos."
        }
        if (rol == "TUTOR") {
            txtAyudaGrupo.text = txtAyudaGrupo.text.toString() + " Como tutor puedes tener varios grupos."
        }
    }

    fun llenarGrupos() {
        grupos = ListaGrupos.grupos.sortedBy { it.nombre }
        val opciones = mutableListOf("Sin grupo por ahora")
        for (grupo in grupos) {
            opciones.add(grupo.nombre)
        }
        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spGrupo.adapter = adaptador

        for (i in grupos.indices) {
            if (grupos[i].id == Usuario.idGrupo) spGrupo.setSelection(i + 1)
        }
    }

    fun pintarOpcion(opcion: TextView, elegida: Boolean) {
        if (elegida) {
            opcion.setBackgroundResource(R.drawable.bg_opcion_activa)
            opcion.setTextColor(ContextCompat.getColor(this, R.color.sobre_primario))
            opcion.setTypeface(null, Typeface.BOLD)
        } else {
            opcion.setBackgroundResource(R.drawable.bg_opcion)
            opcion.setTextColor(ContextCompat.getColor(this, R.color.texto_suave))
            opcion.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
    }
}
