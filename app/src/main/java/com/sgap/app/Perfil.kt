package com.sgap.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkManager
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth

class Perfil : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

        BaseDatos.cargarTareas(this) { mostrarNumeros() }
        BaseDatos.cargarGrupos { mostrarNumeros() }
    }

    fun mostrarNumeros() {
        findViewById<TextView>(R.id.txtNumPendientes).text = ListaTareas.pendientes().toString()
        findViewById<TextView>(R.id.txtNumGrupos).text = ListaGrupos.misGrupos().toString()
        findViewById<TextView>(R.id.txtNumCanales).text = ListaCanales.canales.size.toString()
    }

    fun abrirPendientes() {
        startActivity(Intent(this, Pendientes::class.java))
    }
}
