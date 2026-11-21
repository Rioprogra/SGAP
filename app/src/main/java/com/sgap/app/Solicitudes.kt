package com.sgap.app

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class Solicitudes : AppCompatActivity() {

    lateinit var lvSolicitudes: ListView
    lateinit var txtVacio: TextView
    var solicitudes = mutableListOf<Solicitud>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_solicitudes)

        lvSolicitudes = findViewById(R.id.lvSolicitudes)
        txtVacio = findViewById(R.id.txtVacio)
        findViewById<TextView>(R.id.btnRegresar).setOnClickListener { finish() }

        if (!Usuario.esAdmin) {
            Toast.makeText(this, "Solo los administradores pueden ver esta pantalla", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        cargar()
    }

    fun cargar() {
        BaseDatos.cargarGrupos {}
        BaseDatos.cargarSolicitudes { lista, error ->
            solicitudes.clear()
            solicitudes.addAll(lista)
            lvSolicitudes.adapter = SolicitudAdapter()
            if (error != null) {
                txtVacio.text = "No se pudieron cargar las solicitudes: $error"
                txtVacio.visibility = View.VISIBLE
            } else if (solicitudes.isEmpty()) {
                txtVacio.text = "No hay solicitudes pendientes 👌"
                txtVacio.visibility = View.VISIBLE
            } else {
                txtVacio.visibility = View.GONE
            }
        }
    }

    fun revisar(solicitud: Solicitud, aprobar: Boolean) {
        val accion = if (aprobar) "autorizar" else "rechazar"
        AlertDialog.Builder(this)
            .setTitle(if (aprobar) "Autorizar rol" else "Rechazar solicitud")
            .setMessage("¿Seguro que quieres $accion a ${solicitud.nombre} como ${Usuario.nombreRol(solicitud.rolSolicitado).lowercase()}?")
            .setPositiveButton(if (aprobar) "Autorizar" else "Rechazar") { _, _ ->
                BaseDatos.revisarSolicitud(solicitud, aprobar) { error ->
                    if (error == null) {
                        Toast.makeText(this, if (aprobar) "Rol autorizado" else "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "No se pudo guardar: $error", Toast.LENGTH_LONG).show()
                    }
                    cargar()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    fun nombreGrupo(idGrupo: String): String {
        for (grupo in ListaGrupos.grupos) {
            if (grupo.id == idGrupo) return grupo.nombre
        }
        return "sin grupo"
    }

    inner class SolicitudAdapter : ArrayAdapter<Solicitud>(this, 0, solicitudes) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val vista = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_solicitud, parent, false)
            val solicitud = solicitudes[position]

            vista.findViewById<TextView>(R.id.txtNombre).text = solicitud.nombre.ifBlank { solicitud.correo }
            vista.findViewById<TextView>(R.id.txtCorreo).text = solicitud.correo
            vista.findViewById<TextView>(R.id.txtDetalle).text =
                "Pide ser ${Usuario.nombreRol(solicitud.rolSolicitado).lowercase()} · ${nombreGrupo(solicitud.idGrupo)}"
            vista.findViewById<TextView>(R.id.btnAprobar).setOnClickListener { revisar(solicitud, true) }
            vista.findViewById<TextView>(R.id.btnRechazar).setOnClickListener { revisar(solicitud, false) }
            return vista
        }
    }
}
