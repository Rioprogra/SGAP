package com.sgap.app

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

class TareaAdapter(
    context: Context,
    val tareas: List<Tarea>,
    val alMarcar: (tarea: Tarea) -> Unit
) : ArrayAdapter<Tarea>(context, 0, tareas) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vista = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_pendiente, parent, false)
        val tarea = tareas[position]

        val barraTipo = vista.findViewById<View>(R.id.barraTipo)
        val txtTitulo = vista.findViewById<TextView>(R.id.txtTitulo)
        val txtTipo = vista.findViewById<TextView>(R.id.txtTipo)
        val txtFecha = vista.findViewById<TextView>(R.id.txtFecha)
        val imgCheck = vista.findViewById<ImageView>(R.id.imgCheck)

        txtTitulo.text = tarea.titulo
        txtTipo.text = tarea.tipo
        txtFecha.text = tarea.detalle()

        barraTipo.backgroundTintList = ColorStateList.valueOf(color(tarea.color()))
        txtTipo.setTextColor(color(tarea.color()))
        txtTipo.backgroundTintList = ColorStateList.valueOf(color(tarea.colorSuave()))

        if (tarea.vencida()) {
            txtFecha.setTextColor(color(R.color.error))
        } else if (!tarea.completada && tarea.diasRestantes() <= 1) {
            txtFecha.setTextColor(color(R.color.acento_coral))
        } else {
            txtFecha.setTextColor(color(R.color.texto_suave))
        }

        if (tarea.completada) {
            imgCheck.setImageResource(R.drawable.ic_check_lleno)
            txtTitulo.paintFlags = txtTitulo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            vista.alpha = 0.6f
        } else {
            imgCheck.setImageResource(R.drawable.ic_check_vacio)
            txtTitulo.paintFlags = txtTitulo.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            vista.alpha = 1f
        }

        imgCheck.setOnClickListener { alMarcar(tarea) }

        return vista
    }

    fun color(id: Int): Int {
        return ContextCompat.getColor(context, id)
    }
}
