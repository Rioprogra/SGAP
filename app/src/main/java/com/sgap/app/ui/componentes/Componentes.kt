package com.sgap.app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sgap.app.data.model.TipoTarea
import com.sgap.app.ui.theme.Borde
import com.sgap.app.ui.theme.ColorExamen
import com.sgap.app.ui.theme.ColorProyecto
import com.sgap.app.ui.theme.ColorTarea
import com.sgap.app.ui.theme.Primario
import com.sgap.app.ui.theme.TextoSecundario

// Piezas de interfaz que se repiten en varias pantallas (estilo del Figma).

/** Forma redondeada de campos y botones. */
val FormaCampo = RoundedCornerShape(10.dp)

/** Color que identifica cada tipo de pendiente (sección 10 de CLAUDE.md). */
fun colorDeTipo(tipo: TipoTarea): Color = when (tipo) {
    TipoTarea.TAREA -> ColorTarea
    TipoTarea.EXAMEN -> ColorExamen
    TipoTarea.PROYECTO -> ColorProyecto
}

/** Etiqueta pequeña arriba de un campo, como "Correo institucional". */
@Composable
fun EtiquetaCampo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium
    )
    Spacer(Modifier.height(8.dp))
}

/**
 * Campo de texto con etiqueta arriba y texto de ejemplo (placeholder) adentro.
 * Si [error] no es null, el borde se pone rojo y el mensaje aparece abajo.
 */
@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    esContrasena: Boolean = false,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    unaLinea: Boolean = true,
    lineasMinimas: Int = 1,
    error: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        EtiquetaCampo(etiqueta)
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            placeholder = { Text(placeholder, color = TextoSecundario.copy(alpha = 0.7f)) },
            singleLine = unaLinea,
            minLines = lineasMinimas,
            isError = error != null,
            shape = FormaCampo,
            visualTransformation = if (esContrasena) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (esContrasena) KeyboardType.Password else tipoTeclado
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                errorContainerColor = Color.White,
                unfocusedBorderColor = Borde,
                focusedBorderColor = Primario
            ),
            modifier = Modifier.fillMaxWidth()
        )
        MensajeError(error)
    }
}

/** Texto rojo bajo un campo; no dibuja nada si no hay error. */
@Composable
fun MensajeError(error: String?) {
    if (error != null) {
        Spacer(Modifier.height(4.dp))
        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

/** Botón índigo de ancho completo. Muestra un circulito mientras [cargando]. */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cargando: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(texto, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Cuadro índigo con la "S" del Figma, más el nombre y el subtítulo. */
@Composable
fun MarcaSgap() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(76.dp)
                .background(Primario, RoundedCornerShape(18.dp))
        ) {
            Text("S", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        Text("SGAP", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(
            "Sistema de Gestión Académica Personal",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario
        )
    }
}
