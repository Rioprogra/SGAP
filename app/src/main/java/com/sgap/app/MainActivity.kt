package com.sgap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sgap.app.ui.home.InicioScreen
import com.sgap.app.ui.login.LoginScreen
import com.sgap.app.ui.registro.RegistroScreen
import com.sgap.app.ui.tareas.NuevaTareaScreen
import com.sgap.app.ui.theme.SGAPTheme

/**
 * Única Activity de la app. Solo aplica el tema y define la navegación
 * entre pantallas; cada pantalla vive en su propia carpeta dentro de ui/.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SGAPTheme {
                SgapNavHost()
            }
        }
    }
}

/** Rutas de navegación: cada pantalla tiene un nombre único. */
object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val INICIO = "inicio"
    const val NUEVA_TAREA = "nueva_tarea"
}

@Composable
fun SgapNavHost() {
    val navController = rememberNavController()

    // Al entrar (login o registro) se va a Inicio y se borra el historial,
    // así el botón "atrás" no regresa a la pantalla de login.
    val irAInicio = {
        navController.navigate(Rutas.INICIO) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginExitoso = irAInicio,
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) }
            )
        }
        composable(Rutas.REGISTRO) {
            RegistroScreen(
                onRegistroExitoso = irAInicio,
                onIrALogin = { navController.popBackStack() }
            )
        }
        composable(Rutas.INICIO) {
            InicioScreen(onNuevaTarea = { navController.navigate(Rutas.NUEVA_TAREA) })
        }
        composable(Rutas.NUEVA_TAREA) {
            NuevaTareaScreen(onTerminar = { navController.popBackStack() })
        }
    }
}
