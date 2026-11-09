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
3. Entra con cualquier correo que tenga `@` y una contraseña de 6+ caracteres.
   Si antes te registras, el Perfil muestra tu nombre.

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

## Datos (todavía no hay base de datos)

Todo vive en memoria dentro de un `object`, así todas las pantallas ven la misma
lista. Al cerrar la app se pierde lo que agregaste.

| Modelo (`data class`) | Lista de ejemplo (`object`) | Qué guarda |
|---|---|---|
| `Canal.kt` | `ListaCanales.kt` | Canales del salón y sus mensajes sin leer |
| `EquipoGrupo.kt` | `ListaGrupos.kt` | Equipos y grupos de estudio (se llama así para no chocar con la pantalla `Grupo`) |
| `Tarea.kt` | `ListaTareas.kt` | Tareas, exámenes y proyectos |
| `Notificacion.kt` | `ListaNotificaciones.kt` | Avisos de la campana |
| `Clase.kt` | `ListaHorario.kt` | Clases de cada día |
| — | `Usuario.kt` | Nombre y correo de quien inició sesión |

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
  de 6+ y (en Registro) que coincidan. Registro guarda nombre y correo en `Usuario`.
- **Inicio:** fecha de hoy, chips Todos / No leídos / Materias / Avisos. Tocar un
  canal borra su contador rojo (el chat todavía no está en el Figma). El puntito
  rojo de la campana sale si hay notificaciones sin leer. El "+" abre Nueva tarea.
- **Grupo:** chips Todos / Proyecto / Estudio / Con lugar. Etiqueta "Tu equipo" o
  "Con lugar". Si un grupo tiene lugar, al tocarlo te puedes unir (AlertDialog) y
  se agrega una notificación. Con más de 4 integrantes el último círculo dice "+N".
- **Horario:** chips Lun–Vie, empieza en el día de hoy.
- **Perfil:** iniciales, nombre, correo, número de pendientes / grupos / canales,
  ir a Pendientes y cerrar sesión (borra la pila con `FLAG_ACTIVITY_CLEAR_TASK`).
- **Nueva tarea:** tres botones para el tipo (Tarea viene elegido), calendario que
  no deja escoger días pasados y descripción opcional.
- **Pendientes:** "Marcar como leídas", tocar una notificación la marca leída.
  "Por entregar" muestra las 3 más próximas; "Ver todo" muestra todas, incluidas
  las entregadas (tachadas). El círculo de la derecha marca/desmarca entregada.

## Pendiente para el equipo de diseño

- Diseñar Registro, Horario, Perfil, el chat de un canal y "Crear grupo".
- Decidir qué hace el "+" de Grupo (ahora solo muestra un Toast).
- La letra del Figma es **Inter**; la app usa la letra normal de Android.
