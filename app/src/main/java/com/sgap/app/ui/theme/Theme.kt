package com.sgap.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Asigna nuestra paleta a los "roles" de color de Material 3.
// Los componentes (botones, campos de texto...) toman sus colores de aquí.
private val EsquemaClaro = lightColorScheme(
    primary = Primario,
    onPrimary = Color.White,
    secondary = Secundario,
    onSecondary = Color.White,
    background = Fondo,
    onBackground = Texto,
    surface = Superficie,
    onSurface = Texto,
    error = ColorError,
    onError = Color.White
)

/** Envuelve cualquier pantalla para que use los colores y la tipografía de SGAP. */
@Composable
fun SGAPTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = Tipografia,
        content = content
    )
}
