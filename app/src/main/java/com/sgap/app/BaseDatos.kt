package com.sgap.app

import android.content.Context
import android.graphics.Color
import com.google.firebase.firestore.DocumentSnapshot
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

    fun cargarPerfil(alTerminar: () -> Unit) {
        db.collection("usuarios").document(uid()).get()
            .addOnSuccessListener { doc ->
                Usuario.rol = doc.getString("rol") ?: "ALUMNO"
                Usuario.rolSolicitado = doc.getString("rol_solicitado") ?: Usuario.rol
                Usuario.estadoRol = doc.getString("estado_rol") ?: "APROBADO"
                Usuario.idGrupo = doc.getString("id_grupo") ?: ""
                Usuario.perfilCompleto = doc.getBoolean("perfil_completo") ?: false
                revisarAdmin(alTerminar)
            }
            .addOnFailureListener { revisarAdmin(alTerminar) }
    }

    fun revisarAdmin(alTerminar: () -> Unit) {
        val correo = FirebaseAuth.getInstance().currentUser?.email?.lowercase() ?: ""
        if (correo == "") {
            Usuario.esAdmin = false
            alTerminar()
            return
        }
        db.collection("administradores").document(correo).get()
            .addOnSuccessListener { doc ->
                Usuario.esAdmin = doc.exists()
                alTerminar()
            }
            .addOnFailureListener {
                Usuario.esAdmin = false
                alTerminar()
            }
    }

    fun guardarPerfil(rolSolicitado: String, idGrupo: String, alTerminar: (error: String?) -> Unit) {
        val datos = hashMapOf<String, Any>(
            "id_grupo" to idGrupo,
            "perfil_completo" to true
        )
        val yaLoTiene = Usuario.rol == rolSolicitado && Usuario.estadoRol == "APROBADO"
        if (!yaLoTiene) {
            datos["rol"] = "ALUMNO"
            datos["rol_solicitado"] = rolSolicitado
            if (rolSolicitado == "ALUMNO") {
                datos["estado_rol"] = "APROBADO"
            } else {
                datos["estado_rol"] = "PENDIENTE"
                datos["fecha_solicitud"] = FieldValue.serverTimestamp()
            }
        }

        db.collection("usuarios").document(uid()).set(datos, SetOptions.merge())
            .addOnSuccessListener {
                if (!yaLoTiene) {
                    Usuario.rol = "ALUMNO"
                    Usuario.rolSolicitado = rolSolicitado
                    Usuario.estadoRol = datos["estado_rol"] as String
                }
                Usuario.idGrupo = idGrupo
                Usuario.perfilCompleto = true

                var yaSoyMiembro = false
                for (grupo in ListaGrupos.grupos) {
                    if (grupo.id == idGrupo && grupo.esMio()) yaSoyMiembro = true
                }
                if (idGrupo == "" || yaSoyMiembro) {
                    alTerminar(null)
                } else {
                    unirseAGrupo(idGrupo) { error -> alTerminar(error) }
                }
            }
            .addOnFailureListener { error -> alTerminar(error.message) }
    }

    fun cargarSolicitudes(alTerminar: (lista: List<Solicitud>, error: String?) -> Unit) {
        db.collection("usuarios").whereEqualTo("estado_rol", "PENDIENTE").get()
            .addOnSuccessListener { resultado ->
                val lista = mutableListOf<Solicitud>()
                for (doc in resultado) {
                    val nombre = ((doc.getString("nombre") ?: "") + " " + (doc.getString("apellidos") ?: "")).trim()
                    lista.add(
                        Solicitud(
                            doc.id,
                            nombre,
                            doc.getString("correo") ?: "",
                            doc.getString("rol_solicitado") ?: "ALUMNO",
                            doc.getString("id_grupo") ?: ""
                        )
                    )
                }
                alTerminar(lista, null)
            }
            .addOnFailureListener { error -> alTerminar(listOf(), error.message) }
    }

    fun revisarSolicitud(solicitud: Solicitud, aprobar: Boolean, alTerminar: (error: String?) -> Unit) {
        val rolNuevo = if (aprobar) solicitud.rolSolicitado else "ALUMNO"
        val estado = if (aprobar) "APROBADO" else "RECHAZADO"
        db.collection("usuarios").document(solicitud.idUsuario).update(
            "rol", rolNuevo,
            "estado_rol", estado,
            "revisado_por", uid(),
            "fecha_revision", FieldValue.serverTimestamp()
        )
            .addOnSuccessListener { alTerminar(null) }
            .addOnFailureListener { error -> alTerminar(error.message) }
    }

    fun misGruposIds(alTerminar: (ids: List<String>) -> Unit) {
        db.collection("miembros_grupo")
            .whereEqualTo("id_usuario", uid())
            .whereEqualTo("estado", 1)
            .get()
            .addOnSuccessListener { resultado ->
                val ids = mutableListOf<String>()
                for (doc in resultado) {
                    val idGrupo = doc.getString("id_grupo") ?: ""
                    if (idGrupo != "" && !ids.contains(idGrupo)) ids.add(idGrupo)
                }
                alTerminar(ids)
            }
            .addOnFailureListener { alTerminar(listOf()) }
    }

    fun leerMateria(doc: DocumentSnapshot): Materia {
        return Materia(
            doc.id,
            doc.getString("nombre") ?: doc.id,
            doc.getString("id_grupo") ?: "",
            doc.getString("abreviatura") ?: "",
            doc.getString("profesor") ?: "",
            (doc.getLong("horas_semana") ?: 0).toInt(),
            doc.getString("color") ?: ""
        )
    }

    fun cargarMaterias(alTerminar: (materias: List<Materia>) -> Unit) {
        misGruposIds { ids ->
            if (ids.isEmpty()) {
                ListaMaterias.materias.clear()
                alTerminar(ListaMaterias.materias)
                return@misGruposIds
            }
            db.collection("materias").whereIn("id_grupo", ids.take(30)).get()
                .addOnSuccessListener { resultado ->
                    val materias = mutableListOf<Materia>()
                    for (doc in resultado) {
                        if ((doc.getLong("estado") ?: 1) != 1L) continue
                        materias.add(leerMateria(doc))
                    }
                    ListaMaterias.materias.clear()
                    ListaMaterias.materias.addAll(materias.sortedBy { it.nombre })
                    alTerminar(ListaMaterias.materias)
                }
                .addOnFailureListener { alTerminar(ListaMaterias.materias) }
        }
    }

    fun cargarHorario(alTerminar: (error: String?) -> Unit) {
        cargarMaterias {
            misGruposIds { ids ->
                if (ids.isEmpty()) {
                    ListaHorario.clases.clear()
                    alTerminar(null)
                    return@misGruposIds
                }
                db.collection("horarios").whereIn("id_grupo", ids.take(30)).get()
                    .addOnSuccessListener { resultado ->
                        val clases = mutableListOf<Clase>()
                        for (doc in resultado) {
                            if ((doc.getLong("estado") ?: 1) != 1L) continue
                            val idMateria = doc.getString("id_materia") ?: ""
                            val materia = ListaMaterias.buscar(idMateria)
                            val dia = (doc.getLong("dia_semana") ?: 1).toInt()
                            clases.add(
                                Clase(
                                    Fechas.dias[dia % 7],
                                    doc.getString("hora_inicio") ?: "",
                                    doc.getString("hora_fin") ?: "",
                                    materia?.nombre ?: "Materia",
                                    doc.getString("aula") ?: "",
                                    materia?.profesor ?: "",
                                    materia?.colorEntero() ?: Color.GRAY,
                                    idMateria,
                                    doc.id,
                                    dia
                                )
                            )
                        }
                        ListaHorario.clases.clear()
                        ListaHorario.clases.addAll(clases.sortedBy { it.inicio })
                        alTerminar(null)
                    }
                    .addOnFailureListener { error -> alTerminar(error.message) }
            }
        }
    }

    fun crearGrupoDesdeHorario(horario: HorarioLeido, nombre: String, alTerminar: (error: String?, codigo: String) -> Unit) {
        val codigo = codigoAleatorio()
        val documentoGrupo = db.collection("grupos").document()
        val idGrupo = documentoGrupo.id
        val documentoMiembro = db.collection("miembros_grupo").document("${idGrupo}_${uid()}")
        val soyTutor = Usuario.rol == "TUTOR"

        val grupo = hashMapOf(
            "nombre" to nombre,
            "descripcion" to horario.escuela,
            "codigo_acceso" to codigo,
            "id_creador" to uid(),
            "id_tutor" to if (soyTutor) uid() else "",
            "tutor_nombre" to if (soyTutor) Usuario.nombre else "",
            "fecha_creacion" to FieldValue.serverTimestamp(),
            "estado" to 1,
            "tipo" to "Salón",
            "materia" to "${horario.materias.size} materias",
            "cupo" to 60,
            "total_miembros" to 1
        )

        val lote = db.batch()
        lote.set(documentoGrupo, grupo)
        lote.set(documentoMiembro, datosMiembro(idGrupo, Usuario.rol))
        lote.commit()
            .addOnSuccessListener {
                Usuario.idGrupo = idGrupo
                db.collection("usuarios").document(uid()).update("id_grupo", idGrupo)
                guardarHorarioDeGrupo(idGrupo, horario) { error -> alTerminar(error, codigo) }
            }
            .addOnFailureListener { error -> alTerminar(error.message, "") }
    }

    fun guardarHorarioDeGrupo(idGrupo: String, horario: HorarioLeido, alTerminar: (error: String?) -> Unit) {
        val escrituras = mutableListOf<com.google.android.gms.tasks.Task<Void>>()
        val idsMaterias = HashMap<String, String>()
        for (materia in horario.materias) {
            val documento = db.collection("materias").document()
            idsMaterias[materia.nombre] = documento.id
            escrituras.add(
                documento.set(
                    hashMapOf(
                        "id_grupo" to idGrupo,
                        "nombre" to materia.nombre,
                        "abreviatura" to materia.abreviatura,
                        "profesor" to materia.profesor,
                        "profesor_corto" to materia.profesorCorto,
                        "horas_semana" to materia.horas,
                        "color" to materia.color,
                        "estado" to 1
                    )
                )
            )
        }
        for (clase in horario.clases) {
            val materia = horario.materiaDe(clase) ?: continue
            escrituras.add(
                db.collection("horarios").document().set(
                    hashMapOf(
                        "id_grupo" to idGrupo,
                        "id_materia" to idsMaterias[materia.nombre],
                        "dia_semana" to clase.dia,
                        "hora_inicio" to clase.inicio,
                        "hora_fin" to clase.fin,
                        "aula" to clase.aula,
                        "estado" to 1
                    )
                )
            )
        }
        com.google.android.gms.tasks.Tasks.whenAllComplete(escrituras)
            .addOnCompleteListener {
                var error: String? = null
                for (escritura in escrituras) {
                    if (!escritura.isSuccessful) error = escritura.exception?.message ?: "No se pudo guardar"
                }
                alTerminar(error)
            }
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
                                    val correo = doc.getString("correo") ?: ""
                                    if (correo != "") grupo.correos.add(correo)
                                    if (doc.getString("id_usuario") == uid()) {
                                        grupo.miRol = doc.getString("rol")
                                        if (correo == "" && Usuario.correo != "") {
                                            doc.reference.update("correo", Usuario.correo.lowercase())
                                        }
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

    fun datosMiembro(idGrupo: String, rol: String): HashMap<String, Any> {
        return hashMapOf(
            "id_grupo" to idGrupo,
            "id_usuario" to uid(),
            "rol" to rol,
            "iniciales" to Usuario.iniciales(),
            "correo" to Usuario.correo.lowercase(),
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
                            doc.getLong("leida") == 1L,
                            doc.getString("id_tarea")
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
