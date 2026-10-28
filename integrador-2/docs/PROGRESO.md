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

---

## Paso 2: Golden slice "Categoria" + clases base + tests de arquitectura

### 1. Qué se hizo
- **Clases base de infraestructura reutilizables:**
  - `EntidadBase` (`com.DulceMayor.backend.entities.EntidadBase`): superclase mapeada (`@MappedSuperclass`) con `id: String` (UUID autogenerado) y `eliminado: boolean = false` (borrado lógico). `Usuario` y `Categoria` heredan de `EntidadBase`.
  - `BaseRepository<E extends EntidadBase>` (`com.DulceMayor.backend.repositories.BaseRepository`): interfaz `@NoRepositoryBean` que extiende `JpaRepository<E, String>` con consultas estándar filtrando `eliminado = false` (`findByIdAndEliminadoFalse`, `findAllByEliminadoFalse`, `existsByIdAndEliminadoFalse`).
  - `BaseMapper<E, Req, Res>` (`com.DulceMayor.backend.dtos.BaseMapper`): interfaz genérica para mappers MapStruct (`aEntidad`, `actualizarEntidad`, `aResponse`, `aResponseList`).
  - `BaseService<E extends EntidadBase, Req, Res>` (`com.DulceMayor.backend.services.BaseService`): aplica el patrón **Template Method** en la capa de servicio para las operaciones `crear`, `modificar`, `eliminar` (baja lógica), `buscar` y `listar`, recibiendo y devolviendo DTOs, con pasos personalizables (`prepararRequest`, `validarCrear`, `validarModificar`, `validarEliminar`, `aEntidad`, `actualizarEntidad`, `aResponse`, `buscarEntidadesActivas`, y hooks pre/post persistencia). Incluye `obtenerEntidad(String id)` (lanza `404 NO_ENCONTRADO` si no existe o está eliminada) para consumo interno entre servicios sin pasar por DTO.
  - `BaseController<Req, Res>` (`com.DulceMayor.backend.controllers.BaseController`): aplica el patrón **Template Method** en la capa de controlador mediante los métodos plantilla protegidos `ejecutarListar` (200), `ejecutarBuscar` (200), `ejecutarCrear` (201 con header `Location`), `ejecutarModificar` (200) y `ejecutarEliminar` (204). Los endpoints y anotaciones `@PreAuthorize` se declaran explícitamente en cada controlador concreto.
- **Módulo de referencia (`Categoria`):**
  - `Categoria` (`com.DulceMayor.backend.entities.Categoria`): entidad JPA que extiende `EntidadBase` con atributo `nombre` (`nullable = false`, `length = 100`).
  - `CategoriaRepository` (`com.DulceMayor.backend.repositories.CategoriaRepository`): extiende `BaseRepository<Categoria>` con consultas de unicidad case-insensitive (`existsByNombreIgnoreCase`, `existsByNombreIgnoreCaseAndIdNot`, `existsByNombreIgnoreCaseAndEliminadoFalse`, `existsByNombreIgnoreCaseAndIdNotAndEliminadoFalse`, `findByNombreIgnoreCaseAndEliminadoFalse`) y listado ordenado (`findAllByEliminadoFalseOrderByNombreAsc`).
  - `CategoriaRequest` y `CategoriaResponse` (`com.DulceMayor.backend.dtos`): records que respetan exactamente los schemas de `04-openapi.yaml`.
  - `CategoriaMapper` (`com.DulceMayor.backend.dtos.CategoriaMapper`): mapper `@Mapper(componentModel = "spring")` que implementa `BaseMapper<Categoria, CategoriaRequest, CategoriaResponse>`.
  - `CategoriaService` (`com.DulceMayor.backend.services.CategoriaService`): extiende `BaseService`, quita espacios sobrantes del nombre (`trim()`), valida obligatoriedad y longitud máxima (100 caracteres) y verifica unicidad sin distinguir mayúsculas/minúsculas lanzando `409 Conflict` con `code = DUPLICADO` (sin chocar consigo misma al modificar).
  - `CategoriaController` (`com.DulceMayor.backend.controllers.CategoriaController`): expone `/categorias` y `/api/v1/categorias` con los `operationId` exactos (`listarCategoria`, `crearCategoria`, `buscarCategoria`, `modificarCategoria`, `eliminarCategoria`). `GET` públicos y `POST`/`PUT`/`DELETE` restringidos a `JEFE` y `ADMINISTRATIVO` vía `@PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")`.
- **Tests de arquitectura y del módulo (`src/test/java`):**
  - `ArquitecturaTests`: verifica con ArchUnit (`com.tngtech.archunit:archunit-junit5:1.3.0`) las reglas de la sección 10.1 de `GENERAL.md` (cada `*Service` solo depende de su `<Entidad>Repository` salvo `*LecturaService`; ningún controlador depende de repositorios ni de entidades; entidades no dependen de servicios ni controladores; servicios no dependen de controladores; sin `@Autowired` en campos; ausencia de ciclos entre capas de aplicación/dominio). Se comprobó empíricamente que cada una de las 6 reglas falla en rojo al introducir una violación temporal y vuelve a verde al revertirla.
  - `CategoriaServiceTest` (Mockito): alta, duplicado (`409 DUPLICADO`), modificación, baja lógica y búsqueda de inexistente (`404 NO_ENCONTRADO`).
  - `CategoriaRepositoryTest` (H2): consulta de duplicados sin distinguir mayúsculas/minúsculas y exclusión del propio ID.
  - `CategoriaControllerTest` (MockMvc + Spring Security): pruebas integrales de rutas públicas y privadas, roles permitidos/denegados, validaciones, duplicados, 404, 204 y verificación del JSON `ErrorResponse`.

### 2. Decisiones tomadas
- **Configuración de procesadores de anotaciones en Maven:** en `backend/pom.xml` se configuraron conjuntamente `lombok`, `lombok-mapstruct-binding` (`0.2.0`) y `mapstruct-processor` (`1.5.5.Final`) dentro de `<annotationProcessorPaths>` de `maven-compiler-plugin`, garantizando la generación limpia de builders (`@SuperBuilder`) y mappers Spring en tiempo de compilación.
- **Desacoplamiento total entre `BaseController` y `entities`:** `BaseController<Req, Res>` está parametrizado únicamente por los tipos DTO (`Req`, `Res`) y delega en `BaseService<?, Req, Res>`, de modo que ninguna clase del paquete `controllers` tiene referencias (ni siquiera en firmas genéricas) al paquete `entities`.

### 3. Pendientes anotados para próximos pasos
- **Validación de subcategorías activas al eliminar una `Categoria`:** conforme a la regla 4 de la sección 10.1 de `GENERAL.md` (la dependencia apunta de `SubCategoria` hacia `Categoria` y no al revés), `CategoriaService` NO depende de `SubCategoriaRepository`. Queda **pendiente resolver en el módulo `SubCategoria`** la validación o coordinación previa a la baja lógica de una categoría con subcategorías asociadas.

### 4. Desvíos respecto de los documentos
- Ninguno. Se mantuvo la estructura de paquetes existente (`config`, `controllers`, `dtos`, `entities`, `repositories`, `services`) documentada en `03-arquitectura.md` sin mover archivos existentes.

---

## Avance en los front ends: Desarrollo de interfaces UI
- **Construcción de 3 frontends independientes (React + Tailwind CSS v4):**
  - `frontend-institucional`: catálogo mayorista, sucursales con mapa, registro de usuarios minoristas, carrito con cupones y perfil con detalle de pedido, descarga de factura PDF y visor de QR de pago.
  - `frontend-preventor`: app mobile-first/desktop para preventores (rutero, alta de comercios, catálogo y carrito con promociones) y distribuidores (reparto y tickets de entrega), desacoplados vía `RoleRouter.jsx`.
  - `frontend-admin`: interfaz unificada para administrativo (stock, productos, facturación de pedidos y pagos) y jefe (dashboard con Recharts, estadísticas, gestión de usuarios y reportes descargables en PDF/Excel).
- **Sistema de diseño y responsividad:** tokens compartidos en `@theme` (colores, Poppins, isotipo `candy-mark`), navegación por rol sin menús filtrados, mapas con OpenStreetMap y compilación exitosa (`npm run build`).
## Paso 3: Módulo Comercial / Producto (Imagen, SubCategoria, Producto, VigenciaPrecio, Stock, Promocion)

### 1. Qué se hizo
- **Entidades de dominio (`com.DulceMayor.backend.entities`):**
  - `TipoImagen`: enumeración con `PRODUCTO`, `SUCURSAL`, `LOGO_EMPRESA`.
  - `Imagen`: entidad JPA con `nombre`, `mime`, `contenido` (`@Lob`), `tipoImagen` y borrado lógico.
  - `SubCategoria`: entidad JPA con `nombre` y relación `@ManyToOne` hacia `Categoria`.
  - `Producto`: entidad JPA con `codigo`, `nombre`, `descripcion`, `presentacion`, `unidadesPorBulto`, `enOferta`, `@ManyToOne` a `SubCategoria` e `Imagen`.
  - `VigenciaPrecio`: entidad JPA con `fechaDesde`, `fechaHasta` (nula para la vigencia actual abierta), `precio` y `@ManyToOne` hacia `Producto`.
  - `Stock`: entidad JPA que modela el historial/ledger inmutable de movimientos de stock con `cantidadActual`, `fechaHora`, `observacion` (indicando si es ingreso o egreso). No tiene atributo `producto`; se relaciona únicamente con `DetalleFactura` (comentada en código hasta implementar dicha entidad), desde donde se accede al producto.
  - `Promocion`: entidad JPA con `codigo`, `descripcion`, `porcentajeDescuento`, `cantidadMinima`, `fechaDesde`, `fechaHasta` y `@ManyToOne` opcional hacia `Producto`.
- **Capa de acceso a datos (`com.DulceMayor.backend.repositories`):**
  - `ImagenRepository`: extiende `BaseRepository<Imagen>` con búsqueda por tipo de imagen activa.
  - `SubCategoriaRepository`: extiende `BaseRepository<SubCategoria>` con validación de unicidad de nombre por categoría y listado ordenado.
  - `ProductoRepository`: extiende `BaseRepository<Producto>` con unicidad de código, búsqueda por subcategoría y consulta con filtros (`q`, `categoriaId`, `subCategoriaId`, `enOferta`).
  - `VigenciaPrecioRepository`: extiende `BaseRepository<VigenciaPrecio>` con búsqueda de precio vigente (`fechaHasta == null`), historial y detección de solapamiento cronológico.
  - `StockRepository`: extiende `BaseRepository<Stock>` con consulta del último movimiento registrado de un producto (`findFirstBy...OrderByFechaHoraDesc`) y consulta paginada con filtros (`q`, `soloSinStock`).
  - `PromocionRepository`: extiende `BaseRepository<Promocion>` con unicidad de código case-insensitive y consulta de promociones vigentes por fecha.
- **DTOs, mappers y eventos (`com.DulceMayor.backend.dtos`):**
  - DTOs records según OpenAPI: `ImagenResponse`, `ImagenContenido`, `SubCategoriaRequest`, `SubCategoriaResponse`, `ProductoRequest`, `ProductoResponse`, `ProductoPage`, `VigenciaPrecioRequest`, `VigenciaPrecioResponse`, `StockResponse`, `StockAjusteRequest`, `StockReposicionRequest`, `StockPage`, `PromocionRequest`, `PromocionResponse`.
  - Mappers MapStruct Spring: `SubCategoriaMapper`, `ProductoMapper`, `VigenciaPrecioMapper`, `StockMapper`, `PromocionMapper`.
  - Evento `ProductoCreadoEvent` para desacoplar mediante el patrón Observer la creación de precio y stock inicial.
- **Servicios de negocio (`com.DulceMayor.backend.services`):**
  - `ImagenService`: validación de MIME (`image/jpeg`, `image/png`, `image/webp`), tamaño máximo (5 MB), persistencia, descarga binaria y borrado lógico.
  - `SubCategoriaService`: Template Method (`BaseService`), normalización, verificación de categoría activa vía `CategoriaService.obtenerEntidad` y unicidad de nombre.
  - `ProductoService`: Template Method (`BaseService`), unicidad de código, resolución de subcategoría e imagen, y emisión de `ProductoCreadoEvent`.
  - `ProductoLecturaService`: servicio de lectura (conforme regla 6 de sección 10.1) que combina `ProductoRepository`, `StockRepository` y `VigenciaPrecioRepository` para responder consultas y catálogo paginado con stock y precio en tiempo real.
  - `VigenciaPrecioService`: Template Method (`BaseService`), listener de precio inicial, cierre automático de la vigencia previa (`fechaHasta = fechaDesde - 1 día`), y control estricto de no solapamiento (409 Conflict).
  - `StockService`: listener de stock inicial vía Observer, operaciones append-only (ajuste de inventario físico, reposición acumulativa e inserción de nuevos registros con `fechaHora` y `observacion`), consultas del stock actual y método transaccional `descontar` con validación de existencia suficiente (409 `STOCK_INSUFICIENTE`).
  - `PromocionService`: Template Method (`BaseService`), código en mayúsculas, unicidad, validación de fechas cronológicas y búsqueda de código vigente por fecha del servidor.
- **Controladores REST (`com.DulceMayor.backend.controllers`):**
  - `ImagenController`: `POST /imagenes` (multipart, 201 + Location), `GET /imagenes/{id}/contenido` (público con Content-Type dinámico), `DELETE /imagenes/{id}` (204).
  - `SubCategoriaController`: `GET /subcategorias` (público, con filtro opcional `categoriaId`), `GET /subcategorias/{id}` (público), `POST`, `PUT`, `DELETE` protegidos por roles (`JEFE`, `ADMINISTRATIVO`).
  - `ProductoController`: endpoints `/productos` (paginado, búsqueda, ABM con roles correspondientes).
  - `VigenciaPrecioController`: `GET` y `POST` sobre `/productos/{id}/vigencias-precio`, `PUT` y `DELETE` sobre `/vigencias-precio/{id}`.
  - `StockController`: `GET` y `PUT` sobre `/productos/{id}/stock`, `POST /productos/{id}/stock/reposiciones`, `GET /stock` (paginado).
  - `PromocionController`: ABM sobre `/promociones` y validación de código vigente en `GET /promociones/codigo/{codigo}` (habilitado para `PREVENTISTA`).
- **Pruebas automatizadas (76 tests pasando en verde):**
  - Unit tests de servicios con Mockito: `ImagenServiceTest`, `SubCategoriaServiceTest`, `ProductoServiceTest`, `VigenciaPrecioServiceTest`, `StockServiceTest`, `PromocionServiceTest`.
  - Tests de repositorio H2: `SubCategoriaRepositoryTest`.
  - Tests de integración MockMvc + Spring Security con JWT: `ImagenControllerTest`, `SubCategoriaControllerTest`, `ProductoControllerTest`, `VigenciaPrecioControllerTest`, `StockControllerTest`, `PromocionControllerTest`, `CategoriaControllerTest`.
  - Tests de arquitectura ArchUnit (`ArquitecturaTests`): 100% en verde, garantizando cero acoplamiento indebido y cero ciclos.

---

## Paso 4: Módulo Geografía y Direcciones (Pais, Provincia, Departamento, Localidad, Direccion)

### 1. Qué se hizo
- **Entidades de dominio (`com.DulceMayor.backend.entities`):**
  - `Pais`: extiende `EntidadBase` con atributo `nombre`.
  - `Provincia`: extiende `EntidadBase` con atributo `nombre` y relación unidireccional `@ManyToOne` hacia `Pais`.
  - `Departamento`: extiende `EntidadBase` con atributo `nombre` y relación unidireccional `@ManyToOne` hacia `Provincia`.
  - `Localidad`: extiende `EntidadBase` con atributos `nombre`, `codigoPostal` y relación unidireccional `@ManyToOne` hacia `Departamento`.
  - `Direccion`: extiende `EntidadBase` con atributos obligatorios (`calle`, `numeracion`, `@ManyToOne Localidad localidad`) y opcionales (`barrio`, `manzanaPiso`, `casaDepartamento`, `referencia`, `latitud`, `longitud`).
- **Repositorios (`com.DulceMayor.backend.repositories`):**
  - `PaisRepository`, `ProvinciaRepository`, `DepartamentoRepository`, `LocalidadRepository`, `DireccionRepository`: todos extienden `BaseRepository<T>`, con consultas de unicidad case-insensitive por padre (`existsByNombreIgnoreCaseAnd<Padre>IdAndEliminadoFalse` y variante `...AndIdNot...`) y listados ordenados por nombre.
- **DTOs y Mappers (`com.DulceMayor.backend.dtos`):**
  - Records de entrada y salida: `PaisRequest`, `PaisResponse`, `ProvinciaRequest`, `ProvinciaResponse`, `DepartamentoRequest`, `DepartamentoResponse`, `LocalidadRequest`, `LocalidadResponse`, `DireccionRequest`, `DireccionResponse`.
  - Mappers MapStruct (`@Mapper(componentModel = "spring")`): `PaisMapper`, `ProvinciaMapper`, `DepartamentoMapper`, `LocalidadMapper`, `DireccionMapper` (este último mapea `localidadId`, `localidadNombre`, `departamentoId`, `departamentoNombre` y `provinciaNombre`).
- **Servicios de negocio (`com.DulceMayor.backend.services`):**
  - `PaisService`, `ProvinciaService`, `DepartamentoService`, `LocalidadService`: todos extienden `BaseService<E, Req, Res>`, aplican el patrón Template Method y mantienen flujo de dependencia estrictamente descendente (`LocalidadService -> DepartamentoService -> ProvinciaService -> PaisService`) mediante `obtenerEntidad(id)`. No se creó `DireccionService` dado que `Direccion` se gestiona por composición desde las entidades que la poseen (`PuntoDeVenta`, `Mayorista`, etc.).
- **Controladores REST (`com.DulceMayor.backend.controllers`):**
  - `PaisController` (`/paises`, `/api/v1/paises`), `ProvinciaController` (`/provincias`, `/api/v1/provincias` con filtro `?paisId=`), `DepartamentoController` (`/departamentos`, `/api/v1/departamentos` con filtro `?provinciaId=`), `LocalidadController` (`/localidades`, `/api/v1/localidades` con filtro `?departamentoId=`). Todos extienden `BaseController<Req, Res>`, exponen `GET` públicos y protegen `POST`, `PUT` y `DELETE` con `@PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")`. No se creó `DireccionController`.

---

## Paso 5: Módulo Contactos (TipoContacto, TipoTelefono, Contacto, ContactoCorreoElectronico, ContactoTelefonico)

### 1. Qué se hizo
- **Enumeraciones y entidades de dominio (`com.DulceMayor.backend.entities`):**
  - `TipoContacto`: enumeración con `PERSONAL`, `LABORAL`, `EMPRESA`.
  - `TipoTelefono`: enumeración con `FIJO`, `CELULAR`.
  - `Contacto`: entidad base abstracta que extiende `EntidadBase` con estrategia de herencia `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` y columna discriminadora `medio` (`"CORREO"` o `"TELEFONICO"`), más los atributos comunes `tipoContacto` (obligatorio) y `observacion` (opcional).
  - `ContactoCorreoElectronico`: subclase concreta de `Contacto` (`@DiscriminatorValue("CORREO")`) con atributo `email`.
  - `ContactoTelefonico`: subclase concreta de `Contacto` (`@DiscriminatorValue("TELEFONICO")`) con atributos `telefono` y `tipoTelefono`.
- **Repositorio (`com.DulceMayor.backend.repositories`):**
  - `ContactoRepository`: extiende `BaseRepository<Contacto>` para consultas polimórficas sobre toda la jerarquía y consultas específicas de activos por subclase (`findAllCorreosActivos`, `findAllTelefonosActivos`).
- **DTOs y Mapper polimórfico (`com.DulceMayor.backend.dtos`):**
  - `ContactoRequest` y `ContactoResponse`: records planos que unifican el contrato de entrada/salida con el discriminador `medio` conforme a `04-openapi.yaml`.
  - `ContactoMapper`: mapper `@Mapper(componentModel = "spring")` que extiende `BaseMapper<Contacto, ContactoRequest, ContactoResponse>`, instanciando polimórficamente `ContactoCorreoElectronico` o `ContactoTelefonico` según `medio`, validando los campos obligatorios de cada subclase y proyectando el mapeo inverso a `ContactoResponse`.
- **Servicio interno (`com.DulceMayor.backend.services`):**
  - `ContactoService`: extiende `BaseService<Contacto, ContactoRequest, ContactoResponse>` para validaciones y operaciones internas reutilizables desde `Persona` y `Mayorista` respetando la regla ArchUnit de acceso a repositorios, sin exponer `ContactoController`.





