package com.sgap.app

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

object AvisoClases {

    const val MINUTOS_ANTES = 10

    fun programarTodas(context: Context) {
        for (clase in ListaHorario.clases) {
            programar(context, clase.idHorario, clase.diaNumero, clase.inicio, clase.materia, clase.salon, clase.profesor)
        }
    }

    fun siguienteAviso(diaNumero: Int, inicio: String): Long {
        val hora = inicio.substringBefore(":").toIntOrNull() ?: 7
        val minuto = inicio.substringAfter(":", "0").toIntOrNull() ?: 0

        val aviso = Calendar.getInstance()
        val diaCalendario = if (diaNumero == 7) Calendar.SUNDAY else diaNumero + 1
        aviso.set(Calendar.DAY_OF_WEEK, diaCalendario)
        aviso.set(Calendar.HOUR_OF_DAY, hora)
        aviso.set(Calendar.MINUTE, minuto)
        aviso.set(Calendar.SECOND, 0)
        aviso.set(Calendar.MILLISECOND, 0)
        aviso.add(Calendar.MINUTE, -MINUTOS_ANTES)

        while (aviso.timeInMillis <= System.currentTimeMillis()) {
            aviso.add(Calendar.DAY_OF_MONTH, 7)
        }
        while (aviso.timeInMillis - System.currentTimeMillis() > TimeUnit.DAYS.toMillis(7)) {
            aviso.add(Calendar.DAY_OF_MONTH, -7)
        }
        return aviso.timeInMillis
    }

    fun programar(
        context: Context, idHorario: String, diaNumero: Int, inicio: String,
        materia: String, salon: String, profesor: String
    ) {
        if (idHorario == "") return
        val espera = siguienteAviso(diaNumero, inicio) - System.currentTimeMillis()
        val trabajo = OneTimeWorkRequestBuilder<AvisoClaseWorker>()
            .setInitialDelay(espera, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "idHorario" to idHorario, "dia" to diaNumero, "inicio" to inicio,
                    "materia" to materia, "salon" to salon, "profesor" to profesor
                )
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork("clase_$idHorario", ExistingWorkPolicy.REPLACE, trabajo)
    }
}

class AvisoClaseWorker(context: Context, parametros: WorkerParameters) : Worker(context, parametros) {
    override fun doWork(): Result {
        val idHorario = inputData.getString("idHorario") ?: ""
        val dia = inputData.getInt("dia", 1)
        val inicio = inputData.getString("inicio") ?: ""
        val materia = inputData.getString("materia") ?: "Clase"
        val salon = inputData.getString("salon") ?: ""
        val profesor = inputData.getString("profesor") ?: ""

        var texto = "Empieza a las $inicio"
        if (salon != "") texto += " en el salón $salon"
        if (profesor != "") texto += " · $profesor"

        Recordatorios.avisar(
            applicationContext,
            "En ${AvisoClases.MINUTOS_ANTES} min: $materia",
            texto,
            null,
            ("clase_$idHorario").hashCode()
        )

        AvisoClases.programar(applicationContext, idHorario, dia, inicio, materia, salon, profesor)
        return Result.success()
    }
}
