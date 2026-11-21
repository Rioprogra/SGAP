package com.sgap.app

import android.graphics.Bitmap
import com.google.firebase.Firebase
import org.json.JSONArray
import org.json.JSONObject
import com.google.firebase.ai.Chat
import com.google.firebase.ai.GenerativeModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AsistenteIA {

    const val MODELO = "gemini-3.5-flash-lite"

    val modelo by lazy {
        Firebase.appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())

        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = MODELO,
            generationConfig = generationConfig {
                thinkingConfig = thinkingConfig { thinkingLevel = ThinkingLevel.MINIMAL }
            }
        )
    }

    const val MODELO_RESPALDO = "gemini-3.8-flash"

    val modeloRespaldo by lazy {
        modelo
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = MODELO_RESPALDO,
            generationConfig = generationConfig {
                thinkingConfig = thinkingConfig { thinkingLevel = ThinkingLevel.LOW }
            }
        )
    }

    val modeloHorarioRapido by lazy {
        modelo
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = MODELO,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0f
                thinkingConfig = thinkingConfig { thinkingLevel = ThinkingLevel.LOW }
            }
        )
    }

    val modeloHorario by lazy {
        modelo
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = MODELO_RESPALDO,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                thinkingConfig = thinkingConfig { thinkingLevel = ThinkingLevel.LOW }
            }
        )
    }

    fun esFallaTemporal(e: Exception): Boolean {
        val detalle = (e.toString() + " " + (e.cause?.toString() ?: "")).lowercase()
        return detalle.contains("high demand") || detalle.contains("overloaded")
                || detalle.contains("unavailable") || detalle.contains("503")
                || detalle.contains("429") || detalle.contains("quota")
                || detalle.contains("timeout") || detalle.contains("max_tokens")
                || detalle.contains("deadline") || detalle.contains("internal")
    }

    val instruccionesHorario = """
        Eres un lector de horarios escolares. La imagen es el horario oficial de un grupo de alumnos.
        Arriba de la tabla está el nombre del grupo (por ejemplo DSM41) y la escuela.
        Las columnas son los días (Lunes a Sábado) y las filas son las horas (por ejemplo 7:00 a 8:00).
        Cada celda tiene la materia abreviada, el profesor abreviado y el aula. Una celda puede ocupar varias horas.
        Abajo hay una tabla de Profesores (horas por semana y nombre completo con su abreviatura entre paréntesis)
        y otra de Materias (nombre completo con su abreviatura entre paréntesis), en el mismo orden y con el mismo color.
        Regresa SOLO un JSON con esta forma exacta:
        {
          "grupo": "DSM41",
          "escuela": "Universidad Tecnológica de Ciudad Juárez",
          "materias": [
            {"nombre": "Estructura de Datos", "abreviatura": "Est Datos", "profesor": "Lira Molina David",
             "profesor_corto": "Lira D", "horas": 5, "color": "#2BC4C8"}
          ],
          "clases": [
            {"dia": 1, "inicio": "07:00", "fin": "09:00", "materia": "Cal Varias Variable", "aula": "H105"}
          ]
        }
        Reglas: dia es 1=Lunes, 2=Martes, 3=Miércoles, 4=Jueves, 5=Viernes, 6=Sábado.
        Si una celda ocupa varias horas, ponla una sola vez con su hora de inicio y de fin.
        Revisa columna por columna y hora por hora: cada día tiene clases sin huecos de la primera a la última hora.
        Al final, la suma de horas de las clases de cada materia debe ser igual a sus horas de la tabla de Profesores.
        El campo materia de cada clase debe ser exactamente la abreviatura que aparece en la celda.
        En la tabla de Materias, nombre es la forma más larga y completa (por ejemplo "Aplicaciones Web") y abreviatura la que se usa en las celdas.
        Las aulas empiezan con una letra mayúscula (H, I, J...) seguida de números y a veces un texto, por ejemplo I101, H105, I004, J007 CISC II, I Aud. No confundas la letra I con el número 1.
        horas es el número de horas por semana de la tabla de Profesores. color es el color de fondo en hexadecimal.
        No inventes datos: si algo no se lee, déjalo como texto vacío.
    """.trimIndent()

    suspend fun leerHorario(imagen: Bitmap, avance: (mensaje: String) -> Unit = {}): HorarioLeido {
        val intentos = listOf(modeloHorarioRapido, modeloHorarioRapido, modeloHorario, modeloHorarioRapido)
        var ultimoError: Exception = IllegalStateException("Gemini no regresó respuesta")
        var mejor: HorarioLeido? = null
        for (i in intentos.indices) {
            if (i > 0) {
                if (mejor != null) {
                    avance("Revisando que estén todas las horas de clase...")
                } else {
                    avance("Gemini está ocupado, reintentando (${i + 1} de ${intentos.size})...")
                    delay(1500L * i)
                }
            }
            try {
                val inicio = System.currentTimeMillis()
                val leido = withTimeout(60000) { leerHorarioCon(intentos[i], imagen) }
                android.util.Log.d("AsistenteIA", "Intento ${i + 1}: ${leido.horasTotales()} de ${leido.horasDeLaTabla()} horas en ${System.currentTimeMillis() - inicio} ms")
                if (leido.materias.isNotEmpty() && leido.clases.isNotEmpty()) {
                    if (leido.cuadra()) return leido
                    val anterior = mejor
                    if (anterior == null || leido.diferencia() < anterior.diferencia()) mejor = leido
                } else {
                    ultimoError = IllegalStateException("No se encontraron materias en la foto")
                }
            } catch (e: Exception) {
                android.util.Log.d("AsistenteIA", "Intento ${i + 1} falló: $e")
                ultimoError = e
                if (!esFallaTemporal(e) && e !is org.json.JSONException) throw e
            }
        }
        return mejor ?: throw ultimoError
    }

    private suspend fun leerHorarioCon(modeloUsado: GenerativeModel, imagen: Bitmap): HorarioLeido {
        val respuesta = modeloUsado.generateContent(
            content {
                image(imagen)
                text(instruccionesHorario)
            }
        )
        var texto = (respuesta.text ?: "").trim()
        texto = texto.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        if (texto.isEmpty()) throw IllegalStateException("Gemini no regresó respuesta")

        val json = JSONObject(texto)
        val materias = mutableListOf<MateriaLeida>()
        val listaMaterias = json.optJSONArray("materias") ?: JSONArray()
        for (i in 0 until listaMaterias.length()) {
            val m = listaMaterias.getJSONObject(i)
            materias.add(
                MateriaLeida(
                    m.optString("nombre"),
                    m.optString("abreviatura"),
                    m.optString("profesor"),
                    m.optString("profesor_corto"),
                    m.optInt("horas", 0),
                    m.optString("color")
                )
            )
        }
        val clases = mutableListOf<ClaseLeida>()
        val listaClases = json.optJSONArray("clases") ?: JSONArray()
        for (i in 0 until listaClases.length()) {
            val c = listaClases.getJSONObject(i)
            clases.add(
                ClaseLeida(
                    c.optInt("dia", 1),
                    horaConCeros(c.optString("inicio")),
                    horaConCeros(c.optString("fin")),
                    c.optString("materia"),
                    c.optString("aula")
                )
            )
        }
        return HorarioLeido(json.optString("grupo"), json.optString("escuela"), materias, clases)
    }

    fun horaConCeros(hora: String): String {
        val partes = hora.trim().split(":")
        if (partes.size < 2) return hora.trim()
        return partes[0].padStart(2, '0') + ":" + partes[1].padStart(2, '0')
    }

    private var chat: Chat? = null

    private suspend fun preguntar(instruccion: String): String {
        val respuesta = try {
            modelo.generateContent(instruccion)
        } catch (e: Exception) {
            if (!esFallaTemporal(e)) throw e
            delay(1000)
            modeloRespaldo.generateContent(instruccion)
        }
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
        val respuesta = try {
            actual.sendMessage(mensaje)
        } catch (e: Exception) {
            if (!esFallaTemporal(e)) throw e
            delay(1000)
            chat = null
            return modeloRespaldo.generateContent(resumenPendientes() + "\n\nPregunta del estudiante: " + mensaje)
                .text?.let { limpiar(it) } ?: throw e
        }
        val texto = limpiar(respuesta.text ?: "")
        if (texto.isEmpty()) throw IllegalStateException("Gemini no regresó respuesta")
        return texto
    }

    fun mensajeError(e: Exception): String {
        android.util.Log.w("AsistenteIA", "Error de Gemini", e)
        val detalle = e.toString() + " " + (e.cause?.toString() ?: "")
        return when {
            detalle.contains("No se encontraron materias", true) || detalle.contains("JSON", true) ->
                "No pude leer el horario en esa foto. Usa una como la del ejemplo, completa y derecha."
            detalle.contains("ServiceDisabled", true) ->
                "Gemini aún no está activado: falta activar Firebase AI Logic en la consola de Firebase."
            detalle.contains("App Check", true) || detalle.contains("appcheck", true) ->
                "Gemini no está disponible: falta configurar App Check en Firebase."
            detalle.contains("network", true) || detalle.contains("Unable to resolve host", true) || detalle.contains("timeout", true) ->
                "Sin conexión. Revisa tu internet e inténtalo de nuevo."
            detalle.contains("quota", true) || detalle.contains("429")
                    || detalle.contains("high demand", true) || detalle.contains("503") || detalle.contains("overloaded", true) ->
                "Gemini está muy ocupado en este momento. Toca Reintentar en unos segundos."
            else -> "No pude conectarme con Gemini. Intenta de nuevo más tarde."
        }
    }
}
