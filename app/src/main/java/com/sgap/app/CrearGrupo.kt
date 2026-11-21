package com.sgap.app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CrearGrupo : AppCompatActivity() {

    lateinit var imgFoto: ImageView
    lateinit var txtEstado: TextView
    lateinit var contResultado: LinearLayout
    lateinit var btnElegirFoto: Button

    var horario: HorarioLeido? = null
    var imagenActual: Bitmap? = null
    var lectura: Job? = null
    var idGrupoExistente = ""

    val elegirFoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) leerFoto(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_crear_grupo)

        imgFoto = findViewById(R.id.imgFoto)
        txtEstado = findViewById(R.id.txtEstado)
        contResultado = findViewById(R.id.contResultado)
        btnElegirFoto = findViewById(R.id.btnElegirFoto)

        findViewById<TextView>(R.id.btnRegresar).setOnClickListener { finish() }

        if (!Usuario.puedeCrearGrupos() && intent.getStringExtra("idGrupo") == null) {
            Toast.makeText(this, "Solo un jefe de grupo o tutor autorizado puede crear grupos", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        idGrupoExistente = intent.getStringExtra("idGrupo") ?: ""
        if (idGrupoExistente != "") {
            findViewById<Button>(R.id.btnCrear).text = "Guardar horario"
        }

        btnElegirFoto.setOnClickListener { elegirFoto.launch("image/*") }
        findViewById<Button>(R.id.btnCrear).setOnClickListener { crear() }
        findViewById<Button>(R.id.btnReintentar).setOnClickListener {
            val imagen = imagenActual
            if (imagen != null) analizar(imagen)
        }
        findViewById<TextView>(R.id.btnCancelar).setOnClickListener {
            lectura?.cancel()
            mostrarCargando(false)
            txtEstado.visibility = View.VISIBLE
            txtEstado.text = "Se canceló la lectura."
            findViewById<Button>(R.id.btnReintentar).visibility = View.VISIBLE
        }
    }

    fun leerFoto(uri: Uri) {
        val imagen = cargarImagen(uri)
        if (imagen == null) {
            Toast.makeText(this, "No se pudo abrir la foto", Toast.LENGTH_SHORT).show()
            return
        }
        imagenActual = imagen
        imgFoto.setImageBitmap(imagen)
        imgFoto.visibility = View.VISIBLE
        btnElegirFoto.text = "Elegir otra foto"
        analizar(imagen)
    }

    fun analizar(imagen: Bitmap) {
        contResultado.visibility = View.GONE
        txtEstado.visibility = View.GONE
        findViewById<Button>(R.id.btnReintentar).visibility = View.GONE
        findViewById<TextView>(R.id.txtCargando).text = "Buscando el grupo, las materias, los profesores y las horas..."
        mostrarCargando(true)

        lectura = lifecycleScope.launch {
            try {
                val leido = AsistenteIA.leerHorario(imagen) { mensaje ->
                    findViewById<TextView>(R.id.txtCargando).text = mensaje
                }
                horario = leido
                mostrarCargando(false)
                mostrarResultado(leido)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mostrarCargando(false)
                txtEstado.visibility = View.VISIBLE
                txtEstado.text = "⚠️ " + AsistenteIA.mensajeError(e)
                findViewById<Button>(R.id.btnReintentar).visibility = View.VISIBLE
            }
        }
    }

    fun mostrarCargando(visible: Boolean) {
        findViewById<View>(R.id.pantallaCargando).visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun cargarImagen(uri: Uri): Bitmap? {
        return try {
            val entrada = contentResolver.openInputStream(uri) ?: return null
            val original = BitmapFactory.decodeStream(entrada)
            entrada.close()
            val lado = maxOf(original.width, original.height)
            if (lado <= 1600) {
                original
            } else {
                val escala = 1600f / lado
                Bitmap.createScaledBitmap(original, (original.width * escala).toInt(), (original.height * escala).toInt(), true)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun mostrarResultado(leido: HorarioLeido) {
        val txtNombreGrupo = findViewById<EditText>(R.id.txtNombreGrupo)
        if (idGrupoExistente != "") {
            txtNombreGrupo.setText(intent.getStringExtra("nombreGrupo") ?: leido.grupo)
            txtNombreGrupo.isEnabled = false
        } else {
            txtNombreGrupo.setText(leido.grupo)
        }

        val dias = mutableListOf<String>()
        for (clase in leido.clases.sortedBy { it.dia }) {
            val dia = Fechas.dias[clase.dia % 7]
            if (!dias.contains(dia)) dias.add(dia)
        }
        findViewById<TextView>(R.id.txtResumen).text =
            "${leido.materias.size} materias · ${leido.horasTotales()} horas a la semana · ${dias.joinToString(", ")}"

        val contMaterias = findViewById<LinearLayout>(R.id.contMaterias)
        contMaterias.removeAllViews()
        for (materia in leido.materias) {
            val vista = LayoutInflater.from(this).inflate(R.layout.item_materia, contMaterias, false)
            vista.findViewById<TextView>(R.id.txtNombre).text = materia.nombre
            vista.findViewById<TextView>(R.id.txtDetalle).text = "${materia.profesor} · ${materia.horas} h por semana"
            val color = Materia("", materia.nombre, color = materia.color).colorEntero()
            vista.findViewById<View>(R.id.barraColor).backgroundTintList = ColorStateList.valueOf(color)
            contMaterias.addView(vista)
        }
        contResultado.visibility = View.VISIBLE
        if (!leido.cuadra()) {
            txtEstado.visibility = View.VISIBLE
            txtEstado.text = "⚠️ La tabla dice ${leido.horasDeLaTabla()} horas pero se leyeron ${leido.horasTotales()}. Revisa el horario después de crear el grupo o usa una foto más clara."
        }
        contResultado.post {
            val scroll = contResultado.parent.parent as ScrollView
            scroll.smoothScrollTo(0, imgFoto.top)
        }
    }

    fun crear() {
        val leido = horario ?: return
        val txtNombre = findViewById<EditText>(R.id.txtNombreGrupo)
        val nombre = txtNombre.text.toString().trim().uppercase()
        if (nombre.length < 2) {
            txtNombre.error = "Escribe el nombre del grupo"
            return
        }
        if (idGrupoExistente != "") {
            guardarEnGrupoExistente(leido)
            return
        }
        for (grupo in ListaGrupos.grupos) {
            if (grupo.nombre.equals(nombre, true)) {
                txtNombre.error = "Ya existe un grupo con ese nombre"
                return
            }
        }

        val btnCrear = findViewById<Button>(R.id.btnCrear)
        btnCrear.isEnabled = false
        btnCrear.text = "Creando..."
        BaseDatos.crearGrupoDesdeHorario(leido, nombre) { error, codigo ->
            if (error != null && codigo == "") {
                btnCrear.isEnabled = true
                btnCrear.text = "Crear grupo"
                Toast.makeText(this, "No se pudo crear: $error", Toast.LENGTH_LONG).show()
                return@crearGrupoDesdeHorario
            }
            if (error != null) {
                Toast.makeText(this, "El grupo se creó, pero no se pudo guardar el horario: $error", Toast.LENGTH_LONG).show()
                finish()
                return@crearGrupoDesdeHorario
            }
            AlertDialog.Builder(this)
                .setTitle("¡Grupo $nombre creado!")
                .setMessage("Se guardaron ${leido.materias.size} materias y el horario de la semana.\n\nComparte este código con tus compañeros para que se unan:\n\n$codigo")
                .setCancelable(false)
                .setPositiveButton("Ver horario") { _, _ ->
                    startActivity(Intent(this, Horario::class.java))
                    finish()
                }
                .show()
        }
    }

    fun guardarEnGrupoExistente(leido: HorarioLeido) {
        val btnCrear = findViewById<Button>(R.id.btnCrear)
        btnCrear.isEnabled = false
        btnCrear.text = "Guardando..."
        BaseDatos.guardarHorarioDeGrupo(idGrupoExistente, leido) { error ->
            if (error != null) {
                btnCrear.isEnabled = true
                btnCrear.text = "Guardar horario"
                Toast.makeText(this, "No se pudo guardar: $error", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Se guardaron ${leido.materias.size} materias y el horario", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, Horario::class.java))
                finish()
            }
        }
    }
}
