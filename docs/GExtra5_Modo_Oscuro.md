# GExtra5 — Modo claro y oscuro

## Objetivo

Permitir que cada persona elija entre el tema claro y el oscuro, sin cambiar la identidad de la app: el azul marino, el acero y el naranja siguen siendo los colores de la marca.

## Qué ve la persona

- **Usuarios:** en **Perfil** hay una fila **Modo oscuro** con un interruptor.
- **Administrador:** en **Más** hay la misma fila, justo antes de "Cerrar sesión".
- El cambio se aplica al instante en toda la app y se recuerda al cerrarla y volver a abrirla. La app abre en modo claro la primera vez.
- Los íconos de la barra de estado y de navegación del teléfono se ajustan al modo elegido.
- El menú lateral del administrador y las fotos de la Escuela Militar se ven igual en los dos modos.

## Cómo está hecho

Las pantallas de Material 3 ya usaban `MaterialTheme`, y el tema oscuro ya existía (`DarkColorScheme`), pero estaba fijo en claro. Las pantallas de usuario leen los colores de la marca desde `EspaciosPalette` (unos 200 usos), así que se resolvió ahí y no pantalla por pantalla:

- `LightPalette` y `DarkPalette` (`ui/theme/EspaciosPalette.kt`) guardan los mismos nombres de color en cada modo.
- `EspaciosPalette` entrega el color del modo activo. Su estado es de Compose, así que al cambiar de modo todo lo que lo lee se vuelve a dibujar. Los nombres se mantuvieron, por lo que casi no hubo que tocar las pantallas.
- En oscuro los pares se invierten: `Navy` (texto y botones) pasa a ser claro y `White` (tarjetas y texto sobre botones) pasa a ser oscuro. Así, un botón `Navy` con texto `White` sigue siendo legible.
- `ThemeSettings` (`ui/theme/ThemeSettings.kt`) guarda la elección en `SharedPreferences` (`gaja_settings`, clave `dark_mode`) a través de la interfaz `ThemeStorage`, que se reemplaza por un fake en las pruebas.
- `MainActivity` lee el modo antes de dibujar, para que no parpadee al abrir, y pasa `darkTheme` al tema.
- El esquema claro del tema usa `LightPalette` directamente, para no depender del modo activo.

## Archivos

- `ui/theme/EspaciosPalette.kt`: `LightPalette`, `DarkPalette` y la paleta adaptable.
- `ui/theme/ThemeSettings.kt`: estado, almacenamiento y guardado del modo.
- `ui/theme/Theme.kt` y `ui/theme/Color.kt`: el tema recibe el modo y avisa a la paleta.
- `MainActivity.kt`: lectura inicial y barras del sistema.
- `ui/screen/ProfileScreen.kt` y `ui/screen/admin/AdminMoreScreen.kt`: el interruptor.
- `ui/screen/AdminShell.kt`: el menú lateral usa `LightPalette` para mantenerse azul marino.

## Pruebas

- `ThemeSettingsTest`: abre en claro sin nada guardado, respeta lo guardado, aplica y guarda el cambio, y la paleta sigue al modo.
- `PaletteContrastTest`: comprueba que los pares de texto y fondo del modo oscuro tengan contraste de al menos 4,5, y los del claro que lo conservan.

## Límites

- Las pantallas se revisan a mano en el emulador: no hay pruebas automáticas de interfaz.
- Dos colores del modo claro original (el gris `Slate` sobre crema y el verde sobre verde suave) quedan algo bajo 4,5 de contraste; no se modificaron.
- El modo no sigue al del sistema: es una elección dentro de la app.
