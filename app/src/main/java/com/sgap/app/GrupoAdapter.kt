package com.sgap.app

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class GrupoAdapter(context: Context, val grupos: List<EquipoGrupo>) :
    ArrayAdapter<EquipoGrupo>(context, 0, grupos) {

    val coloresAvatar = listOf(
        R.color.acento_violeta,
        R.color.acento_turquesa,
        R.color.acento_coral,
        R.color.acento_ambar
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vista = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_grupo, parent, false)

        val grupo = grupos[position]

        val txtSiglas = vista.findViewById<TextView>(R.id.txtSiglas)
        val txtNombre = vista.findViewById<TextView>(R.id.txtNombre)
        val txtDetalle = vista.findViewById<TextView>(R.id.txtDetalle)
        val txtEtiqueta = vista.findViewById<TextView>(R.id.txtEtiqueta)
        val txtIntegrantes = vista.findViewById<TextView>(R.id.txtIntegrantes)
        val avatares = listOf<TextView>(
            vista.findViewById(R.id.avatar1),
            vista.findViewById(R.id.avatar2),
            vista.findViewById(R.id.avatar3),
            vista.findViewById(R.id.avatar4)
        )

        txtNombre.text = grupo.nombre
        txtDetalle.text = "${grupo.tipo} · ${grupo.materia}"
        txtIntegrantes.text = "${grupo.integrantes.size} integrantes"

        txtSiglas.text = grupo.siglas()
        txtSiglas.backgroundTintList = ColorStateList.valueOf(color(grupo.fondoIcono()))
        txtSiglas.setTextColor(color(grupo.colorIcono()))

        if (grupo.esMio()) {
            txtEtiqueta.text = "Tu equipo"
            txtEtiqueta.backgroundTintList = ColorStateList.valueOf(color(R.color.fondo))
            txtEtiqueta.setTextColor(color(R.color.primario))
            txtEtiqueta.visibility = View.VISIBLE
        } else if (grupo.tieneLugar()) {
            txtEtiqueta.text = "Con lugar"
            txtEtiqueta.backgroundTintList = ColorStateList.valueOf(color(R.color.borde))
            txtEtiqueta.setTextColor(color(R.color.texto))
            txtEtiqueta.visibility = View.VISIBLE
        } else {
            txtEtiqueta.visibility = View.GONE
        }

        val total = grupo.integrantes.size
        for (i in avatares.indices) {
            val avatar = avatares[i]
            val fondo = avatar.background.mutate() as GradientDrawable

            if (i >= total) {
                avatar.visibility = View.GONE
            } else if (i == 3 && total > 4) {
                avatar.visibility = View.VISIBLE
                avatar.text = "+${total - 3}"
                avatar.setTextColor(color(R.color.texto))
                fondo.setColor(color(R.color.borde))
            } else {
                avatar.visibility = View.VISIBLE
                avatar.text = grupo.integrantes[i]
                avatar.setTextColor(color(R.color.sobre_primario))
                fondo.setColor(color(coloresAvatar[i % coloresAvatar.size]))
            }
        }

        return vista
    }

    fun color(id: Int): Int {
        return ContextCompat.getColor(context, id)
    }
}
