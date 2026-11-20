# Guía: StudyGest versión XML (rama `version-xml`)

La app antes se llamaba SGAP; ahora se llama **StudyGest**. El paquete sigue
siendo `com.sgap.app` (cambiarlo es riesgoso y no se ve en la app).

Esta versión está hecha como en clase: **Empty Views Activity**, un archivo
`.kt` por pantalla y su layout `.xml`, `findViewById`, `Toast`, `Intent`,
`ListView` con adaptador, `AlertDialog` y `DatePickerDialog`. **No usa Fragments**:
cada pestaña de la barra de abajo es una Activity.
La versión con Compose sigue guardada en la rama `main`.

Diseño: Figma "SGAP - Diseño" (pantallas Login, Inicio, Grupo, Nueva tarea, Pendientes).

## Abrir esta versión

1. En Android Studio, abajo a la derecha, da clic en el nombre de la rama
   → `version-xml` → **Checkout**.
2. Espera el **Gradle Sync** y pulsa **Run ▶**.
3. Crea una cuenta en **Regístrate** (se guarda en Firebase) y entra con ese
   correo y contraseña. Necesitas internet.

## Cómo se navega

```
Login ──► Registro
  │
  ▼
Inicio (Mis canales) ──campana──► Pendientes ──► Nueva tarea
  │        └──── + ──────────────► Nueva tarea
  │
  └─ barra de abajo: Inicio · Grupo · Horario · Perfil
                                             └──► Pendientes / Cerrar sesión
```

Inicio siempre se queda abajo de todo. Al cambiar de pestaña, Grupo, Horario y
Perfil se cierran (`finish()`), así el botón "atrás" siempre regresa a Inicio.

## Pantallas

| Pantalla | Código (`app/src/main/java/com/sgap/app/`) | Diseño (`res/layout/`) | ¿Está en el Figma? |
|---|---|---|---|
| Login | `MainActivity.kt` | `activity_main.xml` | Sí |
| Registro | `Registro.kt` | `activity_registro.xml` | No (mismo estilo que Login) |
| Inicio (Mis canales) | `Inicio.kt` | `activity_inicio.xml` | Sí |
| Grupo | `Grupo.kt` | `activity_grupo.xml` | Sí |
| Horario | `Horario.kt` | `activity_horario.xml` | No (versión sencilla) |
| Perfil | `Perfil.kt` | `activity_perfil.xml` | No (versión sencilla) |
| Nueva tarea | `NuevaTarea.kt` | `activity_nueva_tarea.xml` | Sí |
| Pendientes | `Pendientes.kt` | `activity_pendientes.xml` | Sí |

## Firebase (inicio de sesión)

- Proyecto de Firebase: **StudyGest** (ID `studygest`), plan gratuito Spark.
  Consola: https://console.firebase.google.com/project/studygest
- App registrada: `com.sgap.app`. Su archivo de configuración es
  `app/google-services.json` (ya está en el repo; si se borra, la app no compila).
- Se usa **Firebase Authentication** con correo y contraseña:
  - **Registro** crea la cuenta (`createUserWithEmailAndPassword`) y le guarda el nombre.
  - **Login** entra con `signInWithEmailAndPassword`. Si ya habías entrado antes,
    pasa directo a Inicio.
  - **¿Olvidaste tu contraseña?** manda un correo con `sendPasswordResetEmail`
    (escribe el correo arriba primero).
  - **Perfil → Cerrar sesión** hace `signOut()`.
  - Los errores de Firebase se traducen en `ErroresFirebase.kt`.
- Las cuentas creadas se ven en la consola → **Authentication → Usuarios**.
- Gradle: plugin `com.google.gms.google-services`, `firebase-auth` y
  `firebase-firestore` con el BoM 34.19.0 (`gradle/libs.versions.toml`). No subir a
  la 35.x: viene hecha con un Kotlin más nuevo que el del proyecto y no compila.

## Firebase AI Logic (Gemini)

- Archivo: `AsistenteIA.kt`. En **Nueva tarea**, el botón "✨ Sugerir descripción con IA"
  le manda el título a Gemini y pone la respuesta en la descripción.
- Usa la **Gemini Developer API** (gratis). El modelo está en `AsistenteIA.MODELO`
  (`gemini-3.8-flash`, que no pide facturación). Si sale "Gemini aún no está activado"
  es porque falta el paso de abajo en la consola.
- Gradle: `firebase-ai` y `firebase-appcheck-debug` (AI Logic exige App Check).
- Activarlo: consola → **AI Services → AI Logic → Get started** → elegir
  **Gemini Developer API**.
- En el emulador: al usar el botón por primera vez, Logcat muestra un *debug token*
  (busca `DebugAppCheckProvider`). Cópialo en consola → **Security → App Check →
  Apps → (menú ⋮) Manage debug tokens → Add**.
- En Android Studio: **Tools → Firebase → Firebase AI Logic** muestra "Connected".

## Recordatorios, progreso, modo oscuro y sin conexión

- **Avisos con la app cerrada (`AvisosNuevos.kt`, WorkManager):** un trabajo periódico
  (cada 15 minutos, el mínimo de Android, solo con internet) busca en `notificaciones`
  las tuyas sin leer y las muestra como notificación del teléfono aunque la app no esté
  abierta. Tocarla abre la tarea. Para que fuera instantáneo se necesita Firebase Cloud
  Messaging con Cloud Functions, que pide el plan de pago Blaze.
- **Correo al grupo:** al publicar una tarea para un grupo, se abre la app de correo con
  los correos de los integrantes, el asunto y los datos de la tarea; solo falta enviar.
  Los correos salen de `miembros_grupo.correo` (cada quien lo guarda al entrar a la app).
  Mandarlo sin tocar nada necesitaría la extensión "Trigger Email", que pide Blaze.
- **Detalle de tarea (`DetalleTarea.kt`):** se abre al tocar una tarea, una notificación
  de la campana o una notificación del teléfono. Muestra tipo, estado, fecha, materia,
  grupo y descripción, con "Marcar como entregada" y "Editar tarea" (si tú la creaste).
- **Recordatorios (`Recordatorios.kt`, WorkManager):** cada tarea pendiente programa
  una notificación del teléfono **un día antes a las 9:00**. Si esa hora ya pasó pero
  la entrega no, avisa a los 10 segundos (sirve para la demo: crea una tarea para
  mañana). Al editar se reprograma, al entregar o eliminar se cancela, y al cerrar
  sesión se cancelan todas. En Android 13+ Inicio pide el permiso de notificaciones.
- **Progreso por materia (`Progreso.kt`):** Perfil → "Progreso por materia". Una
  tarjeta General y una por materia con % entregado, barra y vencidas (como un
  `GROUP BY id_materia`).
- **Modo oscuro (`Tema.kt` + `res/values-night/colors.xml`):** el tema es `DayNight`.
  Sigue al teléfono hasta que se usa el switch de Perfil; la elección se guarda en
  `SharedPreferences`.
- **Sin conexión:** dos capas.
  - `BaseDatos.db` activa el caché persistente de Firestore (100 MB): lo que guardes
    sin internet se sube solo al volver la red.
  - **Room** (`CacheLocal.kt`, `TareaGuardada.kt`, `TareaDao.kt`): base SQLite local
    `studygest_cache.db` con la tabla `tareas_guardadas` (llave primaria
    `idAsignacion`, columna `idUsuario`). Cada vez que `cargarTareas` trae las tareas
    del servidor, las copia a Room; si Firestore falla o responde vacío sin conexión,
    las tareas se leen de Room. Pendientes avisa "Sin conexión".
  - Gradle: plugin **KSP 2.3.13** y `room-runtime` / `room-compiler` 2.8.5
    (`gradle/libs.versions.toml`). KSP genera `CacheLocal_Impl` al compilar.
- **Reglas (`firestore.rules`):** tareas validan título (3–80), descripción (≤300),
  tipo, fecha y que `id_materia` exista en `materias`; al editar solo se pueden
  cambiar los campos de la pantalla y `estado` (0 o 1). Hay que **publicarlas** en
  la consola → Firestore → Reglas (copiar y pegar el archivo).

## Base de datos (Cloud Firestore)

Se sigue el documento del encargado de la base de datos (**StudyGest – Tablas de la
base de datos**, 14 tablas MySQL), pasado a Firestore: cada **tabla** es una
**colección** y cada **fila** es un **documento**. Todo el acceso está en `BaseDatos.kt`.

**Dónde verla:** consola → **Firestore Database → Datos**
(https://console.firebase.google.com/project/studygest/firestore/databases/-default-/data).
Columna 1 = colecciones (tablas), columna 2 = documentos (filas), columna 3 = campos.

| # | Colección | Id del documento | Campos | ¿La usa la app? |
|---|---|---|---|---|
| 1 | `usuarios` | uid de Authentication | nombre, apellidos, correo, zona_horaria, fecha_registro, estado | Sí (Registro / Login) |
| 2 | `integraciones` | automático | id_usuario, proveedor, servicio, id_externo, tokens… | No (Teams/Outlook necesita servidor) |
| 3 | `grupos` | automático | nombre, descripcion, codigo_acceso, id_creador, fecha_creacion, estado + **tipo, materia, cupo, total_miembros** | Sí (Grupo) |
| 4 | `miembros_grupo` | `{idGrupo}_{idUsuario}` | id_grupo, id_usuario, rol, fecha_union, estado + **iniciales, correo** | Sí (Grupo) |
| 5 | `tareas` | automático | titulo, descripcion, fecha_creacion, fecha_entrega, origen, id_creador, id_grupo, **id_materia**, id_integracion, id_externo, estado + **tipo** | Sí (Nueva tarea) |
| 6 | `asignaciones_tarea` | `{idTarea}_{idUsuario}` | id_tarea, id_usuario, estado, fecha_completada | Sí (Pendientes) |
| 7 | `avisos` | automático | id_grupo, id_autor, titulo, contenido, fechas, estado | Reglas listas, sin pantalla |
| 8 | `materias` | automático | nombre, estado (+ lo del documento) | Sí (selector "Materia" en Nueva tarea; se capturan en la consola) |
| 9–11 | `docentes`, `edificios`, `aulas` | automático | según el documento | Reglas listas (solo lectura) |
| 12 | `horarios` | automático | id_usuario, id_materia, id_docente, id_aula, dia_semana, horas… | Reglas listas, la pantalla aún usa ejemplos |
| 13 | `notificaciones` | automático | id_usuario, id_tarea/id_horario/id_aviso, titulo, mensaje, fechas, leida, estado | Sí (campana y Pendientes) |
| 14 | `dispositivos_usuario` | automático | id_usuario, token_push, plataforma, estado | Reglas listas (falta push) |

Los campos en **negritas** no vienen en el documento; los pide el Figma (y
`id_materia`, que pidió el profe para que sea relacional).

**Cómo se enlazan (modelo relacional):** igual que llaves foráneas en MySQL, cada
documento guarda el id del otro:
`tareas.id_creador → usuarios`, `tareas.id_materia → materias`,
`tareas.id_grupo → grupos`, `asignaciones_tarea.id_tarea → tareas`,
`asignaciones_tarea.id_usuario → usuarios`, `miembros_grupo.id_grupo → grupos`.

**Agregar materias:** las reglas no dejan que la app escriba en `materias` (es un
catálogo). En la consola → Firestore → `materias` → **Agregar documento** →
Id automático → campos `nombre` (string, ej. "Base de Datos") y `estado` (number, 1).

**Plantillas (`_plantilla`):** cada una de las 14 colecciones tiene un documento
llamado `_plantilla`. Sus campos describen el tipo de cada columna, por ejemplo
`"VARCHAR(100) · obligatorio"`, así que se ven como la estructura en phpMyAdmin.
Existen porque en Firestore una colección vacía no se muestra. La app no las usa:
- grupos y miembros: solo lee los que tienen `estado = 1` (número);
- tareas, asignaciones y notificaciones: solo lee las de tu `id_usuario`.

No las borren mientras quieran ver la estructura en la consola. Si las borran,
la app sigue funcionando igual.

**Diferencias con el documento (porque Firestore no es MySQL):**
- No hay `INT AUTO_INCREMENT`: Firestore genera un id de texto. En `usuarios` el id es el uid.
- No hay `contrasena_hash`: la contraseña la guarda Firebase Authentication.
- Los `UNIQUE` se logran con ids armados: `miembros_grupo/{idGrupo}_{idUsuario}` y
  `asignaciones_tarea/{idTarea}_{idUsuario}`.
- Las transacciones se hacen con `runTransaction` (unirse sin pasarse del cupo) o
  `WriteBatch` (crear grupo + ADMIN; tarea de grupo + asignaciones + notificaciones).
- No se borra nada: se cambia `estado` (las reglas no permiten `delete`).

**Reglas del documento que ya cumple la app:**
- Correo en minúsculas.
- Crear grupo mete al creador como **ADMIN** y genera un `codigo_acceso` de 8 caracteres.
- Unirse (tocando el grupo o con código) crea al alumno como **MIEMBRO** y le crea
  asignaciones de las tareas vigentes del grupo.
- Toda tarea crea la asignación de su creador. Las de **GRUPO** solo las publica un
  ADMIN, JEFE o TUTOR y crean una asignación y una notificación por miembro.
- Al marcar COMPLETADA se guarda `fecha_completada`; al desmarcar vuelve a `null`.
- Cada notificación tiene exactamente un origen.

**Seguridad:** `firestore.rules` (copia de lo publicado en la consola → Firestore → Reglas).
Cada alumno solo ve sus propias asignaciones y notificaciones; los grupos los ven todos.

## Datos en memoria

Las listas viven en un `object` para que todas las pantallas vean lo mismo.
`ListaTareas`, `ListaGrupos` y `ListaNotificaciones` se llenan desde Firestore (las
tareas también se guardan en Room); canales y horario son de ejemplo y se pierden al
cerrar la app.

| Modelo (`data class`) | Lista de ejemplo (`object`) | Qué guarda |
|---|---|---|
| `Canal.kt` | `ListaCanales.kt` | Canales del salón (vacío: ya no trae ejemplos) |
| `EquipoGrupo.kt` | `ListaGrupos.kt` | Grupos con sus miembros y tu rol, desde Firestore (se llama así para no chocar con la pantalla `Grupo`) |
| `Tarea.kt` | `ListaTareas.kt` | Tareas, exámenes y proyectos, desde Firestore |
| `Notificacion.kt` | `ListaNotificaciones.kt` | Notificaciones de la campana, desde Firestore |
| `Clase.kt` | `ListaHorario.kt` | Clases de cada día |
| — | `Usuario.kt` | Nombre y correo de quien inició sesión (la cuenta está en Firebase) |

## Piezas que se reusan

- **Barra de abajo:** `res/layout/barra_navegacion.xml` + `Navegacion.kt`.
  En cada pestaña: `<include layout="@layout/barra_navegacion" />` en el XML y
  `Navegacion.configurar(this, R.id.navInicio)` en el `onCreate`.
- **Tarjetas de lista:** cada una tiene su layout `item_*.xml` y su adaptador
  (`CanalAdapter`, `GrupoAdapter`, `ClaseAdapter`). Es un `ArrayAdapter` como el
  de clase, pero en `getView` inflamos nuestro propio layout.
- **Pendientes** no usa `ListView` porque tiene dos listas dentro de un
  `ScrollView`; ahí se crean las tarjetas con `LayoutInflater` y `addView`.
- **Chips de filtro:** `Chips.pintar(...)` pinta el elegido de azul.
- **Fechas cortas** ("Vie 2 oct"): `Fechas.kt`.
- **Estilos** en `res/values/themes.xml`: `Etiqueta`, `Campo`, `BotonPrimario`,
  `Chip`, `Titulo`, `Subtitulo`, `Opcion`, `Regresar`, etc. Ejemplo:
  `<EditText style="@style/Campo" ... />`.
- **Colores** del Figma en `res/values/colors.xml` (`primario`, `fondo`, `borde`,
  `texto`, `acento_violeta`, ...).
- **Fondos** redondeados en `res/drawable/bg_*.xml` y el logo en
  `logo_studygest.xml`.

## Qué hace cada pantalla

- **Login / Registro:** validan campos vacíos, que el correo tenga `@`, contraseña
  de 6+ y (en Registro) que coincidan; Registro pide nombre y apellidos. Usan Firebase Authentication
  (ver la sección Firebase).
- **Inicio:** fecha de hoy, chips Todos / No leídos / Materias / Avisos. Tocar un
  canal borra su contador rojo (el chat todavía no está en el Figma). El puntito
  rojo de la campana sale si hay notificaciones sin leer. El "+" abre Nueva tarea.
- **Grupo:** chips Todos / Proyecto / Estudio / Con lugar. Etiqueta "Tu equipo" o
  "Con lugar". El "+" deja **crear grupo** (quedas como ADMIN y te da el código) o
  **unirte con código**. Tocar un grupo con lugar te une; tocar uno tuyo muestra tu
  rol y el código para invitar. Con más de 4 integrantes el último círculo dice "+N".
- **Horario:** chips Lun–Vie, empieza en el día de hoy.
- **Perfil:** iniciales, nombre, correo, número de pendientes / grupos / canales,
  ir a Pendientes y cerrar sesión (borra la pila con `FLAG_ACTIVITY_CLEAR_TASK`).
- **Nueva tarea:** tres botones para el tipo (Tarea viene elegido), "¿Para quién?"
  (solo para mí, o un grupo donde eres ADMIN/JEFE/TUTOR), calendario que no deja
  escoger días pasados, selector de **Materia** y descripción opcional (con botón de IA).
  Valida título (3 a 80 letras, el error sale en el campo) y fecha.
  **Modo editar:** si se abre con `intent.putExtra("idTarea", ...)` se llama
  "Editar tarea", llena los campos, oculta "¿Para quién?" y muestra **Eliminar tarea**
  (pide confirmación con un `AlertDialog`).
- **Pendientes:** notificaciones desde Firestore ("Marcar como leídas"; tocar una la
  marca leída y abre su tarea).
  "Por entregar" muestra las 3 más próximas; "Ver todo" muestra todas, incluidas
  las entregadas (tachadas). El círculo de la derecha marca/desmarca entregada.
  Chips **Todas / Tarea / Examen / Proyecto / Vencidas / Entregadas** y un selector de
  materia para filtrar. Ordena por fecha; las **vencidas** dicen "Vencida" en rojo.
  Tocar una tarjeta abre la pantalla **Detalle** (`DetalleTarea.kt`).

**CRUD de tareas (todo en `BaseDatos.kt`):**
| Operación | Función | Qué hace en Firestore |
|---|---|---|
| Crear | `guardarTarea` | `tareas` + `asignaciones_tarea` (+ `notificaciones`) en un WriteBatch |
| Leer | `cargarTareas` | mis `asignaciones_tarea` y su tarea (solo `estado = 1`); copia en Room |
| Editar | `actualizarTarea` | `update` de título, tipo, fecha, descripción, `id_materia` |
| Eliminar | `eliminarTarea` | baja lógica: `estado = 0` (las reglas no permiten `delete`) |
| Entregar | `cambiarCompletada` | `asignaciones_tarea.estado` y `fecha_completada` |

## Pendiente para el equipo de diseño

- Diseñar Registro, Horario, Perfil, el chat de un canal y "Crear grupo"
  (hoy es un AlertDialog sencillo).

## Pendiente para el encargado de la base de datos

- Agregar a su documento lo que pide el Figma: `canales` y `mensajes` (no existen),
  y los campos `grupos.tipo`, `grupos.materia`, `grupos.cupo` y `tareas.tipo`.
- La letra del Figma es **Inter**; la app usa la letra normal de Android.
