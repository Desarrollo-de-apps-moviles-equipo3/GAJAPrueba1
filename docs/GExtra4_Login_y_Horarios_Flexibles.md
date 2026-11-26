# GExtra4 — Login automático por rol y horarios flexibles

## Objetivo

Registrar los cambios solicitados para la Escuela Militar: que el rol provenga de la cuenta iniciada y que las reservas permitan elegir una hora de inicio y una hora de término, en vez de obligar a escoger bloques fijos de dos horas.

## 1. Login según la cuenta

### Antes

La pantalla pedía correo, contraseña y una selección manual como **Cadete**, **Oficial** o **Administrador**. Esa selección se enviaba al servicio y podía no corresponder con el rol guardado en la cuenta.

### Ahora

1. El usuario ingresa correo y contraseña.
2. `usuarios-service` busca la cuenta y comprueba la contraseña y el estado del perfil.
3. Si el acceso es válido, el servicio responde con los datos de la persona y su rol registrado.
4. La app guarda el rol de la respuesta y abre la experiencia correspondiente; una cuenta con rol **Administrador** entra al panel administrativo.

El rol ya no forma parte del formulario ni del cuerpo de inicio de sesión. También se retiró el acceso local especial de `profe_exe@duoc.cl`: las cuentas de prueba deben existir y estar aprobadas en `usuarios-service`, igual que las demás cuentas.

### Archivos del login

- `app/src/main/java/com/example/gaja_prueba1/ui/screen/LoginScreen.kt`: elimina el selector “Ingresar como”.
- `app/src/main/java/com/example/gaja_prueba1/ui/viewmodels/LoginViewModel.kt`: valida el correo y la contraseña y llama al repositorio sin enviar un rol elegido.
- `app/src/main/java/com/example/gaja_prueba1/model/AuthRepository.kt`: envía solo correo y contraseña y toma el rol devuelto por el servicio.
- `app/src/main/java/com/example/gaja_prueba1/data/remote/UsuariosApi.kt`: cambia el DTO de login a `email` y `password`.
- `usuarios-service/src/main/java/com/gaja/usuarios/dto/LoginRequest.java`: recibe únicamente correo y contraseña.
- `usuarios-service/src/main/java/com/gaja/usuarios/service/UsuarioService.java`: autentica las credenciales y devuelve el rol almacenado en la cuenta.

## 2. Horarios de reserva flexibles

### Regla de operación

- Ventana diaria: **08:00 a 23:00**.
- Granularidad: horas completas.
- Anticipación inicial: **7 días**.
- No hay un máximo de horas diarias por usuario ni un tope global de solicitudes por fecha.
- Las reservas rechazadas o canceladas ya no ocupan horas ni bloquean el espacio.

### Qué ve la persona

Después de elegir la fecha, la persona toca primero una hora de inicio y luego una hora de término en la misma cuadrícula. Por ejemplo, al escoger `08:00` y luego `11:00`, crea una sola reserva para `08:00 - 11:00`, que ocupa todas las horas de ese intervalo. La hora de término no puede pasar de las 23:00. La app deshabilita horas que ya pasaron y rangos que se crucen con una reserva activa o con un bloqueo de la agenda.

Los extremos seleccionados se destacan en azul oscuro y las horas intermedias usan un fondo azul claro, mientras que las opciones disponibles mantienen contraste. La indicación para elegir el inicio y el término se oculta al completar el rango para no repetir información. El botón **Restablecer**, redondeado y con un ícono de actualización, borra la selección para empezar de nuevo.

Los horarios se siguen guardando y mostrando como un rango de texto, por ejemplo `09:00 - 12:00`. Así, las reservas existentes conservan sus datos. Al comparar disponibilidad, tanto la app como `reservas-service` interpretan el inicio y el fin del rango; por ejemplo, una reserva `09:00 - 12:00` también ocupa `10:00 - 11:00` y `11:00 - 12:00`.

La agenda administrativa representa los bloqueos en intervalos de una hora. Los bloqueos existentes de más de una hora se interpretan por sus intervalos para que sigan impidiendo reservas que se superpongan.

### Reglas editables por administración

En **Más → Reglas de reserva**, el administrador puede cambiar:

- Cuántos días hacia adelante se permite reservar.
- Con cuántas horas de anticipación puede cancelar.

El horario 08:00–23:00 y el uso de horas completas se mantienen como reglas fijas del servicio. La app consulta las reglas para limitar el calendario y la selección; el microservicio vuelve a validarlas al guardar la solicitud.

### Archivos de la app relacionados con reservas

- `app/src/main/java/com/example/gaja_prueba1/validation/ReservationValidator.kt`: define la ventana horaria, representa los bloques de una hora y detecta cruces entre intervalos.
- `app/src/main/java/com/example/gaja_prueba1/ui/screen/ReservationFormScreen.kt`: muestra el calendario, las opciones de hora inicial y final y el rango elegido.
- `app/src/main/java/com/example/gaja_prueba1/ui/viewmodels/ReservationViewModel.kt`: carga reglas y disponibilidad de reservas/bloqueos; valida la selección antes de enviarla.
- `app/src/main/java/com/example/gaja_prueba1/model/RemoteReservationsRepository.kt` y `app/src/main/java/com/example/gaja_prueba1/data/remote/ReservasApi.kt`: consultan disponibilidad, reservas del usuario y reglas del microservicio.
- `app/src/main/java/com/example/gaja_prueba1/model/SpaceAvailability.kt`: calcula el estado de cada hora de la agenda considerando reservas, bloqueos y horas que ya pasaron.
- `app/src/main/java/com/example/gaja_prueba1/model/BlockPlanner.kt` y `app/src/main/java/com/example/gaja_prueba1/ui/screen/admin/AdminAgendaScreen.kt`: adaptan la administración de bloqueos a intervalos horarios.
- `app/src/main/java/com/example/gaja_prueba1/ui/viewmodels/AdminReservationRulesViewModel.kt` y `app/src/main/java/com/example/gaja_prueba1/ui/screen/admin/AdminReservationRulesScreen.kt`: permiten consultar, validar y guardar los límites administrables.
- `app/src/main/java/com/example/gaja_prueba1/model/AdminReservationsRepository.kt` y `app/src/main/java/com/example/gaja_prueba1/model/AdminReportsRepository.kt`: envían la actualización de reglas al servicio.

### Archivos de `reservas-service`

- `reservas-service/src/main/java/com/gaja/reservas/model/ReglasReserva.java`: entidad persistente para las reglas configurables.
- `reservas-service/src/main/java/com/gaja/reservas/repo/ReglasReservaRepository.java`: repositorio que consulta y guarda las reglas.
- `reservas-service/src/main/java/com/gaja/reservas/dto/ReglasReservaResponse.java`: respuesta con límites y horario de atención.
- `reservas-service/src/main/java/com/gaja/reservas/dto/ActualizarReglasReservaRequest.java`: valida los días de anticipación y el plazo de cancelación enviados por el administrador.
- `reservas-service/src/main/java/com/gaja/reservas/service/ReservaService.java`: valida fechas, horarios dentro de la ventana, horas completas y traslapes con reservas activas; no impone un máximo diario de horas.
- `reservas-service/src/main/java/com/gaja/reservas/web/ReservaController.java`: publica las consultas y actualización de reglas.

La configuración inicial que entrega el servicio, mientras no se haya guardado otra, es **7 días** de anticipación y **2 horas** para cancelar. Hibernate tiene `ddl-auto=update`, por lo que al reiniciar `reservas-service` se crea la tabla `reglas_reserva` si aún no existe.

## Cómo probarlo

1. Inicia o reinicia `usuarios-service` y `reservas-service` con el código actualizado.
2. Inicia la app e ingresa con cuentas existentes y aprobadas. Comprueba que no aparece el selector de tipo de usuario y que la cuenta administradora abre el panel administrativo.
3. Como usuario, crea una reserva eligiendo una fecha, una hora de inicio y otra de término. Comprueba que no se permitan horas pasadas ni un rango que se cruce con otra reserva o bloqueo.
4. Reserva, por ejemplo, desde las 08:00 hasta las 11:00. Debe guardarse como un solo rango y bloquear todas las horas que abarca.
5. En **Más → Reglas de reserva**, cambia los días de anticipación o el plazo de cancelación y guarda. Vuelve al formulario para comprobar que se apliquen.
6. Revisa el calendario y el historial: deben mostrar correctamente las reservas anteriores y los nuevos rangos horarios.

## Verificación realizada

- `:app:test`: **BUILD SUCCESSFUL**; 265 pruebas unitarias ejecutadas, 0 fallidas y 0 errores.
- `mvn -DskipTests clean compile` en `usuarios-service`: **BUILD SUCCESS**.
- `mvn -DskipTests clean compile` en `reservas-service`: **BUILD SUCCESS**.
- JaCoCo 0.8.12 mostró avisos de instrumentación de clases internas de JVM con bytecode versión 69; no impidieron que Gradle completara las pruebas correctamente.
- No se hizo una prueba manual en el emulador con los servicios conectados a Supabase.
- `git pull --rebase --autostash` se ejecutó en los repositorios de la app y los microservicios; ambos estaban al día antes de preparar estos commits.
