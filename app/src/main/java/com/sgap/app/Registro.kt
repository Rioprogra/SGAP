package com.sgap.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class Registro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        val auth = FirebaseAuth.getInstance()

        val txtNombre = findViewById<EditText>(R.id.txtNombre)
        val txtApellidos = findViewById<EditText>(R.id.txtApellidos)
        val txtCorreo = findViewById<EditText>(R.id.txtCorreo)
        val txtContrasena = findViewById<EditText>(R.id.txtContrasena)
        val txtConfirmar = findViewById<EditText>(R.id.txtConfirmar)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val txtIniciaSesion = findViewById<TextView>(R.id.txtIniciaSesion)

        btnRegistrar.setOnClickListener {
            val nombre = txtNombre.text.toString().trim()
            val apellidos = txtApellidos.text.toString().trim()
            val correo = txtCorreo.text.toString().trim()
            val contrasena = txtContrasena.text.toString()
            val confirmar = txtConfirmar.text.toString()

            if (nombre.isEmpty() || apellidos.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || confirmar.isEmpty()) {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            } else if (!correo.contains("@")) {
                Toast.makeText(this, "Escribe un correo valido", Toast.LENGTH_SHORT).show()
            } else if (contrasena.length < 6) {
                Toast.makeText(this, "La contraseña debe tener minimo 6 caracteres", Toast.LENGTH_SHORT).show()
            } else if (contrasena != confirmar) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            } else {
                btnRegistrar.isEnabled = false
                btnRegistrar.text = "Creando cuenta..."

                auth.createUserWithEmailAndPassword(correo.lowercase(), contrasena)
                    .addOnCompleteListener { tarea ->
                        if (tarea.isSuccessful) {
                            val cambios = UserProfileChangeRequest.Builder()
                                .setDisplayName("$nombre $apellidos")
                                .build()
                            auth.currentUser?.updateProfile(cambios)
                                ?.addOnCompleteListener {
                                    BaseDatos.crearUsuario(nombre, apellidos, correo) {
                                        auth.signOut()
                                        Toast.makeText(this, "Cuenta creada, ya puedes iniciar sesion", Toast.LENGTH_LONG).show()
                                        finish()
                                    }
                                }
                        } else {
                            btnRegistrar.isEnabled = true
                            btnRegistrar.text = "Crear cuenta"
                            Toast.makeText(this, ErroresFirebase.mensaje(tarea.exception), Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        txtIniciaSesion.setOnClickListener {
            finish()
        }
    }
}
