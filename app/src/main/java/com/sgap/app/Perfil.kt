package com.sgap.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

// Pestana Perfil: tus datos, numeritos y cerrar sesion
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

        datoPendientes.setOnClickListener { abrirPendientes() }
        btnMisPendientes.setOnClickListener { abrirPendientes() }

        btnCerrarSesion.setOnClickListener {
            // Regresa al login y borra las pantallas de atras
            // para que con "atras" no se pueda volver a entrar
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    // Los numeros se actualizan al regresar de Pendientes
    override fun onResume() {
        super.onResume()

        var misGrupos = 0
        for (grupo in ListaGrupos.grupos) {
            if (grupo.esMio) misGrupos++
        }

        findViewById<TextView>(R.id.txtNumPendientes).text = ListaTareas.pendientes().toString()
        findViewById<TextView>(R.id.txtNumGrupos).text = misGrupos.toString()
        findViewById<TextView>(R.id.txtNumCanales).text = ListaCanales.canales.size.toString()
    }

    fun abrirPendientes() {
        startActivity(Intent(this, Pendientes::class.java))
    }
}
