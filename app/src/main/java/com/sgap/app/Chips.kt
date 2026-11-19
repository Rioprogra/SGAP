package com.sgap.app

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.ContextCompat

object Chips {
    fun pintar(context: Context, chip: TextView, seleccionado: Boolean, colorNormal: Int = R.color.texto_suave) {
        if (seleccionado) {
            chip.setBackgroundResource(R.drawable.bg_chip_activo)
            chip.setTextColor(ContextCompat.getColor(context, R.color.sobre_primario))
            chip.setTypeface(null, Typeface.BOLD)
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip)
            chip.setTextColor(ContextCompat.getColor(context, colorNormal))
            chip.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
    }
}
