# G6 Reportes y reglas de reserva

## Objetivo

G6 agrega al panel de administración informes por usuario y por espacio, un gráfico de aprobadas frente a rechazadas al abrir el detalle de una persona, y una pantalla que presenta las reglas vigentes de reserva. Los datos se obtienen del microservicio de reservas para que la app muestre información persistida y use los mismos límites que aplica el servidor.

## Qué se implementó

### Reportes de uso

En **Más → Reportes**, el administrador puede alternar entre **Por usuario** y **Por espacio**. Las filas muestran cuántas solicitudes fueron aprobadas y rechazadas para cada correo o espacio, ordenadas por la cantidad de decisiones.

Al tocar un usuario, se abre su detalle con un gráfico pastel que compara las cantidades aprobadas y rechazadas, sus porcentajes y el total de solicitudes decididas. Las solicitudes pendientes y canceladas no forman parte de esa comparación. Se incluyen los registros que la persona haya ocultado de su propia lista, ya que el administrador consulta el historial del servicio.

La presentación es interactiva dentro de la app; no se genera un archivo CSV.

### Reglas de reserva

En **Más → Reglas de reserva**, el panel consulta y permite actualizar los valores que aplica `reservas-service`:

- Anticipación máxima para reservar.
- No existe un máximo de horas por día ni un tope global de solicitudes. La reserva debe caber en el horario de atención y no cruzarse con un bloqueo o una solicitud activa del mismo espacio.
- Margen mínimo para cancelar antes del inicio.
- Horario de atención: de 08:00 a 23:00, en horas completas.

Los días de anticipación y el margen de cancelación se guardan en la base del microservicio. El servicio valida las fechas, la ventana de atención y los cruces entre intervalos antes de guardar cada solicitud. El horario de atención se mantiene como regla fija del servicio.

## Recorrido de los datos

```text
Pantalla de Reportes ── AdminReportsViewModel ── AdminReportsRepository
                                                   │
                                      GET /api/reservas
                                                   │
                                          reservas-service

Pantalla Reglas ── AdminReservationRulesViewModel ── AdminReportsRepository
                                                        │
                                      GET/PUT /api/reservas/reglas
```

El ViewModel mantiene el estado de carga, error, tipo de informe y resultados. El repositorio solicita las reservas y las reglas mediante el cliente Retrofit ya configurado. La interfaz agrupa aprobadas y rechazadas; al seleccionar un usuario calcula los porcentajes y dibuja las dos partes del gráfico.

## Archivos de la app

- `app/src/main/java/com/example/gaja_prueba1/model/AdminReportsRepository.kt`: obtiene las reservas y las reglas desde el repositorio remoto existente.
- `app/src/main/java/com/example/gaja_prueba1/ui/viewmodels/AdminReportsViewModel.kt`: agrupa aprobadas y rechazadas por correo o espacio y mantiene el estado del informe.
- `app/src/main/java/com/example/gaja_prueba1/ui/screen/admin/AdminReportsScreen.kt`: muestra los resúmenes, abre el detalle del usuario y dibuja el gráfico pastel con su leyenda y porcentajes.
- `app/src/main/java/com/example/gaja_prueba1/ui/viewmodels/AdminReservationRulesViewModel.kt`: carga, valida y guarda las reglas.
- `app/src/main/java/com/example/gaja_prueba1/ui/screen/admin/AdminReservationRulesScreen.kt`: permite ajustar los días de anticipación y el margen de cancelación.
- `app/src/main/java/com/example/gaja_prueba1/data/remote/AdminReservasApi.kt`: declara las consultas HTTP GET y PUT para las reglas.
- `app/src/main/java/com/example/gaja_prueba1/model/AdminReservationsRepository.kt`: adapta y guarda la configuración recibida del microservicio.
- `app/src/main/java/com/example/gaja_prueba1/ui/screen/AdminShell.kt`: conecta los destinos **Reportes** y **Reglas de reserva** del menú con sus pantallas.

El menú **Más** ya contenía las dos opciones, por lo que no fue necesario cambiar `AdminMoreScreen.kt`.

## Archivos del microservicio

- `reservas-service/src/main/java/com/gaja/reservas/dto/ReglasReservaResponse.java`: define el formato JSON que entrega el servicio.
- `reservas-service/src/main/java/com/gaja/reservas/dto/ActualizarReglasReservaRequest.java`: valida los valores que envía el panel.
- `reservas-service/src/main/java/com/gaja/reservas/model/ReglasReserva.java`: persiste la configuración en la tabla `reglas_reserva`.
- `reservas-service/src/main/java/com/gaja/reservas/repo/ReglasReservaRepository.java`: lee y guarda esa configuración.
- `reservas-service/src/main/java/com/gaja/reservas/service/ReservaService.java`: aplica la anticipación y el margen de cancelación y valida intervalos que se superponen.
- `reservas-service/src/main/java/com/gaja/reservas/web/ReservaController.java`: publica `GET` y `PUT /api/reservas/reglas`.

La respuesta contiene `maxDiasAdelante`, `horasLimiteCancelacion`, `horaApertura` y `horaCierre`. Si todavía no se guardó una configuración, el servicio devuelve los valores iniciales: 7 días y 2 horas de anticipación para cancelar.

## Cómo probarlo

1. Inicia o reinicia `reservas-service` en el puerto 8083.
2. Abre la app con una cuenta administradora y entra en **Más → Reportes**.
3. En **Por usuario**, toca un correo y revisa el gráfico, porcentajes, aprobadas y rechazadas.
4. Regresa al listado y cambia a **Por espacio** para revisar el desglose de cada recinto.
5. Vuelve al menú **Más** y entra en **Reglas de reserva**. Compara los valores mostrados con los límites configurados por el microservicio.
6. Si no hay conexión, las pantallas deben mostrar un mensaje y permitir reintentar.

## Verificación

- `:app:compileDebugKotlin`: **BUILD SUCCESSFUL** después de reemplazar la descarga CSV por el detalle con gráfico.
- `mvn clean compile` en `reservas-service`: **BUILD SUCCESS**.
- No se ejecutaron pruebas automatizadas ni se probó el flujo en el emulador conectado a Supabase; esa verificación queda para la prueba manual descrita arriba.

## Relación con otros extras

Este documento registra exclusivamente G6. **GExtra3**, sobre ocultar reservas al usuario y conservarlas en el historial administrativo, se mantiene como documento y cambio independiente.
