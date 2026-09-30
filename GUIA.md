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
   una contraseña de 6+ caracteres** (ej. `david@utcj.edu.mx` / `123456`). Te lleva a Inicio.

Tip: abre cualquier pantalla (por ejemplo `ui/home/InicioScreen.kt`) y pulsa **Split**
(arriba a la derecha) para ver su vista previa (`@Preview`) sin correr la app.

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
        └── java/com/sgap/app/
            ├── MainActivity.kt   ← única Activity: aplica el tema y la navegación
            ├── ui/               ← (Emmanuel) todo lo visual, en Jetpack Compose
            │   ├── theme/        ← Color.kt, Type.kt, Theme.kt: paleta y tipografía
            │   ├── componentes/  ← piezas reutilizables: CampoTexto, BotonPrincipal,
            │   │                   MarcaSgap, colores por tipo y formato de fechas
            │   ├── login/        ← LoginScreen.kt (igual al Figma)
            │   ├── registro/     ← RegistroScreen.kt (mismo estilo que el login)
            │   ├── home/         ← InicioScreen.kt: "Mis pendientes" con filtros
            │   └── tareas/       ← NuevaTareaScreen.kt: formulario con calendario
            ├── viewmodel/        ← un ViewModel por pantalla: Login, Registro,
            │                       Inicio (filtros) y NuevaTarea (validación)
            └── data/             ← (David) datos
                ├── model/        ← Usuario.kt, Tarea.kt (tarea + TipoTarea)
                └── repository/   ← AuthRepository.kt (falso), TareaRepository.kt
                                    (en memoria) y el objeto Repositorios
```

### Conceptos nuevos, en corto

- **Gradle**: la herramienta que descarga librerías y compila. "Sync" = leer los `.gradle.kts`.
- **Activity**: una "ventana" de Android. Usamos solo una; las pantallas cambian dentro.
- **Jetpack Compose**: la UI se escribe en Kotlin con funciones marcadas `@Composable`
  en vez de archivos XML. Cuando el estado cambia, la pantalla se redibuja sola.
- **Navigation**: `NavHost` en `MainActivity.kt` define las rutas (`login`, `registro`,
  `inicio`, `nueva_tarea`). `navigate(ruta)` abre una pantalla y `popBackStack()` regresa.
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
   `MainActivity` navega a Inicio y quita el login del historial.
5. El registro funciona igual (`RegistroViewModel`), también falso por ahora.

## 6. Git en el equipo

- `main` siempre debe compilar. Para trabajar: `git checkout -b feature/tareas`,
  haces commits, subes la rama y abren un Pull Request hacia `main`.
- En Android Studio: menú **Git** → Commit (Ctrl+K), Push (Ctrl+Shift+K), Pull.

## 7. Decisiones tomadas por defecto (por confirmar)

- **Compose** en lugar de XML (aislado en `ui/`; si el profe exige XML solo cambia esa carpeta).
- **Sin Firebase todavía**: el login es falso. Agregar Firebase es una dependencia nueva, se decide aparte.
- **Paquete** `com.sgap.app` y **Android mínimo 8.0 (API 26)**, compileSdk/targetSdk 37.
- **Versiones** (en `gradle/libs.versions.toml`): Android Gradle Plugin 9.3.3 (trae Kotlin integrado), Kotlin 2.2.10, Gradle 9.5.0, Compose BOM 2026.02.01.

## 8. Tareas: cómo fluyen los datos

```
NuevaTareaScreen ─guardar()→ NuevaTareaViewModel ─agregar()→ TareaRepository
                                                               │ (StateFlow con la lista)
InicioScreen ←──── estado ──── InicioViewModel ←── tareas ─────┘
```

- **`Tarea` y `TipoTarea`** (`data/model/Tarea.kt`): los datos de un pendiente. La fecha
  es un `LocalDate` (solo día, sin hora).
- **`TareaRepository`** es una interfaz: dice *qué* se puede hacer (ver la lista, agregar,
  marcar completada). **`TareaRepositoryEnMemoria`** es *cómo* se hace hoy: una lista en
  memoria, así que **las tareas se borran al cerrar la app**.
- **`Repositorios`** es un `object` (un único ejemplar para toda la app). Por eso Inicio y
  Nueva tarea ven la misma lista. Para pasar a Room o Firebase después, se crea otra clase
  que implemente `TareaRepository` y se cambia **una línea** en `Repositorios`.
- **`InicioViewModel`** usa `combine`: junta la lista de tareas y el filtro elegido, y cada
  vez que cualquiera cambia recalcula lo que se ve. Ordena pendientes primero y luego por fecha.
- **`NuevaTareaViewModel`** valida que haya título y fecha antes de guardar. Al guardar pone
  `guardada = true` y la pantalla regresa sola a Inicio.
- **Completar**: tocar el círculo llama a `alternarCompletada(id)`; la tarjeta se tacha y
  el círculo se rellena. Tocar otra vez la desmarca.
- **Calendario**: `DatePickerDialog` de Material 3. Devuelve milisegundos en UTC, por eso
  se convierte con `ZoneOffset.UTC` para no quedar un día antes.

## 9. Qué probar

1. Login → "Regístrate" → llena nombre, correo y contraseña → "Crear cuenta" → Inicio.
2. Inicio vacío muestra "Aún no tienes pendientes".
3. "+" → "Guardar tarea" sin título muestra el error. Llena título, tipo y fecha → guarda →
   aparece en la lista con su color y fecha ("Hoy", "Mañana" o "Vie 2 oct").
4. Crea una de cada tipo y prueba los filtros.
5. Toca el círculo de una tarea: se marca como completada y baja al final.
6. Grupo, Horario, Perfil y "¿Olvidaste tu contraseña?" muestran "disponible próximamente".

## 10. Siguientes pasos

- Editar y eliminar tareas (RF-02).
- Guardar de verdad (Room o Firebase, por decidir).
- Pantallas Grupo, Horario y Perfil.
