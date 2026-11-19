package com.sgap.app

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class CanalAdapter(context: Context, val canales: List<Canal>) :
    ArrayAdapter<Canal>(context, 0, canales) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vista = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_canal, parent, false)

        val canal = canales[position]

        val txtIcono = vista.findViewById<TextView>(R.id.txtIcono)
        val txtNombre = vista.findViewById<TextView>(R.id.txtNombre)
        val txtMensaje = vista.findViewById<TextView>(R.id.txtMensaje)
        val txtHora = vista.findViewById<TextView>(R.id.txtHora)
        val txtNoLeidos = vista.findViewById<TextView>(R.id.txtNoLeidos)

        txtNombre.text = canal.nombre
        txtMensaje.text = canal.ultimoMensaje
        txtHora.text = canal.hora

        txtIcono.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, canal.fondoIcono))
        txtIcono.setTextColor(ContextCompat.getColor(context, canal.colorIcono))

        if (canal.noLeidos > 0) {
            txtNoLeidos.text = canal.noLeidos.toString()
            txtNoLeidos.visibility = View.VISIBLE
        } else {
            txtNoLeidos.visibility = View.GONE
        }

        return vista
    }
}
