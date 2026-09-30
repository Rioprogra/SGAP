package com.sgap.app.ui.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sgap.app.data.model.Tarea
import com.sgap.app.data.model.TipoTarea
import com.sgap.app.ui.componentes.colorDeTipo
import com.sgap.app.ui.componentes.fechaLarga
import com.sgap.app.ui.componentes.fechaRelativa
import com.sgap.app.ui.theme.Borde
import com.sgap.app.ui.theme.Primario
import com.sgap.app.ui.theme.SGAPTheme
import com.sgap.app.ui.theme.TextoSecundario
import com.sgap.app.viewmodel.Filtro
import com.sgap.app.viewmodel.InicioEstado
import com.sgap.app.viewmodel.InicioViewModel
import java.time.LocalDate

/** Pantalla "Mis pendientes" conectada a su ViewModel. */
@Composable
fun InicioScreen(
    onNuevaTarea: () -> Unit,
    viewModel: InicioViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current

    InicioContenido(
        estado = estado,
        onFiltro = viewModel::cambiarFiltro,
        onAlternarCompletada = viewModel::alternarCompletada,
        onNuevaTarea = onNuevaTarea,
        onSeccionPendiente = { nombre ->
            Toast.makeText(contexto, "$nombre: disponible próximamente", Toast.LENGTH_SHORT).show()
        }
    )
}

/** Solo dibuja (diseño del Figma). Recibe el estado y avisa de los toques con funciones. */
@Composable
fun InicioContenido(
    estado: InicioEstado,
    onFiltro: (Filtro) -> Unit,
    onAlternarCompletada: (Long) -> Unit,
    onNuevaTarea: () -> Unit,
    onSeccionPendiente: (String) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevaTarea,
                shape = CircleShape,
                containerColor = Primario,
                contentColor = Color.White
            ) {
                Text("+", fontSize = 30.sp, fontWeight = FontWeight.Medium)
            }
        },
        bottomBar = { BarraInferior(onSeccionPendiente) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Encabezado()
            FiltrosChips(seleccionado = estado.filtro, onFiltro = onFiltro)

            if (estado.tareas.isEmpty()) {
                ListaVacia(hayTareas = estado.hayTareas)
            } else {
                // LazyColumn: lista con scroll que solo dibuja lo que se ve en pantalla
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(estado.tareas, key = { it.id }) { tarea ->
                        TarjetaTarea(tarea = tarea, onAlternarCompletada = { onAlternarCompletada(tarea.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun Encabezado() {
    Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)) {
        Text(
            fechaLarga(LocalDate.now()),
            color = TextoSecundario,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(4.dp))
        Text("Mis pendientes", fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FiltrosChips(seleccionado: Filtro, onFiltro: (Filtro) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Filtro.entries.forEach { filtro ->
            val activo = filtro == seleccionado
            Text(
                text = filtro.etiqueta,
                color = if (activo) Color.White else TextoSecundario,
                fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (activo) Primario else Color.White)
                    .border(1.dp, if (activo) Primario else Borde, CircleShape)
                    .clickable { onFiltro(filtro) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }
    }
}

/** Tarjeta de un pendiente: barra de color, título, etiqueta de tipo, fecha y círculo para completar. */
@Composable
private fun TarjetaTarea(tarea: Tarea, onAlternarCompletada: () -> Unit) {
    val color = colorDeTipo(tarea.tipo)
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tarea.titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (tarea.completada) TextoSecundario else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (tarea.completada) TextDecoration.LineThrough else null
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tarea.tipo.etiqueta,
                        color = color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        fechaRelativa(tarea.fechaEntrega),
                        color = TextoSecundario,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            CirculoCompletar(completada = tarea.completada, color = color, onClick = onAlternarCompletada)
        }
    }
}

/** Círculo vacío = pendiente; círculo lleno con ✓ = completada. */
@Composable
private fun CirculoCompletar(completada: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp) // área tocable más grande que el círculo
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (completada) color else Color.White)
                .border(1.5.dp, if (completada) color else Borde, CircleShape)
        ) {
            if (completada) Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ListaVacia(hayTareas: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            if (hayTareas) "No tienes pendientes de este tipo" else "Aún no tienes pendientes",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Toca + para agregar una tarea, examen o proyecto",
            color = TextoSecundario,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

/** Barra de abajo del Figma. Por ahora solo "Inicio" funciona. */
@Composable
private fun BarraInferior(onSeccionPendiente: (String) -> Unit) {
    Column(
        modifier = Modifier
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = Borde)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 12.dp)
        ) {
            listOf("Inicio", "Grupo", "Horario", "Perfil").forEach { nombre ->
                val activo = nombre == "Inicio"
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !activo) { onSeccionPendiente(nombre) }
                ) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(3.dp)
                            .background(if (activo) Primario else Color.Transparent, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        nombre,
                        fontSize = 12.sp,
                        color = if (activo) Primario else TextoSecundario,
                        fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun InicioPreview() {
    val hoy = LocalDate.now()
    val ejemplo = listOf(
        Tarea(1, "Avance de la app móvil", TipoTarea.PROYECTO, hoy.plusDays(1)),
        Tarea(2, "Tarea de Base de Datos", TipoTarea.TAREA, hoy.plusDays(2)),
        Tarea(3, "Examen de Estructura de Datos", TipoTarea.EXAMEN, hoy.plusDays(5), completada = true)
    )
    SGAPTheme {
        InicioContenido(InicioEstado(Filtro.TODOS, ejemplo, true), {}, {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun InicioVacioPreview() {
    SGAPTheme {
        InicioContenido(InicioEstado(), {}, {}, {}, {})
    }
}
