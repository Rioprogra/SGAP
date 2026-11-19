package com.sgap.app

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

object AsistenteIA {

    const val MODELO = "gemini-4-hydrogen"

    val modelo by lazy {
        Firebase.appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())

        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(MODELO)
    }

    suspend fun sugerirDescripcion(titulo: String, tipo: String): String {
        val instruccion = "Eres un asistente para estudiantes universitarios. " +
                "Escribe en español una descripción breve (máximo 2 oraciones) " +
                "para este pendiente escolar. Tipo: $tipo. Título: $titulo"
        val respuesta = modelo.generateContent(instruccion)
        return respuesta.text?.trim() ?: ""
    }
}
