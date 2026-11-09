package com.sgap.app

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

// Adaptador para cada clase del horario (layout item_clase.xml)
class ClaseAdapter(context: Context, val clases: List<Clase>) :
    ArrayAdapter<Clase>(context, 0, clases) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vista = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_clase, parent, false)

        val clase = clases[position]

        vista.findViewById<TextView>(R.id.txtInicio).text = clase.inicio
        vista.findViewById<TextView>(R.id.txtFin).text = clase.fin
        vista.findViewById<TextView>(R.id.txtMateria).text = clase.materia
        vista.findViewById<TextView>(R.id.txtLugar).text = "${clase.salon} · ${clase.profesor}"

        val barraColor = vista.findViewById<View>(R.id.barraColor)
        barraColor.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, clase.color))

        return vista
    }
}
