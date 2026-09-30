# Guía: proyecto base de SGAP en Android Studio

Esta guía explica cómo abrir el proyecto base, correrlo y qué hace cada archivo.
El proyecto ya trae la estructura de la sección 5 de CLAUDE.md, el tema de la
sección 10 y la pantalla de login.

## 1. Requisitos (una sola vez)

1. Instala **Android Studio** (versión estable más reciente) desde developer.android.com/studio.
2. Al abrirlo por primera vez, deja que el asistente descargue el **Android SDK**.
3. Instala **Git** (git-scm.com) si no lo tienes.

## 2. Clonar y abrir el proyecto

1. En Android Studio: **File → New → Project from Version Control…**
   (en la pantalla de bienvenida es el botón **Clone Repository**).
2. URL: `https://github.com/Rioprogra/SGAP.git` → **Clone**.
3. Android Studio abre el proyecto y hace **Gradle Sync** solo (barra de abajo).
   La primera vez tarda porque descarga librerías. Espera a que termine.
4. Si aparece un aviso para **actualizar el Android Gradle Plugin**, puedes aceptarlo.

Por terminal sería: `git clone https://github.com/Rioprogra/SGAP.git` y luego
**File → Open** sobre la carpeta `SGAP`.

## 3. Correr la app

1. **Tools → Device Manager → Create Virtual Device** → elige un Pixel → una imagen
   de Android reciente (descárgala) → Finish. O conecta tu celular con
   "Depuración USB" activada.
2. Arriba selecciona el dispositivo y pulsa **Run ▶** (Shift+F10).
3. Verás el login. Como aún no hay backend, entra con **cualquier correo válido y
   una contraseña de 6+ caracteres** (ej. `david@utcj.edu.mx` / `123456`). Te lleva a Home.

Tip: abre `ui/login/LoginScreen.kt` y pulsa **Split** (arriba a la derecha) para ver la
vista previa del login sin correr la app.

## 4. Qué es cada archivo

```
SGAP/
├── settings.gradle.kts       ← nombre del proyecto y de dónde se bajan librerías
├── build.gradle.kts          ← declara los plugins (Android, Kotlin, Compose)
├── gradle/libs.versions.toml ← TODAS las versiones de librerías en un lugar
├── gradlew / gradlew.bat     ← descargan la versión correcta de Gradle (no tocar)
└── app/
    ├── build.gradle.kts      ← id de la app, Android mínimo (7.0), dependencias
    └── src/main/
        ├── AndroidManifest.xml   ← "ficha" de la app: nombre, tema, Activity inicial
        ├── res/values/           ← textos (strings.xml) y tema de arranque (themes.xml)
        └── java/mx/edu/utcj/sgap/
            ├── MainActivity.kt   ← única Activity: aplica el tema y la navegación
            ├── ui/               ← (Emmanuel) todo lo visual, en Jetpack Compose
            │   ├── theme/        ← Color.kt, Type.kt, Theme.kt: paleta y tipografía
            │   ├── login/        ← LoginScreen.kt
            │   ├── home/         ← HomeScreen.kt (por ahora solo un saludo)
            │   └── tareas/       ← vacía, para la lista y crear tarea
            ├── viewmodel/        ← LoginViewModel.kt: estado y lógica del login
            └── data/             ← (David) datos
                ├── model/        ← Usuario.kt
                └── repository/   ← AuthRepository.kt: login falso por ahora
```

### Conceptos nuevos, en corto

- **Gradle**: la herramienta que descarga librerías y compila. "Sync" = leer los `.gradle.kts`.
- **Activity**: una "ventana" de Android. Usamos solo una; las pantallas cambian dentro.
- **Jetpack Compose**: la UI se escribe en Kotlin con funciones marcadas `@Composable`
  en vez de archivos XML. Cuando el estado cambia, la pantalla se redibuja sola.
- **Navigation**: `NavHost` en `MainActivity.kt` define las rutas (`login`, `home`).
- **MVVM**: la pantalla (View) solo dibuja; el **ViewModel** guarda el estado
  (`LoginEstado`) y decide qué hacer; el **Repository** trae los datos.
- **StateFlow**: una variable "observable"; la pantalla la lee con `collectAsState()`.
- **Repository con interfaz**: `AuthRepository` es el contrato y `AuthRepositoryFalso`
  la versión de prueba. Cuando se confirme Firebase, se crea otra implementación y
  la pantalla no cambia.

## 5. Flujo del login

1. Escribes correo/contraseña → `LoginViewModel.onCorreoCambio(...)` actualiza el estado.
2. "Entrar" → `iniciarSesion()` valida campos y llama al repositorio.
3. Mientras espera, `cargando = true` y el botón muestra un circulito.
4. Si sale bien, `loginExitoso = true` → `LoginScreen` llama a `onLoginExitoso` →
   `MainActivity` navega a Home y quita el login del historial.

## 6. Git en el equipo

- `main` siempre debe compilar. Para trabajar: `git checkout -b feature/tareas`,
  haces commits, subes la rama y abren un Pull Request hacia `main`.
- En Android Studio: menú **Git** → Commit (Ctrl+K), Push (Ctrl+Shift+K), Pull.

## 7. Decisiones tomadas por defecto (por confirmar)

- **Compose** en lugar de XML (aislado en `ui/`; si el profe exige XML solo cambia esa carpeta).
- **Sin Firebase todavía**: el login es falso. Agregar Firebase es una dependencia nueva, se decide aparte.
- **Paquete** `mx.edu.utcj.sgap` y **Android mínimo 7.0 (API 24)**.

## 8. Siguientes pasos del primer avance

1. Pantalla de registro (el botón "Regístrate" ya existe, falta la pantalla).
2. `data/model/Tarea.kt` + lista de tareas en Home.
3. Pantalla para crear tarea (título, tipo, fecha).
