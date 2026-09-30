package com.sgap.app.ui.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sgap.app.data.model.TipoTarea
import com.sgap.app.ui.componentes.BotonPrincipal
import com.sgap.app.ui.componentes.CampoTexto
import com.sgap.app.ui.componentes.EtiquetaCampo
import com.sgap.app.ui.componentes.FormaCampo
import com.sgap.app.ui.componentes.MensajeError
import com.sgap.app.ui.componentes.fechaCorta
import com.sgap.app.ui.theme.Borde
import com.sgap.app.ui.theme.Primario
import com.sgap.app.ui.theme.SGAPTheme
import com.sgap.app.ui.theme.TextoSecundario
import com.sgap.app.viewmodel.NuevaTareaEstado
import com.sgap.app.viewmodel.NuevaTareaViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Formulario para crear una tarea. Al guardar, llama a [onTerminar] para volver a Inicio. */
@Composable
fun NuevaTareaScreen(
    onTerminar: () -> Unit,
    viewModel: NuevaTareaViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(estado.guardada) {
        if (estado.guardada) onTerminar()
    }

    NuevaTareaContenido(
        estado = estado,
        onTituloCambio = viewModel::onTituloCambio,
        onTipoCambio = viewModel::onTipoCambio,
        onFechaCambio = viewModel::onFechaCambio,
        onDescripcionCambio = viewModel::onDescripcionCambio,
        onGuardar = viewModel::guardar,
        onRegresar = onTerminar
    )
}

@Composable
fun NuevaTareaContenido(
    estado: NuevaTareaEstado,
    onTituloCambio: (String) -> Unit,
    onTipoCambio: (TipoTarea) -> Unit,
    onFechaCambio: (LocalDate) -> Unit,
    onDescripcionCambio: (String) -> Unit,
    onGuardar: () -> Unit,
    onRegresar: () -> Unit
) {
    // Estado solo visual (no es dato de la tarea): si el calendario está abierto
    var mostrarCalendario by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { BarraSuperior(onRegresar) },
        bottomBar = {
            // El botón queda fijo abajo, como en el Figma; imePadding lo sube sobre el teclado
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(24.dp)
            ) {
                BotonPrincipal(texto = "Guardar tarea", onClick = onGuardar)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            CampoTexto(
                etiqueta = "Título",
                valor = estado.titulo,
                onCambio = onTituloCambio,
                placeholder = "Ej. Tarea de Base de Datos",
                error = estado.errorTitulo
            )
            Spacer(Modifier.height(20.dp))

            EtiquetaCampo("Tipo")
            SelectorTipo(seleccionado = estado.tipo, onTipo = onTipoCambio)
            Spacer(Modifier.height(20.dp))

            EtiquetaCampo("Fecha de entrega")
            CampoFecha(
                fecha = estado.fecha,
                hayError = estado.errorFecha != null,
                onClick = { mostrarCalendario = true }
            )
            MensajeError(estado.errorFecha)
            Spacer(Modifier.height(20.dp))

            CampoTexto(
                etiqueta = "Descripción (opcional)",
                valor = estado.descripcion,
                onCambio = onDescripcionCambio,
                placeholder = "Agrega detalles",
                unaLinea = false,
                lineasMinimas = 4
            )
        }
    }

    if (mostrarCalendario) {
        SelectorFecha(
            fechaInicial = estado.fecha,
            onElegir = {
                onFechaCambio(it)
                mostrarCalendario = false
            },
            onCancelar = { mostrarCalendario = false }
        )
    }
}

@Composable
private fun BarraSuperior(onRegresar: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .statusBarsPadding()
            .padding(start = 8.dp, top = 12.dp, bottom = 8.dp)
    ) {
        Text(
            "←",
            fontSize = 26.sp,
            modifier = Modifier
                .clip(FormaCampo)
                .clickable(onClick = onRegresar)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
        Text("Nueva tarea", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

/** Tres botones del mismo ancho: el elegido va relleno de índigo. */
@Composable
private fun SelectorTipo(seleccionado: TipoTarea, onTipo: (TipoTarea) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        TipoTarea.entries.forEach { tipo ->
            val activo = tipo == seleccionado
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(FormaCampo)
                    .background(if (activo) Primario else Color.White)
                    .border(1.dp, if (activo) Primario else Borde, FormaCampo)
                    .clickable { onTipo(tipo) }
            ) {
                Text(
                    tipo.etiqueta,
                    color = if (activo) Color.White else TextoSecundario,
                    fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

/** Parece un campo de texto, pero al tocarlo abre el calendario. */
@Composable
private fun CampoFecha(fecha: LocalDate?, hayError: Boolean, onClick: () -> Unit) {
    val colorBorde = if (hayError) MaterialTheme.colorScheme.error else Borde
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(FormaCampo)
            .background(Color.White)
            .border(1.dp, colorBorde, FormaCampo)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = if (fecha != null) fechaCorta(fecha) else "Seleccionar fecha",
            color = if (fecha != null) MaterialTheme.colorScheme.onSurface else TextoSecundario.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Calendario de Material 3 en un diálogo. El DatePicker trabaja en milisegundos
 * en hora UTC, por eso se convierte con ZoneOffset.UTC a LocalDate (solo fecha).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFecha(
    fechaInicial: LocalDate?,
    onElegir: (LocalDate) -> Unit,
    onCancelar: () -> Unit
) {
    val inicial = (fechaInicial ?: LocalDate.now())
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val estadoCalendario = rememberDatePickerState(initialSelectedDateMillis = inicial)

    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(onClick = {
                estadoCalendario.selectedDateMillis?.let { millis ->
                    onElegir(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                }
            }) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = estadoCalendario)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun NuevaTareaPreview() {
    SGAPTheme {
        NuevaTareaContenido(NuevaTareaEstado(), {}, {}, {}, {}, {}, {})
    }
}
