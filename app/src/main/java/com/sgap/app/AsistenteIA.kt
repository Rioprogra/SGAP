package com.sgap.app

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AsistenteIA {

    const val MODELO = "gemini-3.8-flash"

    val modelo by lazy {
        Firebase.appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())

        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(MODELO)
    }

    var consejoGuardado: String? = null

    private var chat: Chat? = null

    private suspend fun preguntar(instruccion: String): String {
        val respuesta = modelo.generateContent(instruccion)
        val texto = limpiar(respuesta.text ?: "")
        if (texto.isEmpty()) throw IllegalStateException("Gemini no regresó respuesta")
        return texto
    }

    fun limpiar(texto: String): String {
        return texto.replace("**", "").replace("##", "").replace("# ", "").trim()
    }

    fun resumenPendientes(): String {
        val formato = SimpleDateFormat("d 'de' MMMM", Locale("es", "MX"))
        val pendientes = ListaTareas.tareas.filter { !it.completada }.sortedBy { it.fechaEntrega }
        if (pendientes.isEmpty()) return "El estudiante no tiene pendientes registrados."
        val lineas = pendientes.take(10).map {
            val materia = it.nombreMateria()
            "- ${it.tipo}: ${it.titulo}" +
                    (if (materia.isNotBlank()) " ($materia)" else "") +
                    ", entrega el ${formato.format(Date(it.fechaEntrega))}" +
                    (if (it.vencida()) " (VENCIDA)" else "")
        }
        return "Hoy es ${formato.format(Date())}. Pendientes del estudiante:\n" + lineas.joinToString("\n")
    }

    suspend fun sugerirDescripcion(titulo: String, tipo: String): String {
        return preguntar(
            "Eres un asistente para estudiantes universitarios. " +
                    "Escribe en español una descripción breve (máximo 2 oraciones) " +
                    "para este pendiente escolar. Tipo: $tipo. Título: $titulo"
        )
    }

    suspend fun sugerirPrioridad(titulo: String, tipo: String, fecha: String): String {
        return preguntar(
            "Eres un asistente para estudiantes universitarios. Responde en español. " +
                    "Indica la prioridad (Alta, Media o Baja) de este pendiente y explica por qué en una sola oración. " +
                    "Empieza con 'Prioridad: '. Tipo: $tipo. Título: $titulo. Fecha de entrega: $fecha.\n" +
                    resumenPendientes()
        )
    }

    suspend fun planDeEstudio(titulo: String, tipo: String, fecha: String): String {
        return preguntar(
            "Eres un asistente para estudiantes universitarios. Responde en español, sin markdown. " +
                    "Haz un plan de estudio corto de 3 a 4 pasos numerados (una línea cada uno) " +
                    "para terminar a tiempo este pendiente. Tipo: $tipo. Título: $titulo. Fecha de entrega: $fecha."
        )
    }

    suspend fun consejoDelDia(): String {
        val consejo = preguntar(
            "Eres un coach de estudio para universitarios. Responde en español, sin markdown, " +
                    "con un solo consejo práctico de máximo 2 oraciones, personalizado a sus pendientes.\n" +
                    resumenPendientes()
        )
        consejoGuardado = consejo
        return consejo
    }

    fun reiniciarChat() {
        chat = null
    }

    suspend fun enviarMensaje(mensaje: String): String {
        val actual = chat ?: modelo.startChat(
            history = listOf(
                content(role = "user") {
                    text(
                        "Eres el asistente de estudio de la app StudyGest. Responde siempre en español, " +
                                "de forma breve y amable, sin markdown. Ayuda con organización, tareas y técnicas de estudio.\n" +
                                resumenPendientes()
                    )
                },
                content(role = "model") { text("Entendido, estoy listo para ayudarte.") }
            )
        ).also { chat = it }
        val respuesta = actual.sendMessage(mensaje)
        val texto = limpiar(respuesta.text ?: "")
        if (texto.isEmpty()) throw IllegalStateException("Gemini no regresó respuesta")
        return texto
    }

    fun mensajeError(e: Exception): String {
        android.util.Log.w("AsistenteIA", "Error de Gemini", e)
        val detalle = e.toString() + " " + (e.cause?.toString() ?: "")
        return when {
            detalle.contains("ServiceDisabled", true) ->
                "Gemini aún no está activado: falta activar Firebase AI Logic en la consola de Firebase."
            detalle.contains("App Check", true) || detalle.contains("appcheck", true) ->
                "Gemini no está disponible: falta configurar App Check en Firebase."
            detalle.contains("network", true) || detalle.contains("Unable to resolve host", true) || detalle.contains("timeout", true) ->
                "Sin conexión. Revisa tu internet e inténtalo de nuevo."
            detalle.contains("quota", true) || detalle.contains("429") ->
                "Gemini está saturado en este momento. Intenta en un minuto."
            else -> "No pude conectarme con Gemini. Intenta de nuevo más tarde."
        }
    }
}
