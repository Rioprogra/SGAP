package com.sgap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sgap.app.ui.home.HomeScreen
import com.sgap.app.ui.login.LoginScreen
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

/** Rutas de navegación. Agregar aquí las nuevas pantallas (tareas, registro...). */
object Rutas {
    const val LOGIN = "login"
    const val HOME = "home"
}

@Composable
fun SgapNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Rutas.LOGIN) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginExitoso = {
                    // Ir a Home y quitar Login del historial (el botón "atrás" no regresa al login)
                    navController.navigate(Rutas.HOME) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.HOME) {
            HomeScreen()
        }
    }
}
