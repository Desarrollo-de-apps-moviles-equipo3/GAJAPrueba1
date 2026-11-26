# GExtra3 — Ocultar reservas para el usuario y conservar el historial administrativo

## Objetivo

Permitir que una persona quite de **Mis reservas** registros que ya no necesita consultar, sin borrar la reserva de la base de datos. El administrador conserva el historial para revisar qué ocurrió y buscar reservas por usuario y estado.

## Cómo queda el flujo

1. El usuario abre **Mis reservas**.
2. En una reserva cancelada o rechazada aparece **Quitar de mi lista**. También aparece en una reserva aprobada cuando su horario ya terminó.
3. La app pide confirmación y solicita al microservicio que oculte el registro para ese usuario.
4. El microservicio marca `ocultaParaUsuario = true`. No elimina ni cambia el estado de la reserva.
5. La lista del usuario deja de incluir ese registro. El administrador lo sigue viendo en **Admin → Reservas → Historial**.

Las reservas aprobadas que todavía no han terminado y las solicitudes pendientes no se pueden ocultar desde la app, porque el usuario podría necesitar consultar o gestionar una reserva activa.

## Qué se hizo y por qué

### Marca de ocultamiento en la base de datos

Se añadió el campo booleano `ocultaParaUsuario` a la entidad de reserva. Su valor inicial es `false`, así que las reservas existentes siguen apareciendo para sus dueños. La configuración actual de Hibernate (`ddl-auto=update`) agrega la columna al iniciar el servicio.

Esta marca separa dos cosas distintas: que una reserva exista en el historial y que se muestre en la lista personal. Así se evita perder el registro administrativo.

### Endpoint para ocultar una reserva

El microservicio incorpora:

```text
PATCH /api/reservas/{id}/ocultar?email={correo-del-usuario}
```

El servicio busca la reserva asociada al correo indicado y valida que su estado permita ocultarla. Si corresponde, guarda la marca de ocultamiento. Si no corresponde, responde con un conflicto y no cambia el registro.

La consulta de reservas personales ahora devuelve solamente las reservas no ocultas y las ordena desde la más reciente. La consulta administrativa general sigue devolviendo todos los registros.

### Acción “Quitar de mi lista” en la app

En la tarjeta de la reserva, la acción aparece únicamente para estados terminados permitidos. Antes de enviarla, la app muestra una confirmación que explica que el administrador conservará el registro. Si el servicio confirma el cambio, la tarjeta desaparece de la lista; si falla, se muestra un mensaje y se conserva en pantalla.

### Historial administrativo

En la sección de reservas del administrador se agregó la pestaña **Historial**. Muestra todos los estados, incluidos los registros ocultos por sus usuarios. Incluye:

- Búsqueda parcial por correo del usuario.
- Filtros por todos, pendiente, aprobada, rechazada o cancelada.
- Orden de resultados por fecha de creación, desde el más reciente.
- Una indicación cuando la reserva está oculta para el usuario.

Las pestañas operativas de solicitudes pendientes, aprobadas y rechazadas se mantienen.

### Límite de la lista personal

La pantalla del usuario presenta las **10 reservas más recientes** que siguen visibles para él. Esto mantiene “Mis reservas” enfocada en registros actuales y recientes. El historial administrativo no tiene ese límite.

## Archivos modificados

### Aplicación Android

- `app/src/main/java/com/example/gaja_prueba1/data/remote/ReservasApi.kt`: agrega la llamada al endpoint de ocultamiento.
- `app/src/main/java/com/example/gaja_prueba1/data/remote/ReservationDtos.kt`: incorpora el campo de ocultamiento en la respuesta.
- `app/src/main/java/com/example/gaja_prueba1/model/RemoteReservationsRepository.kt`: expone la operación desde el repositorio de reservas.
- `app/src/main/java/com/example/gaja_prueba1/ui/viewmodels/MyReservationsViewModel.kt`: ordena y limita la lista personal, ejecuta el ocultamiento y maneja el estado de carga y error.
- `app/src/main/java/com/example/gaja_prueba1/ui/screen/MyReservationsScreen.kt`: agrega la acción, confirmación y mensaje de error en la interfaz.
- `app/src/main/java/com/example/gaja_prueba1/data/remote/AdminReservasApi.kt`: permite pedir todos los estados para el historial.
- `app/src/main/java/com/example/gaja_prueba1/model/AdminReservationsRepository.kt`: carga todas las reservas y adapta los datos del servicio al modelo administrativo.
- `app/src/main/java/com/example/gaja_prueba1/ui/viewmodels/AdminReservationsViewModel.kt`: añade la pestaña Historial y carga la lista completa.
- `app/src/main/java/com/example/gaja_prueba1/ui/screen/admin/AdminReservationsScreen.kt`: agrega búsqueda por correo, filtros y la marca visual de reserva oculta.

### Microservicio de reservas

- `reservas-service/src/main/java/com/gaja/reservas/model/Reserva.java`: añade `ocultaParaUsuario`, con valor predeterminado falso.
- `reservas-service/src/main/java/com/gaja/reservas/repo/ReservaRepository.java`: añade la consulta de reservas visibles ordenadas por creación.
- `reservas-service/src/main/java/com/gaja/reservas/service/ReservaService.java`: filtra la lista personal y valida/guarda el ocultamiento.
- `reservas-service/src/main/java/com/gaja/reservas/web/ReservaController.java`: publica la ruta `PATCH /{id}/ocultar`.

## Cómo probarlo

1. Inicia o reinicia `reservas-service`. Hibernate debe agregar la nueva columna `oculta_para_usuario` a la tabla `reservas`.
2. Ejecuta la aplicación y entra con un usuario que tenga reservas.
3. En **Mis reservas**, busca una reserva cancelada o rechazada, o una aprobada cuyo horario ya terminó.
4. Toca **Quitar de mi lista** y confirma. La reserva debe desaparecer de la lista personal.
5. Entra como administrador, abre **Reservas → Historial**, busca el correo del usuario y selecciona el estado correspondiente.
6. El mismo registro debe aparecer con la indicación **Oculta para el usuario · registro conservado**.

## Verificación realizada

- `mvn clean compile` en `reservas-service`: **BUILD SUCCESS**.
- `:app:compileDebugKotlin` en Android: **BUILD SUCCESSFUL**.
- No se ejecutaron pruebas automatizadas ni una prueba de extremo a extremo contra Supabase; esa comprobación requiere iniciar los servicios y probar el flujo en el emulador.
