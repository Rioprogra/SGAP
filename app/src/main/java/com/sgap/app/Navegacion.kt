package com.sgap.app

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

object Navegacion {

    fun configurar(activity: Activity, pestanaActual: Int) {
        pestana(activity, pestanaActual, R.id.navInicio, R.id.icoInicio, R.id.txtNavInicio, Inicio::class.java)
        pestana(activity, pestanaActual, R.id.navGrupo, R.id.icoGrupo, R.id.txtNavGrupo, Grupo::class.java)
        pestana(activity, pestanaActual, R.id.navIA, 0, R.id.txtNavIA, Asistente::class.java)
        pestana(activity, pestanaActual, R.id.navHorario, R.id.icoHorario, R.id.txtNavHorario, Horario::class.java)
        pestana(activity, pestanaActual, R.id.navPerfil, R.id.icoPerfil, R.id.txtNavPerfil, Perfil::class.java)
    }

    private fun pestana(
        activity: Activity,
        pestanaActual: Int,
        idBoton: Int,
        idIcono: Int,
        idTexto: Int,
        pantalla: Class<*>
    ) {
        val boton = activity.findViewById<LinearLayout>(idBoton)
        val texto = activity.findViewById<TextView>(idTexto)

        if (idBoton == pestanaActual) {
            texto.setTypeface(texto.typeface, Typeface.BOLD)
            if (idIcono != 0) {
                val icono = activity.findViewById<ImageView>(idIcono)
                val color = ContextCompat.getColor(activity, R.color.primario)
                icono.setBackgroundResource(R.drawable.bg_nav_activo)
                icono.imageTintList = ColorStateList.valueOf(color)
                texto.setTextColor(color)
            }
        } else {
            boton.setOnClickListener {
                if (pantalla == Inicio::class.java) {
                    activity.finish()
                } else {
                    activity.startActivity(Intent(activity, pantalla))
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    if (activity !is Inicio) {
                        activity.finish()
                    }
                }
            }
        }
    }
}
