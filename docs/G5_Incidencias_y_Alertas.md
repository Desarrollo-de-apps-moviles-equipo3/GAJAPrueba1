# G5 · Incidencias y alertas

## Objetivo

El administrador necesita detectar incidencias pendientes, revisar quién las reportó y qué ocurrió, y cambiar su estado cuando las resuelva. Los registros deben venir del microservicio para que permanezcan guardados y sean visibles en todos los dispositivos que consulten el servicio.

## Qué quedó implementado

1. **Alertas de administración**: muestra cuántas incidencias siguen pendientes y una vista rápida de hasta cinco. Al tocar una, abre su detalle desde la lista de incidencias.
2. **Lista de incidencias**: consulta al servicio y permite filtrar Pendientes, Resueltas y Todas. Incluye actualización manual y estados de carga, error y lista vacía.
3. **Nueva incidencia del panel**: primero se elige Canchas, Reservas, Cuenta u Otro. Canchas muestra el catálogo; Reservas permite elegir una solicitud o marcar un día con el calendario. Cuenta y Otro no piden una referencia adicional. El administrador completa el correo relacionado y la descripción.
4. **Detalle de incidencia**: presenta identificador, categoría, correo, fecha y descripción. Si sigue pendiente, permite marcarla como resuelta. El cambio se guarda en el servicio y actualiza el estado de la lista.

La categoría y referencia elegidas se guardan juntas en el campo `espacio` que ya acepta el microservicio (por ejemplo, `Canchas: Cancha de Fútbol A`, `Reservas: solicitud #15 · Cancha de Fútbol A · 2026-11-28 · 10:00 - 12:00` o `Reservas: día 2026-11-28`). Así el detalle conserva el contexto sin cambiar el modelo del servidor. La sección **Soporte y contacto** es independiente y no forma parte de este cambio.

## Archivos y responsabilidades

### `app/src/main/java/com/example/gaja_prueba1/data/remote/IncidenciasApi.kt`

Declara las llamadas HTTP de Retrofit. Se agregaron:

- `listarTodas()` → `GET /api/incidencias`
- `resolver(id)` → `PATCH /api/incidencias/{id}/resolver`

Ya existían las llamadas para crear incidencias y consultar las del usuario. No hizo falta cambiar el microservicio: esas rutas ya estaban implementadas.

### `app/src/main/java/com/example/gaja_prueba1/model/AdminIncidenciasRepository.kt`

Es la capa entre la pantalla y Retrofit. Convierte la respuesta del servicio a `AdminIncidencia`, que contiene los datos que usa el panel, y ofrece las operaciones de listar, crear y resolver. La persistencia permanece en la base remota del servicio; el ViewModel no guarda copias locales como fuente de verdad.

### `app/src/main/java/com/example/gaja_prueba1/ui/viewmodels/AdminIncidenciasViewModel.kt`

Mantiene el estado de la experiencia en un `StateFlow`: registros, filtro actual, detalle seleccionado, campos del formulario, opciones de canchas y reservas, carga, guardado y errores. Valida el correo, categoría, referencia condicional y una descripción de al menos 10 caracteres antes de crear. Para las opciones relacionadas consulta el catálogo de espacios y las reservas del servicio. Después de resolver, actualiza el estado y mueve el filtro a Resueltas.

### `app/src/main/java/com/example/gaja_prueba1/ui/screen/admin/AdminIncidenciasScreen.kt`

Contiene las vistas de Alertas, lista, formulario y detalle. Muestra estados de carga y mensajes de error, deja volver desde el formulario o el detalle, y presenta las acciones adecuadas según el estado de la incidencia.

### `app/src/main/java/com/example/gaja_prueba1/ui/screen/admin/AdminMoreScreen.kt`

Agrega la opción **Alertas** al menú de administración.

### `app/src/main/java/com/example/gaja_prueba1/ui/screen/AdminShell.kt`

Conecta las opciones **Alertas** e **Incidencias** con sus pantallas. Ambas obtienen el mismo `AdminIncidenciasViewModel` de la actividad para conservar la selección al pasar de las alertas al detalle de una incidencia.

## Recorrido de los datos

```text
AdminShell
  ├── Alertas ──────┐
  └── Incidencias ──┴── AdminIncidenciasViewModel
                           │
                    AdminIncidenciasRepository
                           │ Retrofit
                    reservas-service
                           │
                    base de datos remota
```

- Al abrir la pantalla o tocar actualizar, el ViewModel pide la lista al servicio.
- Al guardar el formulario, envía `usuarioEmail`, `espacio` (la categoría) y `descripcion` mediante `POST /api/incidencias`.
- Al resolver una incidencia, llama a `PATCH /api/incidencias/{id}/resolver`.
- El backend asigna el estado inicial `PENDIENTE`, persiste el registro y cambia a `RESUELTA` cuando recibe la acción de resolver.

## Cómo probar manualmente

1. Inicia el microservicio `reservas-service` en el puerto 8083 y asegúrate de que la URL configurada en `ApiConfig` sea accesible desde el emulador o teléfono.
2. Abre el panel de administrador y entra a **Más → Alertas** o **Más → Incidencias**.
3. Entra a **Más → Incidencias → Registrar incidencia** y selecciona una categoría.
4. Si eliges Canchas, selecciona una cancha. Si eliges Reservas, selecciona una solicitud del sistema o un día del calendario. Completa el correo y la descripción y guarda.
5. Comprueba que la incidencia aparece en el detalle y en **Todas** o **Pendientes**, con la referencia seleccionada.
6. Marca la incidencia como resuelta y comprueba que aparezca en **Resueltas**.
7. Desde Alertas, toca una incidencia pendiente y confirma que abre el mismo detalle.

## Verificación realizada

Se ejecutó `:app:compileDebugKotlin` y terminó con `BUILD SUCCESSFUL` después de corregir los imports y el estado delegado de los menús. No se ejecutaron pruebas unitarias ni instrumentadas. La comprobación de persistencia requiere el microservicio y la base de datos disponibles durante la prueba manual.
