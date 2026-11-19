package com.sgap.app

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

object Tema {

    fun aplicar(context: Context) {
        val ajustes = context.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val modo = ajustes.getInt("modo_tema", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(modo)
    }

    fun cambiar(context: Context, oscuro: Boolean) {
        val modo = if (oscuro) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        context.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
            .edit().putInt("modo_tema", modo).apply()
        AppCompatDelegate.setDefaultNightMode(modo)
    }

    fun esOscuro(context: Context): Boolean {
        val noche = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return noche == Configuration.UI_MODE_NIGHT_YES
    }
}
