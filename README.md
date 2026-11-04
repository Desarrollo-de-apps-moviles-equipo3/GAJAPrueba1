# GAJA · Reserva de espacios deportivos

Aplicación móvil Android para solicitar y consultar la reserva de espacios deportivos (canchas, gimnasios y recintos). Desarrollada en Kotlin con Jetpack Compose como proyecto de la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105)**, Evaluación Parcial 2.

**Equipo 3**

| Integrante | Módulo |
|---|---|
| Josefa Gaete | Acceso, sesión y roles; Mis reservas |
| Angella Caiozzi | Inicio, espacios y ubicación |
| Gabriel Alcalá | Formulario de reserva y reservas |
| Almendra Vera | Base de datos, componentes reutilizables, íconos de error y pruebas |

## Funcionalidades implementadas

**Acceso**
- Inicio de sesión con correo, contraseña (con botón para mostrar u ocultar) y selector de rol: Cadete, Oficial, Oficial de semana e Institución externa.
- Validaciones por campo con mensaje e ícono de error, indicador de carga en el botón y mensajes de credenciales incorrectas.
- Recuperar acceso, abierto desde "¿Olvidaste tu contraseña?", con su propia validación.
- La sesión se guarda en Room: al cerrar y volver a abrir la app se entra directo al Inicio. Cerrar sesión la elimina.

**Inicio y espacios**
- Saludo, contadores de reservas próximas y pasadas (leídos desde Room), buscador y carrusel de espacios.
- "Cerca de mí": pide el permiso de ubicación en ejecución y ordena los espacios por distancia.
- Listado completo de canchas desde la pestaña Reservas.

**Reservas**
- Formulario de reserva: espacio, fecha, horario de 2 horas y motivo, con validaciones y mensajes con ícono de error.
- Reglas de negocio: máximo 3 reservas por día, hasta 7 días de anticipación, motivo de al menos 10 caracteres, no se permite una fecha pasada ni un horario ocupado.
- Pantalla de solicitud enviada con animación.
- Mis reservas: lista de las reservas guardadas, accesible desde Perfil.

**Perfil**
- Foto de perfil elegida desde la galería, guardada en el almacenamiento interno de la app.

**Recursos nativos del dispositivo**
- Ubicación (permisos `ACCESS_COARSE_LOCATION` y `ACCESS_FINE_LOCATION`, con manejo de permiso concedido y negado).
- Galería de fotos (selector de fotos del sistema).

**Animaciones**
- Transición entre el login y el Inicio, indicador de carga en los botones, mensajes de error animados, animaciones en el Inicio y en el cambio de pestañas, y animación de la pantalla de solicitud enviada.

## Funcionalidades pendientes

- Pestañas Calendario y Alertas (muestran "Próximamente").
- Datos personales y Cambiar contraseña desde Perfil.
- Servidor y base de datos remota; por ahora todo funciona localmente.
- Panel de administrador, aprobación de solicitudes y notificaciones.

## Tecnologías

- Kotlin y Jetpack Compose con Material Design 3
- Arquitectura MVVM con `StateFlow`
- Room (con KSP) para la persistencia local
- Android mínimo: API 24 (Android 7.0)
- Solo librerías gratuitas y de código abierto

## Estructura del proyecto

```
app/src/main/java/com/example/gaja_prueba1/
├── data/          Base de datos Room (entidades, DAOs y AppDatabase)
├── model/         Modelos y repositorios (sesión, reservas, espacios, autenticación)
├── validation/    Validaciones de login y de reservas (lógica fuera de la interfaz)
├── ui/
│   ├── components/  Componentes reutilizables (GajaButton, GajaTextField, GajaChip, GajaCard, GajaSelector)
│   ├── screen/      Pantallas
│   ├── viewmodels/  ViewModels
│   └── theme/       Colores, tipografía y tema
└── util/          Ubicación y almacenamiento de la foto de perfil
```

## Cómo ejecutar

1. Clona el repositorio: `git clone https://github.com/Desarrollo-de-apps-moviles-equipo3/GAJAPrueba1.git`
2. Ábrelo con Android Studio y espera a que Gradle termine de sincronizar.
3. Selecciona un emulador o un celular con depuración USB activada.
4. Presiona **Run**.

### Usuarios de prueba

La contraseña de todos es `12345`. El rol seleccionado en el login debe coincidir con el del usuario.

| Correo | Rol |
|---|---|
| cadete@gaja.cl | Cadete |
| oficial@gaja.cl | Oficial |
| oficial.semana@gaja.cl | Oficial de semana |
| externo@gaja.cl | Institución externa |
| profe_exe@duoc.cl | Cualquier rol |

## Pruebas

- Unitarias (validaciones de login, recuperar acceso y reservas, filtro de espacios y cálculo de distancia):
  ```
  ./gradlew test
  ```
- Instrumentadas de componentes Compose (requieren un emulador o celular conectado):
  ```
  ./gradlew connectedAndroidTest
  ```

## Metodología de trabajo

- Desarrollo ágil en sprints, con entregas parciales y dailies en clases.
- Planificación y distribución de tareas en Trello.
- Control de versiones en GitHub, con commits por integrante y revisión entre compañeros.
