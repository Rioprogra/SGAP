package com.sgap.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

// Pantalla de inicio de sesion
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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
                // Todavia no hay base de datos, entonces cualquier correo entra
                val intent = Intent(this, Inicio::class.java)
                startActivity(intent)
                txtContrasena.setText("")
            }
        }

        txtOlvide.setOnClickListener {
            Toast.makeText(this, "Proximamente", Toast.LENGTH_SHORT).show()
        }

        txtRegistrate.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }
    }
}
