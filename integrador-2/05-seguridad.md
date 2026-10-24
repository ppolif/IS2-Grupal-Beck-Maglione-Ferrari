# Seguridad y Control de Acceso — Dulce Mayor

Este documento detalla las estrategias, políticas y configuraciones de seguridad implementadas en el ecosistema de aplicaciones "Dulce Mayor", cumpliendo con los requerimientos del Trabajo Práctico Integrador N°2.

## 1. Autenticación de Usuarios

La aplicación utiliza un esquema de autenticación sin estado (stateless) basado en **JWT (JSON Web Tokens)** para proteger la API REST.

### 1.1. Empleados (Autenticación Tradicional)

* **Login:** Se realiza mediante correo electrónico y contraseña.

* **Tokens:** Tras una validación exitosa, el backend emite un JWT firmado que contiene la identidad del usuario (ID, correo) y su rol. Este token tiene un tiempo de expiración (ej. 8 horas) para mitigar el riesgo de robo de sesión.

* **Envío:** El frontend debe incluir este token en el header `Authorization: Bearer <token>` en cada petición subsecuente.

### 1.2. Clientes Minoristas (Social Login y Tradicional)

* **Login Tradicional:** Correo y contraseña (igual que empleados).

* **OAuth2 (Social Login):** Los clientes pueden registrarse e iniciar sesión utilizando proveedores externos (ej. Google). El backend gestiona este flujo y, en caso de éxito, emite el mismo formato de JWT. El proveedor utilizado se registra en el atributo `Usuario.proveedorAuth`.

## 2. Gestión de Contraseñas y Primer Ingreso

Para garantizar el cumplimiento de las normativas de "Protección de Datos" y "Seguridad de Acceso" solicitadas:

* **Encriptación:** Todas las contraseñas se hashean utilizando **BCrypt** antes de ser guardadas en la base de datos. **Nunca** se almacenan, loguean ni transmiten contraseñas en texto plano.

* **Clave por defecto:** Al crear un nuevo empleado (Preventor, Distribuidor, Administrativo), el sistema le asigna la contraseña por defecto `dulcemayor` y marca el atributo `claveTemporal = true`.

* **Forzado de cambio de clave:**

  * Si un usuario con `claveTemporal = true` intenta acceder a cualquier endpoint del sistema (excepto el de cambio de clave), el backend debe interceptar la petición y responder con un código HTTP `403 Forbidden` y un mensaje de error claro (ej. "DEBE_CAMBIAR_CLAVE").

  * El frontend, al recibir este código, redirigirá automáticamente al usuario a la pantalla de "Cambio de Contraseña".

  * Una vez cambiada exitosamente, `claveTemporal` pasa a `false` y se libera el acceso.

## 3. Autorización y Gestión de Permisos (RBAC)

El acceso a los recursos está restringido según el rol del usuario autenticado. Esto se implementa en Spring Security utilizando anotaciones como `@PreAuthorize("hasRole('...')")` a nivel de controlador o método.

### 3.1. Matriz de Roles y Accesos principales

| Rol | Accesos y Permisos Permitidos | 
| ----- | ----- | 
| **PREVENTOR** | Ver catálogo completo (con stock en tiempo real). Dar de alta clientes (minoristas) y Puntos de Venta. Cargar pedidos con validación de códigos de promoción. Consultar estado de los pedidos propios. Ver ruta de visitas sugerida en el mapa. | 
| **DISTRIBUIDOR** | Ver ruta de entregas del día. Marcar pedidos como `ENTREGADO`. Recibir sugerencia del próximo punto (solo uso de App Móvil). | 
| **ADMINISTRATIVO** | ABM de Productos y Categorías. Modificar Precios (`VigenciaPrecio`). Procesar y facturar pedidos (`CARGADO` -> `FACTURADO`). Registrar pagos. Actualizar stock manualmente. | 
| **JEFE_ADMINISTRATIVO** | Todos los permisos del Administrativo. ABM de Usuarios/Empleados. Configuración de Promociones. Ver y descargar reportes (PDF/Excel) y gráficas estadísticas. | 
| **MINORISTA** | Acceder a su historial de pedidos. Ver el estado de sus envíos. Descargar facturas propias en PDF. Visualizar QR de pago de sus pedidos (billetera virtual). | 

*Nota: Cualquier usuario autenticado puede acceder a los endpoints para modificar su propia contraseña.*

## 4. Securitización de Rutas en la API (Filtros de Seguridad)

La configuración de Spring Security divide los endpoints en dos grandes grupos:

### 4.1. Endpoints Públicos (No requieren JWT)

Se permite el acceso irrestricto (permitAll) a:

* `POST /api/v1/auth/login` (Autenticación).

* `POST /api/v1/clientes/registro` (Registro público de minoristas).

* `GET /api/v1/productos/catalogo` (Catálogo público de productos en stock).

* `GET /api/v1/institucional/**` (Información de la empresa, sucursales).

* `GET /api/v1/cotizacion-dolar` (Consulta de dólar).

* `POST /api/v1/chatbot/presupuesto` (Interacción con el bot de IA).

### 4.2. Endpoints Privados (Requieren JWT válido)

Cualquier endpoint no listado arriba requiere obligatoriamente un token JWT válido. El filtro de seguridad validará la firma y expiración del token antes de derivar la petición al controlador.

## 5. Medidas de Seguridad Adicionales (Hardening)

1. **Gestión de Secretos:**

   * Las claves críticas como `JWT_SECRET`, credenciales de base de datos, API Keys (IA, Cotización Dólar) y configuración SMTP (`ConfiguracionCorreoEmpresa`) deben inyectarse mediante **Variables de Entorno** (`.env`). **Nunca** deben estar hardcodeadas en el código fuente ni subidas al repositorio Git.

2. **CORS (Cross-Origin Resource Sharing):**

   * El backend estará configurado para rechazar peticiones de orígenes desconocidos. Solo se permitirán solicitudes (GET, POST, PUT, DELETE, OPTIONS) provenientes de las URLs exactas de los 3 frontends (Dashboard, Sitio Público, App Móvil).

3. **Validación de Entradas (Input Validation):**

   * Se utilizará **Bean Validation** (`@Valid`, `@NotNull`, `@Size`, etc.) en todos los DTOs de entrada para prevenir inyecciones y garantizar la integridad de los datos antes de que lleguen a la capa de negocio.

4. **Manejo Seguro de Errores (Data Leakage Prevention):**

   * Se implementará un `@RestControllerAdvice` global que estandarice todas las respuestas de error en un formato único (`timestamp`, `status`, `code`, `message`, `path`).

   * **Bajo ninguna circunstancia** la API devolverá un "Stacktrace" (traza de error de Java) ni excepciones internas de la base de datos (SQL exceptions) al cliente. Los mensajes deben ser genéricos y controlados (ej. "Error de validación de datos", "Recurso no encontrado").