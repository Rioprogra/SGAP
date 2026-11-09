package com.sgap.app

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

// Hace que funcione la barra de abajo (layout barra_navegacion.xml).
// Cada pestana es una Activity diferente y se cambia con un Intent.
//
// Para no llenar la pila de pantallas:
//  - Inicio siempre se queda abajo de todo.
//  - Grupo, Horario y Perfil se cierran (finish) cuando cambias de pestana.
//  - Asi el boton "atras" del celular siempre te regresa a Inicio.
object Navegacion {

    fun configurar(activity: Activity, pestanaActual: Int) {
        pestana(activity, pestanaActual, R.id.navInicio, R.id.indInicio, R.id.txtNavInicio, Inicio::class.java)
        pestana(activity, pestanaActual, R.id.navGrupo, R.id.indGrupo, R.id.txtNavGrupo, Grupo::class.java)
        pestana(activity, pestanaActual, R.id.navHorario, R.id.indHorario, R.id.txtNavHorario, Horario::class.java)
        pestana(activity, pestanaActual, R.id.navPerfil, R.id.indPerfil, R.id.txtNavPerfil, Perfil::class.java)
    }

    private fun pestana(
        activity: Activity,
        pestanaActual: Int,
        idBoton: Int,
        idIndicador: Int,
        idTexto: Int,
        pantalla: Class<*>
    ) {
        val boton = activity.findViewById<LinearLayout>(idBoton)
        val indicador = activity.findViewById<View>(idIndicador)
        val texto = activity.findViewById<TextView>(idTexto)

        if (idBoton == pestanaActual) {
            // Pestana en la que estamos: rayita azul y texto en negritas
            indicador.visibility = View.VISIBLE
            texto.setTextColor(ContextCompat.getColor(activity, R.color.primario))
            texto.setTypeface(texto.typeface, Typeface.BOLD)
        } else {
            boton.setOnClickListener {
                if (pantalla == Inicio::class.java) {
                    // Inicio ya esta abajo, solo cerramos la pantalla actual
                    activity.finish()
                } else {
                    activity.startActivity(Intent(activity, pantalla))
                    if (activity !is Inicio) {
                        activity.finish()
                    }
                }
            }
        }
    }
}
