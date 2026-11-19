package com.sgap.app

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import java.util.Date

object BaseDatos {

    val db = FirebaseFirestore.getInstance().apply {
        firestoreSettings = firestoreSettings {
            setLocalCacheSettings(persistentCacheSettings {
                setSizeBytes(100L * 1024 * 1024)
            })
        }
    }

    var sinConexion = false

    fun uid(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    fun ahora(): Timestamp {
        return Timestamp(Date())
    }

    fun crearUsuario(nombre: String, apellidos: String, correo: String, alTerminar: () -> Unit) {
        val datos = hashMapOf(
            "nombre" to nombre,
            "apellidos" to apellidos,
            "correo" to correo.lowercase(),
            "zona_horaria" to "America/Mexico_City",
            "fecha_registro" to FieldValue.serverTimestamp(),
            "estado" to 1
        )
        db.collection("usuarios").document(uid()).set(datos)
            .addOnCompleteListener { alTerminar() }
    }

    fun asegurarUsuario(nombreCompleto: String, correo: String) {
        val documento = db.collection("usuarios").document(uid())
        documento.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                val nombre = nombreCompleto.substringBefore(" ")
                val apellidos = nombreCompleto.substringAfter(" ", "")
                crearUsuario(nombre, apellidos, correo) {}
            }
        }
    }

    fun cargarMaterias(alTerminar: (materias: List<Materia>) -> Unit) {
        db.collection("materias").get()
            .addOnSuccessListener { resultado ->
                val materias = mutableListOf<Materia>()
                for (doc in resultado) {
                    if (doc.id == "_plantilla") continue
                    if ((doc.getLong("estado") ?: 1) != 1L) continue
                    materias.add(Materia(doc.id, doc.getString("nombre") ?: doc.id))
                }
                ListaMaterias.materias.clear()
                ListaMaterias.materias.addAll(materias.sortedBy { it.nombre })
                alTerminar(ListaMaterias.materias)
            }
            .addOnFailureListener { alTerminar(listOf()) }
    }

    fun cargarGrupos(alTerminar: (error: String?) -> Unit) {
        db.collection("grupos").whereEqualTo("estado", 1).get()
            .addOnSuccessListener { resultadoGrupos ->
                val grupos = mutableListOf<EquipoGrupo>()
                for (doc in resultadoGrupos) {
                    grupos.add(
                        EquipoGrupo(
                            doc.id,
                            doc.getString("nombre") ?: "",
                            doc.getString("descripcion") ?: "",
                            doc.getString("tipo") ?: "Proyecto",
                            doc.getString("materia") ?: "",
                            (doc.getLong("cupo") ?: 5).toInt(),
                            doc.getString("codigo_acceso") ?: "",
                            doc.getString("id_creador") ?: ""
                        )
                    )
                }

                db.collection("miembros_grupo").whereEqualTo("estado", 1).get()
                    .addOnSuccessListener { resultadoMiembros ->
                        for (doc in resultadoMiembros) {
                            val idGrupo = doc.getString("id_grupo")
                            for (grupo in grupos) {
                                if (grupo.id == idGrupo) {
                                    grupo.integrantes.add(doc.getString("iniciales") ?: "?")
                                    grupo.miembrosUid.add(doc.getString("id_usuario") ?: "")
                                    if (doc.getString("id_usuario") == uid()) {
                                        grupo.miRol = doc.getString("rol")
                                    }
                                }
                            }
                        }
                        ListaGrupos.grupos.clear()
                        ListaGrupos.grupos.addAll(grupos.sortedBy { it.nombre })
                        alTerminar(null)
                    }
                    .addOnFailureListener { error -> alTerminar(error.message) }
            }
            .addOnFailureListener { error -> alTerminar(error.message) }
    }

    fun codigoAleatorio(): String {
        val letras = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        var codigo = ""
        repeat(8) { codigo += letras.random() }
        return codigo
    }

    fun crearGrupo(
        nombre: String, descripcion: String, tipo: String, materia: String, cupo: Int,
        alTerminar: (error: String?, codigo: String) -> Unit
    ) {
        val codigo = codigoAleatorio()
        val documentoGrupo = db.collection("grupos").document()
        val documentoMiembro = db.collection("miembros_grupo").document("${documentoGrupo.id}_${uid()}")

        val grupo = hashMapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "codigo_acceso" to codigo,
            "id_creador" to uid(),
            "fecha_creacion" to FieldValue.serverTimestamp(),
            "estado" to 1,
            "tipo" to tipo,
            "materia" to materia,
            "cupo" to cupo,
            "total_miembros" to 1
        )
        val miembro = datosMiembro(documentoGrupo.id, "ADMIN")

        val lote = db.batch()
        lote.set(documentoGrupo, grupo)
        lote.set(documentoMiembro, miembro)
        lote.commit()
            .addOnSuccessListener { alTerminar(null, codigo) }
            .addOnFailureListener { error -> alTerminar(error.message, "") }
    }

    fun datosMiembro(idGrupo: String, rol: String): HashMap<String, Any> {
        return hashMapOf(
            "id_grupo" to idGrupo,
            "id_usuario" to uid(),
            "rol" to rol,
            "iniciales" to Usuario.iniciales(),
            "fecha_union" to FieldValue.serverTimestamp(),
            "estado" to 1
        )
    }

    fun unirseConCodigo(codigo: String, alTerminar: (error: String?, nombreGrupo: String) -> Unit) {
        db.collection("grupos")
            .whereEqualTo("codigo_acceso", codigo.uppercase())
            .whereEqualTo("estado", 1)
            .get()
            .addOnSuccessListener { resultado ->
                if (resultado.isEmpty) {
                    alTerminar("No existe un grupo con ese código", "")
                } else {
                    val doc = resultado.documents[0]
                    val nombre = doc.getString("nombre") ?: ""
                    unirseAGrupo(doc.id) { error -> alTerminar(error, nombre) }
                }
            }
            .addOnFailureListener { error -> alTerminar(error.message, "") }
    }

    fun unirseAGrupo(idGrupo: String, alTerminar: (error: String?) -> Unit) {
        val documentoGrupo = db.collection("grupos").document(idGrupo)
        val documentoMiembro = db.collection("miembros_grupo").document("${idGrupo}_${uid()}")

        db.runTransaction { transaccion ->
            val grupo = transaccion.get(documentoGrupo)
            val miembro = transaccion.get(documentoMiembro)
            val total = grupo.getLong("total_miembros") ?: 0
            val cupo = grupo.getLong("cupo") ?: 5

            if (miembro.exists() && miembro.getLong("estado") == 1L) {
                throw Exception("Ya estás en este grupo")
            }
            if (total >= cupo) {
                throw Exception("El grupo ya está lleno")
            }
            transaccion.set(documentoMiembro, datosMiembro(idGrupo, "MIEMBRO"))
            transaccion.update(documentoGrupo, "total_miembros", total + 1)
            null
        }
            .addOnSuccessListener {
                crearAsignacionesDelGrupo(idGrupo)
                alTerminar(null)
            }
            .addOnFailureListener { error -> alTerminar(error.message) }
    }

    fun crearAsignacionesDelGrupo(idGrupo: String) {
        db.collection("tareas")
            .whereEqualTo("id_grupo", idGrupo)
            .whereEqualTo("estado", 1)
            .get()
            .addOnSuccessListener { resultado ->
                val lote = db.batch()
                for (doc in resultado) {
                    val asignacion = db.collection("asignaciones_tarea").document("${doc.id}_${uid()}")
                    lote.set(asignacion, datosAsignacion(doc.id, uid()), SetOptions.merge())
                }
                lote.commit()
            }
    }

    fun datosAsignacion(idTarea: String, idUsuario: String): HashMap<String, Any?> {
        return hashMapOf(
            "id_tarea" to idTarea,
            "id_usuario" to idUsuario,
            "estado" to "PENDIENTE",
            "fecha_completada" to null
        )
    }

    fun cargarTareas(contexto: Context, alTerminar: (error: String?) -> Unit) {
        db.collection("asignaciones_tarea").whereEqualTo("id_usuario", uid()).get()
            .addOnSuccessListener { asignaciones ->
                sinConexion = asignaciones.metadata.isFromCache
                val tareas = mutableListOf<Tarea>()
                var faltan = asignaciones.size()

                if (faltan == 0) {
                    ListaTareas.tareas.clear()
                    if (sinConexion) {
                        ListaTareas.tareas.addAll(CacheLocal.leerTareas(contexto, uid()))
                    } else {
                        CacheLocal.guardarTareas(contexto, uid(), ListaTareas.tareas)
                    }
                    alTerminar(null)
                    return@addOnSuccessListener
                }

                for (asignacion in asignaciones) {
                    val idTarea = asignacion.getString("id_tarea") ?: ""
                    db.collection("tareas").document(idTarea).get()
                        .addOnCompleteListener { resultado ->
                            val doc = resultado.result
                            if (resultado.isSuccessful && doc != null && doc.exists() && doc.getLong("estado") == 1L) {
                                val tarea = Tarea(
                                    doc.getString("titulo") ?: "",
                                    doc.getString("tipo") ?: "Tarea",
                                    doc.getTimestamp("fecha_entrega")?.toDate()?.time ?: 0,
                                    doc.getString("descripcion") ?: "",
                                    doc.getString("origen") ?: "PERSONAL",
                                    doc.getString("id_grupo"),
                                    doc.getString("id_materia"),
                                    asignacion.getString("estado") == "COMPLETADA"
                                )
                                tarea.id = doc.id
                                tarea.idAsignacion = asignacion.id
                                tarea.idCreador = doc.getString("id_creador") ?: ""
                                tareas.add(tarea)
                            }
                            faltan--
                            if (faltan == 0) {
                                ListaTareas.tareas.clear()
                                ListaTareas.tareas.addAll(tareas)
                                if (!sinConexion) CacheLocal.guardarTareas(contexto, uid(), tareas)
                                alTerminar(null)
                            }
                        }
                }
            }
            .addOnFailureListener { error ->
                val guardadas = CacheLocal.leerTareas(contexto, uid())
                if (guardadas.isEmpty()) {
                    alTerminar(error.message)
                } else {
                    sinConexion = true
                    ListaTareas.tareas.clear()
                    ListaTareas.tareas.addAll(guardadas)
                    alTerminar(null)
                }
            }
    }

    fun guardarTarea(tarea: Tarea, grupo: EquipoGrupo?) {
        val documentoTarea = db.collection("tareas").document()
        tarea.id = documentoTarea.id

        val datos = hashMapOf(
            "titulo" to tarea.titulo,
            "descripcion" to tarea.descripcion,
            "fecha_creacion" to FieldValue.serverTimestamp(),
            "fecha_entrega" to Timestamp(Date(tarea.fechaEntrega)),
            "origen" to tarea.origen,
            "id_creador" to uid(),
            "id_grupo" to tarea.idGrupo,
            "id_materia" to tarea.idMateria,
            "id_integracion" to null,
            "id_externo" to null,
            "estado" to 1,
            "tipo" to tarea.tipo
        )

        val lote = db.batch()
        lote.set(documentoTarea, datos)

        val alumnos = mutableListOf(uid())
        if (grupo != null) {
            for (idUsuario in grupo.miembrosUid) {
                if (!alumnos.contains(idUsuario)) alumnos.add(idUsuario)
            }
        }

        for (idUsuario in alumnos) {
            val asignacion = db.collection("asignaciones_tarea").document("${tarea.id}_$idUsuario")
            lote.set(asignacion, datosAsignacion(tarea.id, idUsuario))

            if (grupo != null && idUsuario != uid()) {
                val notificacion = hashMapOf(
                    "id_usuario" to idUsuario,
                    "id_tarea" to tarea.id,
                    "id_horario" to null,
                    "id_aviso" to null,
                    "titulo" to "Nueva tarea en ${grupo.nombre}",
                    "mensaje" to "${tarea.titulo} · entrega ${tarea.fecha()}",
                    "fecha_programada" to ahora(),
                    "fecha_envio" to ahora(),
                    "leida" to 0,
                    "estado" to "ENVIADA"
                )
                lote.set(db.collection("notificaciones").document(), notificacion)
            }
        }

        lote.commit()
        tarea.idAsignacion = "${tarea.id}_${uid()}"
        tarea.idCreador = uid()
        ListaTareas.tareas.add(tarea)
    }

    fun actualizarTarea(tarea: Tarea) {
        db.collection("tareas").document(tarea.id).update(
            "titulo", tarea.titulo,
            "tipo", tarea.tipo,
            "descripcion", tarea.descripcion,
            "fecha_entrega", Timestamp(Date(tarea.fechaEntrega)),
            "id_materia", tarea.idMateria
        )
    }

    fun eliminarTarea(tarea: Tarea) {
        db.collection("tareas").document(tarea.id).update("estado", 0)
        ListaTareas.tareas.remove(tarea)
    }

    fun cambiarCompletada(tarea: Tarea) {
        val asignacion = db.collection("asignaciones_tarea").document(tarea.idAsignacion)
        if (tarea.completada) {
            asignacion.update("estado", "COMPLETADA", "fecha_completada", FieldValue.serverTimestamp())
        } else {
            asignacion.update("estado", "PENDIENTE", "fecha_completada", null)
        }
    }

    fun cargarNotificaciones(alTerminar: (error: String?) -> Unit) {
        db.collection("notificaciones").whereEqualTo("id_usuario", uid()).get()
            .addOnSuccessListener { resultado ->
                val lista = mutableListOf<Notificacion>()
                for (doc in resultado) {
                    if (doc.getString("estado") == "CANCELADA") continue

                    var origen = "TAREA"
                    if (doc.getString("id_aviso") != null) origen = "AVISO"
                    if (doc.getString("id_horario") != null) origen = "HORARIO"

                    lista.add(
                        Notificacion(
                            doc.id,
                            doc.getString("titulo") ?: "",
                            doc.getString("mensaje") ?: "",
                            doc.getTimestamp("fecha_programada")?.toDate()?.time ?: 0,
                            origen,
                            doc.getLong("leida") == 1L
                        )
                    )
                }
                lista.sortByDescending { it.fechaProgramada }
                ListaNotificaciones.notificaciones.clear()
                ListaNotificaciones.notificaciones.addAll(lista)
                alTerminar(null)
            }
            .addOnFailureListener { error -> alTerminar(error.message) }
    }

    fun marcarLeida(notificacion: Notificacion) {
        notificacion.leida = true
        db.collection("notificaciones").document(notificacion.id).update("leida", 1)
    }

    fun marcarTodasLeidas() {
        val lote = db.batch()
        for (notificacion in ListaNotificaciones.notificaciones) {
            if (!notificacion.leida) {
                notificacion.leida = true
                lote.update(db.collection("notificaciones").document(notificacion.id), "leida", 1)
            }
        }
        lote.commit()
    }
}
