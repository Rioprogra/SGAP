# Guía: SGAP versión XML (rama `version-xml`)

Esta versión está hecha como en clase: **Empty Views Activity**, un archivo
`.kt` por pantalla y su layout `.xml`, `findViewById`, `Toast`, `Spinner`,
`ListView` con CheckBox y `Intent` para cambiar de pantalla.
La versión con Compose sigue guardada en la rama `main`.

## Abrir esta versión

1. En Android Studio, abajo a la derecha, da clic en el nombre de la rama
   (dice `main`) → `origin/version-xml` → **Checkout**.
2. Espera el **Gradle Sync** y pulsa **Run ▶**.
3. Entra con cualquier correo que tenga `@` y una contraseña de 6+ caracteres.

## Archivos

| Pantalla | Código (`app/src/main/java/com/sgap/app/`) | Diseño (`app/src/main/res/layout/`) |
|---|---|---|
| Login | `MainActivity.kt` | `activity_main.xml` |
| Registro | `Registro.kt` | `activity_registro.xml` |
| Inicio (lista de pendientes) | `Inicio.kt` | `activity_inicio.xml` |
| Nueva tarea | `NuevaTarea.kt` | `activity_nueva_tarea.xml` |

Otros archivos:

- `Tarea.kt`: la clase con los datos de una tarea (título, tipo, fecha, descripción, completada).
- `ListaTareas.kt`: la lista donde se guardan las tareas mientras la app está abierta.
  Es un `object` para que Inicio y NuevaTarea usen la misma lista.
- `res/values/colors.xml`: colores del equipo (primario, secundario, fondo, texto, error).
- `AndroidManifest.xml`: aquí se registra cada Activity. Si creas una nueva pantalla
  con **File → New → Activity → Empty Views Activity**, Android Studio la agrega solo.

## Cómo funciona

- **Login**: valida que los campos no estén vacíos, que el correo tenga `@` y la
  contraseña 6+ caracteres. Si todo está bien abre `Inicio` con un `Intent`.
- **Registro**: valida los campos y que las contraseñas coincidan; con `finish()`
  regresa al login. Todavía no guarda el usuario (no hay base de datos).
- **Inicio**: el `Spinner` filtra por tipo (Todos, Tarea, Examen, Proyecto).
  El `ListView` usa `simple_list_item_multiple_choice`, que trae un CheckBox en cada
  renglón; al tocarlo la tarea se marca como completada. En `onResume()` se recarga
  la lista para que aparezca la tarea que acabas de crear.
- **Nueva tarea**: `RadioGroup` para el tipo y `DatePickerDialog` para la fecha.
  Al guardar se agrega a `ListaTareas.tareas` y con `finish()` regresa a Inicio.

Las tareas se pierden al cerrar la app porque solo están en memoria.
