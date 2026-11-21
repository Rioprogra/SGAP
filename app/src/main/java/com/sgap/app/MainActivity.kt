package com.sgap.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        Tema.aplicar(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val usuarioActual = auth.currentUser
        if (usuarioActual != null) {
            abrirInicio(usuarioActual.email ?: "", usuarioActual.displayName)
            return
        }

        val txtCorreo = findViewById<EditText>(R.id.txtCorreo)
        val txtContrasena = findViewById<EditText>(R.id.txtContrasena)
        val btnIniciar = findViewById<Button>(R.id.btnIniciar)
        val txtOlvide = findViewById<TextView>(R.id.txtOlvide)
        val txtRegistrate = findViewById<TextView>(R.id.txtRegistrate)

        btnIniciar.setOnClickListener {
            val correo = txtCorreo.text.toString().trim()
            val contrasena = txtContrasena.text.toString()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            } else if (!correo.contains("@")) {
                Toast.makeText(this, "Escribe un correo valido", Toast.LENGTH_SHORT).show()
            } else if (contrasena.length < 6) {
                Toast.makeText(this, "La contraseña debe tener minimo 6 caracteres", Toast.LENGTH_SHORT).show()
            } else {
                btnIniciar.isEnabled = false
                btnIniciar.text = "Entrando..."

                auth.signInWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener { tarea ->
                        btnIniciar.isEnabled = true
                        btnIniciar.text = "Iniciar sesión"

                        if (tarea.isSuccessful) {
                            val usuario = auth.currentUser
                            abrirInicio(usuario?.email ?: correo, usuario?.displayName)
                        } else {
                            Toast.makeText(this, ErroresFirebase.mensaje(tarea.exception), Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        txtOlvide.setOnClickListener {
            val correo = txtCorreo.text.toString().trim()
            if (!correo.contains("@")) {
                Toast.makeText(this, "Escribe tu correo arriba y vuelve a tocar aquí", Toast.LENGTH_LONG).show()
            } else {
                auth.sendPasswordResetEmail(correo)
                    .addOnCompleteListener { tarea ->
                        if (tarea.isSuccessful) {
                            Toast.makeText(this, "Te enviamos un correo para cambiar tu contraseña", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, ErroresFirebase.mensaje(tarea.exception), Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        txtRegistrate.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }
    }

    fun abrirInicio(correo: String, nombre: String?) {
        Usuario.iniciarSesion(correo, nombre)
        BaseDatos.asegurarUsuario(Usuario.nombre, correo)
        BaseDatos.cargarPerfil {
            if (Usuario.perfilCompleto) {
                startActivity(Intent(this, Inicio::class.java))
            } else {
                startActivity(Intent(this, ConfigurarPerfil::class.java))
            }
            finish()
        }
    }
}
