package com.sgap.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkManager
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth

class Perfil : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_perfil)

        Navegacion.configurar(this, R.id.navPerfil)

        val txtIniciales = findViewById<TextView>(R.id.txtIniciales)
        val txtNombre = findViewById<TextView>(R.id.txtNombre)
        val txtCorreo = findViewById<TextView>(R.id.txtCorreo)
        val datoPendientes = findViewById<LinearLayout>(R.id.datoPendientes)
        val btnMisPendientes = findViewById<TextView>(R.id.btnMisPendientes)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        txtNombre.text = Usuario.nombre
        txtCorreo.text = Usuario.correo
        txtIniciales.text = Usuario.iniciales()

        findViewById<TextView>(R.id.btnAsistentePerfil).setOnClickListener {
            startActivity(Intent(this, Asistente::class.java))
            finish()
        }

        findViewById<TextView>(R.id.btnCambiarPerfil).setOnClickListener {
            val intent = Intent(this, ConfigurarPerfil::class.java)
            intent.putExtra("editar", true)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.btnCrearGrupo).setOnClickListener {
            startActivity(Intent(this, CrearGrupo::class.java))
        }

        findViewById<TextView>(R.id.btnSolicitudes).setOnClickListener {
            startActivity(Intent(this, Solicitudes::class.java))
        }

        findViewById<TextView>(R.id.btnProgreso).setOnClickListener {
            startActivity(Intent(this, Progreso::class.java))
        }

        val swModoOscuro = findViewById<MaterialSwitch>(R.id.swModoOscuro)
        swModoOscuro.isChecked = Tema.esOscuro(this)
        swModoOscuro.setOnCheckedChangeListener { _, activado ->
            Tema.cambiar(this, activado)
        }

        datoPendientes.setOnClickListener { abrirPendientes() }
        btnMisPendientes.setOnClickListener { abrirPendientes() }

        btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            WorkManager.getInstance(this).cancelAllWork()
            ListaTareas.tareas.clear()
            Usuario.limpiar()
            ListaGrupos.grupos.clear()
            ListaNotificaciones.notificaciones.clear()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        mostrarNumeros()
        mostrarRol()

        BaseDatos.cargarTareas(this) { mostrarNumeros() }
        BaseDatos.cargarGrupos {
            mostrarNumeros()
            mostrarRol()
        }
        BaseDatos.cargarPerfil { mostrarRol() }
    }

    fun mostrarRol() {
        var texto = Usuario.descripcionRol()
        if (Usuario.esAdmin) texto += " · Administrador"
        findViewById<TextView>(R.id.txtRol).text = texto

        val misGrupos = mutableListOf<String>()
        for (grupo in ListaGrupos.grupos) {
            if (grupo.esMio() || grupo.id == Usuario.idGrupo) misGrupos.add(grupo.nombre)
        }
        if (misGrupos.isEmpty()) {
            findViewById<TextView>(R.id.txtGrupo).text = "Sin grupo"
        } else {
            findViewById<TextView>(R.id.txtGrupo).text = misGrupos.joinToString(" · ")
        }
        if (Usuario.esAdmin) {
            findViewById<TextView>(R.id.btnSolicitudes).visibility = View.VISIBLE
        }
        findViewById<TextView>(R.id.btnCrearGrupo).visibility = if (Usuario.puedeCrearGrupos()) View.VISIBLE else View.GONE
    }

    fun mostrarNumeros() {
        findViewById<TextView>(R.id.txtNumPendientes).text = ListaTareas.pendientes().toString()
        findViewById<TextView>(R.id.txtNumGrupos).text = ListaGrupos.misGrupos().toString()
        findViewById<TextView>(R.id.txtNumEntregadas).text = ListaTareas.tareas.count { it.completada }.toString()
    }

    fun abrirPendientes() {
        startActivity(Intent(this, Pendientes::class.java))
    }
}
