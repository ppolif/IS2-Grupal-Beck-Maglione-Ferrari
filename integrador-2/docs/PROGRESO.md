# PROGRESO.md — Seguimiento de implementación Dulce Mayor

## Paso 1: Núcleo de seguridad del backend

### 1. Qué se hizo
- **Modelo de dominio de seguridad (`com.DulceMayor.backend.entities`):**
  - `RolUsuario`: enumeración con los valores exactos de `01-dominio.md` (`ADMINISTRATIVO`, `JEFE`, `PREVENTISTA`, `MINORISTA`, `DISTRIBUIDOR`).
  - `ProveedorAuth`: enumeración con los valores de `01-dominio.md` (`LOCAL`, `GOOGLE`, `FACEBOOK`).
  - `Usuario`: entidad JPA con `id` (UUID), `nombreUsuario` (único, no nulo), `clave` (encriptada con BCrypt, excluida de serialización JSON y `toString`), `rol`, `claveTemporal`, `proveedorAuth` y `eliminado`. Incluye la constante `CLAVE_POR_DEFECTO = "dulcemayor"`.
- **Repositorio y servicios (`com.DulceMayor.backend.repositories`, `com.DulceMayor.backend.services`):**
  - `UsuarioRepository`: consultas por `nombreUsuario` y por `id` filtrando `eliminado = false`.
  - `UsuarioService`: implementa `UserDetailsService`, autenticación (`login`), verificación y cambio de contraseña (`modificarClave`), consulta de perfil (`obtenerPerfil`) y creación de usuarios semilla con clave por defecto y `claveTemporal = true`.
- **Seguridad JWT, filtro stateless y CORS (`com.DulceMayor.backend.config`):**
  - `JwtProvider`: firma y validación HS256 (`io.jsonwebtoken:jjwt-*` versión fija `0.11.5`). Exige `JWT_SECRET` de al menos 32 caracteres al iniciar la aplicación y configura expiración mediante `app.jwt.expiration-minutes` (por defecto 60). Incluye `sub = id` y `rol`, sin incluir `claveTemporal` en el token.
  - `JwtAuthenticationFilter`: valida el token en cada request, consulta el usuario activo en la base de datos y verifica `claveTemporal` en tiempo real. Si `claveTemporal = true`, bloquea cualquier endpoint distinto de `POST /auth/cambiar-clave` y `GET /auth/perfil` respondiendo `403 Forbidden` con `code = CAMBIO_CLAVE_REQUERIDO`.
  - `PasswordConfig`: expone `BCryptPasswordEncoder`.
  - `SecurityConfig`: sesión `STATELESS`, CSRF deshabilitado, `@EnableMethodSecurity`, autoridades `"ROLE_" + rol`, CORS configurable mediante `app.cors.allowed-origins` (sin `*` con credenciales), rutas públicas (`POST /auth/login`, `GET /publico/**`, lecturas `GET` de `/paises`, `/provincias`, `/departamentos`, `/localidades`, `/categorias`, `/subcategorias` y `GET /imagenes/{id}/contenido`, más Swagger solo en perfil `dev`) y manejadores `AuthenticationEntryPoint` (401) y `AccessDeniedHandler` (403) con `ErrorResponse`.
- **Manejo centralizado de errores (`com.DulceMayor.backend.config`, `com.DulceMayor.backend.dtos`):**
  - `ErrorResponse`: record con `timestamp`, `status`, `code`, `message`, `path` y `errors[{campo, mensaje}]`.
  - `GlobalExceptionHandler` (`@RestControllerAdvice`): unifica respuestas 400 (`VALIDACION`), 401 (`CREDENCIALES_INVALIDAS` / `NO_AUTENTICADO`), 403 (`ACCESO_DENEGADO` / `CAMBIO_CLAVE_REQUERIDO`), 404 (`NO_ENCONTRADO`), 409 (`CONFLICTO` / `DUPLICADO`) y 500 (`ERROR_INTERNO`) sin exponer stack traces ni detalles internos.
- **Endpoints y DTOs de autenticación (`com.DulceMayor.backend.controllers`, `com.DulceMayor.backend.dtos`):**
  - `AuthController`: implementa `POST /auth/login`, `POST /auth/cambiar-clave` (204) y `GET /auth/perfil` (200 con `personaId`, `nombre` y `apellido` en `null` hasta implementar `Persona`).
  - DTOs como records separados de la entidad (`LoginRequest`, `AuthResponse`, `UsuarioResponse`, `CambiarClaveRequest`, `PerfilResponse`).
- **Seed de datos (`com.DulceMayor.backend.config.DataInitializer`):**
  - Crea de manera idempotente un usuario por rol (`jefe`, `administrativo`, `preventista`, `distribuidor`, `minorista`) con clave `"dulcemayor"` hasheada con BCrypt y `claveTemporal = true` únicamente cuando `app.seed.enabled = true` (por defecto `false`).
- **Infraestructura y pruebas:**
  - `docker-compose.yml`: agregadas las variables `JWT_SECRET`, `APP_SEED_ENABLED=true` y `APP_CORS_ALLOWED_ORIGINS` al servicio `backend`.
  - `.env.example` creado en la raíz del proyecto (`.env` ignorado en `.gitignore`).
  - Suite de pruebas de integración con H2 en memoria (`SeguridadIntegracionTests` y `SeguridadTestController`).

### 2. Decisiones tomadas
- **Librería JWT:** se eligió `io.jsonwebtoken` (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) fijada en la versión exacta `0.11.5`.
- **Prevención de dependencias circulares:** se configuró `spring.main.allow-circular-references=false` y se separó `PasswordConfig` de `SecurityConfig`, inyectando todas las dependencias por constructor.
- **Compatibilidad de rutas con y sin prefijo `/api/v1`:** `AuthController`, `SecurityConfig` y `JwtAuthenticationFilter` aceptan tanto `/auth/...` como `/api/v1/auth/...` (y análogamente las rutas públicas), garantizando compatibilidad tanto con llamadas directas (`/auth/login`) como con clientes generados desde `04-openapi.yaml` cuyo server base incluye `/api/v1`.
- **Nota sobre Jenkinsfile:** el pipeline ejecuta `mvn clean test -Dspring.profiles.active=test`, que utiliza `application-test.properties` con H2 en memoria. Si en un entorno CI se desea inyectar `JWT_SECRET` desde credenciales de Jenkins, basta con envolver el paso con `withEnv(['JWT_SECRET=...'])` o `withCredentials([string(credentialsId: 'jwt-secret', variable: 'JWT_SECRET')])`.

### 3. Supuestos
- Mientras no exista la entidad `Persona`, `GET /auth/perfil` devuelve `personaId = null`, `nombre = null` y `apellido = null` tal como se especificó en el alcance del Paso 1.
- En `POST /auth/cambiar-clave`, los incumplimientos de reglas (clave actual incorrecta, confirmación distinta, longitud menor a 8 o igual a `"dulcemayor"`) responden `400 Bad Request` con `code = VALIDACION`, alineado con las respuestas declaradas para esa operación en `04-openapi.yaml`.

### 4. Desvíos respecto de los documentos
- Ninguno respecto del dominio (`01-dominio.md`) ni del contrato (`04-openapi.yaml`). Se respetó la estructura de paquetes existente en el repositorio (`config`, `controllers`, `dtos`, `entities`, `repositories`, `services`) tal como indica `03-arquitectura.md` y la consigna del Paso 1.

