package com.sgap.app

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class Asistente : AppCompatActivity() {

    data class Mensaje(val texto: String, val esUsuario: Boolean)

    companion object {
        val mensajes = mutableListOf<Mensaje>()
    }

    lateinit var lvMensajes: ListView
    lateinit var txtPregunta: EditText
    lateinit var btnEnviar: ImageView
    lateinit var progresoEnvio: ProgressBar
    lateinit var adaptador: MensajeAdapter

    var enviando = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_asistente)

        Navegacion.configurar(this, R.id.navIA)

        lvMensajes = findViewById(R.id.lvMensajes)
        txtPregunta = findViewById(R.id.txtPregunta)
        btnEnviar = findViewById(R.id.btnEnviar)
        progresoEnvio = findViewById(R.id.progresoEnvio)

        if (mensajes.isEmpty()) agregarBienvenida()

        adaptador = MensajeAdapter(this, mensajes)
        lvMensajes.adapter = adaptador

        btnEnviar.setOnClickListener { enviar(txtPregunta.text.toString()) }

        findViewById<ImageView>(R.id.btnLimpiar).setOnClickListener {
            mensajes.clear()
            AsistenteIA.reiniciarChat()
            agregarBienvenida()
            adaptador.notifyDataSetChanged()
        }

        val contSugerencias = findViewById<LinearLayout>(R.id.contSugerencias)
        for (i in 0 until contSugerencias.childCount) {
            val chip = contSugerencias.getChildAt(i) as TextView
            chip.setOnClickListener {
                val pregunta = when (i) {
                    0 -> "Hazme un plan de estudio para esta semana con mis pendientes"
                    1 -> "¿Qué pendiente debo entregar primero y por qué?"
                    2 -> "Dame 3 tips para prepararme para un examen"
                    else -> "Explícame la técnica Pomodoro en pocas líneas"
                }
                enviar(pregunta)
            }
        }

        val inicial = intent.getStringExtra("pregunta")
        if (!inicial.isNullOrBlank() && savedInstanceState == null) enviar(inicial)
    }

    fun agregarBienvenida() {
        val nombre = Usuario.nombre.split(" ").firstOrNull().orEmpty()
        val saludo = if (nombre.isNotBlank()) "¡Hola, $nombre!" else "¡Hola!"
        mensajes.add(
            Mensaje(
                "$saludo Soy tu asistente de estudio con Gemini ✨\n" +
                        "Puedo armarte un plan, decirte qué entregar primero o explicarte un tema. ¿En qué te ayudo?",
                false
            )
        )
    }

    fun enviar(texto: String) {
        val pregunta = texto.trim()
        if (pregunta.isEmpty() || enviando) return

        enviando = true
        txtPregunta.setText("")
        mensajes.add(Mensaje(pregunta, true))
        mensajes.add(Mensaje("Pensando...", false))
        adaptador.notifyDataSetChanged()
        btnEnviar.visibility = View.INVISIBLE
        progresoEnvio.visibility = View.VISIBLE

        lifecycleScope.launch {
            val respuesta = try {
                AsistenteIA.enviarMensaje(pregunta)
            } catch (e: Exception) {
                AsistenteIA.reiniciarChat()
                "⚠️ " + AsistenteIA.mensajeError(e)
            }
            mensajes[mensajes.size - 1] = Mensaje(respuesta, false)
            adaptador.notifyDataSetChanged()
            btnEnviar.visibility = View.VISIBLE
            progresoEnvio.visibility = View.GONE
            enviando = false
        }
    }

    class MensajeAdapter(val context: Context, val lista: List<Mensaje>) : BaseAdapter() {

        override fun getCount() = lista.size

        override fun getItem(position: Int) = lista[position]

        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val vista = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_mensaje, parent, false)
            val mensaje = lista[position]
            val fila = vista.findViewById<LinearLayout>(R.id.filaMensaje)
            val imgGemini = vista.findViewById<ImageView>(R.id.imgGemini)
            val txtMensaje = vista.findViewById<TextView>(R.id.txtMensaje)

            txtMensaje.text = mensaje.texto
            if (mensaje.esUsuario) {
                fila.gravity = Gravity.END or Gravity.BOTTOM
                imgGemini.visibility = View.GONE
                txtMensaje.setBackgroundResource(R.drawable.bg_burbuja_usuario)
                txtMensaje.setTextColor(ContextCompat.getColor(context, R.color.sobre_primario))
            } else {
                fila.gravity = Gravity.START or Gravity.BOTTOM
                imgGemini.visibility = View.VISIBLE
                txtMensaje.setBackgroundResource(R.drawable.bg_burbuja_ia)
                txtMensaje.setTextColor(ContextCompat.getColor(context, R.color.texto))
            }
            return vista
        }
    }
}
