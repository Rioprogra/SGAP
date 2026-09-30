package com.sgap.app.ui.registro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sgap.app.ui.componentes.BotonPrincipal
import com.sgap.app.ui.componentes.CampoTexto
import com.sgap.app.ui.componentes.MarcaSgap
import com.sgap.app.ui.componentes.MensajeError
import com.sgap.app.ui.login.EnlaceInferior
import com.sgap.app.ui.theme.SGAPTheme
import com.sgap.app.viewmodel.RegistroEstado
import com.sgap.app.viewmodel.RegistroViewModel

/** Pantalla de registro con el mismo estilo que el login (no está en el Figma aún). */
@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit,
    viewModel: RegistroViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(estado.registroExitoso) {
        if (estado.registroExitoso) onRegistroExitoso()
    }

    RegistroContenido(
        estado = estado,
        onNombreCambio = viewModel::onNombreCambio,
        onCorreoCambio = viewModel::onCorreoCambio,
        onContrasenaCambio = viewModel::onContrasenaCambio,
        onCrearCuenta = viewModel::registrar,
        onIrALogin = onIrALogin
    )
}

@Composable
fun RegistroContenido(
    estado: RegistroEstado,
    onNombreCambio: (String) -> Unit,
    onCorreoCambio: (String) -> Unit,
    onContrasenaCambio: (String) -> Unit,
    onCrearCuenta: () -> Unit,
    onIrALogin: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            MarcaSgap()
            Spacer(Modifier.height(32.dp))

            CampoTexto(
                etiqueta = "Nombre",
                valor = estado.nombre,
                onCambio = onNombreCambio,
                placeholder = "Tu nombre completo"
            )
            Spacer(Modifier.height(16.dp))
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
                placeholder = "Mínimo 6 caracteres",
                esContrasena = true
            )

            MensajeError(estado.error)
            Spacer(Modifier.height(24.dp))

            BotonPrincipal(texto = "Crear cuenta", onClick = onCrearCuenta, cargando = estado.cargando)

            Spacer(Modifier.height(20.dp))
            EnlaceInferior(pregunta = "¿Ya tienes cuenta?", accion = "Inicia sesión", onClick = onIrALogin)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RegistroPreview() {
    SGAPTheme {
        RegistroContenido(RegistroEstado(), {}, {}, {}, {}, {})
    }
}
