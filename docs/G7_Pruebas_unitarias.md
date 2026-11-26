# G7 Pruebas unitarias

## Objetivo

G7 verifica con pruebas automatizadas las reglas de reserva, la selección de horarios, los ViewModels y los repositorios de la aplicación. Las pruebas usan datos controlados y servicios simulados, así se puede comprobar la lógica sin depender de Supabase ni de que los microservicios estén encendidos.

## Qué se agregó y corrigió

### Reglas de reserva

`ReservationValidatorTest` comprueba que las fechas pasadas y fuera del límite se rechacen, que hoy sea aceptado, que el motivo cumpla el mínimo de caracteres, que el rango se genere correctamente y que la detección de traslapes distinga horarios superpuestos de horarios consecutivos.

### ViewModel de reservas

`ReservationViewModelTest` cubre la elección de un inicio y un término, el botón para restablecer la selección, el envío del rango completo al repositorio y el rechazo de un rango que cruza una hora ocupada.

Se permitió inyectar `RemoteReservationsRepository` y `BlocksRepository` en `ReservationViewModel`, manteniendo valores predeterminados para el uso normal de la aplicación. En las pruebas se reemplazan por dobles controlados; esto evita llamadas de red y permite observar con precisión qué datos se envían.

### Reportes, reglas y repositorios

- `AdminReportsViewModelTest` revisa que el informe cuente aprobadas y rechazadas, ignore pendientes y canceladas, y pueda agrupar por usuario o recinto.
- `AdminReservationRulesViewModelTest` verifica la carga, el guardado y el rechazo de valores fuera del rango permitido.
- `RemoteReservationsRepositoryTest` comprueba la conversión de reglas recibidas y la eliminación de horarios duplicados.
- `AdminReportsRepositoryTest` comprueba que la capa de reportes delegue la lectura de reservas y la actualización de reglas.

### Pruebas existentes alineadas

Se actualizaron pruebas anteriores que ya no coincidían con el contrato actual: el login ya no envía un rol elegido; las interfaces Retrofit ahora incluyen operaciones para reglas e incidencias; la agenda trabaja con bloques de una hora; y los informes de reservas usan el historial completo. También se fijó una hora de referencia en una prueba de disponibilidad para que no dependa de la hora real del computador.

## Texto retirado de Reportes

En la vista **Por usuario** se quitó la frase que indicaba tocar a una persona para revisar sus reservas. El usuario puede abrir el detalle tocando la fila, pero la pantalla ya no muestra esa instrucción debajo del título. La ayuda correspondiente a **Por espacio** se mantiene.

## Archivos principales

- `app/src/main/java/com/example/gaja_prueba1/ui/screen/admin/AdminReportsScreen.kt`
- `app/src/main/java/com/example/gaja_prueba1/ui/viewmodels/ReservationViewModel.kt`
- `app/src/test/java/com/example/gaja_prueba1/testutil/MainDispatcherRule.kt`
- `app/src/test/java/com/example/gaja_prueba1/validation/ReservationValidatorTest.kt`
- `app/src/test/java/com/example/gaja_prueba1/model/RemoteReservationsRepositoryTest.kt`
- `app/src/test/java/com/example/gaja_prueba1/model/AdminReportsRepositoryTest.kt`
- `app/src/test/java/com/example/gaja_prueba1/ui/viewmodels/ReservationViewModelTest.kt`
- `app/src/test/java/com/example/gaja_prueba1/ui/viewmodels/AdminReportsViewModelTest.kt`
- `app/src/test/java/com/example/gaja_prueba1/ui/viewmodels/AdminReservationRulesViewModelTest.kt`

También se ajustaron `AuthRepositoryTest`, `AdminReservationsRepositoryTest`, `BookingsRepositoryTest`, `RemoteSupportRepositoryTest`, `BlockPlannerTest`, `SpaceAvailabilityTest`, `AdminAgendaViewModelTest` y `AdminReservationsViewModelTest` para que reflejen las APIs y reglas actuales.

## Cómo ejecutar

Desde la carpeta raíz del proyecto Android:

```powershell
.\gradlew.bat test
```

## Verificación

La ejecución de `:app:testDebugUnitTest` terminó correctamente: **265 pruebas, 0 fallidas y 0 errores**.

JaCoCo 0.8.12 imprime avisos al intentar instrumentar algunas clases internas de la JVM con versión de bytecode 69. Estos avisos no impidieron ejecutar las pruebas; el resultado de Gradle fue `BUILD SUCCESSFUL`.
