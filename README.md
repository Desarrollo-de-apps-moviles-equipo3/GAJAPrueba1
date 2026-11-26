# GAJA · Reserva de espacios deportivos

Aplicación móvil Android para solicitar y consultar la reserva de espacios deportivos (canchas, gimnasios y recintos). Desarrollada en Kotlin con Jetpack Compose como proyecto de la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105)**, Evaluación Parcial 2.

**Link Trello**
https://trello.com/invite/b/6aa1ceeb2a571c1c37ebe421/ATTI0431bfec356275823e26300420aa7227A4B89F4A/gaja

**Equipo 3**

| Integrante | Módulo |
|---|---|
| Josefa Gaete | Acceso, sesión y roles; Mis reservas |
| Angella Caiozzi | Inicio, espacios y ubicación |
| Gabriel Alcalá | Formulario de reserva y reservas |
| Almendra Vera | Base de datos, componentes reutilizables, íconos de error y pruebas |

## Cómo está armado

La app Android se conecta a tres microservicios Spring Boot (repositorio **GAJA-microservicios**), cada uno con su propio esquema en Supabase (PostgreSQL):

| Servicio | Puerto | Qué hace |
|---|---|---|
| `usuarios-service` | 8081 | Perfiles, login, estados del perfil (pendiente, aprobado, rechazado, desactivado) y registro de sesiones |
| `espacios-service` | 8082 | Espacios, categorías y bloqueos de agenda |
| `reservas-service` | 8083 | Reservas, alertas, incidencias y reglas de reserva |

En el teléfono solo se guarda la sesión (Room), la foto de perfil y el modo claro u oscuro.

## Funcionalidades

**Acceso**
- Inicio de sesión con correo y contraseña. No se elige rol: cada cuenta entra con el rol que tiene registrado y una cuenta Administrador abre el panel de administración.
- Un perfil pendiente, rechazado o desactivado no entra y el mensaje explica por qué.
- Recuperar acceso, con validación. La sesión se guarda en Room y "Cerrar sesión" la elimina.

**Cadete, Oficial, Oficial de semana e Institución externa**
- Inicio con buscador, carrusel de espacios y "Cerca de mí" (permiso de ubicación).
- Calendario por espacio con horas libres, reservadas, bloqueadas y pasadas.
- Reserva con hora de inicio y de término (08:00 a 23:00, horas completas), validada en la app y en el servicio.
- Mis reservas: cancelar con el plazo que fije el administrador y quitar de la lista las ya finalizadas.
- Alertas por cada cambio en una reserva.
- Perfil: foto, datos personales, cambiar contraseña, notificaciones, ayuda y soporte con seguimiento de la solicitud, y modo oscuro.

**Administrador**
- Inicio con resumen, Agenda (bloqueos), Reservas (aprobar o rechazar con motivo) y Perfiles (aprobar, rechazar, desactivar, activar y crear usuarios).
- Menú Más: Espacios y categorías, Alertas, Incidencias, Reportes, Soporte y contacto, Registro de sesiones, Notificaciones y Reglas de reserva (días de anticipación y plazo de cancelación; por defecto 7 días y 2 horas).
- Modo oscuro desde el mismo menú.

**Modo claro y oscuro**
- Un interruptor en Perfil (usuarios) y en Más (administrador). Se aplica al instante y se recuerda al cerrar la app. Detalle en `docs/GExtra5_Modo_Oscuro.md`.

**Recursos nativos**
- Ubicación y galería de fotos.

## Tecnologías

- Kotlin y Jetpack Compose con Material Design 3
- Arquitectura MVVM con `StateFlow`, repositorios con dependencias inyectadas
- Retrofit, Gson y OkHttp para hablar con los microservicios
- Room (con KSP) para la sesión
- Android mínimo: API 24 (Android 7.0)

## Estructura del proyecto

```
app/src/main/java/com/example/gaja_prueba1/
├── data/          Room y clientes Retrofit (data/remote)
├── model/         Modelos y repositorios
├── validation/    Validaciones (lógica fuera de la interfaz)
├── ui/
│   ├── components/  Componentes reutilizables (GajaButton, GajaTextField, GajaChip, GajaCard, GajaSelector)
│   ├── screen/      Pantallas (screen/admin para el administrador)
│   ├── viewmodels/  ViewModels
│   └── theme/       Colores, paleta clara y oscura, tipografía y tema
└── util/          Ubicación y almacenamiento de la foto de perfil
```

## Cómo ejecutar

1. Enciende los tres microservicios (ver el README de cada uno; necesitan las variables `SUPABASE_DB_URL`, `SUPABASE_DB_USER` y `SUPABASE_DB_PASSWORD`).
2. En `data/remote/ApiClient.kt`, deja `HOST` con la dirección del computador que corre los servicios (`http://10.0.2.2` si usas el emulador de Android Studio).
3. Abre el proyecto con Android Studio, espera a que Gradle sincronice y presiona **Run**.

### Usuarios de prueba

La contraseña de todos es `12345`. Cada correo entra con su propio rol.

| Correo | Rol / estado |
|---|---|
| admin@gaja.cl | Administrador |
| cadete@gaja.cl | Cadete |
| oficial@gaja.cl | Oficial |
| oficial.semana@gaja.cl | Oficial de semana |
| externo@gaja.cl | Institución externa |
| cadete.nuevo@gaja.cl, externo.nuevo@gaja.cl | Pendientes de aprobación |
| cadete.rechazado@gaja.cl | Rechazado |
| oficial.baja@gaja.cl | Desactivado |

## Pruebas

- App (ViewModels, repositorios, validaciones y paleta de colores):
  ```
  ./gradlew test
  ```
- Microservicios, desde la carpeta de cada servicio:
  ```
  mvn test
  ```
- Las pantallas se prueban a mano en el emulador, usando una cuenta por rol.

## Documentación

La carpeta `docs/` tiene una guía por cada entrega: incidencias y alertas, reportes y reglas, pruebas unitarias, historial de reservas, login y horarios flexibles, y modo oscuro.

## Metodología de trabajo

- Desarrollo ágil en sprints, con entregas parciales y dailies en clases.
- Planificación y distribución de tareas en Trello.
- Control de versiones en GitHub, con commits por integrante y revisión entre compañeros.
