package com.sgap.app.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sgap.app.ui.theme.SGAPTheme
import com.sgap.app.viewmodel.LoginEstado
import com.sgap.app.viewmodel.LoginViewModel

/**
 * Pantalla de login "conectada": obtiene el ViewModel y le pasa el estado
 * al contenido. Cuando el login sale bien, avisa con [onLoginExitoso].
 */
@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    // Se ejecuta cuando loginExitoso cambia a true
    LaunchedEffect(estado.loginExitoso) {
        if (estado.loginExitoso) onLoginExitoso()
    }

    LoginContenido(
        estado = estado,
        onCorreoCambio = viewModel::onCorreoCambio,
        onContrasenaCambio = viewModel::onContrasenaCambio,
        onEntrar = viewModel::iniciarSesion,
        onCrearCuenta = { /* TODO: navegar a registro (siguiente paso) */ }
    )
}

/** Solo dibuja; no sabe nada del ViewModel. Así se puede previsualizar en Android Studio. */
@Composable
fun LoginContenido(
    estado: LoginEstado,
    onCorreoCambio: (String) -> Unit,
    onContrasenaCambio: (String) -> Unit,
    onEntrar: () -> Unit,
    onCrearCuenta: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SGAP",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tus tareas, exámenes y proyectos en un solo lugar",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = estado.correo,
                onValueChange = onCorreoCambio,
                label = { Text("Correo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = estado.contrasena,
                onValueChange = onContrasenaCambio,
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            // Mensaje de error (solo aparece si hay uno)
            if (estado.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = estado.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onEntrar,
                enabled = !estado.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (estado.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Entrar")
                }
            }

            TextButton(onClick = onCrearCuenta) {
                Text("¿No tienes cuenta? Regístrate", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

// Vista previa: se ve en el panel "Split/Design" de Android Studio sin correr la app
@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    SGAPTheme {
        LoginContenido(
            estado = LoginEstado(),
            onCorreoCambio = {},
            onContrasenaCambio = {},
            onEntrar = {},
            onCrearCuenta = {}
        )
    }
}
