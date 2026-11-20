package com.sgap.app

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

object AvisosNuevos {

    fun programar(context: Context) {
        val conInternet = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodico = PeriodicWorkRequestBuilder<AvisosWorker>(15, TimeUnit.MINUTES)
            .setConstraints(conInternet)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork("avisos_nuevos", ExistingPeriodicWorkPolicy.KEEP, periodico)
    }

    fun revisarAhora(context: Context) {
        val unaVez = OneTimeWorkRequestBuilder<AvisosWorker>().build()
        WorkManager.getInstance(context).enqueue(unaVez)
    }
}

class AvisosWorker(context: Context, parametros: WorkerParameters) : Worker(context, parametros) {
    override fun doWork(): Result {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()

        val resultado = try {
            Tasks.await(
                FirebaseFirestore.getInstance().collection("notificaciones")
                    .whereEqualTo("id_usuario", uid)
                    .whereEqualTo("leida", 0)
                    .get(),
                30, TimeUnit.SECONDS
            )
        } catch (e: Exception) {
            return Result.retry()
        }

        val mostradas = applicationContext.getSharedPreferences("avisos_mostrados", Context.MODE_PRIVATE)
        val haceDosDias = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)

        for (doc in resultado) {
            if (mostradas.getBoolean(doc.id, false)) continue
            if (doc.getString("estado") == "CANCELADA") continue
            val fecha = doc.getTimestamp("fecha_envio")?.toDate()?.time ?: 0
            if (fecha < haceDosDias) continue

            Recordatorios.avisar(
                applicationContext,
                doc.getString("titulo") ?: "Nuevo aviso",
                doc.getString("mensaje") ?: "",
                doc.getString("id_tarea"),
                doc.id.hashCode()
            )
            mostradas.edit().putBoolean(doc.id, true).apply()
        }
        return Result.success()
    }
}
