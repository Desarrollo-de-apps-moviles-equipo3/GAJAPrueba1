# GExtra2 — Estética del inicio y mensajes de error

## Objetivo

Dar más personalidad a la pantalla Inicio y mejorar cómo se ven los errores, manteniendo una apariencia apropiada para la Escuela Militar y coherente con el resto de GAJA.

## Qué se hizo y por qué

### Inicio con más color y formas simples

- Se dejó la fotografía de la Escuela Militar como elemento principal y se añadieron dos círculos translúcidos dorados sobre la imagen. Son formas sencillas que dan profundidad sin competir con el contenido.
- Se cambió el fondo azul continuo por un fondo crema. El azul marino sigue identificando títulos, navegación y acciones principales; el verde, beige, blanco y naranja dan contraste y hacen que la pantalla se sienta menos plana.
- La cabecera tiene las esquinas inferiores redondeadas para separar visualmente la foto del contenido.
- Las tres tarjetas de resumen ahora pueden elegir su propio color de fondo. Disponibilidad usa verde suave, próximas reservas beige e historial blanco. Se conservaron colores por defecto para las demás pantallas que usan el mismo componente.
- Los títulos de “Canchas destacadas”, “Mi semana” y “Alertas” se adaptaron al fondo claro para mantener buen contraste.

### Errores con contexto y reintento

- Los errores de carga en Inicio ahora aparecen en una tarjeta suave con icono de nube, explicación y botón “Reintentar”.
- El error del clima y los problemas de ubicación se muestran en tarjetas con colores de error legibles, en vez de texto suelto sobre un fondo oscuro.
- El error del calendario también tiene icono, fondo destacado y botón para volver a cargar.
- La pantalla común “Sin conexión”, usada por reservas, alertas y todas las canchas, ahora presenta un icono dentro de una forma circular, una tarjeta con sombra suave y el botón naranja de reintento.
- En “Todas las canchas”, el estado de error utiliza esa misma pantalla para que las partes de la app se vean consistentes.

## Archivos modificados

- `app/src/main/java/com/example/gaja_prueba1/ui/screen/HomeScreen.kt`: fondo, cabecera, formas decorativas, tarjetas, títulos y mensajes de error de Inicio.
- `app/src/main/java/com/example/gaja_prueba1/ui/components/GajaCard.kt`: permite personalizar los colores de las tarjetas numéricas sin cambiar los valores por defecto de otras pantallas.
- `app/src/main/java/com/example/gaja_prueba1/ui/screen/OfflineErrorScreen.kt`: rediseño del estado compartido de desconexión.
- `app/src/main/java/com/example/gaja_prueba1/ui/screen/CalendarScreen.kt`: tarjeta de error del calendario.
- `app/src/main/java/com/example/gaja_prueba1/ui/screen/AllCourtsScreen.kt`: fondo crema y uso del estado compartido de error.

## Cómo revisarlo

1. Abre la aplicación y entra en **Inicio**. Revisa el fondo crema, la imagen superior, los acentos geométricos y las tres tarjetas de resumen.
2. Apaga internet o detén el servicio de espacios. Inicio y **Todas las canchas** deben mostrar una tarjeta con error y el botón **Reintentar**.
3. Abre **Calendario** sin conexión para ver el mensaje de error integrado en una tarjeta.
4. Enciende internet o vuelve a iniciar el servicio y toca **Reintentar**. Los datos deben volver a cargarse.

## Verificación

La tarea `:app:assembleDebug` terminó con **BUILD SUCCESSFUL** después de los cambios visuales. La comprobación de los distintos estados sin conexión debe hacerse en el emulador con el servicio detenido y luego restaurado.
