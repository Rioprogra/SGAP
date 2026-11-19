package com.sgap.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

object Recordatorios {

    const val CANAL = "recordatorios_tareas"

    fun programar(context: Context, tarea: Tarea) {
        val nombre = "recordatorio_${tarea.id}"
        val workManager = WorkManager.getInstance(context)

        if (tarea.completada || tarea.id == "") {
            workManager.cancelUniqueWork(nombre)
            return
        }

        val cuando = Calendar.getInstance()
        cuando.timeInMillis = tarea.fechaEntrega
        cuando.add(Calendar.DAY_OF_MONTH, -1)
        cuando.set(Calendar.HOUR_OF_DAY, 9)
        cuando.set(Calendar.MINUTE, 0)
        cuando.set(Calendar.SECOND, 0)

        val avisos = context.getSharedPreferences("recordatorios", Context.MODE_PRIVATE)
        if (avisos.getLong(tarea.id, 0) == tarea.fechaEntrega) return

        var politica = ExistingWorkPolicy.REPLACE
        var espera = cuando.timeInMillis - System.currentTimeMillis()
        if (espera <= 0) {
            val finDeEntrega = tarea.fechaEntrega + TimeUnit.DAYS.toMillis(1)
            if (finDeEntrega > System.currentTimeMillis()) {
                espera = TimeUnit.SECONDS.toMillis(10)
                politica = ExistingWorkPolicy.KEEP
            } else {
                workManager.cancelUniqueWork(nombre)
                return
            }
        }

        val trabajo = OneTimeWorkRequestBuilder<RecordatorioWorker>()
            .setInitialDelay(espera, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "id" to tarea.id, "fecha" to tarea.fechaEntrega,
                    "titulo" to tarea.titulo, "tipo" to tarea.tipo
                )
            )
            .build()
        workManager.enqueueUniqueWork(nombre, politica, trabajo)
    }

    fun programarTodas(context: Context) {
        for (tarea in ListaTareas.tareas) {
            programar(context, tarea)
        }
    }

    fun cancelar(context: Context, tarea: Tarea) {
        WorkManager.getInstance(context).cancelUniqueWork("recordatorio_${tarea.id}")
    }

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(CANAL, "Recordatorios de tareas", NotificationManager.IMPORTANCE_DEFAULT)
            canal.description = "Avisos un día antes de cada entrega"
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    fun mostrar(context: Context, titulo: String, tipo: String) {
        crearCanal(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(context, MainActivity::class.java)
        val abrir = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(R.drawable.ic_campana)
            .setContentTitle("Mañana se entrega: $titulo")
            .setContentText("$tipo · no lo dejes para el último momento")
            .setContentIntent(abrir)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(titulo.hashCode(), notificacion)
    }
}

class RecordatorioWorker(context: Context, parametros: WorkerParameters) : Worker(context, parametros) {
    override fun doWork(): Result {
        val titulo = inputData.getString("titulo") ?: "Tarea"
        val tipo = inputData.getString("tipo") ?: "Tarea"
        Recordatorios.mostrar(applicationContext, titulo, tipo)

        applicationContext.getSharedPreferences("recordatorios", Context.MODE_PRIVATE)
            .edit()
            .putLong(inputData.getString("id") ?: "", inputData.getLong("fecha", 0))
            .apply()
        return Result.success()
    }
}
