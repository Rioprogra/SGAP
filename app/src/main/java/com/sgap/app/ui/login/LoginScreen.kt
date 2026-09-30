package com.sgap.app.ui.login

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sgap.app.ui.componentes.BotonPrincipal
import com.sgap.app.ui.componentes.CampoTexto
import com.sgap.app.ui.componentes.MarcaSgap
import com.sgap.app.ui.componentes.MensajeError
import com.sgap.app.ui.theme.Primario
import com.sgap.app.ui.theme.SGAPTheme
import com.sgap.app.ui.theme.TextoSecundario
import com.sgap.app.viewmodel.LoginEstado
import com.sgap.app.viewmodel.LoginViewModel

/**
 * Pantalla de login "conectada": obtiene el ViewModel y le pasa el estado
 * al contenido. Cuando el login sale bien, avisa con [onLoginExitoso].
 */
@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current

    // Se ejecuta cuando loginExitoso cambia a true
    LaunchedEffect(estado.loginExitoso) {
        if (estado.loginExitoso) onLoginExitoso()
    }

    LoginContenido(
        estado = estado,
        onCorreoCambio = viewModel::onCorreoCambio,
        onContrasenaCambio = viewModel::onContrasenaCambio,
        onEntrar = viewModel::iniciarSesion,
        onOlvideContrasena = {
            Toast.makeText(contexto, "Disponible próximamente", Toast.LENGTH_SHORT).show()
        },
        onCrearCuenta = onIrARegistro
    )
}

/** Solo dibuja (diseño del Figma); no sabe nada del ViewModel. Así se puede previsualizar. */
@Composable
fun LoginContenido(
    estado: LoginEstado,
    onCorreoCambio: (String) -> Unit,
    onContrasenaCambio: (String) -> Unit,
    onEntrar: () -> Unit,
    onOlvideContrasena: () -> Unit,
    onCrearCuenta: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()) // para que el teclado no tape los campos
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            MarcaSgap()
            Spacer(Modifier.height(40.dp))

            CampoTexto(
                etiqueta = "Correo institucional",
                valor = estado.correo,
                onCambio = onCorreoCambio,
                placeholder = "matricula@utcj.edu.mx",
                tipoTeclado = KeyboardType.Email
            )
            Spacer(Modifier.height(16.dp))
            CampoTexto(
                etiqueta = "Contraseña",
                valor = estado.contrasena,
                onCambio = onContrasenaCambio,
                placeholder = "••••••••",
                esContrasena = true
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "¿Olvidaste tu contraseña?",
                color = Primario,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(onClick = onOlvideContrasena)
            )

            MensajeError(estado.error)
            Spacer(Modifier.height(24.dp))

            BotonPrincipal(texto = "Iniciar sesión", onClick = onEntrar, cargando = estado.cargando)

            Spacer(Modifier.height(20.dp))
            EnlaceInferior(
                pregunta = "¿No tienes cuenta?",
                accion = "Regístrate",
                onClick = onCrearCuenta
            )
        }
    }
}

/** Renglón tipo "¿No tienes cuenta? Regístrate" donde solo la acción es tocable. */
@Composable
fun EnlaceInferior(pregunta: String, accion: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(pregunta, color = TextoSecundario, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = " $accion",
            color = Primario,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable(onClick = onClick)
        )
    }
}

// Vista previa: se ve en el panel "Split/Design" de Android Studio sin correr la app
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginPreview() {
    SGAPTheme {
        LoginContenido(
            estado = LoginEstado(),
            onCorreoCambio = {},
            onContrasenaCambio = {},
            onEntrar = {},
            onOlvideContrasena = {},
            onCrearCuenta = {}
        )
    }
}
